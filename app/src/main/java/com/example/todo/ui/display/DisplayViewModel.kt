package com.example.todo.ui.display

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.repo.ToDoModel
import com.example.todo.repo.ToDoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class DisplayViewModelState(val item: ToDoModel? = null)

class DisplayViewModel(private val repository: ToDoRepository, private val modelId: String): ViewModel() {
    val states = repository.find(modelId)
        .map { DisplayViewModelState(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, DisplayViewModelState())
}