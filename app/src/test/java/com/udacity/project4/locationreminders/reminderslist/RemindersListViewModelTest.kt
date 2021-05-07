package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.base.Strings.isNullOrEmpty
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.rule.MainCoroutineRule
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import net.bytebuddy.matcher.ElementMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects
    //TODO write test for loadReminders and invalidateShowNodata

    private lateinit var reminderListViewModel: RemindersListViewModel
    private lateinit var fakeDataSource: FakeDataSource

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUpViewModel() {
        fakeDataSource = FakeDataSource(buildReminderData())
        reminderListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loadReminders_addToReminderList_returnPopulateList() =
        mainCoroutineRule.runBlockingTest {

            //Given
            reminderListViewModel.loadReminders()


            //When
            val reminderList = reminderListViewModel.remindersList.value

            //Then
            assertThat(reminderList, hasSize(equalTo(4)))

        }


    @Test
    fun loadReminders_whenNoRemindersAreStored_ReturnError() =
        mainCoroutineRule.runBlockingTest {

            //Given
            fakeDataSource.deleteAllReminders()
            reminderListViewModel.loadReminders()


            //When
            val reminderList = reminderListViewModel.remindersList.value

            //Then
            assertThat(reminderList, hasSize(equalTo(0))  )

        }

    private fun buildReminderData() = arrayListOf(
        ReminderDTO(
            "someTitle",
            "someDescription", "someLocation", 32.776665,
            -96.796989
        ),
        ReminderDTO(
            "someTitle",
            "someDescription", "someLocation", 32.776665,
            -96.796989

        ),
        ReminderDTO(
            "someTitle",
            "someDescription", "someLocation", 32.776665,
            -96.796989
        ),
        ReminderDTO(
            "someTitle",
            "someDescription", "someLocation", 32.776665,
            -96.796989
        )
    )
}