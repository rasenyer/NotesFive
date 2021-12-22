package com.rasenyer.notesfive.ui.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.rasenyer.notesfive.R
import com.rasenyer.notesfive.databinding.FragmentHomeBinding
import com.rasenyer.notesfive.model.Note
import com.rasenyer.notesfive.utils.SortBy
import com.rasenyer.notesfive.utils.SortBy.*
import com.rasenyer.notesfive.ui.adapter.NoteAdapter
import com.rasenyer.notesfive.vm.NoteViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

private lateinit var noteAdapter: NoteAdapter

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val noteViewModel: NoteViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mFloatingActionButton.setOnClickListener { findNavController().navigate(R.id.action_HomeFragment_to_AddFragment) }

        noteViewModel.getAll.observe(viewLifecycleOwner, { noteList ->

            textViewNoNotes(noteList.isEmpty())
            setNoteList(noteList = noteList)
            setNoteListAll(noteListAll = noteList)

            if (noteViewModel.readFromDataStore.value != null) { sortNoteList(SortBy.valueOf(noteViewModel.readFromDataStore.value!!)) }

        })

        noteViewModel.readFromDataStore.observe(viewLifecycleOwner, { sortBy -> sortNoteList(valueOf(sortBy)) })

        setHasOptionsMenu(true)
        setupRecyclerView()
        hideKeyboard()

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.menu_home, menu)
        val search = menu.findItem(R.id.mSearch)
        val searchView = search?.actionView as SearchView
        searchView.queryHint = resources.getString(R.string.search_notes)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean { return false }

            override fun onQueryTextChange(query: String?): Boolean {

                if (query!!.isNotEmpty()) { filterNotes(query.lowercase(Locale.getDefault())) }
                else { setNoteList(noteAdapter.listOfNotesAll) }

                return true

            }

        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.mDeleteAll -> { deleteAllNotes() }

            R.id.mTheNewest -> {
                sortNoteList(DATE_NEWEST)
                noteViewModel.saveToDataStore(DATE_NEWEST.toString())
            }

            R.id.mTheOldest -> {
                sortNoteList(DATE_OLDEST)
                noteViewModel.saveToDataStore(DATE_OLDEST.toString())
            }

            R.id.mMoreImportant -> {
                sortNoteList(PRIORITY_HIGHEST)
                noteViewModel.saveToDataStore(PRIORITY_HIGHEST.toString())
            }

            R.id.mLessImportant -> {
                sortNoteList(PRIORITY_LOWEST)
                noteViewModel.saveToDataStore(PRIORITY_LOWEST.toString())
            }

        }

        return super.onOptionsItemSelected(item)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun textViewNoNotes(isEmpty: Boolean) {

        if (isEmpty) { binding.mTextViewNoNotes.visibility = View.VISIBLE }
        else { binding.mTextViewNoNotes.visibility = View.GONE }

    }

    private fun setNoteList(noteList: List<Note>) {
        noteAdapter.setNoteList(noteList)
    }

    private fun setNoteListAll(noteListAll: List<Note>) {
        noteAdapter.setNoteListAll(noteListAll)
    }

    private fun sortNoteList(sortBy: SortBy) {

        var notes = noteAdapter.listOfNotes

        notes = when (sortBy) {
            DATE_NEWEST -> notes.sortedByDescending { it.date }
            DATE_OLDEST -> notes.sortedBy { it.date }
            PRIORITY_HIGHEST -> notes.sortedWith(compareBy<Note> { it.isImportant }.thenBy { it.date }).reversed()
            PRIORITY_LOWEST -> notes.sortedWith(compareBy<Note> { it.isImportant }.thenByDescending { it.date })
        }

        setNoteList(notes)
        setNoteListAll(notes)

    }

    private fun setupRecyclerView() {

        noteAdapter = NoteAdapter()

        binding.mRecyclerView.apply {

            adapter = noteAdapter

            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

            swipeToDelete(this)

            this.itemAnimator = jp.wasabeef.recyclerview.animators.ScaleInAnimator().apply {
                addDuration = 250
                removeDuration = 250
                moveDuration = 250
                changeDuration = 250
            }

        }

    }

    private fun swipeToDelete(recyclerView: RecyclerView) {

        val swipeToDeleteCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean { return false }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                    val note = noteAdapter.listOfNotes[viewHolder.adapterPosition]
                    noteViewModel.delete(note)
                    showSnackbarUndoDelete(viewHolder.itemView, note)

                }

            }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

    }

    private fun showSnackbarUndoDelete(itemView: View, note: Note) {

        val snackbar = Snackbar.make(itemView, R.string.note_deleted, Snackbar.LENGTH_LONG)
        snackbar.setAction(R.string.undo) { noteViewModel.insert(note) }.show()

    }

    private fun filterNotes(query: String) {

        val allNotes = noteAdapter.listOfNotesAll
        val filteredNotes = mutableListOf<Note>()

        allNotes.forEach { note ->

            if (note.title.lowercase(Locale.getDefault()).contains(query) || note.description.lowercase(Locale.getDefault()).contains(query)) {
                filteredNotes.add(note)
            }

        }

        setNoteList(filteredNotes)

    }

    private fun hideKeyboard() {

        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        requireActivity().currentFocus?.let {
            inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }

    }

    private fun deleteAllNotes() {

        val builder = AlertDialog.Builder(requireContext())
        builder.setIcon(R.drawable.ic_delete)
        builder.setTitle(R.string.delete_all_notes)
        builder.setMessage(R.string.are_you_sure_you_want_to_delete_all_notes)

        builder.setPositiveButton(R.string.yes) { _: DialogInterface, _: Int ->
            noteViewModel.deleteAll()
            Toast.makeText(requireContext(), R.string.notes_deleted, Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton(R.string.no) { _, _ -> }

        builder.create().show()

    }

}