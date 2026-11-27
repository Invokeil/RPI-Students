package com.rpi.student

import android.app.Application
import com.rpi.student.core.theme.isFullGlassSupported
import com.rpi.student.data.local.AppDatabase
import com.rpi.student.data.remote.Appwrite

class RpiApp : Application() {

    lateinit var db: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Local offline-first cache (Room)
        db = AppDatabase.build(this)

        // Backend — Appwrite (sgp.cloud.appwrite.io) — project id BuildConfig এ
        Appwrite.init(this)

        // Notification channel (exam focus banner, chat, mess)
        val channel = android.app.NotificationChannel(
            CHANNEL_MAIN, "General", android.app.NotificationManager.IMPORTANCE_DEFAULT
        )
        getSystemService(android.app.NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_MAIN = "rpi_main"
        lateinit var instance: RpiApp
            private set
        val glassFull get() = isFullGlassSupported
    }
}
