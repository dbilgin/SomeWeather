# Some Weather

A simple weather application for Android smartphones and Wear OS devices, featuring home screen widgets, watch tiles, and complications. No location permissions required.

[Android App](./mobile/README.md)

[Android Wearable App](./wear/README.md)

[Backend API](./backend/README.md)

<div>
   <img width="300" alt="AndroidMain1" src="https://github.com/user-attachments/assets/f9e72fee-3907-4c39-a35b-3a40b3163bf0" />
   <img width="300" alt="WearMain" src="https://github.com/user-attachments/assets/e373b719-5203-4897-9120-095c96cebc27" />
</div>

## Setup

1. For backend follow the backend [README](/backend/README.md).
2. Create key.properties file following [key.properties.example](key.properties.example).
3. Clone this repository
4. Open the project in Android Studio
5. Connect a Wear OS device or start an emulator
6. Build and run the app

## Project Structure

The project consists of:

- `:wear` - Wear OS application
- `:mobile` - Mobile application
- `:shared` - Shared business logic and data models used by both apps
- `/backend` - Node backend API with PostgreSQL caching

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
