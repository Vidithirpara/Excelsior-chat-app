package com.ext.ext_firebase

data class User(
    var userId: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val profileImageUrl: String = ""
)
