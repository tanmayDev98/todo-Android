package com.example.todo.ui.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.todo.R
import com.example.todo.repo.ToDoModel
import com.example.todo.databinding.FragmentEditBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class EditFragment: Fragment() {
    private val args: EditFragmentArgs by navArgs()
    private var binding: FragmentEditBinding? = null
    private val viewModel: EditViewModel by viewModel { parametersOf(args.modelId) }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = FragmentEditBinding.inflate(inflater, container, false).apply {
        binding = this
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
       viewLifecycleOwner.lifecycleScope.launchWhenStarted {
           viewModel.states.collect {
               if(savedInstanceState == null) {
                   it.item?.let {
                       binding?.apply {
                           isCompleted.isChecked = it.isCompleted
                           editDesc.setText(it.description)
                           editNotes.setText(it.notes)
                       }
                   }
               }
           }
       }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.actions_edit, menu)
        menu.findItem(R.id.delete).isVisible = args.modelId != null
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.save -> {
                save()
                return true
            }
            R.id.delete -> {
                delete()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun save() {
        binding?.apply {
            val model = viewModel.states.value.item
            val edited = model?.copy(
                description = editDesc.text.toString(),
                isCompleted = isCompleted.isChecked,
                notes = editNotes.text.toString()
            ) ?: ToDoModel(
                description = editDesc.text.toString(),
                isCompleted = isCompleted.isChecked,
                notes = editNotes.text.toString()
            )
            edited.let { viewModel.save(it) }
        }

        backToDisplay()
    }

    private fun delete() {
        val model = viewModel.states.value.item
        model?.let { viewModel.delete(it) }
        navToList()
    }

    private fun backToDisplay() {
        findNavController().popBackStack()
    }

    private fun navToList() {
        findNavController().popBackStack(R.id.roasterFragment, false)
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }
}