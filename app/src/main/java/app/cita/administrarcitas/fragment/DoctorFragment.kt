package com.example.appcitas.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import app.cita.administrarcitas.CalendarAdapter
import app.cita.administrarcitas.R
import app.cita.administrarcitas.databinding.FragmentDoctorBinding
import com.example.appcitas.model.Appointment
import com.example.appcitas.model.CalendarDay
import com.google.firebase.database.DatabaseReference
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter


class DoctorFragment : Fragment() {
    private lateinit var calendarAdapter: CalendarAdapter
    private var currentMonth = YearMonth.now()
    private var days: List<CalendarDay> = emptyList()
    private val appointments = mutableListOf<Appointment>()
    private lateinit var reference: DatabaseReference
    private val minDate = LocalDate.now()
    private lateinit var binding: FragmentDoctorBinding
    private lateinit var calendarGrid: GridView
    private lateinit var monthYearText: TextView
    private lateinit var appointmentsRecyclerView: RecyclerView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDoctorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupCalendar()
        updateCalendarForMonth(currentMonth)
    }

    private fun setupViews() {
        // Asignar las vistas del binding a las propiedades de la clase
        calendarGrid = binding.calendarGrid
        monthYearText = binding.monthYearText
        appointmentsRecyclerView = binding.appointmentsRecyclerView

        // Configurar el botón "Mes Anterior"
        binding.previousMonth.apply {
            setOnClickListener {
                val previousMonth = currentMonth.minusMonths(1)
                if (previousMonth.year >= minDate.year && previousMonth.monthValue >= minDate.monthValue) {
                    currentMonth = previousMonth
                    updateCalendarForMonth(currentMonth)
                }
            }
            isEnabled = false // Inicialmente deshabilitado
        }

        // Configurar el botón "Mes Siguiente"
        binding.nextMonth.setOnClickListener {
            currentMonth = currentMonth.plusMonths(1)
            updateCalendarForMonth(currentMonth)
        }
    }


    private fun setupCalendar() {
        calendarAdapter = CalendarAdapter(requireContext()) { selectedDay ->
            if (selectedDay.isSelectable) {
                val bundle =Bundle().apply {
                    putString("fecha",selectedDay.date.toString())
                }
                findNavController().navigate(R.id.action_doctorFragment_to_verCitaAdministradorFragment,bundle)
            }
        }
        binding.calendarGrid.adapter = calendarAdapter
    }

    private fun updateCalendarForMonth(yearMonth: YearMonth) {
        binding.monthYearText.text = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

        // Actualizar estado del botón "mes anterior"
        binding.previousMonth.isEnabled =
            yearMonth.year > minDate.year || (yearMonth.year == minDate.year && yearMonth.monthValue > minDate.monthValue)

        val newDays = mutableListOf<CalendarDay>()
        val firstDayOfMonth = yearMonth.atDay(1)
        val lastDayOfMonth = yearMonth.atEndOfMonth()

        // Días del mes anterior
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value
        if (firstDayOfWeek > 1) {
            for (i in firstDayOfWeek - 1 downTo 1) {
                val date = firstDayOfMonth.minusDays(i.toLong())
                newDays.add(
                    CalendarDay(
                        date = date,
                        isCurrentMonth = false,
                        isSelectable = date >= minDate // Solo seleccionable si es después de hoy
                    )
                )
            }
        }

        // Días del mes actual
        for (dayOfMonth in 1..lastDayOfMonth.dayOfMonth) {
            val date = yearMonth.atDay(dayOfMonth)
            newDays.add(
                CalendarDay(
                    date = date,
                    isCurrentMonth = true,
                    isSelectable = date >= minDate // Solo seleccionable si es después de hoy
                )
            )
        }

        // Días del mes siguiente
        val remainingDays = 42 - newDays.size
        if (remainingDays > 0) {
            for (i in 1..remainingDays) {
                val date = lastDayOfMonth.plusDays(i.toLong())
                newDays.add(
                    CalendarDay(
                        date = date,
                        isCurrentMonth = false,
                        isSelectable = true // Siempre seleccionable ya que es futuro
                    )
                )
            }
        }

        days = newDays
        calendarAdapter.updateDays(days)
    }
}
