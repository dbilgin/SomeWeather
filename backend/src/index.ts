import express from "express";
import cors from "cors";
import { initDatabase } from "./db";
import { validateApiKey } from "./middleware/auth";
import weatherRouter from "./routes/weather";
import searchRouter from "./routes/search";

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());

// Health check endpoint (no auth required)
app.get("/health", (_req, res) => {
  res.json({ status: "ok", timestamp: new Date().toISOString() });
});

// API routes (auth required)
app.use("/weather", validateApiKey, weatherRouter);
app.use("/search", validateApiKey, searchRouter);

// Start server
async function start(): Promise<void> {
  try {
    await initDatabase();
    app.listen(PORT, () => {
      console.log(`SomeWeather API running on port ${PORT}`);
    });
  } catch (error) {
    console.error("Failed to start server:", error);
    process.exit(1);
  }
}

start();

