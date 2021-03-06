package by.rudkouski.widget.app

import android.app.Application
import android.content.Context
import by.rudkouski.widget.entity.Setting
import by.rudkouski.widget.repository.SettingRepository.getSettingByCode
import by.rudkouski.widget.update.receiver.LocationUpdateBroadcastReceiver.Companion.setCurrentLocation
import by.rudkouski.widget.update.receiver.WidgetUpdateBroadcastReceiver.registerWidgetUpdateReceiver
import by.rudkouski.widget.update.receiver.WidgetUpdateBroadcastReceiver.unregisterWidgetUpdateReceiver
import by.rudkouski.widget.update.scheduler.UpdateWeatherScheduler.startLocationUpdateScheduler
import by.rudkouski.widget.update.scheduler.UpdateWeatherScheduler.startWeatherUpdateScheduler
import by.rudkouski.widget.update.scheduler.UpdateWeatherScheduler.stopLocationUpdateScheduler
import by.rudkouski.widget.update.scheduler.UpdateWeatherScheduler.stopWeatherUpdateScheduler
import com.rohitss.uceh.UCEHandler

class App : Application() {

    companion object {
        lateinit var appContext: Context
        var weatherUpdateInMinutes: Long = 30
        var locationUpdateInMinutes: Long = 15
        var isLocationExact: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        weatherUpdateInMinutes = getSettingByCode(Setting.Code.SETTING_WEATHER)?.value?.toLong() ?: weatherUpdateInMinutes
        locationUpdateInMinutes = getSettingByCode(Setting.Code.SETTING_LOCATION)?.value?.toLong() ?: locationUpdateInMinutes
        isLocationExact = getSettingByCode(Setting.Code.SETTING_EXACT_LOCATION)?.getBooleanValue() ?: isLocationExact
        registerWidgetUpdateReceiver(this)
        startWeatherUpdateScheduler()
        startLocationUpdateScheduler()
        setCurrentLocation()
        UCEHandler.Builder(applicationContext).setUCEHEnabled(false).build()
    }

    override fun onTerminate() {
        super.onTerminate()
        unregisterWidgetUpdateReceiver(this)
        stopWeatherUpdateScheduler()
        stopLocationUpdateScheduler()
    }
}