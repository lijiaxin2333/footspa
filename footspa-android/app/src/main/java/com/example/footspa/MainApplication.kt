package com.example.footspa

import android.app.Application

class MainApplication : Application() {

    companion object {
        lateinit var instance: Application
    }

    init {
        instance = this
    }

}