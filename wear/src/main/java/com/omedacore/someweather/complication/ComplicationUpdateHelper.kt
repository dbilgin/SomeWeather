package com.omedacore.someweather.complication

import android.content.Context
import android.util.Log
import android.content.ComponentName
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester

fun requestWeatherComplicationRefresh(context: Context) {
    try {
        val requester = ComplicationDataSourceUpdateRequester.create(
            context,
            ComponentName(context, WeatherComplicationProviderService::class.java)
        )
        requester.requestUpdateAll()
        Log.d("ComplicationUpdateHelper", "Successfully requested complication update")
    } catch (e: IllegalArgumentException) {
        Log.e("ComplicationUpdateHelper", "ComplicationDataSourceUpdateRequester failed: ${e.message}", e)
    } catch (e: Exception) {
        Log.e("ComplicationUpdateHelper", "Failed to request complication update", e)
    }
}

object ComplicationUpdateHelper {
    private const val TAG = "ComplicationUpdateHelper"

    /**
     * Requests an update for all complications by sending broadcast intents.
     * The Wear OS system will call onComplicationRequest() on the complication service.
     */
    fun requestUpdate(context: Context) {
        requestWeatherComplicationRefresh(context)
    }
}
