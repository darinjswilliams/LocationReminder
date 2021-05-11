package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest: KoinTest {

    //    TODO: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.
//    TODO: add testing for the error messages.

    private lateinit var reminderRepo: FakeDataSource

    private lateinit var applicationContext: Application

    private lateinit var reminderDataSource: FakeDataSource


    @Before
    fun init() {
        stopKoin()

        applicationContext = getApplicationContext()
        reminderRepo = FakeDataSource()
        reminderDataSource = FakeDataSource()


        val reminderModule = module {
            viewModel {
                RemindersListViewModel(applicationContext, reminderRepo)
            }

            single{ SaveReminderViewModel(applicationContext, reminderRepo) }
            single { FakeDataSource() }
            single { LocalDB.createRemindersDao(applicationContext) }
        }


        startKoin {  androidContext(applicationContext)
        modules(listOf(reminderModule)) }

        runBlockingTest {
            reminderRepo.deleteAllReminders()
        }

    }

    @After
    fun tearDown() {
        stopKoin()
    }

    //TODO Create ServiceLocator Pattern
    @Test
    fun reminderList_DisplayInUi() = runBlocking<Unit>{

        //Given
        reminderRepo.saveReminder(buildReminder())
        reminderRepo.saveReminder(buildAnotherReminder())


        //When
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)


        //Then - Expresso
        onView(withText("SomeDescription")).check(matches(isDisplayed()))
        onView(withText("SomeLocationB")).check(matches(isDisplayed()))
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
    //subjectUnderTest_actionOrInput_resultState
    fun onFAButton_whenClicked_navigateToSaveReminder() {
        //Given

        //Then
        val navController =  mock(NavController::class.java)

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        //When - click fab
        onView(withId(R.id.addReminderFAB)).perform(click())

        //CORRECT NAVIGATION METHOD IS CALLED.
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())

    }

    private fun buildReminder() = ReminderDTO(
        "SomeTitle",
        "SomeDescription",
        "SomeLocation",
        36.94593,
        -35.67789
    )

    private fun buildAnotherReminder() = ReminderDTO(
        "SomeTitleB",
        "SomeDescriptionA",
        "SomeLocationA",
        36.9459368,
        -35.6778934
    )
}