package com.example.todo.repo.ui.roaster

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import com.example.todo.R
import com.example.todo.repo.ToDoDatabase
import com.example.todo.repo.ToDoModel
import com.example.todo.repo.ToDoRemoteDataSource
import com.example.todo.repo.ToDoRepository
import com.example.todo.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

class RoasterListFragmentTest {
    private lateinit var repo: ToDoRepository
    private val items = listOf(
        ToDoModel("This is a test"),
        ToDoModel("Test 2"),
        ToDoModel("Test 3")
    )

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = ToDoDatabase.newTestInstance(context)
        val appScope = CoroutineScope(SupervisorJob())

        repo = ToDoRepository(db.todoStore(), appScope, ToDoRemoteDataSource(OkHttpClient()))

        loadKoinModules(module {
            single { repo }
        })

        runBlocking { items.forEach { repo.save(it) } }
    }

    @Test
    fun testListContents() {
        ActivityScenario.launch(MainActivity::class.java)

        onView(withId(R.id.items)).check(matches(hasChildCount(3)))
    }
}