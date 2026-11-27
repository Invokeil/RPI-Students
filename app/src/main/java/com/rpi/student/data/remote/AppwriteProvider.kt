package com.rpi.student.data.remote

import android.content.Context
import com.rpi.student.BuildConfig
import io.appwrite.Client

/**
 * Appwrite = backend (DB + Auth + Realtime) — https://sgp.cloud.appwrite.io/v1
 * Project id BuildConfig-এ আছে (non-secret); API key app-এ নেই ও কখনো রাখা হবে না
 * (API key শুধু server/admin panel side-এ চলে)।
 */
object Appwrite {

    private var _client: Client? = null

    fun init(context: Context) {
        if (_client != null) return
        _client = Client(context)
            .setEndpoint(BuildConfig.APPWRITE_ENDPOINT)
            .setProject(BuildConfig.APPWRITE_PROJECT_ID)
    }

    val client: Client
        get() = _client ?: throw IllegalStateException("Appwrite.init() আগে কল করতে হবে (RpiApp.onCreate)")

    /** App চালু হওয়ার পর সবসময় true (RpiApp.onCreate-এ init হয়)। */
    fun isReady(): Boolean = _client != null

    inline fun <reified T> service(): T {
        val c = client
        return when (T::class) {
            io.appwrite.services.Account::class -> io.appwrite.services.Account(c) as T
            io.appwrite.services.Databases::class -> io.appwrite.services.Databases(c) as T
            else -> throw IllegalArgumentException("Unknown service: ${T::class.simpleName}")
        }
    }
}
