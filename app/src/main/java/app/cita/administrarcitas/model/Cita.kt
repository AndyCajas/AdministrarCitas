package com.example.appcitas.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Cita(
    val id: String,
    val nombre: String,
    val email: String,
    val apellido: String,
    val fecha: String,
    val hora: String,
    val sexo: String,
    val color: String,
    val telefono: String,
    var estado:String,
    var bandera:Boolean=true,
    val idAdministrador:String

) : Parcelable {
    constructor() : this("", "", "", "", "",
        "", "", "","","",true,"")

}