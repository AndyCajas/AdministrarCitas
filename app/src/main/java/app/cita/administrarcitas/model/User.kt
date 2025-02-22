package com.example.appcitas.model

data class User (
    var id:String,
    var nombre:String,
    var apellido:String,
    var email:String,
    var password:String,
    var imagen:String,
    var rol:String,
){
    constructor():this("","","","","","","")
}