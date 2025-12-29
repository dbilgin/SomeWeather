# Some Weather - Wearable

A standalone Wear OS weather app that displays current conditions, temperature directly on your watch. No phone companion app required, no location permissions needed.

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
   <img width="300" alt="WearMain1" src="https://github.com/user-attachments/assets/a30e0602-353c-4d20-80b6-290a8160993f" />
   <img width="300" alt="WearMain2" src="https://github.com/user-attachments/assets/140d9b6f-9372-4a26-93dc-643204c3b6b0" />
</div>

### Complications

Add weather complications to your watch face:

- **Short Text** - Shows temperature with weather icon
- **Ranged Value** - Shows minimum and maximum temperature values as a range with visual indicator

Complications automatically update (configurable via `UPDATE_PERIOD_SECONDS` in AndroidManifest.xml).

<div>
   <img width="300" alt="WearComplication" src="https://github.com/user-attachments/assets/2543e30c-b446-4825-b15a-4ce267ebc3f1" />
</div>

### Tile

Add the weather tile to your watch's tile carousel:

- Displays essential weather information: city name, temperature, and condition
- Compact design optimized for tile viewport
- Tap anywhere on the tile to open the main app

<div>
   <img width="300" alt="WearTile" src="https://github.com/user-attachments/assets/e933ae18-5d97-4d4f-9230-7c95412de126" />
</div>

### Settings

<div>
   <img width="300" alt="WearSettings1" src="https://github.com/user-attachments/assets/b82031ce-eef7-4f96-941c-c652741c773f" />
   <img width="300" alt="WearSettings2" src="https://github.com/user-attachments/assets/85509c31-4301-4052-ae97-b08c336efae4" />
</div>
