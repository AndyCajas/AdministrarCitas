package com.example.appcitas.model

import java.time.LocalDate

data class CalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
    val hasAppointments: Boolean = false,
    val isSelectable: Boolean = true
)