package com.rasenyer.notesfive.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rasenyer.notesfive.repository.DataStoreRepository
import com.rasenyer.notesfive.repository.NoteRepository
import com.rasenyer.notesfive.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel
@Inject constructor(
    private val noteRepository: NoteRepository,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    private val _getAll: LiveData<List<Note>> = noteRepository.getAll
    val getAll: LiveData<List<Note>>
        get() = _getAll

    private val _readFromDataStore: LiveData<String> = dataStoreRepository.readFromDataStore.asLiveData()
    val readFromDataStore: LiveData<String>
        get() = _readFromDataStore

    fun insert(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.insert(note)
        }
    }

    fun update(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.update(note)
        }
    }

    fun delete(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.delete(note)
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.deleteAll()
        }
    }

    fun saveToDataStore(sortBy: String) = viewModelScope.launch(Dispatchers.IO) {
        dataStoreRepository.saveToDataStore(sortBy)
    }

    fun noteIsValid(title: String, description: String): Boolean {
        return title.isNotEmpty() && description.isNotEmpty()
    }

}