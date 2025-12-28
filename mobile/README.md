# Some Weather - Mobile

A clean and modern Android weather app that displays current conditions and detailed forecasts. No location permissions required - simply enter your city name.

## Requirements

- Android API 30+
- OpenWeatherMap API key

## Usage

### Main App

The main app displays comprehensive weather information including:

- City name
- Current temperature
- "Feels like" temperature
- Weather condition description
- Humidity percentage
- Wind speed and direction
- Sunrise and sunset times
- Expandable details section with:
  - Atmospheric pressure
  - Visibility
  - Cloudiness
  - Wind gust speed

### 5-Day Forecast

View detailed weather forecasts for the next 5 days:

- Daily forecast cards grouped by day
- Expandable cards showing hourly breakdowns
- Each hourly forecast includes:
  - Time
  - Temperature
  - Weather icon and condition
  - Humidity
  - Wind speed, direction, and gust
  - Precipitation probability and amount
  - Cloudiness

### Widget

Add a weather widget to your home screen:

- Displays current city, temperature, and condition
- Compact design optimized for home screen
- Tap to open the main app
- Automatically updates (configurable via `updatePeriodMillis` in `weather_widget_info.xml`)

### Settings

Configure your weather preferences:

- Change city
- Switch between metric and imperial units
- Refresh weather data
