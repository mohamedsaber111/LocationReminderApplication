package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.DataReminder
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.data.FakeDataSource
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //executes tasks synchronously use Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    //set main coroutine dispatcher for unit test
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var remindersListViewModel: RemindersListViewModel

    //Use a fake repository to be injected into the viewModel
    private lateinit var remindersRepository: FakeDataSource

    @Before
    fun setupViewModel() {
        stopKoin()
        //initialize remindersRepository and remindersListViewModel before test
        remindersRepository = FakeDataSource()

        remindersListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(), remindersRepository
        )
    }


    @Test
    //make sure that the show loading value is true while load and when finish make it false
    fun loadReminders_loading() {
        //we use coroutine because we set showLoading true and false at same time so we use pauseDispatcher()
        //until loading true then use resumeDispatcher() and make showLoading equal false and hide loading

        //pause dispatcher to make sure to initial values
        mainCoroutineRule.pauseDispatcher()

        //WHEN load reminders
        remindersListViewModel.loadReminders()

        //THEN show progress indicator
        Assert.assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), Is.`is`(true))

        //execute pending coroutines actions
        mainCoroutineRule.resumeDispatcher()

        //THEN hide progress indicator
        Assert.assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), Is.`is`(false))
    }


    @Test
    // test list of item is loading success then then verify by Assert that title equal title in repository
    fun loadReminders_Is_Success() = runBlockingTest {
        // GIVEN items
        DataReminder.items.forEach { reminderDTO ->
            remindersRepository.saveReminder(reminderDTO)
        }

        //WHEN load reminders
        remindersListViewModel.loadReminders()

        //THEN verify Data is same with source
        val loadedItems = remindersListViewModel.remindersList.getOrAwaitValue()
        MatcherAssert.assertThat(loadedItems.size, `is`(DataReminder.items.size))

        for (i in loadedItems.indices) {
            //title = title in repository
            MatcherAssert.assertThat(loadedItems[i].title, `is`(DataReminder.items[i].title))
        }
        //showNoData to false
        MatcherAssert.assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), CoreMatchers.`is`(false))
    }



    @Test
    //snackBar error message when load reminder fails return error
    fun loadRemindersWhenAreUnavailable_callErrorToDisplay(){
        runBlockingTest {
            //GIVEN repository return error
            remindersRepository.setShouldReturnError(true)
            saveReminder()
            //WHEN load reminders
            remindersListViewModel.loadReminders()

            //THEN SnackBar Show error message
            MatcherAssert.assertThat(
                remindersListViewModel.showSnackBar.value, CoreMatchers.`is`("return testing error")

            )
        }
    }


    @Test
    //delete reminders and there is no data
    fun loadReminders_noReminders() = runBlockingTest {
        // GIVEN delete all reminders from repository
        remindersRepository.deleteAllReminders()

        //WHEN load reminders
        remindersListViewModel.loadReminders()

        //THEN load data
        val loadedItems = remindersListViewModel.remindersList.getOrAwaitValue()
        //we found there is no reminders and size equal to zero
        MatcherAssert.assertThat(loadedItems.size, CoreMatchers.`is`(0))

        //showNoData equal true
        MatcherAssert.assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), CoreMatchers.`is`(true))
    }

    private suspend fun saveReminder() {
        remindersRepository.saveReminder(
            ReminderDTO("title",
                "description",
                "location",
                150.00,
                50.00)
        )
    }
}