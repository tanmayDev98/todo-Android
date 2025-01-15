package com.example.todo.repo

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.Exception

class ImportWorker(context: Context, parameters: WorkerParameters):
    CoroutineWorker(context, parameters), KoinComponent {
    private val repo: ToDoRepository by inject()
    private val prefs: PrefsRepository by inject()

    override suspend fun doWork(): Result = try {
        repo.importItems(prefs.loadWebServiceUrl())
        Result.success()
    } catch(ex: Exception) {
        Log.e("ToDo", "Exception importing items in doWork()", ex)
        Result.failure()
    }
}