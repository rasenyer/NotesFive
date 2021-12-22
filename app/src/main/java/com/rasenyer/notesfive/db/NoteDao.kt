package com.rasenyer.notesfive.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.rasenyer.notesfive.model.Note

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("DELETE FROM note_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM note_table ORDER by date DESC")
    fun getAll(): LiveData<List<Note>>

}