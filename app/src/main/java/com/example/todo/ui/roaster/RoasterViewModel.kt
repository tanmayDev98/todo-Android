package com.example.todo.ui.roaster

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.BuildConfig
import com.example.todo.repo.FILTERED
import com.example.todo.repo.PrefsRepository
import com.example.todo.repo.ToDoModel
import com.example.todo.repo.ToDoRepository
import com.example.todo.report.RoasterListReport
import com.example.todo.ui.ErrorScenario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.Exception

data class RoasterViewState(val item: List<ToDoModel> = listOf(),
                            val isLoading: Boolean = false,
                            val filterMode: FILTERED = FILTERED.All)

sealed class Nav {
    data class ViewReport(val doc: Uri): Nav()
    data class ShareReport(val doc: Uri) : Nav()
}

private const val AUTHORITY = BuildConfig.APPLICATION_ID + ".provider"

class RoasterViewModel(private val repository: ToDoRepository,
                       private val report: RoasterListReport,
                       private val context: Application,
                       private val appScope: CoroutineScope,
                       private val prefs: PrefsRepository
): ViewModel() {
    private var job: Job? = null
    private val _states = MutableStateFlow(RoasterViewState())
    val states = _states.asStateFlow()
    private val _navEvents = MutableSharedFlow<Nav>()
    val navEvents = _navEvents.asSharedFlow()
    private val _errorEvents = MutableSharedFlow<ErrorScenario>()
    val errorEvents = _errorEvents.asSharedFlow()

    init {
        load(FILTERED.All)
    }

    fun save(model: ToDoModel) {
        viewModelScope.launch {
            repository.save(model)
        }
    }

    fun load(filterMode: FILTERED) {
        job?.cancel()

        //it will simplify our RosterListFragment if there always is one StateFlow
        //supplying the RosterViewState objects, rather than having to know to subscribe to
        //different StateFlow objects at different times for different reasons
        job = viewModelScope.launch {
            repository.items(filterMode).collect {
                _states.emit(RoasterViewState(it, true, filterMode))
            }
        }
    }

    fun saveReport(doc: Uri?) {
        viewModelScope.launch {
            report.generate(_states.value.item, doc)
        }
    }

    fun shareReport() {
        viewModelScope.launch {
            saveForSharing()
        }
    }

    fun importItems() {
        viewModelScope.launch {
            try {
                repository.importItems(prefs.loadWebServiceUrl())
            } catch (ex: Exception) {
                Log.e("ToDo", "Exception importing items", ex)
                _errorEvents.emit(ErrorScenario.Import)
            }
        }
    }

    private suspend fun saveForSharing() {
        withContext(Dispatchers.IO + appScope.coroutineContext) {
            val shared = File(context.cacheDir, "shared").also { it.mkdir() }
            val reportFile = File(shared, "report.html")
            val doc = FileProvider.getUriForFile(context, AUTHORITY, reportFile)

            _states.value.let { report.generate(it.item, doc) }
            _navEvents.emit(Nav.ShareReport(doc))
        }
    }

    //That is because we always loaded all of the to-do items. still could have kept this
    //code, but it would not give our UI the ability to change the filter mode, which is
    //what we are trying to achieve.
    //However, if  later call repo.items(FilterMode.COMPLETED) or
    //repo.items(FilterMode.OUTSTANDING), we get a different Flow than the one we had
    //originally. That highlights a limitation of stateIn(): it can only give us one
    //StateFlow. In our case, we may have several, as the user toggles between various
    //filter options.
//    val states = repository.items()
//        .map { RoasterViewState(it, true) }
//        .stateIn(viewModelScope, SharingStarted.Eagerly, RoasterViewState())
}