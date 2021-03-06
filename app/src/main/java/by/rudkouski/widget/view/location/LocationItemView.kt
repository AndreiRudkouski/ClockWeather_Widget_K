package by.rudkouski.widget.view.location

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import by.rudkouski.widget.R
import by.rudkouski.widget.entity.Location
import by.rudkouski.widget.entity.Location.Companion.CURRENT_LOCATION_ID

class LocationItemView : LinearLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun updateLocationItemView(location: Location, isSelectedLocation: Boolean) {
        val view = findViewById<View>(R.id.location_item)
        if (isSelectedLocation) {
            view.setBackgroundResource(R.color.colorSelected)
        }
        val locationTextView = view.findViewById<TextView>(R.id.location_name)
        locationTextView.text = getLocationItemName(location)
    }

    private fun getLocationItemName(location: Location): String {
        return if (CURRENT_LOCATION_ID == location.id) {
            "${context.getString(R.string.current_location)}: "
        } else {
            ""
        } + location.getName(context)
    }
}