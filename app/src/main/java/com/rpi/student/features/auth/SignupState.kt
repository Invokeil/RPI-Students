package com.rpi.student.features.auth

import com.rpi.student.data.local.UserEntity

/**
 * Signup flow-র মধ্যবর্তী অবস্থা — screens এর মধ্যে (memory-only):
 * Signup(email/pass) → Academic form → OTP verify → profile doc save → pending।
 * App process মরলেই মুছে যায় — আবার signup করতে হবে (v1 acceptable)।
 */
object SignupState {
    var userId: String = ""
    var email: String = ""
    var password: String = ""

    var fullName: String = ""
    var roll: String = ""
    var registration: String = ""
    var session: String = ""
    var technology: String = ""
    var semester: Int = 1
    var shift: String = "Morning"
    var section: String = "A"
    var busRoute: String = ""
    var messMember: Boolean = false

    fun toUserEntity(): UserEntity = UserEntity(
        id = userId.ifBlank { email },
        email = email,
        fullName = fullName.ifBlank { email.substringBefore("@") },
        roll = roll,
        registration = registration,
        session = session,
        technology = technology,
        semester = semester,
        shift = shift,
        section = section,
        busRoute = busRoute,
        messMember = messMember,
        badge = "none",
        status = "pending",
    )

    fun clear() {
        userId = ""; email = ""; password = ""
        fullName = ""; roll = ""; registration = ""; session = ""; technology = ""
        semester = 1; shift = "Morning"; section = "A"; busRoute = ""; messMember = false
    }
}
