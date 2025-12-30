import { Router, Request, Response } from "express";
import pool from "../db";

const router = Router();

const OPEN_METEO_API = "https://api.open-meteo.com/v1/forecast";
const GEOCODING_API = "https://geocoding-api.open-meteo.com/v1/search";
const CACHE_DURATION_MS = 30 * 60 * 1000; // 30 minutes

interface WeatherRequestBody {
  city?: string;
  units?: "metric" | "imperial";
}

interface GeocodingResult {
  latitude: number;
  longitude: number;
  name: string;
}

interface GeocodingResponse {
  results?: GeocodingResult[];
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
    const { city, units = "metric" } = body;

    if (!city) {
      res.status(400).json({
        error: "Bad Request",
        message: "City parameter is required",
      });
      return;
    }

    console.log(`Fetching weather for city: ${city}, units: ${units}`);

    // Normalize city name for cache key
    const cacheKey = city.toLowerCase().trim();

    // Check cache
    const cacheResult = await pool.query(
      "SELECT * FROM weather_cache WHERE city = $1 AND units = $2 LIMIT 1",
      [cacheKey, units]
    );

    if (cacheResult.rows.length > 0) {
      const cached = cacheResult.rows[0];
      const updatedAt = new Date(cached.updated_at).getTime();
      const now = Date.now();

      if (now - updatedAt < CACHE_DURATION_MS) {
        // Cache is fresh, return it
        console.log(`Cache hit for ${city}`);
        const weatherData: WeatherData = JSON.parse(cached.data);
        res.json(weatherData);
        return;
      } else {
        // Cache expired, delete it
        console.log(`Cache expired for ${city}, deleting...`);
        await pool.query("DELETE FROM weather_cache WHERE id = $1", [
          cached.id,
        ]);
      }
    }

    // No cache or expired - fetch from Open-Meteo
    console.log(`Fetching from Open-Meteo for ${city}`);

    // First, geocode the city
    const geoUrl = `${GEOCODING_API}?name=${encodeURIComponent(city)}&count=1`;
    const geoResponse = await fetch(geoUrl);
    const geoData = (await geoResponse.json()) as GeocodingResponse;

    if (!geoData.results || geoData.results.length === 0) {
      res.status(404).json({
        error: "Not Found",
        message: "City not found",
      });
      return;
    }

    const location = geoData.results[0];
    const { latitude, longitude, name: cityName } = location;

    // Determine units
    const temperatureUnit = units === "metric" ? "celsius" : "fahrenheit";
    const windspeedUnit = units === "metric" ? "ms" : "mph";
    const precipitationUnit = units === "metric" ? "mm" : "inch";

    // Fetch weather data
    const weatherUrl =
      `${OPEN_METEO_API}?latitude=${latitude}&longitude=${longitude}` +
      `&current=temperature_2m,relative_humidity_2m,weathercode,wind_speed_10m,wind_direction_10m,wind_gusts_10m,pressure_msl,visibility,is_day` +
      `&hourly=temperature_2m,relative_humidity_2m,weathercode,wind_speed_10m,wind_direction_10m,wind_gusts_10m,precipitation,precipitation_probability` +
      `&daily=weathercode,temperature_2m_max,temperature_2m_min,sunrise,sunset` +
      `&temperature_unit=${temperatureUnit}&windspeed_unit=${windspeedUnit}&precipitation_unit=${precipitationUnit}` +
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

    // Add city name to response
    weatherData.name = cityName;
    weatherData.city_query = city;

    // Cache the result
    try {
      await pool.query(
        `INSERT INTO weather_cache (city, units, data, updated_at) 
         VALUES ($1, $2, $3, $4)
         ON CONFLICT (city, units) 
         DO UPDATE SET data = $3, updated_at = $4`,
        [cacheKey, units, JSON.stringify(weatherData), new Date().toISOString()]
      );
      console.log(`Cached weather data for ${city}`);
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

