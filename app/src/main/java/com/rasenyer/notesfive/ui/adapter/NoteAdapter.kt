package com.rasenyer.notesfive.ui.adapter

import android.content.Context
import android.net.Uri
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.rasenyer.notesfive.R
import com.rasenyer.notesfive.databinding.ItemNoteBinding
import com.rasenyer.notesfive.utils.NotesDiffUtil
import com.rasenyer.notesfive.model.Note
import com.rasenyer.notesfive.ui.fragments.HomeFragmentDirections
import java.util.*

class NoteAdapter : RecyclerView.Adapter<NoteAdapter.MyViewHolder>() {

    var listOfNotes = emptyList<Note>()
    var listOfNotesAll = emptyList<Note>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemNoteBinding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(itemNoteBinding)
    }

    override fun onBindViewHolder(myViewHolder: MyViewHolder, position: Int) {
        val note = listOfNotes[position]
        myViewHolder.bind(note, myViewHolder.itemView.context)
    }

    override fun getItemCount(): Int { return listOfNotes.size }

    class MyViewHolder(private val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note, context: Context) {

            val calendar = Calendar.getInstance(Locale.getDefault())
            calendar.timeInMillis = note.date.toLong()
            val dateFormat = DateFormat.format("EEEE, d MMMM yyyy", calendar).toString()

            if (note.image.isNotEmpty()){
                binding.mImageView.visibility = View.VISIBLE
                binding.mImageView.setImageURI(Uri.parse(note.image))
            } else {
                binding.mImageView.visibility = View.GONE
            }

            binding.mTextViewTitle.text = note.title
            binding.mTextViewDescription.text = note.description
            binding.mTextViewDate.text = dateFormat

            when (note.isImportant) {

                false -> {
                    binding.mTextViewPriority.text = context.getString(R.string.less_important)
                    binding.mTextViewPriority.setBackgroundResource(R.color.teal_200)
                }
                true -> {
                    binding.mTextViewPriority.text = context.getString(R.string.more_important)
                    binding.mTextViewPriority.setBackgroundResource(R.color.purple_500)
                }

            }

            binding.mMaterialCardView.setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToUpdateFragment(note)
                it.findNavController().navigate(action)
            }

        }

    }

    fun setNoteList(newListOfNotes: List<Note>) {

        val notesDiffResult = DiffUtil.calculateDiff(
            NotesDiffUtil(
                oldNotesList = listOfNotes,
                newNotesList = newListOfNotes
            )
        )

        this.listOfNotes = newListOfNotes
        notesDiffResult.dispatchUpdatesTo(this)

    }

    fun setNoteListAll(notesListAll: List<Note>?) {
        listOfNotesAll = notesListAll!!
    }

}