package com.example.todo.ui.roaster

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.todo.repo.ToDoModel
import com.example.todo.databinding.RowTodoBinding

class RoasterViewHolder(private val binding: RowTodoBinding, val onCheckBoxToggle: (ToDoModel) -> Unit, val onRowClick: (ToDoModel) -> Unit): ViewHolder(binding.root) {
    fun bind(model: ToDoModel) {
        binding.apply {
            checkBox.isChecked = model.isCompleted
            checkBox.setOnCheckedChangeListener {_, _ -> onCheckBoxToggle(model) }
            root.setOnClickListener { onRowClick(model)}
            desc.text = model.description
        }
    }
}