package by.rudkouski.widget.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.*
import by.rudkouski.widget.R
import by.rudkouski.widget.app.App
import by.rudkouski.widget.app.App.Companion.appContext
import by.rudkouski.widget.entity.*
import by.rudkouski.widget.entity.Location.Companion.CURRENT_LOCATION_ID
import java.util.*
import kotlin.Int.Companion.MIN_VALUE
import kotlin.collections.ArrayList

class DBHelper private constructor(context: Context, dbName: String, factory: SQLiteDatabase.CursorFactory,
                                   dbVersion: Int) : SQLiteOpenHelper(context, dbName, factory, dbVersion) {

    private val database: SQLiteDatabase = writableDatabase

    companion object {
        private const val DATABASE_VERSION: Int = 2
        private const val DATABASE_NAME: String = "clock_weather"
        val INSTANCE = DBHelper(App.appContext, DATABASE_NAME, Factory(), DATABASE_VERSION)

        private const val LOCATION_TABLE = "locations"
        private const val LOCATION_ID = "location_id"
        private const val LOCATION_LATITUDE = "location_latitude"
        private const val LOCATION_LONGITUDE = "location_longitude"
        private const val LOCATION_NAME_CODE = "location_name_code"
        private const val LOCATION_TIME_ZONE = "location_time_zone"

        private const val WIDGET_TABLE = "widgets"
        private const val WIDGET_ID = "widget_id"
        private const val WIDGET_BOLD = "widget_bold"
        private const val WIDGET_LOCATION_ID = "widget_location_id"

        private const val WEATHER_DATA_TABLE = "weather_data"
        private const val WEATHER_DATA_ID = "weather_data_id"
        private const val WEATHER_DATA_DATE = "weather_data_date"
        private const val WEATHER_DATA_DESCRIPTION = "weather_data_description"
        private const val WEATHER_DATA_ICON = "weather_data_icon"
        private const val WEATHER_DATA_PRECIPITATION_INTENSITY = "weather_data_precip_intensity"
        private const val WEATHER_DATA_PRECIPITATION_PROBABILITY = "weather_data_precip_probability"
        private const val WEATHER_DATA_DEW_POINT = "weather_data_dew_point"
        private const val WEATHER_DATA_HUMIDITY = "weather_data_humidity"
        private const val WEATHER_DATA_PRESSURE = "weather_data_pressure"
        private const val WEATHER_DATA_WIND_SPEED = "weather_data_wind_speed"
        private const val WEATHER_DATA_WIND_GUST = "weather_data_wind_gust"
        private const val WEATHER_DATA_WIND_DIRECTION = "weather_data_wind_direction"
        private const val WEATHER_DATA_CLOUD_COVER = "weather_data_cloud_cover"
        private const val WEATHER_DATA_UV_INDEX = "weather_data_uv_index"
        private const val WEATHER_DATA_VISIBILITY = "weather_data_visibility"
        private const val WEATHER_DATA_OZONE = "weather_data_ozone"
        private const val WEATHER_DATA_LOCATION_ID = "weather_data_location_id"

        private const val WEATHER_TABLE = "weathers"
        private const val WEATHER_ID = "weather_id"
        private const val WEATHER_TEMP = "weather_temp"
        private const val WEATHER_APPARENT_TEMP = "weather_apparent_temp"
        private const val WEATHER_LOCATION_ID = "weather_location_id"

        private const val FORECAST_TABLE = "forecasts"
        private const val FORECAST_ID = "forecast_id"
        private const val FORECAST_SUNRISE_TIME = "forecast_sunrise_time"
        private const val FORECAST_SUNSET_TIME = "forecast_sunset_time"
        private const val FORECAST_MOON_PHASE = "forecast_moon_phase"
        private const val FORECAST_PRECIPITATION_INTENSITY_MAX = "forecast_precip_intensity_max"
        private const val FORECAST_PRECIPITATION_INTENSITY_MAX_TIME = "forecast_precip_intensity_max_time"
        private const val FORECAST_PRECIPITATION_ACCUMULATION = "forecast_precip_accumulation"
        private const val FORECAST_PRECIPITATION_TYPE = "forecast_precip_type"
        private const val FORECAST_TEMP_HIGH = "forecast_temp_high"
        private const val FORECAST_TEMP_HIGH_TIME = "forecast_temp_high_time"
        private const val FORECAST_TEMP_LOW = "forecast_temp_low"
        private const val FORECAST_TEMP_LOW_TIME = "forecast_temp_low_time"
        private const val FORECAST_APPARENT_TEMP_HIGH = "forecast_apparent_temp_high"
        private const val FORECAST_APPARENT_TEMP_HIGH_TIME = "forecast_apparent_temp_high_time"
        private const val FORECAST_APPARENT_TEMP_LOW = "forecast_apparent_temp_low"
        private const val FORECAST_APPARENT_TEMP_LOW_TIME = "forecast_apparent_temp_low_time"
        private const val FORECAST_LOCATION_ID = "forecast_location_id"

        private const val IS_EQUAL_PARAMETER = " = ?"
        private const val DROP_TABLE_IF_EXISTS: String = "DROP TABLE IF EXISTS "
        private const val CURRENT_LOCATION = "current_location"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys = 'ON'")
        db.execSQL("CREATE TABLE IF NOT EXISTS " + LOCATION_TABLE + " (" + LOCATION_ID + " INTEGER PRIMARY KEY, " +
            LOCATION_LATITUDE + " DOUBLE, " + LOCATION_LONGITUDE + " DOUBLE, " + LOCATION_NAME_CODE + " TEXT, " +
            LOCATION_TIME_ZONE + " TEXT);")
        db.execSQL("CREATE TABLE IF NOT EXISTS " + WIDGET_TABLE + " (" + WIDGET_ID + " INTEGER PRIMARY KEY, " +
            WIDGET_BOLD + " INTEGER, " + WIDGET_LOCATION_ID + " INTEGER, FOREIGN KEY (" + WIDGET_LOCATION_ID + ") REFERENCES " +
            LOCATION_TABLE + " (" + LOCATION_ID + ") ON DELETE CASCADE);")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS " + WEATHER_DATA_TABLE + " (" + WEATHER_DATA_ID + " INTEGER PRIMARY KEY, " +
                WEATHER_DATA_DATE + " INTEGER, " + WEATHER_DATA_DESCRIPTION + " TEXT, " + WEATHER_DATA_ICON + " TEXT, " +
                WEATHER_DATA_PRECIPITATION_INTENSITY + " DOUBLE, " + WEATHER_DATA_PRECIPITATION_PROBABILITY + " DOUBLE, " +
                WEATHER_DATA_DEW_POINT + " DOUBLE, " + WEATHER_DATA_HUMIDITY + " DOUBLE, " + WEATHER_DATA_PRESSURE + " DOUBLE, " +
                WEATHER_DATA_WIND_SPEED + " DOUBLE, " + WEATHER_DATA_WIND_GUST + " DOUBLE, " + WEATHER_DATA_WIND_DIRECTION + " INTEGER, " +
                WEATHER_DATA_CLOUD_COVER + " DOUBLE, " + WEATHER_DATA_UV_INDEX + " INTEGER, " + WEATHER_DATA_VISIBILITY + " DOUBLE, " +
                WEATHER_DATA_OZONE + " DOUBLE, " + WEATHER_DATA_LOCATION_ID + " INTEGER, FOREIGN KEY (" + WEATHER_DATA_LOCATION_ID +
                ") REFERENCES " + LOCATION_TABLE + " (" + LOCATION_ID + ") ON DELETE CASCADE);")
        db.execSQL("CREATE TABLE IF NOT EXISTS " + WEATHER_TABLE + " (" + WEATHER_ID + " INTEGER PRIMARY KEY, " +
            WEATHER_TEMP + " DOUBLE, " + WEATHER_APPARENT_TEMP + " DOUBLE, " + WEATHER_LOCATION_ID +
            " INTEGER, FOREIGN KEY (" + WEATHER_LOCATION_ID + ") REFERENCES " + LOCATION_TABLE +
            " (" + LOCATION_ID + ") ON DELETE CASCADE);")
        db.execSQL("CREATE TABLE IF NOT EXISTS " + FORECAST_TABLE + " (" + FORECAST_ID + " INTEGER PRIMARY KEY, " +
            FORECAST_SUNRISE_TIME + " INTEGER, " + FORECAST_SUNSET_TIME + " INTEGER, " + FORECAST_MOON_PHASE + " DOUBLE, " +
            FORECAST_PRECIPITATION_INTENSITY_MAX + " DOUBLE, " + FORECAST_PRECIPITATION_INTENSITY_MAX_TIME + " INTEGER, " +
            FORECAST_PRECIPITATION_ACCUMULATION + " DOUBLE, " + FORECAST_PRECIPITATION_TYPE + " TEXT, " +
            FORECAST_TEMP_HIGH + " DOUBLE, " + FORECAST_TEMP_HIGH_TIME + " INTEGER, " + FORECAST_TEMP_LOW + " DOUBLE, " +
            FORECAST_TEMP_LOW_TIME + " INTEGER, " + FORECAST_APPARENT_TEMP_HIGH + " DOUBLE, " +
            FORECAST_APPARENT_TEMP_HIGH_TIME + " INTEGER, " + FORECAST_APPARENT_TEMP_LOW + " DOUBLE, " +
            FORECAST_APPARENT_TEMP_LOW_TIME + " INTEGER, " + FORECAST_LOCATION_ID + " INTEGER, FOREIGN KEY (" +
            FORECAST_LOCATION_ID + ") REFERENCES " + LOCATION_TABLE + " (" + LOCATION_ID + ") ON DELETE CASCADE);")
        addDefaultLocations(db)
    }

    private fun addDefaultLocations(db: SQLiteDatabase) {
        val defaultLocations: Array<String> = App.appContext.resources.getStringArray(R.array.default_locations)
        for (defaultLocation in defaultLocations) {
            val location = defaultLocation.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            with(ContentValues()) {
                put(LOCATION_NAME_CODE, if (location[0].isEmpty()) CURRENT_LOCATION else location[0])
                put(LOCATION_LATITUDE, location[1])
                put(LOCATION_LONGITUDE, location[2])
                if (location.size == 4) put(LOCATION_TIME_ZONE, location[3])
                db.insert(LOCATION_TABLE, null, this)
            }
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        with(db) {
            execSQL(DROP_TABLE_IF_EXISTS + LOCATION_TABLE)
            execSQL(DROP_TABLE_IF_EXISTS + WIDGET_TABLE)
            execSQL(DROP_TABLE_IF_EXISTS + WEATHER_TABLE)
            onCreate(this)
        }
    }

    //Location methods

    fun getAllLocations(): List<Location> {
        val locations = ArrayList<Location>()
        database.query(LOCATION_TABLE, null, null, null, null, null, null).use { cursor ->
            if (cursor.moveToFirst()) {
                for (i in 0 until cursor.count) {
                    if (cursor.moveToPosition(i)) {
                        val location = createLocation(cursor)
                        locations.add(location)
                    }
                }
            }
        }
        return locations
    }

    fun getLocationById(locationId: Int): Location {
        return getLocationFromDatabaseById(database, locationId)
    }

    fun updateCurrentLocation(locationName: String, latitude: Double, longitude: Double) {
        val values = ContentValues()
        values.put(LOCATION_NAME_CODE, locationName)
        values.put(LOCATION_LATITUDE, latitude)
        values.put(LOCATION_LONGITUDE, longitude)
        database.update(LOCATION_TABLE, values, LOCATION_ID + IS_EQUAL_PARAMETER,
            arrayOf(Location.CURRENT_LOCATION_ID.toString()))
    }

    fun isCurrentLocationNotUpdated() =
        getLocationById(Location.CURRENT_LOCATION_ID).name == appContext.getString(R.string.default_location)

    private fun getLocationFromDatabaseById(db: SQLiteDatabase, locationId: Int): Location {
        db.query(LOCATION_TABLE, null, LOCATION_ID + IS_EQUAL_PARAMETER, arrayOf(locationId.toString()), null, null,
            null).use { cursor ->
            if (cursor.moveToFirst()) {
                return createLocation(cursor)
            }
        }
        throw RuntimeException("An error occurred while getting the location with id = $locationId")
    }

    private fun createLocation(cursor: Cursor): Location {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(LOCATION_ID))
        val nameCode = cursor.getString(cursor.getColumnIndexOrThrow(LOCATION_NAME_CODE))
        val latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(LOCATION_LATITUDE))
        val longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(LOCATION_LONGITUDE))
        return if (id != CURRENT_LOCATION_ID) {
            val timeZone = TimeZone.getTimeZone(cursor.getString(cursor.getColumnIndexOrThrow(LOCATION_TIME_ZONE)))
            Location(id, nameCode, latitude, longitude, timeZone)
        } else {
            Location.createCurrentLocation(
                if (nameCode == CURRENT_LOCATION) appContext.getString(R.string.default_location) else nameCode,
                latitude, longitude)
        }
    }

    fun getLocationIdsContainedInAllWidgets(): List<Int> {
        return getLocationIdsForAllWidgetsFromDatabase(database)
    }

    fun getLocationByWidgetId(widgetId: Int): Int {
        val widget = getWidgetFromDatabase(database, widgetId)
        return widget?.location?.id ?: MIN_VALUE
    }

    private fun getLocationIdsForAllWidgetsFromDatabase(db: SQLiteDatabase): List<Int> {
        val locationIds = ArrayList<Int>()
        db.query(true, WIDGET_TABLE, arrayOf(WIDGET_LOCATION_ID), null, null, null, null, null, null).use { cursor ->
            if (cursor.moveToFirst()) {
                for (i in 0 until cursor.count) {
                    if (cursor.moveToPosition(i)) {
                        locationIds.add(cursor.getInt(cursor.getColumnIndexOrThrow(WIDGET_LOCATION_ID)))
                    }
                }
            }
        }
        return locationIds
    }

    //widget methods

    fun getWidgetById(widgetId: Int): Widget? {
        return getWidgetFromDatabase(database, widgetId)
    }

    private fun getWidgetFromDatabase(db: SQLiteDatabase, widgetId: Int): Widget? {
        db.query(WIDGET_TABLE, null, WIDGET_ID + IS_EQUAL_PARAMETER, arrayOf(widgetId.toString()), null, null, null)
            .use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow(WIDGET_ID))
                    val isBold = cursor.getInt(cursor.getColumnIndexOrThrow(WIDGET_BOLD)) != 0
                    val location =
                        getLocationFromDatabaseById(db, cursor.getInt(cursor.getColumnIndexOrThrow(WIDGET_LOCATION_ID)))
                    return Widget(id, location, isBold)
                }
            }
        return null
    }

    fun deleteWidgetById(widgetId: Int) {
        database.beginTransaction()
        try {
            val widget = getWidgetFromDatabase(database, widgetId)
            if (widget != null) {
                database.delete(WIDGET_TABLE, WIDGET_ID + IS_EQUAL_PARAMETER, arrayOf(widgetId.toString()))
                val oldLocationId = widget.location.id
                deleteWeatherForLocationWithoutWidget(database, oldLocationId)
                database.setTransactionSuccessful()
            }
        } finally {
            database.endTransaction()
        }
    }

    fun setWidgetById(widgetId: Int, locationId: Int): Boolean {
        database.beginTransaction()
        try {
            val existedWidget = getWidgetFromDatabase(database, widgetId)
            val location = getLocationFromDatabaseById(database, locationId)
            if (existedWidget != null) {
                if (existedWidget.location.id == locationId) {
                    return false
                }
                val newWidget = Widget(widgetId, location, existedWidget.isBold)
                if (existedWidget.location.id != locationId) {
                    updateWidget(database, newWidget)
                    deleteWeatherForLocationWithoutWidget(database, existedWidget.location.id)
                    database.setTransactionSuccessful()
                }
            } else {
                val newWidget = Widget(widgetId, location, false)
                addWidget(database, newWidget)
                database.setTransactionSuccessful()
            }
            return true
        } finally {
            database.endTransaction()
        }
    }

    private fun addWidget(db: SQLiteDatabase, widget: Widget) {
        val values = createValuesForWidget(widget)
        values.put(WIDGET_ID, widget.id)
        db.insert(WIDGET_TABLE, null, values)
    }

    private fun updateWidget(db: SQLiteDatabase, widget: Widget) {
        val values = createValuesForWidget(widget)
        db.update(WIDGET_TABLE, values, WIDGET_ID + IS_EQUAL_PARAMETER, arrayOf((widget.id.toString())))
    }

    private fun createValuesForWidget(widget: Widget): ContentValues {
        with(ContentValues()) {
            put(WIDGET_LOCATION_ID, widget.location.id)
            put(WIDGET_BOLD, if (widget.isBold) 1 else 0)
            return this
        }
    }


    fun changeWidgetTextBold(widgetId: Int) {
        database.beginTransaction()
        try {
            val existWidget = getWidgetFromDatabase(database, widgetId)
            if (existWidget != null) {
                val newWidget = Widget(widgetId, existWidget.location, !existWidget.isBold)
                updateWidget(database, newWidget)
                database.setTransactionSuccessful()
            }
        } finally {
            database.endTransaction()
        }
    }

    //weather methods

    fun setWeatherByLocationId(newWeather: CurrentWeather, locationId: Int) {
        database.beginTransaction()
        try {
            val existedWeather = getWeatherFromDatabase(database, locationId)
            if (existedWeather != null) {
                updateWeather(database, CurrentWeather(existedWeather.id, newWeather))
            } else {
                addWeather(database, newWeather, locationId)
            }
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }

    fun getWeatherByLocationId(locationId: Int): CurrentWeather? {
        return getWeatherFromDatabase(database, locationId)
    }

    private fun getWeatherFromDatabase(db: SQLiteDatabase, locationId: Int): CurrentWeather? {
        val query = "SELECT * FROM " + WEATHER_TABLE + " INNER JOIN " + WEATHER_DATA_TABLE + " ON " +
            WEATHER_ID + " = " + WEATHER_DATA_ID + " WHERE " + WEATHER_LOCATION_ID + IS_EQUAL_PARAMETER
        db.rawQuery(query, arrayOf(locationId.toString()))
            .use { cursor ->
                if (cursor.moveToFirst()) {
                    return createWeather(cursor)
                }
            }
        return null
    }

    private fun createWeather(cursor: Cursor): CurrentWeather {
        val date = Date(cursor.getLong(cursor.getColumnIndexOrThrow(WEATHER_DATA_DATE)))
        val description = cursor.getString(cursor.getColumnIndexOrThrow(WEATHER_DATA_DESCRIPTION))
        val icon = cursor.getString(cursor.getColumnIndexOrThrow(WEATHER_DATA_ICON))
        val precipitationIntensity =
            cursor.getDouble(cursor.getColumnIndexOrThrow(WEATHER_DATA_PRECIPITATION_INTENSITY))
        val precipitationProbability =
            cursor.getDouble(cursor.getColumnIndexOrThrow(WEATHER_DATA_PRECIPITATION_PROBABILITY))
        val dewPoint = cursor.getDouble(cursor.getColumnIndexOrThrow(WEATHER_DATA_DEW_POINT))
        val humidity = cursor.getDouble(cursor.getColumnIndexOrThrow(WEATHER_DATA_HUMIDITY))
        val pressure = cursor.getDouble(cursor.getColumnIndexOrThrow(WEATHER_DATA_PRESSURE))
        val windSpeed = cursor.getDouble(cursor.getColumnIndexOrThrow(WEATHER_DATA_WIND_SPEED))
        val windGust = cursor.getDouble(cursor.getColumnIndexOrThrow(WEATHER_DATA_WIND_GUST))
        val windDirection = cursor.getInt(cursor.getColumnIndexOrThrow(WEATHER_DATA_WIND_DIRECTION))
        val cloudCover = cursor.getDouble(cursor.getColumnIndexOrThrow(WEATHER_DATA_CLOUD_COVER))
        val uvIndex = cursor.getInt(cursor.getColumnIndexOrThrow(WEATHER_DATA_UV_INDEX))
        val visibility = cursor.getDouble(cursor.getColumnIndexOrThrow(WEATHER_DATA_VISIBILITY))
        val ozone = cursor.getDouble(cursor.getColumnIndexOrThrow(WEATHER_DATA_OZONE))

        val id = cursor.getLong(cursor.getColumnIndexOrThrow(WEATHER_ID))
        val temperature = cursor.getDouble(cursor.getColumnIndexOrThrow(WEATHER_TEMP))
        val apparentTemperature = cursor.getDouble(cursor.getColumnIndexOrThrow(WEATHER_APPARENT_TEMP))

        return CurrentWeather(id, date, description, icon, precipitationIntensity, precipitationProbability, dewPoint,
            humidity, pressure, windSpeed, windGust, windDirection, cloudCover, visibility, ozone, uvIndex, temperature,
            apparentTemperature)
    }

    private fun updateWeather(db: SQLiteDatabase, weather: CurrentWeather) {
        val weatherValues = createWeatherContentValues(weather)
        db.update(WEATHER_TABLE, weatherValues, WEATHER_ID + IS_EQUAL_PARAMETER, arrayOf(weather.id.toString()))
        val weatherDataValues = createWeatherDataContentValues(weather)
        db.update(WEATHER_DATA_TABLE, weatherDataValues, WEATHER_DATA_ID + IS_EQUAL_PARAMETER,
            arrayOf(weather.id.toString()))
    }

    private fun addWeather(db: SQLiteDatabase, weather: CurrentWeather, locationId: Int) {
        val commonId = getId()
        val weatherDataValues = createWeatherDataContentValues(weather)
        weatherDataValues.put(WEATHER_DATA_ID, commonId)
        weatherDataValues.put(WEATHER_DATA_LOCATION_ID, locationId)
        db.insert(WEATHER_DATA_TABLE, null, weatherDataValues)
        val weatherValues = createWeatherContentValues(weather)
        weatherValues.put(WEATHER_ID, commonId)
        weatherValues.put(WEATHER_LOCATION_ID, locationId)
        db.insert(WEATHER_TABLE, null, weatherValues)
    }

    private fun createWeatherDataContentValues(weatherData: WeatherData): ContentValues {
        with(ContentValues()) {
            put(WEATHER_DATA_DATE, weatherData.date.time)
            put(WEATHER_DATA_DESCRIPTION, weatherData.description)
            put(WEATHER_DATA_ICON, weatherData.iconName)
            put(WEATHER_DATA_PRECIPITATION_INTENSITY, weatherData.precipitationIntensity)
            put(WEATHER_DATA_PRECIPITATION_PROBABILITY, weatherData.precipitationProbability)
            put(WEATHER_DATA_DEW_POINT, weatherData.dewPoint)
            put(WEATHER_DATA_HUMIDITY, weatherData.humidity)
            put(WEATHER_DATA_PRESSURE, weatherData.pressure)
            put(WEATHER_DATA_WIND_SPEED, weatherData.windSpeed)
            put(WEATHER_DATA_WIND_GUST, weatherData.windGust)
            put(WEATHER_DATA_WIND_DIRECTION, weatherData.windDirection)
            put(WEATHER_DATA_CLOUD_COVER, weatherData.cloudCover)
            put(WEATHER_DATA_UV_INDEX, weatherData.uvIndex)
            put(WEATHER_DATA_VISIBILITY, weatherData.visibility)
            put(WEATHER_DATA_OZONE, weatherData.ozone)
            return this
        }
    }

    private fun createWeatherContentValues(weather: CurrentWeather): ContentValues {
        with(ContentValues()) {
            put(WEATHER_TEMP, weather.temperature)
            put(WEATHER_APPARENT_TEMP, weather.apparentTemperature)
            return this
        }
    }

    private fun deleteWeatherForLocationWithoutWidget(db: SQLiteDatabase, locationId: Int) {
        if (!getLocationIdsForAllWidgetsFromDatabase(db).contains(locationId)) {
            db.delete(WEATHER_DATA_TABLE, WEATHER_DATA_LOCATION_ID + IS_EQUAL_PARAMETER, arrayOf(locationId.toString()))
            db.delete(WEATHER_TABLE, WEATHER_LOCATION_ID + IS_EQUAL_PARAMETER, arrayOf(locationId.toString()))
            db.delete(FORECAST_TABLE, FORECAST_LOCATION_ID + IS_EQUAL_PARAMETER, arrayOf(locationId.toString()))
        }
    }

    //forecast methods

    fun setDayForecastByLocationId(dayForecast: DayForecast, locationId: Int) {
        database.beginTransaction()
        try {
            val existedForecasts = getForecastsFromDatabase(database, locationId)
            if (existedForecasts.isNullOrEmpty()) {
                addForecasts(database, dayForecast.forecasts, locationId)
            } else {
                updateForecasts(database, existedForecasts, dayForecast.forecasts, locationId)
            }
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }

    private fun createForecast(cursor: Cursor): Forecast {
        val date = Date(cursor.getLong(cursor.getColumnIndexOrThrow(WEATHER_DATA_DATE)))
        val description = cursor.getString(cursor.getColumnIndexOrThrow(WEATHER_DATA_DESCRIPTION))
        val icon = cursor.getString(cursor.getColumnIndexOrThrow(WEATHER_DATA_ICON))
        val precipitationIntensity =
            cursor.getDouble(cursor.getColumnIndexOrThrow(WEATHER_DATA_PRECIPITATION_INTENSITY))
        val precipitationProbability =
            cursor.getDouble(cursor.getColumnIndexOrThrow(WEATHER_DATA_PRECIPITATION_PROBABILITY))
        val dewPoint = cursor.getDouble(cursor.getColumnIndexOrThrow(WEATHER_DATA_DEW_POINT))
        val humidity = cursor.getDouble(cursor.getColumnIndexOrThrow(WEATHER_DATA_HUMIDITY))
        val pressure = cursor.getDouble(cursor.getColumnIndexOrThrow(WEATHER_DATA_PRESSURE))
        val windSpeed = cursor.getDouble(cursor.getColumnIndexOrThrow(WEATHER_DATA_WIND_SPEED))
        val windGust = cursor.getDouble(cursor.getColumnIndexOrThrow(WEATHER_DATA_WIND_GUST))
        val windDirection = cursor.getInt(cursor.getColumnIndexOrThrow(WEATHER_DATA_WIND_DIRECTION))
        val cloudCover = cursor.getDouble(cursor.getColumnIndexOrThrow(WEATHER_DATA_CLOUD_COVER))
        val uvIndex = cursor.getInt(cursor.getColumnIndexOrThrow(WEATHER_DATA_UV_INDEX))
        val visibility = cursor.getDouble(cursor.getColumnIndexOrThrow(WEATHER_DATA_VISIBILITY))
        val ozone = cursor.getDouble(cursor.getColumnIndexOrThrow(WEATHER_DATA_OZONE))

        val id = cursor.getLong(cursor.getColumnIndexOrThrow(FORECAST_ID))
        val sunrise = Date(cursor.getLong(cursor.getColumnIndexOrThrow(FORECAST_SUNRISE_TIME)))
        val sunset = Date(cursor.getLong(cursor.getColumnIndexOrThrow(FORECAST_SUNSET_TIME)))
        val moonPhase = cursor.getDouble(cursor.getColumnIndexOrThrow(FORECAST_MOON_PHASE))
        val precipIntensityMax = cursor.getDouble(cursor.getColumnIndexOrThrow(FORECAST_PRECIPITATION_INTENSITY_MAX))
        val precipIntensityMaxTime =
            Date(cursor.getLong(cursor.getColumnIndexOrThrow(FORECAST_PRECIPITATION_INTENSITY_MAX_TIME)))
        val precipAccumulation = cursor.getDouble(cursor.getColumnIndexOrThrow(FORECAST_PRECIPITATION_ACCUMULATION))
        val precipType = cursor.getString(cursor.getColumnIndexOrThrow(FORECAST_PRECIPITATION_TYPE))
        val tempHigh = cursor.getDouble(cursor.getColumnIndexOrThrow(FORECAST_TEMP_HIGH))
        val tempHighTime = Date(cursor.getLong(cursor.getColumnIndexOrThrow(FORECAST_TEMP_HIGH_TIME)))
        val tempLow = cursor.getDouble(cursor.getColumnIndexOrThrow(FORECAST_TEMP_LOW))
        val tempLowTime = Date(cursor.getLong(cursor.getColumnIndexOrThrow(FORECAST_TEMP_LOW_TIME)))
        val apparentTempHigh = cursor.getDouble(cursor.getColumnIndexOrThrow(FORECAST_APPARENT_TEMP_HIGH))
        val apparentTempHighTime = Date(cursor.getLong(cursor.getColumnIndexOrThrow(FORECAST_APPARENT_TEMP_HIGH_TIME)))
        val apparentTempLow = cursor.getDouble(cursor.getColumnIndexOrThrow(FORECAST_APPARENT_TEMP_LOW))
        val apparentTempLowTime = Date(cursor.getLong(cursor.getColumnIndexOrThrow(FORECAST_APPARENT_TEMP_LOW_TIME)))

        return Forecast(id, date, description, icon, precipitationIntensity, precipitationProbability, dewPoint,
            humidity, pressure, windSpeed, windGust, windDirection, cloudCover, visibility, ozone, uvIndex, sunrise,
            sunset, moonPhase, precipIntensityMax, precipIntensityMaxTime, precipAccumulation, precipType, tempHigh,
            tempHighTime, tempLow, tempLowTime, apparentTempHigh, apparentTempHighTime, apparentTempLow,
            apparentTempLowTime)
    }

    fun getDayForecastsByLocationId(locationId: Int): List<Forecast>? {
        return getForecastsFromDatabase(database, locationId)
    }

    private fun getForecastsFromDatabase(db: SQLiteDatabase, locationId: Int): List<Forecast>? {
        val query = "SELECT * FROM " + FORECAST_TABLE + " INNER JOIN " + WEATHER_DATA_TABLE + " ON " +
            FORECAST_ID + " = " + WEATHER_DATA_ID + " WHERE " + FORECAST_LOCATION_ID + IS_EQUAL_PARAMETER + "ORDER BY " + WEATHER_DATA_DATE
        val forecasts = ArrayList<Forecast>()
        db.rawQuery(query, arrayOf(locationId.toString())).use { cursor ->
            if (cursor.moveToFirst()) {
                for (i in 0 until cursor.count) {
                    if (cursor.moveToPosition(i)) {
                        forecasts.add(createForecast(cursor))
                    }
                }
            }
        }
        return forecasts
    }

    private fun updateForecasts(db: SQLiteDatabase, existedForecasts: List<Forecast>, forecasts: List<Forecast>,
                                locationId: Int) {
        if (existedForecasts.size == forecasts.size) {
            for (i in 0 until existedForecasts.size) {
                val forecastForUpdate = Forecast(existedForecasts[i].id, forecasts[i])
                val forecastValues = createForecastContentValues(forecastForUpdate)
                db.update(
                    FORECAST_TABLE, forecastValues, FORECAST_ID + IS_EQUAL_PARAMETER, arrayOf(forecastForUpdate.id.toString()))
                val weatherDataValues = createWeatherDataContentValues(forecastForUpdate)
                db.update(WEATHER_DATA_TABLE, weatherDataValues, WEATHER_DATA_ID + IS_EQUAL_PARAMETER,
                    arrayOf(forecastForUpdate.id.toString()))
            }
        } else {
            db.delete(FORECAST_TABLE, FORECAST_LOCATION_ID + IS_EQUAL_PARAMETER, arrayOf(locationId.toString()))
            addForecasts(db, forecasts, locationId)
        }


    }

    private fun addForecasts(db: SQLiteDatabase, forecasts: List<Forecast>, locationId: Int) {
        for (forecast in forecasts) {
            val commonId = getId()
            val weatherDataValues = createWeatherDataContentValues(forecast)
            weatherDataValues.put(WEATHER_DATA_ID, commonId)
            weatherDataValues.put(WEATHER_DATA_LOCATION_ID, locationId)
            db.insert(WEATHER_DATA_TABLE, null, weatherDataValues)
            val forecastValues = createForecastContentValues(forecast)
            forecastValues.put(FORECAST_ID, commonId)
            forecastValues.put(FORECAST_LOCATION_ID, locationId)
            db.insert(FORECAST_TABLE, null, forecastValues)
        }
    }

    private fun createForecastContentValues(forecast: Forecast): ContentValues {
        with(ContentValues()) {
            put(FORECAST_SUNRISE_TIME, forecast.sunriseTime.time)
            put(FORECAST_SUNSET_TIME, forecast.sunsetTime.time)
            put(FORECAST_MOON_PHASE, forecast.moonPhase)
            put(FORECAST_PRECIPITATION_INTENSITY_MAX, forecast.precipitationIntensityMax)
            put(FORECAST_PRECIPITATION_INTENSITY_MAX_TIME, forecast.precipitationIntensityMaxTime.time)
            put(FORECAST_PRECIPITATION_ACCUMULATION, forecast.precipitationAccumulation)
            put(FORECAST_PRECIPITATION_TYPE, forecast.precipitationType)
            put(FORECAST_TEMP_HIGH, forecast.temperatureHigh)
            put(FORECAST_TEMP_HIGH_TIME, forecast.temperatureHighTime.time)
            put(FORECAST_TEMP_LOW, forecast.temperatureLow)
            put(FORECAST_TEMP_LOW_TIME, forecast.temperatureLowTime.time)
            put(FORECAST_APPARENT_TEMP_HIGH, forecast.apparentTemperatureHigh)
            put(FORECAST_APPARENT_TEMP_HIGH_TIME, forecast.apparentTemperatureHighTime.time)
            put(FORECAST_APPARENT_TEMP_LOW, forecast.apparentTemperatureLow)
            put(FORECAST_APPARENT_TEMP_LOW_TIME, forecast.apparentTemperatureLowTime.time)
            return this
        }
    }

    //common methods

    private fun getId() = UUID.randomUUID().mostSignificantBits

    class Factory : SQLiteDatabase.CursorFactory {
        override fun newCursor(db: SQLiteDatabase?, masterQuery: SQLiteCursorDriver?, editTable: String?,
                               query: SQLiteQuery?): Cursor = SQLiteCursor(masterQuery, editTable, query)
    }
}