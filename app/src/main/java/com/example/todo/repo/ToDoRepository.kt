package com.example.todo.repo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow

enum class FILTERED{
    All,
    OUTSTANDING,
    COMPLETED
}

class ToDoRepository(
    private val store: ToDoEntity.Store,
    private val appScope: CoroutineScope,
    private val remoteDataSource: ToDoRemoteDataSource
) {
    fun items(filterMode: FILTERED = FILTERED.All): Flow<List<ToDoModel>> =
        filteredEntities(filterMode).map { all -> all.map { it.toModel() } }

    suspend fun save(model: ToDoModel) {
       withContext(appScope.coroutineContext) {
           store.save(ToDoEntity(model))
       }
    }

    fun find(id: String?): Flow<ToDoModel?> = store.find(id).map { it?.toModel() }

    suspend fun delete(model: ToDoModel) {
       withContext(appScope.coroutineContext) {
           store.delete(ToDoEntity(model))
       }
    }

    private fun filteredEntities(filterMode: FILTERED) = when(filterMode)  {
            FILTERED.All -> store.all()
            FILTERED.OUTSTANDING -> store.filtered(isCompleted = false)
            FILTERED.COMPLETED -> store.filtered(isCompleted = true)
    }

    suspend fun importItems(url: String) {
        withContext(appScope.coroutineContext) {
            store.importItems(remoteDataSource.load(url).map { it.toEntity() })
        }
    }
}