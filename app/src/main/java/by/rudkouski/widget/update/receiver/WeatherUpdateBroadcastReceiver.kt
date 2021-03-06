package by.rudkouski.widget.update.receiver

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import by.rudkouski.widget.app.Constants.API_KEY
import by.rudkouski.widget.app.Constants.CURRENT_WEATHER_UPDATE_ACTION
import by.rudkouski.widget.app.Constants.CURRENT_WEATHER_UPDATE_REQUEST_CODE
import by.rudkouski.widget.app.Constants.OTHER_WEATHER_UPDATE_ACTION
import by.rudkouski.widget.app.Constants.OTHER_WEATHER_UPDATE_REQUEST_CODE
import by.rudkouski.widget.entity.Location
import by.rudkouski.widget.entity.Location.Companion.CURRENT_LOCATION_ID
import by.rudkouski.widget.provider.WidgetProvider.Companion.updateWidget
import by.rudkouski.widget.repository.ForecastRepository.setForecastsByLocationId
import by.rudkouski.widget.repository.LocationRepository.getAllUsedLocations
import by.rudkouski.widget.repository.LocationRepository.getLocationById
import by.rudkouski.widget.repository.LocationRepository.isLocationNotUsed
import by.rudkouski.widget.repository.LocationRepository.resetCurrentLocation
import by.rudkouski.widget.repository.LocationRepository.updateCurrentLocationZoneIdName
import by.rudkouski.widget.repository.WeatherRepository.setCurrentWeather
import by.rudkouski.widget.repository.WeatherRepository.setHourWeathersByLocationId
import by.rudkouski.widget.update.receiver.LocationUpdateBroadcastReceiver.Companion.isPermissionsDenied
import by.rudkouski.widget.update.receiver.NetworkChangeChecker.isOnline
import by.rudkouski.widget.update.receiver.NetworkChangeChecker.registerNetworkChangeReceiver
import by.rudkouski.widget.util.JsonUtils.getCurrentWeatherFromResponseBody
import by.rudkouski.widget.util.JsonUtils.getCurrentZoneIdFromResponseBody
import by.rudkouski.widget.util.JsonUtils.getDayForecastFromResponseBody
import by.rudkouski.widget.util.JsonUtils.getHourWeathersFromResponseBody
import by.rudkouski.widget.view.forecast.ForecastActivity.Companion.updateForecastActivityBroadcast
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.threeten.bp.OffsetDateTime
import java.util.*

class WeatherUpdateBroadcastReceiver : BroadcastReceiver() {

    companion object : NetworkChangeChecker.NetworkObserver {
        /*There is used Dark Sky API as data provider(https://darksky.net)*/
        private const val WEATHER_QUERY_BY_COORDINATES = "https://api.darksky.net/forecast/%1\$s/%2\$s,%3\$s?lang=%4\$s&units=si"

        override fun startUpdate(context: Context) {
            updateOtherWeathers(context)
        }

        fun getWeatherUpdatePendingIntent(context: Context): PendingIntent {
            return getPendingIntent(context, OTHER_WEATHER_UPDATE_ACTION, OTHER_WEATHER_UPDATE_REQUEST_CODE)
        }

        fun updateOtherWeathers(context: Context) {
            getWeatherUpdatePendingIntent(context).send()
        }

        fun updateCurrentWeather(context: Context) {
            getPendingIntent(context, CURRENT_WEATHER_UPDATE_ACTION, CURRENT_WEATHER_UPDATE_REQUEST_CODE).send()
        }

        private fun getPendingIntent(context: Context, action: String, actionCode: Int): PendingIntent {
            val intent = Intent(context, WeatherUpdateBroadcastReceiver::class.java)
            intent.action = action
            return PendingIntent.getBroadcast(context, actionCode, intent, FLAG_UPDATE_CURRENT)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (OTHER_WEATHER_UPDATE_ACTION == intent.action || CURRENT_WEATHER_UPDATE_ACTION == intent.action) {
            GlobalScope.launch {
                try {
                    if (OTHER_WEATHER_UPDATE_ACTION == intent.action) {
                        if (isOnline()) updateOtherWeathers(context) else registerNetworkChangeReceiver(Companion)
                    } else {
                        if (isOnline()) updateCurrentWeather(context) else registerNetworkChangeReceiver(LocationUpdateBroadcastReceiver.Companion)
                    }
                } catch (e: Throwable) {
                    Log.e(this.javaClass.simpleName, e.message ?: "Error during updating weather")
                }
            }
        }
    }

    private fun updateOtherWeathers(context: Context) {
        getAllUsedLocations()?.filter { CURRENT_LOCATION_ID != it.id }?.forEach { updateWeather(it) }?.also { sendUpdateIntents(context) }
    }

    private fun updateCurrentWeather(context: Context) {
        if (!isLocationNotUsed(CURRENT_LOCATION_ID)) {
            val location = getLocationById(CURRENT_LOCATION_ID)
            updateWeather(location!!)
            sendUpdateIntents(context)
        }
    }

    private fun updateWeather(location: Location) {
        if (CURRENT_LOCATION_ID == location.id && isPermissionsDenied()) {
            resetCurrentLocation()
            return
        }
        val responseBody = getResponseBodyForLocationCoordinates(location.latitude, location.longitude)
        if (responseBody != null) {
            val zoneId =
                if (location.id == CURRENT_LOCATION_ID) {
                    val currentZoneId = getCurrentZoneIdFromResponseBody(responseBody)
                    updateCurrentLocationZoneIdName(currentZoneId)
                    currentZoneId
                } else {
                    location.zoneId
                }
            val updateTime = OffsetDateTime.now(zoneId)
            val currentWeather = getCurrentWeatherFromResponseBody(responseBody, location.id, zoneId, updateTime)
            setCurrentWeather(currentWeather, location.id)
            val hourWeathers = getHourWeathersFromResponseBody(responseBody, location.id, zoneId, updateTime)
            setHourWeathersByLocationId(hourWeathers, location.id)
            val forecasts = getDayForecastFromResponseBody(responseBody, location.id, zoneId)
            setForecastsByLocationId(forecasts, location.id)
        }
    }

    private fun getResponseBodyForLocationCoordinates(latitude: Double, longitude: Double): String? {
        val request = String.format(Locale.getDefault(), WEATHER_QUERY_BY_COORDINATES, API_KEY, latitude, longitude, Locale.getDefault().language)
        return getResponseBodyForRequest(request)
    }

    private fun getResponseBodyForRequest(req: String): String? {
        val client = OkHttpClient.Builder().build()
        val request = Request.Builder().url(req).build()
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val responseBody = response.body
                if (responseBody != null) {
                    return responseBody.string()
                }
            }
        }
        return null
    }

    private fun sendUpdateIntents(context: Context) {
        updateWidget(context)
        updateForecastActivityBroadcast(context)
    }
}