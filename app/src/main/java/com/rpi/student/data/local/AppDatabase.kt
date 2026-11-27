package com.rpi.student.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class, SubjectEntity::class, RoutineSlotEntity::class,
        BusRouteEntity::class, GroupEntity::class, MessageEntity::class,
        MessEntryEntity::class, PomoSessionEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun subjectDao(): SubjectDao
    abstract fun routineDao(): RoutineDao
    abstract fun busDao(): BusDao
    abstract fun chatDao(): ChatDao
    abstract fun messDao(): MessDao
    abstract fun pomoDao(): PomoDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "rpi_student.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
