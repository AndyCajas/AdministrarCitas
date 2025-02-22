package com.example.appcitas.model

import java.time.LocalDate

data class Appointment(
    val id: Long,
    val date: LocalDate,
    val title: String,
    val description: String,
    // Puedes agregar más campos según necesites
    val time: String? = null,
    val location: String? = null
)