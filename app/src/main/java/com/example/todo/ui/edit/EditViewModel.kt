package com.example.todo.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.repo.ToDoModel
import com.example.todo.repo.ToDoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EditModelViewState(
    val item: ToDoModel? = null
)

class EditViewModel(private val repository: ToDoRepository, private val modelId: String?): ViewModel() {

    val states = repository.find(modelId)
        .map { EditModelViewState(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, EditModelViewState())

    fun save(model: ToDoModel) {
        viewModelScope.launch {
            repository.save(model)
        }
    }

    fun delete(model: ToDoModel) {
        viewModelScope.launch {
            repository.delete(model)
        }
    }
}