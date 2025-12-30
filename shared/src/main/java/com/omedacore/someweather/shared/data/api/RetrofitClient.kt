package com.omedacore.someweather.shared.data.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private var baseUrl: String = ""
    private var weatherApiKey: String = ""
    private var isInitialized = false

    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Initialize the RetrofitClient with backend configuration.
     * Must be called before using the API clients.
     */
    fun initialize(baseUrl: String, apiKey: String) {
        this.baseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        weatherApiKey = apiKey
        isInitialized = true
    }

    private fun createApiKeyInterceptor(): Interceptor {
        return Interceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("X-API-Key", weatherApiKey)
                .header("Content-Type", "application/json")
                .method(original.method, original.body)
                .build()
            chain.proceed(request)
        }
    }

    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(createApiKeyInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun getBaseUrl(): String {
        check(isInitialized) { "RetrofitClient must be initialized before use" }
        return baseUrl
    }

    val weatherAPI: WeatherAPI by lazy {
        Retrofit.Builder()
            .baseUrl(getBaseUrl())
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(WeatherAPI::class.java)
    }

    val citySearchAPI: CitySearchAPI by lazy {
        Retrofit.Builder()
            .baseUrl(getBaseUrl())
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(CitySearchAPI::class.java)
    }
}
