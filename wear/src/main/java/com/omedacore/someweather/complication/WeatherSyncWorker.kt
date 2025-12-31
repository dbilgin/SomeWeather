package com.omedacore.someweather.complication

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.omedacore.someweather.shared.data.local.PreferencesManager
import com.omedacore.someweather.shared.data.repository.WeatherRepository
import java.io.IOException
import java.net.UnknownHostException

class WeatherSyncWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    companion object {
        const val TAG =  "Worker"
        private const val MAX_RETRY_ATTEMPTS = 5
    }

    override suspend fun doWork(): Result {
        val attemptCount = runAttemptCount
        
        // Stop retrying after max attempts
        if (attemptCount >= MAX_RETRY_ATTEMPTS) {
            Log.w(TAG, "Max retry attempts ($MAX_RETRY_ATTEMPTS) reached, giving up")
            return Result.failure()
        }
        
        if (!hasValidatedInternet(applicationContext)) {
            Log.d(TAG, "No validated internet, retry attempt $attemptCount")
            return Result.retry()
        }

        val repository by lazy {
            val preferencesManager = PreferencesManager(applicationContext)
            WeatherRepository(preferencesManager)
        }

        return try {
            Log.d(TAG, "Trying weather fetch (attempt $attemptCount)")
            val health = repository.getWeatherWithCoordinates()
            if (health.isFailure) {
                Log.d(TAG, "Weather fetch failed")
                throw IOException()
            }

            ComplicationUpdateHelper.requestUpdate(applicationContext)
            Log.d(TAG, "Weather sync successful")
            Result.success()
        } catch (e: UnknownHostException) {
            Log.w(TAG, "UnknownHostException on attempt $attemptCount: ${e.message}")
            Result.retry()
        } catch (e: IOException) {
            Log.w(TAG, "IOException on attempt $attemptCount: ${e.message}")
            Result.retry()
        }
    }

    private fun hasValidatedInternet(ctx: Context): Boolean {
        val cm = ctx.getSystemService(ConnectivityManager::class.java)
        val net = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
