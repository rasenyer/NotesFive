package com.rasenyer.notesfive.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rasenyer.notesfive.model.Note

@Database(entities = [Note::class], version = 1)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

}