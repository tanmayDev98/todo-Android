package com.example.todo.repo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.OkHttpClient
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.empty

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule

@RunWith(AndroidJUnit4::class)
class ToDoRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val db = ToDoDatabase.newTestInstance(context)
    private val remoteDataSource = ToDoRemoteDataSource(OkHttpClient())

    @Test
    fun canAddItems() = runBlockingTest {
        val underTest = ToDoRepository(db.todoStore(), this, remoteDataSource)
        val results = mutableListOf<List<ToDoModel>>()

        val itemsJob = launch {
            underTest.items().collect{results.add(it)}
        }

        //Here we check that result list has one element that we got as an initial result
        //and it is an empty list
        assertThat(results.size, equalTo(1))
        assertThat(results[0], empty())

        val testModel = ToDoModel("test model")
        underTest.save(testModel)

        assertThat(results.size, equalTo(2))
        assertThat(results[1], contains(testModel))
        assertThat(underTest.find(testModel.id).first(), equalTo(testModel))

        itemsJob.cancel()
    }

    @Test
    fun canModify() = runBlockingTest{
        val underTest = ToDoRepository(db.todoStore(), this, remoteDataSource)
        val results = mutableListOf<List<ToDoModel>>()
        val testModel = ToDoModel("This is test model")
        val replacementModel = testModel.copy("This is replacement model")

        val itemsJob = launch {
            underTest.items().collect {results.add(it)}
        }

        assertThat(results[0], empty())

        underTest.save(testModel)
        assertThat(results[1], contains(testModel))

        underTest.save(replacementModel)
        assertThat(results[2], contains(replacementModel))

        itemsJob.cancel()
    }

    @Test
    fun canRemove() = runBlockingTest {
        val underTest = ToDoRepository(db.todoStore(), this, remoteDataSource)
        val results = mutableListOf<List<ToDoModel>>()
        val testModel = ToDoModel("This is the test model")

        val itemsJob = launch {
            underTest.items().collect{results.add(it)}
        }

        assertThat(results[0], empty())

        underTest.save(testModel)
        assertThat(results[1], contains(testModel))

        underTest.delete(testModel)
        assertThat(results[2], empty())

        itemsJob.cancel()
    }
}