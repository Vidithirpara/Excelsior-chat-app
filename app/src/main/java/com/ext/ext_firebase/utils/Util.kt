package com.ext.ext_firebase.utils

import android.content.Context
import android.text.TextUtils
import android.util.Patterns
import android.widget.EditText
import android.widget.Toast

fun validateName(context: Context, firstName : EditText): Boolean{
    if (TextUtils.isEmpty(firstName.text.toString())){
        Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
        return false
    }
    return true
}

fun validatePhoneNumber(context: Context, phoneNumber : EditText): Boolean{
    if (TextUtils.isEmpty(phoneNumber.text.toString())){
        Toast.makeText(context, "Please enter phone number", Toast.LENGTH_SHORT).show()
        return false
    }
    else if(phoneNumber.text.length < 10){
        Toast.makeText(context, "Please enter valid phone number", Toast.LENGTH_SHORT).show()
        return false
    }
    return true
}

fun validateEmailAddress(context: Context, emailAddress : EditText) : Boolean{
    if (TextUtils.isEmpty(emailAddress.text)){
        Toast.makeText(context, "Email address not empty", Toast.LENGTH_SHORT).show()
        return false
    }
    else if(!Patterns.EMAIL_ADDRESS.matcher(emailAddress.getText().toString()).matches()){
        Toast.makeText(context, "Enter valid email address", Toast.LENGTH_SHORT).show()
        return false
    }
    return true
}

fun validatePassword(context: Context, password : EditText): Boolean{
    if (TextUtils.isEmpty(password.text.toString())){
        Toast.makeText(context, "Please enter password", Toast.LENGTH_SHORT).show()
        return false
    }
    return true
}

