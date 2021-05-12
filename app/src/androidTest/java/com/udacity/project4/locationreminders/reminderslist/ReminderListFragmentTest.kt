package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.repo.FakeAndroidRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : KoinTest {

    //    TODO: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.
//    TODO: add testing for the error messages.

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var applicationContext: Application

    private val reminderFakeRep: FakeAndroidRepository by inject()


    @Before
    fun init() {
        stopKoin()

        applicationContext = getApplicationContext()


        val reminderModule = module {
            viewModel {
                RemindersListViewModel(applicationContext, get() as FakeAndroidRepository)
            }

            single {
                Room.inMemoryDatabaseBuilder(
                    get(),
                    RemindersDatabase::class.java
                )
                    .allowMainThreadQueries()
                    .build()
            }

            single { FakeAndroidRepository() }
            single { LocalDB.createRemindersDao(applicationContext) }
        }


        startKoin {
            androidContext(applicationContext)
            modules(listOf(reminderModule))
        }

    }

    @After
    fun tearDown() {
        runBlockingTest {
            reminderFakeRep.deleteAllReminders()
        }
        stopKoin()
    }

    //TODO Create ServiceLocator Pattern
    @Test
    fun reminderList_DisplayInUi() = runBlocking<Unit> {

        //Given
        reminderFakeRep.insertReminders(buildReminderData())


        //When
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)


        //Then - Expresso
        onView(withText("someTitleA")).check(matches(isDisplayed()))
        onView(withText("someTitleD")).check(matches(isDisplayed()))
        onView(withText("someDescriptionB")).check(matches(isDisplayed()))
        onView(withText("someLocationC")).check(matches(isDisplayed()))
    }

    @Test
    //subjectUnderTest_actionOrInput_resultState
    fun reminderList_whenNoRemindersSaved_DisplayNoData() {
        //When
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        //Then - Expresso
        onView(withText("No Data")).check(matches(isDisplayed()))

    }

    @Test
    fun onFAButton_whenClicked_navigateToSaveReminder() = runBlockingTest {
        //Given
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)


        val navController = mock(NavController::class.java)


        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        //When - click fab
        onView(withId(R.id.addReminderFAB)).perform(click())

        //Then
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())

    }

    private fun buildReminderData() = arrayListOf(
        ReminderDTO(
            "someTitleA",
            "someDescriptionA", "someLocationA", 32.776665,
            -96.796989
        ),
        ReminderDTO(
            "someTitleB",
            "someDescriptionB", "someLocationB", 32.776665,
            -96.796989

        ),
        ReminderDTO(
            "someTitleC",
            "someDescriptionC", "someLocationC", 32.776665,
            -96.796989
        ),
        ReminderDTO(
            "someTitleD",
            "someDescriptionD", "someLocationD", 32.776665,
            -96.796989
        )
    )
}