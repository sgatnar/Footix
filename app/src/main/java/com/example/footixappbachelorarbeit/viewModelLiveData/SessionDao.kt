package com.example.footixappbachelorarbeit.viewModelLiveData

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SessionDao {

    @Insert
    suspend fun insert(session: Session)

    @Query("SELECT * FROM session")
    fun getAllSessions(): LiveData<List<Session>>

    @Query("SELECT * FROM session WHERE current_date LIKE :date LIMIT 1")
    suspend fun findDataByDate(date: String): Session

    @Delete
    suspend fun delete(session: Session)
}