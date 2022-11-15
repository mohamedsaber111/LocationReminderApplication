package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.data.FakeDataSource
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var saveReminderViewModel: SaveReminderViewModel

    //executes tasks synchronously use Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    //set main coroutine dispatcher for unit test
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp(){
        //initialize saveReminderViewModel before test
        saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), FakeDataSource())
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    //test add reminder add show loading
    fun saveReminder_Loading_ReturnTrue () {
        //Given fresh SaveReminderViewModel so that stop coroutine so we use pauseDispatcher()
        mainCoroutineRule.pauseDispatcher()
        //add reminder
        saveReminderViewModel.validateAndSaveReminder(
            ReminderDataItem(
                "title",
                "description",
                "location",
                520.5,
                -652.4)
        )
        MatcherAssert.assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true)
        )
    }


    @Test
    //if there are no location entered then return false
    fun emptyLocation_returnFalse () {
        //add reminder with no location
        val result=saveReminderViewModel.validateEnteredData(
            ReminderDataItem(
                "title",
                "description",
                "",
                370.5, -250.8)
        )

        MatcherAssert.assertThat(result,`is`(false))
    }

    @Test
    //if there are no title entered then return false
    fun  emptyTitle_returnFalse () {
        ////add reminder with no title
        val result=saveReminderViewModel.validateEnteredData(
            ReminderDataItem(
                "",
                "description",
                "location",750.2,220.88
            )
        )
        MatcherAssert.assertThat(result, `is`(false))
    }
}