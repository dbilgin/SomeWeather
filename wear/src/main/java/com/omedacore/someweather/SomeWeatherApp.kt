package com.omedacore.someweather

import android.app.Application
import com.omedacore.someweather.shared.data.api.RetrofitClient

class SomeWeatherApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize RetrofitClient based on build flavor
        if (BuildConfig.USE_OPENMETEO) {
            RetrofitClient.initialize(
                useOpenMeteo = true,
                baseUrl = "",
                apiKey = ""
            )
        } else {
            RetrofitClient.initialize(
                useOpenMeteo = false,
                baseUrl = BuildConfig.BASE_URL,
                apiKey = BuildConfig.WEATHER_API_KEY
            )
        }
    }
}

