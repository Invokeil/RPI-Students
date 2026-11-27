package com.rpi.student.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun observeMe(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun clear()
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects WHERE technology=:tech AND regulation=:reg AND semester=:sem ORDER BY code")
    fun bySemester(tech: String, reg: Int, sem: Int): Flow<List<SubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(subjects: List<SubjectEntity>)
}

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routine_slots WHERE technology=:tech AND semester=:sem AND section=:section AND shift=:shift AND day=:day ORDER BY startTime")
    fun byDay(tech: String, sem: Int, section: String, shift: String, day: Int): Flow<List<RoutineSlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(slots: List<RoutineSlotEntity>)
}

@Dao
interface BusDao {
    @Query("SELECT * FROM bus_routes WHERE routeName=:route AND shift=:shift ORDER BY stopOrder")
    fun routeStops(route: String, shift: String): Flow<List<BusRouteEntity>>

    @Query("SELECT DISTINCT routeName FROM bus_routes")
    fun routeNames(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(routes: List<BusRouteEntity>)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM groups ORDER BY kind, lastMessageAt DESC")
    fun groups(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM messages WHERE groupId=:groupId ORDER BY sentAt ASC")
    fun messages(groupId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGroups(groups: List<GroupEntity>)
}

@Dao
interface MessDao {
    @Query("SELECT * FROM mess_entries WHERE date=:date")
    fun byDate(date: String): Flow<List<MessEntryEntity>>

    @Query("SELECT * FROM mess_entries WHERE date LIKE :monthPrefix")
    fun byMonth(monthPrefix: String): Flow<List<MessEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: MessEntryEntity)
}

@Dao
interface PomoDao {
    @Query("SELECT * FROM pomo_sessions ORDER BY startedAt DESC LIMIT 50")
    fun recent(): Flow<List<PomoSessionEntity>>

    @Query("SELECT COUNT(*) FROM pomo_sessions WHERE startedAt >= :sinceMillis")
    fun countSince(sinceMillis: Long): Flow<Int>

    @Insert
    suspend fun insert(session: PomoSessionEntity)
}
