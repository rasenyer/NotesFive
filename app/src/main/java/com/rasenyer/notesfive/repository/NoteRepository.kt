package com.rasenyer.notesfive.repository

import androidx.lifecycle.LiveData
import com.rasenyer.notesfive.db.NoteDao
import com.rasenyer.notesfive.model.Note
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

@ActivityRetainedScoped
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) {

    suspend fun insert(note: Note) = noteDao.insert(note)

    suspend fun update(note: Note) = noteDao.update(note)

    suspend fun delete(note: Note) = noteDao.delete(note)

    suspend fun deleteAll() = noteDao.deleteAll()

    val getAll: LiveData<List<Note>> = noteDao.getAll()

}