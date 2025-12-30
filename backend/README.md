# Some Weather - Backend API

A Node backend API that provides weather data and city search functionality for the Some Weather mobile and Wear OS applications. Features caching with PostgreSQL to minimize API calls and improve response times.

## Features

- **Weather API** - Fetches current weather and forecasts from Open-Meteo
- **City Search API** - Geocoding search for city names
- **PostgreSQL Caching** - Reduces external API calls with smart caching strategies
- **API Key Authentication** - Secure access control via `X-API-Key` header
- **Docker Support** - Containerized for easy deployment

## API Endpoints

### Health Check

```
GET /health
```

No authentication required. Returns server status.

### Weather Data

```
POST /weather
Headers: X-API-Key: <your-api-key>
Body: {
  "city": "Berlin",
  "units": "metric" | "imperial"
}
```

Returns weather data for the specified city. Weather data is cached for 30 minutes.

### City Search

```
POST /search
Headers: X-API-Key: <your-api-key>
Body: {
  "query": "Berlin",
  "count": 5
}
```

Returns a list of matching cities. Search results are cached permanently.

## Caching Strategy

- **Weather Data**: After expiry, old cache is deleted and fresh data is fetched.
- **City Search**: Cached permanently since city data rarely changes.

## Setup

### Prerequisites

- Node.js 20+
- PostgreSQL database
- Docker (optional, for containerized deployment)

### Local Development

1. Install dependencies:

   ```bash
   npm install
   ```

2. Create a `.env` file:

   ```env
   DATABASE_URL=postgresql://user:password@localhost:5432/someweather
   WEATHER_API_KEY=your-secret-api-key-here
   PORT=3000
   ```

3. Start the development server:
   ```bash
   npm run dev
   ```

### Production Build

1. Build TypeScript:

   ```bash
   npm run build
   ```

2. Start the server:
   ```bash
   npm start
   ```

### Docker Deployment

1. Build the Docker image:

   ```bash
   docker build -t someweather-api .
   ```

2. Run the container:
   ```bash
   docker run -d \
     -p 3000:3000 \
     -e DATABASE_URL=postgresql://user:password@host:5432/someweather \
     -e WEATHER_API_KEY=your-secret-api-key-here \
     someweather-api
   ```

## Environment Variables

Refer to [.env.example](./.env.example)

## Database Schema

The backend automatically creates two tables on startup:

- `weather_cache` - Stores weather data with 30-minute expiry
- `city_cache` - Stores city search results permanently

## Attribution

<a href="https://open-meteo.com/">
	Weather data by Open-Meteo.com
</a>
-
<a href="https://github.com/open-meteo/open-meteo/blob/main/LICENSE">
   LICENSE
</a>
</br>
Weather data is cached.
