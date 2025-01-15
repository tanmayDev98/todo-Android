package com.example.todo.repo

import java.time.Instant
import java.util.UUID

data class ToDoModel(
    val id: String = UUID.randomUUID().toString(),
    val notes: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val createdOn: Instant = Instant.now()
)