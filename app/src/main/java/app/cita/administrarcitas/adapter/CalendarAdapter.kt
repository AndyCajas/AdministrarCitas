package app.cita.administrarcitas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat

import com.example.appcitas.model.CalendarDay

class CalendarAdapter(
    private val context: Context,
    private val onDayClick: (CalendarDay) -> Unit
) : BaseAdapter() {
    private var days: List<CalendarDay> = emptyList()

    fun updateDays(newDays: List<CalendarDay>) {
        days = newDays
        notifyDataSetChanged()
    }

    override fun getCount(): Int = days.size

    override fun getItem(position: Int): CalendarDay = days[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.calendar_day_item, parent, false)

        val day = getItem(position)
        val dayText = view.findViewById<TextView>(R.id.dayText)

        dayText.text = day.date.dayOfMonth.toString()

        // Aplicar estilo según si es seleccionable
        if (!day.isSelectable) {
            dayText.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            dayText.alpha = 0.5f
            view.isClickable = false
        } else {
            dayText.setTextColor(
                if (day.isCurrentMonth)
                    ContextCompat.getColor(context, android.R.color.black)
                else
                    ContextCompat.getColor(context, android.R.color.darker_gray)
            )
            dayText.alpha = 1.0f
            view.setOnClickListener { onDayClick(day) }
        }

        return view
    }
}