package com.example.todo.repo

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.UUID

@Entity(tableName = "todos", indices = [Index(value = ["id"])])
data class ToDoEntity(
    val description: String,
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val notes: String = "",
    val createdOn: Instant = Instant.now(),
    val isCompleted: Boolean = false
) {
    constructor(model: ToDoModel): this(
        id = model.id,
        description = model.description,
        isCompleted = model.isCompleted,
        notes = model.notes,
        createdOn = model.createdOn
    )

    @Dao
    interface Store {
        @Query("SELECT * FROM todos ORDER By description")
        fun all(): Flow<List<ToDoEntity>>

        @Query("SELECT * FROM todos WHERE id = :modelId")
        fun find(modelId: String?): Flow<ToDoEntity?>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun save(vararg entities: ToDoEntity)

        @Delete
        suspend fun delete(vararg entities: ToDoEntity)

        @Query("SELECT * FROM todos WHERE isCompleted = :isCompleted ORDER By description")
        fun filtered(isCompleted: Boolean): Flow<List<ToDoEntity>>

        @Insert(onConflict = OnConflictStrategy.IGNORE)
        suspend fun importItems(entities: List<ToDoEntity>)
    }

    fun toModel(): ToDoModel {
        return ToDoModel(
            id =id,
            description = description,
            isCompleted = isCompleted,
            notes = notes,
            createdOn = createdOn
        )
    }
}