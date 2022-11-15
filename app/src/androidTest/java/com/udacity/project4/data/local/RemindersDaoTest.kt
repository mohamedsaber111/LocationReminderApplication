package com.udacity.project4.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test to test DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database : RemindersDatabase

    //executes tasks synchronously use Architecture Components
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        //create database before test
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()


    @Test
    //insert reminder and get it from database by id
    fun insertReminder_GetReminderById() = runBlockingTest {
        //GIVEN insert a reminder item
        val reminder = ReminderDTO(
            title = "title",
            description = "description",
            location = "location",
            latitude = 75.6488751,
            longitude = 149.8151731
        )
        //save reminder
        database.reminderDao().saveReminder(reminder)

        //WHEN get reminder by id from database
        val loaded = database.reminderDao().getReminderById(reminder.id)

        //THEN loaded data with their expected values
        //make sure that task comeback
        MatcherAssert.assertThat<ReminderDTO>(loaded as ReminderDTO, CoreMatchers.notNullValue())
        // then check id , title , description, latitude, longitude, location are match to reminder that insert in database
        MatcherAssert.assertThat(loaded.id , CoreMatchers.`is`(reminder.id))
        MatcherAssert.assertThat(loaded.title , CoreMatchers.`is`(reminder.title))
        MatcherAssert.assertThat(loaded.description , CoreMatchers.`is`(reminder.description))
        MatcherAssert.assertThat(loaded.latitude , CoreMatchers.`is`(reminder.latitude))
        MatcherAssert.assertThat(loaded.longitude , CoreMatchers.`is`(reminder.longitude))
        MatcherAssert.assertThat(loaded.location , CoreMatchers.`is`(reminder.location))

    }

}