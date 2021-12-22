package com.rasenyer.notesfive.ui.fragments

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.drjacky.imagepicker.ImagePicker
import com.rasenyer.notesfive.R
import com.rasenyer.notesfive.databinding.FragmentAddBinding
import com.rasenyer.notesfive.vm.NoteViewModel
import com.rasenyer.notesfive.model.Note
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.internal.managers.FragmentComponentManager

@AndroidEntryPoint
class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    private val noteViewModel: NoteViewModel by viewModels()
    private var importance = false
    private var imageUri: Uri? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

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

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_add, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.mAdd -> { ImagePicker.with(FragmentComponentManager.findActivity(view?.context) as Activity).crop().createIntentFromDialog { launcherImage.launch(it) } }
            R.id.mSave -> { insertNote() }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun insertNote() {

        val title = binding.mEditTextTitle.text.toString()
        val description = binding.mEditTextDescription.text.toString()

        if (noteViewModel.noteIsValid(title, description)) {

            if (imageUri == null) {

                val note = Note(
                    id = 0,
                    title =  title,
                    description =  description,
                    date = System.currentTimeMillis().toString(),
                    image = "",
                    isImportant = importance
                )

                noteViewModel.insert(note)
                Toast.makeText(context, R.string.note_added_successfully, Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_AddFragment_to_HomeFragment)

            } else {

                val note = Note(
                    id = 0,
                    title =  title,
                    description =  description,
                    date = System.currentTimeMillis().toString(),
                    image = imageUri.toString(),
                    isImportant = importance
                )

                noteViewModel.insert(note)
                Toast.makeText(context, R.string.note_added_successfully, Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_AddFragment_to_HomeFragment)

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