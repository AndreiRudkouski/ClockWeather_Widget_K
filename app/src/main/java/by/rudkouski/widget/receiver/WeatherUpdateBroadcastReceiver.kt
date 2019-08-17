package by.rudkouski.widget.receiver

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import by.rudkouski.widget.app.App
import by.rudkouski.widget.database.DBHelper.Companion.INSTANCE
import by.rudkouski.widget.entity.Location.Companion.CURRENT_LOCATION_ID
import by.rudkouski.widget.listener.LocationChangeListener.isPermissionsDenied
import by.rudkouski.widget.provider.WidgetProvider
import by.rudkouski.widget.view.forecast.ForecastActivity
import by.rudkouski.widget.view.weather.WeatherUtils
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.*
import java.util.concurrent.Executors

class WeatherUpdateBroadcastReceiver : BroadcastReceiver() {

    private val executorService = Executors.newFixedThreadPool(1)
    private val dbHelper = INSTANCE

    companion object {
        private const val WEATHER_UPDATE_REQUEST_CODE = 1002
        private const val CURRENT_WEATHER_UPDATE_REQUEST_CODE = 1003
        private const val WEATHER_UPDATE = "by.rudkouski.widget.WEATHER_UPDATE"
        private const val CURRENT_WEATHER_UPDATE = "by.rudkouski.widget.CURRENT_WEATHER_UPDATE"
        /*There is used Dark Sky API as data provider(https://darksky.net)*/
        private const val WEATHER_QUERY_BY_COORDINATES =
            "https://api.darksky.net/forecast/%1\$s/%2\$s,%3\$s?lang=%4\$s&units=si"

        fun getUpdateWeatherPendingIntent(context: Context): PendingIntent {
            return getPendingIntent(context, WEATHER_UPDATE, WEATHER_UPDATE_REQUEST_CODE)
        }

        fun updateAllWeathers(context: Context) {
            getUpdateWeatherPendingIntent(context).send()
        }

        fun updateCurrentWeather(context: Context) {
            getPendingIntent(context, CURRENT_WEATHER_UPDATE, CURRENT_WEATHER_UPDATE_REQUEST_CODE).send()
        }

        private fun getPendingIntent(context: Context, action: String, actionCode: Int): PendingIntent {
            val intent = Intent(context, WeatherUpdateBroadcastReceiver::class.java)
            intent.action = action
            return PendingIntent.getBroadcast(context, actionCode, intent, FLAG_UPDATE_CURRENT)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (WEATHER_UPDATE == intent.action || CURRENT_WEATHER_UPDATE == intent.action) {
            if (NetworkChangeChecker.isOnline()) {
                if (WEATHER_UPDATE == intent.action) {
                    updateAllWeathers(context)
                } else {
                    updateCurrentWeather(context)
                }
            } else {
                NetworkChangeChecker.registerReceiver()
            }
        }
    }

    private fun updateAllWeathers(context: Context) {
        executorService.execute {
            val locationIds = dbHelper.getLocationIdsContainedInAllWidgets()
            for (locationId in locationIds) {
                updateWeather(locationId)
            }
            sendIntentsForWidgetUpdate(context)
        }
    }

    private fun updateCurrentWeather(context: Context) {
        executorService.execute {
            updateWeather(CURRENT_LOCATION_ID)
            sendIntentsForWidgetUpdate(context)
        }
    }

    private fun updateWeather(locationId: Int) {
        if (CURRENT_LOCATION_ID == locationId && isPermissionsDenied()) {
            dbHelper.resetCurrentLocation()
            return
        }
        val location = dbHelper.getLocationById(locationId)
        try {
            val responseBody = getResponseBodyForLocationCoordinates(location.latitude, location.longitude)
            if (responseBody != null) {
                if (location.id == CURRENT_LOCATION_ID) {
                    val currentTimeZoneName =
                        WeatherUtils.getCurrentTimeZoneNameFromResponseBody(responseBody)
                    dbHelper.updateCurrentLocationTimeZoneName(currentTimeZoneName)
                }
                val currentWeather = WeatherUtils.getWeatherFromResponseBody(responseBody)
                val hourWeather = WeatherUtils.getHourWeatherFromResponseBody(responseBody)
                val dayForecast = WeatherUtils.getDayForecastFromResponseBody(responseBody)
                dbHelper.setWeatherByLocationId(currentWeather, locationId)
                dbHelper.setHourWeathersByLocationId(hourWeather, locationId)
                dbHelper.setDayForecastByLocationId(dayForecast, locationId)
            }
        } catch (e: Throwable) {
            Log.e(this.javaClass.simpleName, e.toString())
        }
    }

    private fun getResponseBodyForLocationCoordinates(latitude: Double, longitude: Double): String? {
        val request = String.format(Locale.getDefault(), WEATHER_QUERY_BY_COORDINATES, App.apiKey, latitude, longitude,
            Locale.getDefault().language)
        return getResponseBodyForRequest(request)
    }

    private fun getResponseBodyForRequest(req: String): String? {
        val client = OkHttpClient.Builder().build()
        val request = Request.Builder().url(req).build()
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    return responseBody.string()
                }
            }
        }
        return null
    }

    private fun sendIntentsForWidgetUpdate(context: Context) {
        WidgetProvider.updateWidget(context)
        ForecastActivity.updateActivityBroadcast(context)
    }
}