package com.example.footixappbachelorarbeit.viewModelLiveData

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.util.Date

@Dao
interface SessionDao {

    @Insert
    suspend fun insert(session: Session)

    @Update
    suspend fun update(session: Session)

    @Query("SELECT * FROM session")
    fun getAllSessions(): List<Session>

    @Query("SELECT COUNT(*) FROM session")
    suspend fun getCount(): Int

    @Query("SELECT * FROM session WHERE current_date = :date")
    suspend fun getDataByDate(date: String): Session

    @Delete
    suspend fun delete(session: Session)

    @Query("DELETE FROM session")
    suspend fun clearSessions()
}