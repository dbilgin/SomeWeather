package com.omedacore.someweather

import android.app.Application
import com.omedacore.someweather.shared.data.api.RetrofitClient

class SomeWeatherApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize RetrofitClient with backend configuration
        RetrofitClient.initialize(
            baseUrl = BuildConfig.BASE_URL,
            apiKey = BuildConfig.WEATHER_API_KEY
        )
    }
}

