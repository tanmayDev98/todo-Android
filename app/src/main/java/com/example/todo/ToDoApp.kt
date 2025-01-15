package com.example.todo

import android.app.Application
import android.text.format.DateUtils
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.todo.repo.ImportWorker
import com.example.todo.repo.PrefsRepository
import com.example.todo.repo.ToDoDatabase
import com.example.todo.repo.ToDoRemoteDataSource
import com.example.todo.repo.ToDoRepository
import com.example.todo.report.RoasterListReport
import com.example.todo.ui.display.DisplayViewModel
import com.example.todo.ui.edit.EditViewModel
import com.example.todo.ui.roaster.RoasterViewModel
import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Helper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.time.Instant
import java.util.concurrent.TimeUnit

private const val TAG_IMPORT_WORK = "doPeriodicImport"

class ToDoApp: Application(), KoinComponent {
    private val koinModule = module {
        single {
            ToDoRepository(
                get<ToDoDatabase>().todoStore(),
                get(named("appScope")),
                get()
            )
        }
        viewModel{ RoasterViewModel(get(), get(), androidApplication(), get(named("appScope")), get()) }
        viewModel{(modelId: String) -> DisplayViewModel(get(), modelId) }
        viewModel{(modelId: String) -> EditViewModel(get(), modelId) }
        single { ToDoDatabase.newInstance(androidContext()) }
        single(named("appScope")) { CoroutineScope(SupervisorJob())}
        single {
            Handlebars().apply {
                registerHelper("dateFormat", Helper<Instant> { value, _ ->
                    DateUtils.getRelativeDateTimeString(
                        androidContext(),
                        value.toEpochMilli(),
                        DateUtils.MINUTE_IN_MILLIS,
                        DateUtils.WEEK_IN_MILLIS, 0
                    )
                })
            }
        }
        single { RoasterListReport(androidContext(), get(), get(named("appScope"))) }
        single { OkHttpClient.Builder().build()}
        single { ToDoRemoteDataSource(get())}
        single { PrefsRepository(androidContext()) }
    }

    override fun onCreate() {
        super.onCreate()
        println("Java version: ${System.getProperty("java.version")}")
        startKoin {
            androidLogger()
            androidContext(this@ToDoApp)
            modules(koinModule)
        }

        scheduleWork()
    }

    private fun scheduleWork() {
        val prefs: PrefsRepository by inject()
        val appScope: CoroutineScope by inject(named("appScope"))
        val workManager = WorkManager.getInstance(this)

        appScope.launch {
            prefs.observeImportChanges().collect {
                if(it) {
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                    val request = PeriodicWorkRequestBuilder<ImportWorker>(15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .addTag(TAG_IMPORT_WORK)
                        .build()
                    workManager.enqueueUniquePeriodicWork(
                        TAG_IMPORT_WORK,
                        ExistingPeriodicWorkPolicy.REPLACE,
                        request
                    )
                } else {
                    workManager.cancelAllWorkByTag(TAG_IMPORT_WORK)
                }
            }
        }
    }
}