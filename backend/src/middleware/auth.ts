import { Request, Response, NextFunction } from "express";

export function validateApiKey(
  req: Request,
  res: Response,
  next: NextFunction
): void {
  const apiKey = req.headers["x-api-key"];
  const expectedApiKey = process.env.WEATHER_API_KEY;

  if (!apiKey || apiKey !== expectedApiKey) {
    res.status(401).json({
      error: "Unauthorized",
      message: "Invalid or missing API key",
    });
    return;
  }

  next();
}

