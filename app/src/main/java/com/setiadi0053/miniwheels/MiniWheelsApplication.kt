package com.setiadi0053.miniwheels

import android.app.Application
import com.google.firebase.FirebaseApp

class MiniWheelsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
    }
}
