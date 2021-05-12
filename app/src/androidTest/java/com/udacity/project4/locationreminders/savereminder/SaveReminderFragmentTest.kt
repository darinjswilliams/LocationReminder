package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
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
import org.koin.test.inject
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@MediumTest
@RunWith(MockitoJUnitRunner::class)
class SaveReminderFragmentTest : KoinTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val reminderFakeRep: FakeAndroidRepository by inject()

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
    //subjectUnderTest_actionOrInput_resultState
    fun onClickSelection_navigateToSelectionFragment() = runBlockingTest {

        //Given
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)

        val navController = mock(NavController::class.java)
        val mockNavCmd = mock(BaseFragment::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        //When
        onView(withId(R.id.selectLocation)).perform(click())

        //Then
        verify(navController).navigate(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        verify(mockNavCmd).findNavController()
            .navigate(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
    }


    @Test
    //subjectUnderTest_actionOrInput_resultState
    fun saveReminder_whenReminderLocation_isClicked() = runBlockingTest {

        //Given

        val reminder  = buildReminder()

        reminderFakeRep.saveReminder(buildReminder())

        val reminderId = reminderFakeRep.getReminder(reminder.id)


        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)

        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        //When
        onView(withId(R.id.saveReminder)).perform(click())


        //Then
       onView(withId(R.id.reminderTitle)).check(matches(withText("Description")))

    }


    private fun buildReminder() = ReminderDTO(
        "someTitleD",
        "someDescriptionD", "someLocationD", 32.776665,
        -96.796989
    )
}