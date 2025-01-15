package com.example.todo.ui.roaster

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.todo.repo.ToDoModel
import com.example.todo.databinding.RowTodoBinding

class RoasterAdapter(private val inflater: LayoutInflater,
                     private val onRowClick: (ToDoModel) -> Unit,
                     private val onCheckBoxToggle: (ToDoModel) -> Unit):  ListAdapter<ToDoModel, RoasterViewHolder>(
    DiffCallBack
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoasterViewHolder =
        RoasterViewHolder(
            RowTodoBinding.inflate(inflater, parent, false),
            onCheckBoxToggle,
            onRowClick
        )

    override fun onBindViewHolder(holder: RoasterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

private object DiffCallBack : DiffUtil.ItemCallback<ToDoModel>() {
    override fun areItemsTheSame(oldItem: ToDoModel, newItem: ToDoModel): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ToDoModel, newItem: ToDoModel): Boolean = oldItem.isCompleted == newItem.isCompleted && oldItem.description == newItem.description
}