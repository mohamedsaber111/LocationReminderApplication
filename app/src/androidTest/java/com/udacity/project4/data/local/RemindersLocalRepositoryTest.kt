package com.udacity.project4.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test so that test repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var database: RemindersDatabase

    private lateinit var repository: RemindersLocalRepository

    // Executes tasks synchronously by Architecture Components
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private fun getReminder(): ReminderDTO {
        return ReminderDTO(
            title = "title",
            description = "description",
            location = "location",
            latitude = 70.5689551,
            longitude = 166.9621731)
    }

    @Before
    fun setup() {
        //use in memory database so that hide the information stored here if process is killed
        //create database before test
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        //initialize repository
        repository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun cleanUp() {
        //close database after test
        database.close()
    }

    @Test

    fun saveReminder_retrievesReminder() = runBlocking {
        // GIVEN add reminder
        val reminder = getReminder()
        //save it in database
        repository.saveReminder(reminder)

        //WHEN get reminder by id
        val result = repository.getReminder(reminder.id)

        // THEN Same reminder is returned.
        MatcherAssert.assertThat(result is Result.Success, `is`(true))
        result as Result.Success
        //verify data equal data that saved in database
        MatcherAssert.assertThat(result.data.title, `is`(reminder.title))
        MatcherAssert.assertThat(result.data.description, `is`(reminder.description))
        MatcherAssert.assertThat(result.data.latitude, `is`(reminder.latitude))
        MatcherAssert.assertThat(result.data.longitude,`is`(reminder.longitude))
        MatcherAssert.assertThat(result.data.location, `is`(reminder.location))

    }

    @Test
    fun deleteAllReminders_getReminderById() = runBlocking {
        // GIVEN add reminder
        val reminder = getReminder()
        // save it in repository
        repository.saveReminder(reminder)
        // then delete all reminders in  repository
        repository.deleteAllReminders()

        //WHEN get reminder by id
        val result = repository.getReminder(reminder.id)

        //THEN result equal to error because there is no reminder
        MatcherAssert.assertThat(result is Result.Error, `is`(true))
        result as Result.Error
        MatcherAssert.assertThat(result.message,`is`("Reminder not found!"))

    }
}