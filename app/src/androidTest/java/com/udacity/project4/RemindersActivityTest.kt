package com.udacity.project4

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.authentication.AuthenticationViewModel
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
//end to end test
@LargeTest

class RemindersActivityTest : AutoCloseKoinTest() {

    private val TAG = "RemindersActivityTest"
    private lateinit var repository: ReminderDataSource
    private lateinit var applicationContext: Application


    private var authentication: FirebaseAuth


    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    val activityRule = ActivityTestRule(RemindersActivity::class.java)

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    @get:Rule
    val backgroundPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )
    init{
        //sign out
        authentication = FirebaseAuth.getInstance()
        authentication.signOut()
    }


    @Before
    fun init() {
        ////stop the original app koin
        stopKoin()
        applicationContext = ApplicationProvider.getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    applicationContext,
                    get() as ReminderDataSource
                )
            }
            viewModel {
                AuthenticationViewModel()
            }
            single {
                SaveReminderViewModel(
                    applicationContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(applicationContext) }
        }
        //declare a new koin module
        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            loadKoinModules(myModule)
        }


        //get real repository
        repository = get()

        //clear the data
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Before
    fun setUpIdlingResources() {
        //EspressoIdlingResource and dataBindingIdlingResource watching whether or not app is idle when register these two in test
        //when either if those two register is busy espresso wait until idle before moving in next commend
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @Before
    fun login() = runBlocking {
        Log.d(TAG, "login running")
        authentication.signInWithEmailAndPassword("tet@test.com", "123456789")
        delay(4000)
    }

    @After
    fun unregisterIdlingResources() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }


    @Test
    fun addReminder() = runBlocking {

        // Start up Tasks screen that give direct control over activities lifecycle for testing
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        //determine which activity your dataBindingIdlingResource will be monitoring layouts of
        dataBindingIdlingResource.monitorActivity(activityScenario)

        //click on add reminder and write title and description
        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.reminderTitle))
            .perform(ViewActions.typeText("Title"))
        Espresso.onView(withId(R.id.reminderDescription))
            .perform(ViewActions.typeText("Desc"), ViewActions.closeSoftKeyboard())

        //click on select location then the map is opened
        Espresso.onView(withId(R.id.selectLocation)).perform(ViewActions.click())

        delay(1000)

        //check map is displayed then then choose your location
        Espresso.onView(withId(R.id.fragments_map)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.fragments_map)).perform(ViewActions.click())

        delay(1000)
        //click on the save button and saveReminder
        Espresso.onView(withId(R.id.save_reminder_location)).perform(ViewActions.click())

        delay(1000)

        Espresso.onView(withId(R.id.reminderTitle))
            .check(ViewAssertions.matches(ViewMatchers.withText("Title")))
        Espresso.onView(withId(R.id.reminderDescription))
            .check(ViewAssertions.matches(ViewMatchers.withText("Desc")))

        activityScenario.close()
    }

    @Test
    fun saveReminder_showMessageToast_Working() = runBlocking {
        // GIVEN
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        //click on add reminder and write title and description
        Espresso.onView(withId(R.id.noDataTextView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.reminderTitle))
            .perform(ViewActions.typeText("test title"))
        Espresso.onView(withId(R.id.reminderDescription))
            .perform(ViewActions.typeText("test description"))

        //click to select location then the map is open then save it
        Espresso.onView(withId(R.id.selectLocation)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.fragments_map)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.save_reminder_location)).perform(ViewActions.click())

        delay(1000)
        //click to save reminder
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())

        // THEN show message that reminder saved
        Espresso.onView(ViewMatchers.withText(R.string.reminder_saved))
            .inRoot(RootMatchers.withDecorView(Matchers.not(Matchers.`is`(getActivity(activityScenario).window.decorView))))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        activityScenario.close()
    }
    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity {
        lateinit var activity: Activity
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }

}