package by.rudkouski.widget.database

import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import by.rudkouski.widget.R
import by.rudkouski.widget.app.App.Companion.appContext
import by.rudkouski.widget.database.dao.ForecastDao
import by.rudkouski.widget.database.dao.LocationDao
import by.rudkouski.widget.database.dao.WeatherDao
import by.rudkouski.widget.database.dao.WidgetDao
import by.rudkouski.widget.entity.Forecast
import by.rudkouski.widget.entity.Location
import by.rudkouski.widget.entity.Location.Companion.CURRENT_LOCATION
import by.rudkouski.widget.entity.Weather
import by.rudkouski.widget.entity.Widget
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.threeten.bp.ZoneId.of
import org.threeten.bp.ZoneId.systemDefault

@Database(entities = [Location::class, Widget::class, Weather::class, Forecast::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        private val callback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                initDefaultData()
            }
        }

        private fun initDefaultData() {
            GlobalScope.launch {
                val defaultLocations: Array<String> = appContext.resources.getStringArray(R.array.default_locations)
                for (i in 0 until defaultLocations.size) {
                    val locationData = defaultLocations[i].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val code = if (locationData[0].isEmpty()) CURRENT_LOCATION else locationData[0]
                    val latitude = locationData[1].toDouble()
                    val longitude = locationData[2].toDouble()
                    val zoneId = if (locationData.size == 4) of(locationData[3]) else systemDefault()
                    val location = Location(i + 1, code, latitude, longitude, zoneId)
                    INSTANCE.locationDao().insert(location)
                }
            }
        }

        val INSTANCE = databaseBuilder(appContext, AppDatabase::class.java, "clock_weather_database").addCallback(callback).build()
    }

    abstract fun locationDao(): LocationDao

    abstract fun widgetDao(): WidgetDao

    abstract fun weatherDao(): WeatherDao

    abstract fun forecastDao(): ForecastDao
}