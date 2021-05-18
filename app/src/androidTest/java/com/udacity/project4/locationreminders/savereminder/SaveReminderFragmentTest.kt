package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.repo.FakeAndroidRepository
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@MediumTest
@RunWith(MockitoJUnitRunner::class)
class SaveReminderFragmentTest : KoinTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var applicationContext: Application

    @Before
    fun init() {
        stopKoin()

        applicationContext = ApplicationProvider.getApplicationContext()

        val reminderModule = module {
            viewModel {
                SaveReminderViewModel(applicationContext, get() as FakeAndroidRepository)
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

        stopKoin()
    }


    @Test
    fun saveReminderFaB_whenClick_saveViewModelAndValidate() = runBlockingTest {

        //Given

        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)

        val navController = mock(NavController::class.java)

        //When
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }


        //Then
        onView(withId(R.id.reminderTitle)).perform(
            ViewActions.typeText("someTitleD"))

        onView(withId(R.id.reminderDescription)).perform(
                ViewActions.typeText("someDescription"))


        onView(withId(R.id.selectLocation)).perform(click())

        verify(navController).navigate(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())

    }


    @Test
    fun reminderLocation_whenOnClick_NavigateToSelectionLocationFragment() = runBlockingTest {

        // GIVEN - On the home screen
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)

        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        //When
        onView(withId(R.id.selectLocation)).perform(click())


        //Then
       verify(navController).navigate(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())

    }


    @Test
    fun snackBarNoTitle_whenClickingSaveReminder_returnDisplayTitleSnackBarMessage() = runBlockingTest {
        // GIVEN - On the home screen
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)

        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        //When
        onView(withId(R.id.saveReminder)).perform(click())


        //Then
        onView(withText("Please enter title")).check(matches(isDisplayed()))

    }

    private fun buildReminder() = ReminderDTO(
        "someTitleD",
        "someDescriptionD", "someLocationD", 32.776665,
        -96.796989
    )
}