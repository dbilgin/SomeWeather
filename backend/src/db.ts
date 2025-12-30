import { Pool } from "pg";

const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
});

export async function initDatabase(): Promise<void> {
  const client = await pool.connect();
  try {
    // Create weather_cache table
    await client.query(`
      CREATE TABLE IF NOT EXISTS weather_cache (
        id SERIAL PRIMARY KEY,
        city VARCHAR(255) NOT NULL,
        units VARCHAR(10) NOT NULL,
        data TEXT NOT NULL,
        updated_at TIMESTAMP NOT NULL,
        UNIQUE(city, units)
      )
    `);

    // Create city_cache table
    await client.query(`
      CREATE TABLE IF NOT EXISTS city_cache (
        id SERIAL PRIMARY KEY,
        query VARCHAR(255) UNIQUE NOT NULL,
        results TEXT NOT NULL,
        created_at TIMESTAMP NOT NULL
      )
    `);

    console.log("Database tables initialized");
  } finally {
    client.release();
  }
}

export default pool;

