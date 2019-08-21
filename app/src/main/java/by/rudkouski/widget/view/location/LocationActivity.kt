package by.rudkouski.widget.view.location

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ListView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import by.rudkouski.widget.R
import by.rudkouski.widget.app.App.Companion.db
import by.rudkouski.widget.app.App.Companion.uiScope
import by.rudkouski.widget.entity.Location.Companion.CURRENT_LOCATION_ID
import by.rudkouski.widget.message.Message.showNetworkAndLocationEnableMessage
import by.rudkouski.widget.provider.WidgetProvider
import by.rudkouski.widget.update.listener.LocationChangeListener.isPermissionsDenied
import by.rudkouski.widget.update.listener.LocationChangeListener.startLocationUpdate
import by.rudkouski.widget.update.receiver.WeatherUpdateBroadcastReceiver
import by.rudkouski.widget.view.BaseActivity

class LocationActivity : BaseActivity(), LocationsViewAdapter.OnLocationItemClickListener {

    private lateinit var activityUpdateBroadcastReceiver: LocationActivityUpdateBroadcastReceiver

    companion object {
        private const val REQUEST_PERMISSION_CODE = 12345
        private const val LOCATION_ACTIVITY_UPDATE = "by.rudkouski.widget.LOCATION_ACTIVITY_UPDATE"

        fun startIntent(context: Context, widgetId: Int): Intent {
            val intent = Intent(context, LocationActivity::class.java)
            intent.putExtra(EXTRA_APPWIDGET_ID, widgetId)
            return intent
        }

        fun updateActivityBroadcast(context: Context) {
            val intent = Intent(LOCATION_ACTIVITY_UPDATE)
            context.sendBroadcast(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.location_activity)
        activityUpdateBroadcastReceiver = LocationActivityUpdateBroadcastReceiver()
        registerReceiver(activityUpdateBroadcastReceiver, IntentFilter(LOCATION_ACTIVITY_UPDATE))
        if (isPermissionsDenied()) {
            dbHelper.resetCurrentLocation()
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_PERMISSION_CODE)
        } else {
            initActivity()
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(activityUpdateBroadcastReceiver)
        finish()
    }

    private fun initActivity() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_config)
        setSupportActionBar(toolbar)
        setResult(RESULT_CANCELED)
        val handler = Handler(Looper.getMainLooper())
        handler.post(this::setLocations)
    }

    private fun setLocations() {
        val locationsView: ListView = findViewById(R.id.locations_config)
        var locations = dbHelper.getAllLocations()
        if (isPermissionsDenied()) {
            locations = locations.filter { location -> location.id != CURRENT_LOCATION_ID }
        } else {
            showNetworkAndLocationEnableMessage(locationsView, CURRENT_LOCATION_ID, this)
        }
        locationsView.adapter = LocationsViewAdapter(this, this, locations, getSelectedLocationId())
    }

    private fun getSelectedLocationId(): Int = dbHelper.getLocationByWidgetId(widgetId)

    override fun onLocationItemClick(view: View, locationId: Int) {
        val handler = Handler(Looper.getMainLooper())
        handler.post { locationItemClickEvent(locationId) }
    }

    private fun locationItemClickEvent(locationId: Int) {
        if (dbHelper.setWidgetById(widgetId, locationId)) {
            updateWidgetAndWeather()
            setResultIntent()
        } else {
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                    startLocationUpdate()
                    initActivity()
                }
            }
        }
    }

    private fun updateWidgetAndWeather() {
        WidgetProvider.updateWidget(this)
        WeatherUpdateBroadcastReceiver.updateAllWeathers(this)
    }

    private fun setResultIntent() {
        val result = Intent()
        result.putExtra(EXTRA_APPWIDGET_ID, widgetId)
        setResult(Activity.RESULT_OK, result)
        finish()
    }

    private inner class LocationActivityUpdateBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            initActivity()
        }
    }
}