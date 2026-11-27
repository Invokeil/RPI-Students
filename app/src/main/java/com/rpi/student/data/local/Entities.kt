package com.rpi.student.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Blueprint §10 schema-র সাথে aligned (subset, v1 slice)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,                 // supabase auth uid
    val email: String,
    val fullName: String,
    val roll: String = "",
    val registration: String = "",
    val session: String = "",                   // যেমন 2022-23
    val technology: String = "",                // Civil/CST/Electrical/Electromedical/Electronics/Mechanical/Power
    val semester: Int = 0,                      // 1..8
    val shift: String = "",                     // Morning/Second
    val section: String = "",                   // A/B
    val busRoute: String = "",                  // খালি হলে bus group নেই
    val messMember: Boolean = false,
    val badge: String = "none",                 // none | verified | gold (server-only set)
    val status: String = "pending",             // pending | verified | rejected
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,                 // code-tech-reg-sem
    val code: String,
    val name: String,
    val technology: String,
    val regulation: Int,                        // 2016 | 2022
    val semester: Int,
    val credit: Double,
    val hasTheory: Boolean,
    val theoryFull: Int?,                       // null = not available (btebresultszone)
    val hasPractical: Boolean,
    val practicalFull: Int?,
    val totalFull: Int,
)

@Entity(tableName = "routine_slots")
data class RoutineSlotEntity(
    @PrimaryKey val id: String,
    val technology: String,
    val semester: Int,
    val section: String,
    val shift: String,
    val day: Int,                               // 0=Sat..6=Fri (BD week)
    val startTime: String,                      // "09:00"
    val endTime: String,
    val subjectCode: String,
    val subjectName: String,
    val room: String = "",
    val teacher: String = "",
)

@Entity(tableName = "bus_routes")
data class BusRouteEntity(
    @PrimaryKey val id: String,
    val routeName: String,
    val shift: String,
    val stopName: String,
    val stopOrder: Int,
    val time: String,
    val notice: String = "",                    // admin notice
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val kind: String,                           // ALL | FAMILY | OWN | BUS | CUSTOM
    val nonLeaveable: Boolean,
    val memberCount: Int = 0,
    val lastMessage: String = "",
    val lastMessageAt: Long = 0,
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val senderId: String,
    val senderName: String,
    val body: String,                           // v1 = text-only (Media Share flag থাকলে pointer পরে)
    val sentAt: Long,
    val isMine: Boolean = false,
)

@Entity(tableName = "mess_entries")
data class MessEntryEntity(
    @PrimaryKey val id: String,
    val memberName: String,
    val date: String,                           // yyyy-MM-dd
    val meals: Int,
    val isBazar: Boolean = false,
    val amount: Double = 0.0,                   // bazar amount
    val note: String = "",
)

@Entity(tableName = "pomo_sessions")
data class PomoSessionEntity(
    @PrimaryKey val id: String,
    val subject: String,
    val preset: String,                         // Theory 25/5 | Math 40/8 | Lab 50/10 | Exam 45/10
    val startedAt: Long,
    val minutes: Int,
    val completed: Boolean,
)
