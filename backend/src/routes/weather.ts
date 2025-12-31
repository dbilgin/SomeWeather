import { Router, Request, Response } from "express";
import pool from "../db";

const router = Router();

const OPEN_METEO_API = "https://api.open-meteo.com/v1/forecast";
const CACHE_DURATION_MS = 30 * 60 * 1000; // 30 minutes

interface WeatherRequestBody {
  latitude?: number;
  longitude?: number;
  temperature_unit?: "celsius" | "fahrenheit";
  windspeed_unit?: "ms" | "mph";
  precipitation_unit?: "mm" | "inch";
}

interface WeatherData {
  name?: string;
  city_query?: string;
  error?: boolean;
  reason?: string;
  [key: string]: unknown;
}

router.post("/", async (req: Request, res: Response): Promise<void> => {
  try {
    const body: WeatherRequestBody = req.body;
    const {
      latitude,
      longitude,
      temperature_unit = "celsius",
      windspeed_unit = "ms",
      precipitation_unit = "mm",
    } = body;

    if (latitude === undefined || longitude === undefined) {
      res.status(400).json({
        error: "Bad Request",
        message: "Latitude and longitude parameters are required",
      });
      return;
    }

    if (
      temperature_unit === undefined ||
      windspeed_unit === undefined ||
      precipitation_unit === undefined
    ) {
      res.status(400).json({
        error: "Bad Request",
        message:
          "temperature_unit, windspeed_unit, and precipitation_unit parameters are required",
      });
      return;
    }

    console.log(
      `Fetching weather for coordinates: ${latitude}, ${longitude}, units: ${temperature_unit}/${windspeed_unit}/${precipitation_unit}`
    );

    // Create cache key from coordinates and all unit parameters
    const cacheKey = `${latitude.toFixed(4)},${longitude.toFixed(4)}`;
    const unitsKey = `${temperature_unit}_${windspeed_unit}_${precipitation_unit}`;

    // Check cache
    const cacheResult = await pool.query(
      "SELECT * FROM weather_cache WHERE city = $1 AND units = $2 LIMIT 1",
      [cacheKey, unitsKey]
    );

    if (cacheResult.rows.length > 0) {
      const cached = cacheResult.rows[0];
      const updatedAt = new Date(cached.updated_at).getTime();
      const now = Date.now();

      if (now - updatedAt < CACHE_DURATION_MS) {
        // Cache is fresh, return it
        console.log(`Cache hit for coordinates ${cacheKey}`);
        const weatherData: WeatherData = JSON.parse(cached.data);
        res.json(weatherData);
        return;
      } else {
        // Cache expired, delete it
        console.log(`Cache expired for coordinates ${cacheKey}, deleting...`);
        await pool.query("DELETE FROM weather_cache WHERE id = $1", [
          cached.id,
        ]);
      }
    }

    // No cache or expired - fetch from Open-Meteo
    console.log(
      `Fetching from Open-Meteo for coordinates: ${latitude}, ${longitude}`
    );

    // Fetch weather data using provided unit parameters directly
    const weatherUrl =
      `${OPEN_METEO_API}?latitude=${latitude}&longitude=${longitude}` +
      `&current=temperature_2m,relative_humidity_2m,weathercode,wind_speed_10m,wind_direction_10m,wind_gusts_10m,pressure_msl,visibility,is_day` +
      `&hourly=temperature_2m,relative_humidity_2m,weathercode,wind_speed_10m,wind_direction_10m,wind_gusts_10m,precipitation,precipitation_probability` +
      `&daily=weathercode,temperature_2m_max,temperature_2m_min,sunrise,sunset` +
      `&temperature_unit=${temperature_unit}&windspeed_unit=${windspeed_unit}&precipitation_unit=${precipitation_unit}` +
      `&timezone=auto`;

    const weatherResponse = await fetch(weatherUrl);
    const weatherData = (await weatherResponse.json()) as WeatherData;

    if (weatherData.error) {
      res.status(502).json({
        error: "API Error",
        message: weatherData.reason || "Failed to fetch weather",
      });
      return;
    }

    // Cache the result
    try {
      await pool.query(
        `INSERT INTO weather_cache (city, units, data, updated_at) 
         VALUES ($1, $2, $3, $4)
         ON CONFLICT (city, units) 
         DO UPDATE SET data = $3, updated_at = $4`,
        [
          cacheKey,
          unitsKey,
          JSON.stringify(weatherData),
          new Date().toISOString(),
        ]
      );
      console.log(`Cached weather data for coordinates ${cacheKey}`);
    } catch (cacheError) {
      console.error(`Failed to cache weather data: ${cacheError}`);
    }

    res.json(weatherData);
  } catch (err) {
    const errorMessage = err instanceof Error ? err.message : "Unknown error";
    console.error(`Error in weather endpoint: ${errorMessage}`);
    res.status(500).json({
      error: "Internal Server Error",
      message: errorMessage,
    });
  }
});

export default router;
