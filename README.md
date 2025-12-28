# Some Weather

A simple weather application for Android smartphones and Wear OS devices, featuring home screen widgets, watch tiles, and complications. No location permissions required.

[Android App](./mobile/README.md)

[Android Wearable App](./wear/README.md)

<div>
   <img width="300" alt="AndroidMain1" src="https://github.com/user-attachments/assets/f9e72fee-3907-4c39-a35b-3a40b3163bf0" />
   <img width="300" alt="WearMain" src="https://github.com/user-attachments/assets/e373b719-5203-4897-9120-095c96cebc27" />
</div>

## Setup

1. Clone this repository
2. Create `key.properties` file at the root
3. Get your API key from [OpenWeatherMap](https://openweathermap.org/api) and add it to `key.properties`:

   ```
   WEATHER_API_KEY=your_api_key_here
   ```

   Note: The app uses OpenWeatherMap's Geocoding API to convert city names to coordinates (no location permissions required).

4. Open the project in Android Studio
5. Connect a Wear OS device or start an emulator
6. Build and run the app

## Project Structure

The project consists of:

- `:wear` - Wear OS application
- `:mobile` - Mobile application
- `:shared` - Shared business logic and data models used by both apps

## Shared Functionality

- **Current weather display** - Temperature, conditions, and weather icons that adapt to day/night
- **Manual city selection** - Enter any city name manually, no location permissions require
- **Unit system support** - Switch between metric and imperial units
- **Astronomy data** - Sunrise and sunset times for your selected location
