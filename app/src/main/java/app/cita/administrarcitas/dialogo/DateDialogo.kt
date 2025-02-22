package app.cita.administrarcitas

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.Calendar

class DateDialogo : DialogFragment(), DatePickerDialog.OnDateSetListener {
    var listener: ((Int, Int, Int) -> Unit)? = null
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        listener?.invoke(year, month, dayOfMonth)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val dialog = DatePickerDialog(requireContext(), this, year, month, day)
        dialog.datePicker.minDate = calendar.timeInMillis
        return dialog
    }
}