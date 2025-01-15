package com.example.todo.ui.display

import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.todo.R
import com.example.todo.databinding.FragmentDisplayBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.util.Date

class DisplayFragment: Fragment() {
    private val args: DisplayFragmentArgs by navArgs()
    private var binding: FragmentDisplayBinding? =null
    private val displayViewModel: DisplayViewModel by viewModel{ parametersOf(args.modelId) }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentDisplayBinding.inflate(inflater, container, false).apply {
        binding = this
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
       viewLifecycleOwner.lifecycleScope.launchWhenStarted {
           displayViewModel.states.collect{
               it.item?.let {
                   binding?.apply {
                       completed.visibility =
                           if(it.isCompleted) View.VISIBLE else View.GONE
                       desc.text = it.description
                       createdOn.text = DateUtils.getRelativeDateTimeString(
                           requireContext(),
                           it.createdOn.toEpochMilli(),
                           DateUtils.MINUTE_IN_MILLIS,
                           DateUtils.WEEK_IN_MILLIS,
                           0
                       )
                       notes.text = it.notes
                   }
               }
           }
       }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.actions_display, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.edit -> {
                edit()
                return true
            }
            else ->return super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }

    private fun edit() {
        findNavController().navigate( DisplayFragmentDirections.editModel(args.modelId) )
    }
}