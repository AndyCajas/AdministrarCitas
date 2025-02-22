package app.cita.administrarcitas

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.Calendar

class TimeDialogo : DialogFragment(), TimePickerDialog.OnTimeSetListener {
    var listener: ((Int, Int) -> Unit)? = null
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        listener?.invoke(hourOfDay, minute)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val dialog = TimePickerDialog(requireContext(), this, hour, minute, false)
        return dialog
    }
}