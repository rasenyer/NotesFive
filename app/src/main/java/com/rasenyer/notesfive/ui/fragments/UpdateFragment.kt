package com.rasenyer.notesfive.ui.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.drjacky.imagepicker.ImagePicker
import com.rasenyer.notesfive.R
import com.rasenyer.notesfive.databinding.FragmentUpdateBinding
import com.rasenyer.notesfive.vm.NoteViewModel
import com.rasenyer.notesfive.model.Note
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.internal.managers.FragmentComponentManager
import java.util.*

@AndroidEntryPoint
class UpdateFragment : Fragment() {

    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!

    private val updateFragmentArgs by navArgs<UpdateFragmentArgs>()
    private val noteViewModel: NoteViewModel by viewModels()
    private var importance = false
    private var imageUri: Uri? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUpdateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mMaterialCardViewDelete.setOnClickListener {
            imageUri = null
            binding.mRelativeLayout.visibility = View.GONE
        }

        binding.mSwitchMaterial.setOnCheckedChangeListener { _, isChecked ->

            importance = isChecked

            if (isChecked) {
                binding.mSwitchMaterial.text = resources.getString(R.string.more_important)
                binding.mSwitchMaterial.setBackgroundResource(R.color.purple_500)
            } else {
                binding.mSwitchMaterial.text = resources.getString(R.string.less_important)
                binding.mSwitchMaterial.setBackgroundResource(R.color.teal_200)
            }

        }

        setHasOptionsMenu(true)
        setViews()

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_update, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.mAdd -> { ImagePicker.with(FragmentComponentManager.findActivity(view?.context) as Activity).crop().createIntentFromDialog { launcherImage.launch(it) } }
            R.id.mDelete -> { deleteNote() }
            R.id.mUpdate -> { updateNote() }
        }

        return super.onOptionsItemSelected(item)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setViews() {

        imageUri = Uri.parse(updateFragmentArgs.currentNote.image)

        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeInMillis = updateFragmentArgs.currentNote.date.toLong()
        val dateFormat = DateFormat.format("EEEE, d MMMM yyyy, HH:mm:ss", calendar).toString()

        if (updateFragmentArgs.currentNote.image.isNotEmpty()) {

            binding.mRelativeLayout.visibility = View.VISIBLE
            binding.mImageView.setImageURI(imageUri)

            binding.mMaterialCardViewDelete.setOnClickListener {
                imageUri = null
                binding.mRelativeLayout.visibility = View.GONE
            }

        } else { binding.mRelativeLayout.visibility = View.GONE }

        binding.mTextViewDate.text = dateFormat
        binding.mEditTextTitle.setText(updateFragmentArgs.currentNote.title)
        binding.mEditTextDescription.setText(updateFragmentArgs.currentNote.description)

        when (updateFragmentArgs.currentNote.isImportant) {

            true -> {
                binding.mSwitchMaterial.isChecked = true
                importance = true
                binding.mSwitchMaterial.text = resources.getString(R.string.more_important)
                binding.mSwitchMaterial.setBackgroundResource(R.color.purple_500)
            }

            false -> {
                binding.mSwitchMaterial.isChecked = false
                importance = false
                binding.mSwitchMaterial.text = resources.getString(R.string.less_important)
                binding.mSwitchMaterial.setBackgroundResource(R.color.teal_200)
            }

        }

    }

    private fun deleteNote() {

        val builder = AlertDialog.Builder(requireContext())
        builder.setIcon(R.drawable.ic_delete)
        builder.setTitle(R.string.delete)
        builder.setMessage(R.string.are_you_sure_you_want_to_permanently_delete_this_note)

        builder.setPositiveButton(R.string.yes) { _: DialogInterface, _: Int ->
            noteViewModel.delete(updateFragmentArgs.currentNote)
            findNavController().navigate(R.id.action_UpdateFragment_to_HomeFragment)
            Toast.makeText(requireContext(), R.string.note_deleted, Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton(R.string.no) { _, _ -> }
        builder.create().show()

    }

    private fun updateNote() {

        val title = binding.mEditTextTitle.text.toString()
        val description = binding.mEditTextDescription.text.toString()

        if (noteViewModel.noteIsValid(title, description)) {

            if (imageUri == null) {

                val note = Note(
                    id = updateFragmentArgs.currentNote.id,
                    title = title,
                    description = description,
                    date = System.currentTimeMillis().toString(),
                    image = "",
                    isImportant = importance
                )

                noteViewModel.update(note)
                Toast.makeText(context, R.string.note_updated_successfully, Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_UpdateFragment_to_HomeFragment)

            } else {

                val note = Note(
                    id = updateFragmentArgs.currentNote.id,
                    title = title,
                    description = description,
                    date = System.currentTimeMillis().toString(),
                    image = imageUri.toString(),
                    isImportant = importance
                )

                noteViewModel.update(note)
                Toast.makeText(context, R.string.note_updated_successfully, Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_UpdateFragment_to_HomeFragment)

            }

        } else { Toast.makeText(context, R.string.please_fill_in_the_fields, Toast.LENGTH_SHORT).show() }

    }

    private val launcherImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

        if (it.resultCode == Activity.RESULT_OK) {
            imageUri = it.data?.data!!
            binding.mRelativeLayout.visibility = View.VISIBLE
            binding.mImageView.setImageURI(imageUri)
        }

    }

}