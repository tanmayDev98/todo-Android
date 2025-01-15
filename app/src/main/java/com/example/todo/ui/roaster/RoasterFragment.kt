package com.example.todo.ui.roaster

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todo.R
import com.example.todo.databinding.FragmentTodoRoasterBinding
import com.example.todo.repo.FILTERED
import com.example.todo.repo.ToDoModel
import com.example.todo.ui.ErrorDialogFragment
import com.example.todo.ui.ErrorScenario
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val TAG = "ToDo"

class RoasterFragment : Fragment() {
    private val roasterViewModel: RoasterViewModel by viewModel()
    private var binding: FragmentTodoRoasterBinding? = null
    private val menuMap = mutableMapOf<FILTERED, MenuItem>()
    private val createDoc = registerForActivityResult(ActivityResultContracts.CreateDocument()) {
        roasterViewModel.saveReport(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.actions_add, menu)

        menuMap.apply {
            put(FILTERED.All, menu.findItem(R.id.all))
            put(FILTERED.COMPLETED, menu.findItem(R.id.completed))
            put(FILTERED.OUTSTANDING, menu.findItem(R.id.outstanding))
        }

        menuMap[roasterViewModel.states.value.filterMode]?.isChecked = true
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentTodoRoasterBinding.inflate(inflater, container, false)
        .also { binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = RoasterAdapter(layoutInflater, onRowClick = ::display) {roasterViewModel.save(it.copy(isCompleted = !it.isCompleted))}

        binding?.items?.apply {
            setAdapter(adapter)
            layoutManager = LinearLayoutManager(context)

            addItemDecoration(
                DividerItemDecoration(
                    activity,
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
           roasterViewModel.states.collect{
               adapter.submitList(it.item)
               binding?.apply {
                 loading.visibility = View.GONE
                 when {
                     it.item.isEmpty() && it.filterMode == FILTERED.All -> {
                         textView.visibility = View.VISIBLE
                         textView.setText(R.string.txt_empty)
                     }
                     it.item.isEmpty() -> {
                         textView.visibility = View.VISIBLE
                         textView.setText(R.string.msg_empty_filtered)
                     }
                     else -> textView.visibility = View.GONE
                 }
               }
           }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            roasterViewModel.navEvents.collect{nav ->
                when(nav) {
                    is Nav.ViewReport -> viewReport(nav.doc)
                    is Nav.ShareReport -> shareReport(nav.doc)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            roasterViewModel.errorEvents.collect {error ->
                when (error) {
                    ErrorScenario.Import -> handleImportError()
                    ErrorScenario.None -> TODO()
                }
            }
        }

        findNavController()
            .getBackStackEntry(R.id.roasterFragment)
            .savedStateHandle
            .getLiveData<ErrorScenario>(ErrorDialogFragment.KEY_RETRY)
            .observe(viewLifecycleOwner) {
                when(it) {
                    ErrorScenario.Import -> {
                        clearImportError()
                        roasterViewModel.importItems()
                    }

                    ErrorScenario.None -> TODO()
                }
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.add -> {
                add()
                return true
            }
            R.id.all -> {
                item.isChecked = true
                roasterViewModel.load(FILTERED.All)
                return true
            }
            R.id.completed -> {
                item.isChecked = true
                roasterViewModel.load(FILTERED.COMPLETED)
                return true
            }
            R.id.outstanding -> {
                item.isChecked = true
                roasterViewModel.load(FILTERED.OUTSTANDING)
                return true
            }
            R.id.save -> {
                saveReport()
                return true
            }
            R.id.share -> {
                roasterViewModel.shareReport()
                return true
            }
            R.id.importItems -> {
                roasterViewModel.importItems()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun display(model: ToDoModel) {
        findNavController().navigate(RoasterFragmentDirections.displayModel(model.id))
    }

    private fun add() {
        findNavController().navigate(RoasterFragmentDirections.createModel(null))
    }

    private fun saveReport() {
        createDoc.launch("report.html")
    }

    private fun safeStartActivity(intent: Intent) {
        try {
            startActivity(intent)
        } catch (t: Throwable) {
            Log.e(TAG, "Exception starting $intent", t)
            Toast.makeText(requireActivity(), R.string.oops, Toast.LENGTH_LONG).show()
        }
    }

    private fun viewReport(uri: Uri) {
        safeStartActivity(
            Intent(Intent.ACTION_VIEW, uri)
                .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        )
    }

    private fun shareReport(doc: Uri) {
        safeStartActivity(
            Intent(Intent.ACTION_SEND)
                .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .setType("text/html")
                .putExtra(Intent.EXTRA_STREAM, doc)
        )
    }

    private fun handleImportError() {
        findNavController().navigate(
            RoasterFragmentDirections.showError(
                getString(R.string.import_error_title),
                getString(R.string.import_error_message),
                ErrorScenario.Import
            )
        )
    }

    private fun clearImportError() {
        findNavController()
            .getBackStackEntry(R.id.roasterFragment)
            .savedStateHandle[ErrorDialogFragment.KEY_RETRY] = ErrorScenario.None
    }
}