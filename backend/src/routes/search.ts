import { Router, Request, Response } from "express";
import pool from "../db";

const router = Router();

const GEOCODING_API = "https://geocoding-api.open-meteo.com/v1/search";

interface SearchRequestBody {
  query?: string;
  count?: number;
}

interface GeocodingResult {
  latitude: number;
  longitude: number;
  name: string;
  country?: string;
  admin1?: string;
}

interface GeocodingResponse {
  results?: GeocodingResult[];
}

router.post("/", async (req: Request, res: Response): Promise<void> => {
  try {
    const body: SearchRequestBody = req.body;
    const { query, count = 5 } = body;

    if (!query) {
      res.status(400).json({
        error: "Bad Request",
        message: "Query parameter is required",
      });
      return;
    }

    console.log(`Searching for city: ${query}`);

    // Normalize query for cache key
    const cacheKey = query.toLowerCase().trim();

    // Check cache (permanent cache - never expires)
    const cacheResult = await pool.query(
      "SELECT * FROM city_cache WHERE query = $1 LIMIT 1",
      [cacheKey]
    );

    if (cacheResult.rows.length > 0) {
      // Return cached results
      console.log(`Cache hit for city search: ${query}`);
      const cached = cacheResult.rows[0];
      const results: GeocodingResponse = JSON.parse(cached.results);
      res.json(results);
      return;
    }

    // No cache - fetch from Open-Meteo Geocoding API
    console.log(`Fetching from Open-Meteo Geocoding for: ${query}`);

    const geoUrl = `${GEOCODING_API}?name=${encodeURIComponent(query)}&count=${count}`;
    const geoResponse = await fetch(geoUrl);
    const geoData = (await geoResponse.json()) as GeocodingResponse;

    // Cache the result permanently
    try {
      await pool.query(
        `INSERT INTO city_cache (query, results, created_at) 
         VALUES ($1, $2, $3)
         ON CONFLICT (query) 
         DO UPDATE SET results = $2`,
        [cacheKey, JSON.stringify(geoData), new Date().toISOString()]
      );
      console.log(`Cached city search results for: ${query}`);
    } catch (cacheError) {
      console.error(`Failed to cache city search: ${cacheError}`);
    }

    res.json(geoData);
  } catch (err) {
    const errorMessage = err instanceof Error ? err.message : "Unknown error";
    console.error(`Error in search endpoint: ${errorMessage}`);
    res.status(500).json({
      error: "Internal Server Error",
      message: errorMessage,
    });
  }
});

export default router;

