package com.example.todo.ui

import com.example.todo.MainDispatcherRule
import com.example.todo.repo.ToDoModel
import com.example.todo.repo.ToDoRepository
import com.example.todo.ui.edit.EditViewModel
import kotlinx.coroutines.flow.first

import org.mockito.kotlin.mock
import org.mockito.kotlin.doReturn

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.verify

class EditViewModelTest {

    //This applies MainDispatcherRule as a JUnit rule to our JUnit test.
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(paused = true)

    private val testModel = ToDoModel("This is a test")

    private lateinit var underTest: EditViewModel

    private val repo: ToDoRepository = mock {
        on { find(testModel.id) } doReturn flowOf(testModel)
    }

    @Before
    fun setUp() {
        underTest = EditViewModel(repo, testModel.id)
    }

    @Test
    fun `initial state`() {
        mainDispatcherRule.dispatcher.scheduler.runCurrent()

        runBlocking {
            val item = underTest.states.first().item

            assertEquals(testModel, item)
        }
    }

    @Test
    fun `actions pass through to repo`() {
        val replacement = testModel.copy("Whatevs")

        underTest.save(replacement)
        mainDispatcherRule.dispatcher.scheduler.runCurrent()

        runBlocking { verify(repo).save(replacement) }

        underTest.delete(replacement)
        mainDispatcherRule.dispatcher.scheduler.runCurrent()

        runBlocking { verify(repo).delete(replacement) }
    }
}