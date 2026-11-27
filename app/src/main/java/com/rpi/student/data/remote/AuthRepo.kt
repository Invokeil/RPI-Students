package com.rpi.student.data.remote

import com.rpi.student.data.local.UserEntity
import io.appwrite.ID
import io.appwrite.Permission
import io.appwrite.Query
import io.appwrite.Role
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Account
import io.appwrite.services.Databases

/**
 * Auth + profile — Appwrite Account (email/password + email OTP token) ও rpi_main.profiles।
 * সব call suspend — UI থেকে rememberCoroutineScope/LaunchedEffect-এ চালাও।
 */
object AuthRepo {

    private const val DB = "rpi_main"
    private const val PROFILES = "profiles"

    data class Profile(
        val userId: String,
        val name: String,
        val email: String,
        val roll: String,
        val registration: String,
        val session: String,
        val technology: String,
        val semester: Int,
        val shift: String,
        val section: String,
        val busRoute: String,
        val messMember: Boolean,
        val badge: String,
        val status: String,
        val docId: String?,
    )

    // ---------- low-level helpers ----------

    private fun account(): Account = Appwrite.service()
    private fun db(): Databases = Appwrite.service()

    private fun mapToProfile(id: String, data: Map<String, Any?>): Profile = Profile(
        userId = data["userId"] as? String ?: "",
        name = data["name"] as? String ?: "",
        email = data["email"] as? String ?: "",
        roll = data["roll"] as? String ?: "",
        registration = data["registration"] as? String ?: "",
        session = data["session"] as? String ?: "",
        technology = data["technology"] as? String ?: "",
        semester = (data["semester"] as? Number)?.toInt() ?: 0,
        shift = data["shift"] as? String ?: "",
        section = data["section"] as? String ?: "",
        busRoute = data["busRoute"] as? String ?: "",
        messMember = data["messMember"] as? Boolean ?: false,
        badge = data["badge"] as? String ?: "none",
        status = data["status"] as? String ?: "pending",
        docId = id,
    )

    private fun UserEntity.toMap(userId: String): Map<String, Any?> = mapOf(
        "userId" to userId,
        "name" to fullName,
        "email" to email,
        "roll" to roll,
        "registration" to registration,
        "session" to session,
        "technology" to technology,
        "semester" to semester,
        "shift" to shift,
        "section" to section,
        "busRoute" to busRoute,
        "messMember" to messMember,
        "badge" to badge,
        "status" to status,
    )

    fun friendlyError(e: Throwable): String = when (e) {
        is AppwriteException -> when (e.type) {
            "user_already_exists", "user_email_already_exists" -> "এই ইমেইলে অ্যাকাউন্ট আছে — লগইন করো"
            "user_invalid_credentials" -> "ইমেইল/পাসওয়ার্ড ভুল"
            "user_session_not_found" -> "সেশন শেষ — আবার লগইন করো"
            "general_rate_limit_exceeded" -> "অনেকবার চেষ্টা — একটু পরে আবার"
            else -> e.message ?: "Appwrite error (${e.code})"
        }
        else -> e.message ?: "নেটওয়ার্ক সমস্যা"
    }

    // ---------- auth ----------

    /** নতুন অ্যাকাউন্ট তৈরি → userId দেয় (session হয় না — OTP ধাপে হবে)। */
    suspend fun signUp(email: String, password: String, name: String?): Result<String> = runCatching {
        val user = account().create(userId = ID.unique(), email = email, password = password, name = name)
        user.id
    }

    /** ইমেইল+পাসওয়ার্ড সেশন (OTP ছাড়া fallback / পুরনো ইউজার)। */
    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        account().createEmailPasswordSession(email = email, password = password)
        Unit
    }

    /** OTP ইমেইল পাঠায় (Appwrite SMTP — Console-এ Resend SMTP সেট থাকতে হবে)। */
    suspend fun sendOtp(userId: String, email: String): Result<Unit> = runCatching {
        account().createEmailToken(userId = userId, email = email)
        Unit
    }

    /** ৬-ডিজিট OTP দিয়ে সেশন তৈরি। */
    suspend fun verifyOtp(userId: String, code: String): Result<Unit> = runCatching {
        account().createSession(userId = userId, secret = code)
        Unit
    }

    /** বর্তমান সেশনের user — না থাকলে null। */
    suspend fun currentUserOrNull(): Pair<String, String>? = runCatching {
        val u = account().get()
        u.id to u.email
    }.getOrNull()

    suspend fun logout(): Result<Unit> = runCatching {
        account().deleteSession(sessionId = "current")
        Unit
    }

    // ---------- profile documents ----------

    suspend fun findMyProfile(userId: String): Profile? = runCatching {
        val res = db().listDocuments(
            databaseId = DB,
            collectionId = PROFILES,
            queries = listOf(Query.equal("userId", userId), Query.limit(1)),
        )
        res.documents.firstOrNull()?.let { mapToProfile(it.id, it.data) }
    }.getOrNull()

    suspend fun saveProfile(userId: String, entity: UserEntity): Result<Profile> = runCatching {
        val existing = findMyProfile(userId)
        val perms = listOf(
            Permission.read(Role.any()),
            Permission.update(Role.user(userId)),
            Permission.delete(Role.user(userId)),
        )
        val data = entity.toMap(userId)
        val doc = if (existing?.docId != null) {
            db().updateDocument(DB, PROFILES, existing.docId, data, perms)
        } else {
            db().createDocument(DB, PROFILES, ID.unique(), data, perms)
        }
        mapToProfile(doc.id, doc.data)
    }

    /** পুরনো ইউজার লগইন → profile doc তৈরি না থাকলে বানায়। */
    suspend fun ensureProfile(userId: String, email: String, entity: UserEntity?): Result<Profile> = runCatching {
        val existing = findMyProfile(userId)
        if (existing != null) return existing
        val e = entity ?: UserEntity(
            id = userId,
            email = email,
            fullName = email.substringBefore("@"),
        )
        val perms = listOf(
            Permission.read(Role.any()),
            Permission.update(Role.user(userId)),
            Permission.delete(Role.user(userId)),
        )
        val doc = db().createDocument(DB, PROFILES, ID.unique(), e.toMap(userId), perms)
        mapToProfile(doc.id, doc.data)
    }
}
