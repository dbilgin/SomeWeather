package com.omedacore.someweather.data.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.omedacore.someweather.shared.data.api.RetrofitClient
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object MobileRetrofitClient {
    // Forecast API uses the same base URL as weather API
    // Access the internal weatherRetrofit from shared module
    val forecastAPI: ForecastAPI = RetrofitClient.weatherRetrofit.create(ForecastAPI::class.java)
}

