# Some Weather - Wearable

A standalone Wear OS weather app that displays current conditions, temperature directly on your watch. No phone companion app required, no location permissions needed.

## Requirements

- Wear OS 3.0+ (Android API 30+)
- OpenWeatherMap API key

## Usage

### Main App

The main app displays comprehensive weather information including:

- City name
- Current temperature (large, prominent display)
- Weather condition description
- Weather icon (adapts to day/night based on sunrise/sunset)
- Humidity percentage
- Wind speed (formatted with locale-aware number formatting)
- Attribution

<div>
   <img width="300" height="300" alt="Current1"  src="https://github.com/user-attachments/assets/2f6a8c54-0bec-4850-9199-a12d9db60532" />
   <img width="300" height="300" alt="Current2" src="https://github.com/user-attachments/assets/200ae89e-7fb7-44a8-a5ff-a8b0f3134501" />
</div>

### Complications

Add weather complications to your watch face:

- **Short Text** - Shows temperature with weather icon
- **Ranged Value** - Shows minimum and maximum temperature values as a range with visual indicator

Complications automatically update (configurable via `UPDATE_PERIOD_SECONDS` in AndroidManifest.xml).

<div>
   <img width="300" height="300" alt="Complication" src="https://github.com/user-attachments/assets/e73fea7c-92b6-4d6e-b58e-889415648909" />
</div>

### Tile

Add the weather tile to your watch's tile carousel:

- Displays essential weather information: city name, temperature, and condition
- Compact design optimized for tile viewport
- Tap anywhere on the tile to open the main app

<div>
   <img width="300" height="300" alt="Tile" src="https://github.com/user-attachments/assets/bc5afdb7-88e2-46f4-9fa6-d2d988df4874" />
</div>

### Settings

<div>
   <img width="300" height="300" alt="ChangeCity" src="https://github.com/user-attachments/assets/e56e1baa-2fc9-422b-b598-45279438ac83" />
   <img width="300" height="300" alt="Unit" src="https://github.com/user-attachments/assets/fcc17673-e8f9-4b82-8472-31a9799c02ee" />
</div>
