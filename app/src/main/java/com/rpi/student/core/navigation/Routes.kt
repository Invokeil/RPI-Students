package com.rpi.student.core.navigation

// App-এর সব page/screen — এক জায়গায় (page inventory-র single source)
object Routes {
    // Root
    const val SPLASH = "splash"

    // Auth flow (blueprint §3, §8)
    const val LOGIN = "auth/login"
    const val SIGNUP = "auth/signup"
    const val ACADEMIC_FORM = "auth/academic-form"
    const val OTP = "auth/otp?email={email}"
    fun otp(email: String) = "auth/otp?email=$email"
    const val PENDING = "auth/pending"

    // Main tabs (bottom glass tabs)
    const val HOME = "main/home"
    const val ACADEMIC = "main/academic"
    const val CHAT_LIST = "main/chat"
    const val MESS = "main/mess"
    const val PROFILE = "main/profile"

    // Academic
    const val ROUTINE = "academic/routine"
    const val RESULTS = "academic/results"
    const val GPA = "academic/gpa"
    const val CGPA = "academic/cgpa"
    const val CREDITS = "academic/credits"
    const val EXAM = "academic/exam"

    // Bus (blueprint §6)
    const val BUS = "bus"

    // Chat (blueprint §7 — 4 auto groups)
    const val CHAT_ROOM = "chat/{groupId}"
    fun chatRoom(groupId: String) = "chat/$groupId"

    // Mess (blueprint §16)
    const val MESS_ENTRY = "mess/entry"
    const val BAZAR = "mess/bazar"
    const val DEPOSITS = "mess/deposits"
    const val MONTHLY_BILL = "mess/bill"

    // Productivity (blueprint §15)
    const val POMODORO = "focus/pomodoro"
    const val FITNESS = "focus/fitness"

    // Profile
    const val SETTINGS = "profile/settings"
    const val EDIT_REQUEST = "profile/edit-request"   // personal data change → OTP (blueprint §3)
    const val ABOUT = "profile/about"
    const val DEVELOPER = "profile/developer"         // ডেভেলপার ডিটেইলস + redirect লিংক + OTA

    val mainTabs = listOf(HOME, ACADEMIC, CHAT_LIST, MESS, PROFILE)
}
