package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValueForTest
import com.udacity.project4.locationreminders.rule.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
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


    private lateinit var reminderListViewModel: RemindersListViewModel
    private lateinit var fakeDataSource: FakeDataSource

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUpViewModel() {
        fakeDataSource = FakeDataSource(buildReminderData().toMutableList())
        reminderListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loadReminder_whenShowLoading_returnTrue_afterLoadingData_returnFalse() {
        // Pause dispatcher so you can verify initial values.
        mainCoroutineRule.pauseDispatcher()

        //load the data
        reminderListViewModel.loadReminders()

        // Then assert that the progress indicator is shown.
        assertThat(reminderListViewModel.showLoading.getOrAwaitValueForTest(), `is`(true))

        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()

        // Then assert that the progress indicator is shown.
        assertThat(reminderListViewModel.showLoading.getOrAwaitValueForTest(), `is`(false))


    }

    @Test
    fun loadReminders_whenShouldReturnError_isFalse_thenReturnList() = mainCoroutineRule.runBlockingTest {
        fakeDataSource.setShouldReturnError(false)
        reminderListViewModel.loadReminders()

        val rListValue = reminderListViewModel.remindersList.getOrAwaitValueForTest()

        assertThat(rListValue, `is`(notNullValue()))
        assertThat(rListValue, hasSize(equalTo(4)))
    }

    @Test
    fun loadReminders_whenShouldReturnError_isTrue_thenReturnRemindersNotFound() =
        mainCoroutineRule.runBlockingTest {
            fakeDataSource.setShouldReturnError(true)
            reminderListViewModel.loadReminders()
            val value = reminderListViewModel.showSnackBar.getOrAwaitValueForTest()

            assertThat(value, `is`("Reminders not found"))
        }



    @Test
    fun loadReminders_whenAllRemindersAreDeleted_thenCallLoadReminder_ReturnEmpty() =
        mainCoroutineRule.runBlockingTest {

            //Given
            fakeDataSource.deleteAllReminders()

            reminderListViewModel.loadReminders()

            //When
            val reminderList = reminderListViewModel.remindersList.value
            val reminderMessage = reminderListViewModel.showSnackBar.value

            //Then
            assertThat(reminderMessage, `is`(nullValue()))
            assertThat(reminderList, hasSize(equalTo(0)))
        }

    @Test
    fun invalidateShowNoData_whenNoDatainReminderList_returnShowNoDataMessageTrue() =
        mainCoroutineRule.runBlockingTest {

            //Given
            fakeDataSource.deleteAllReminders()
            reminderListViewModel.loadReminders()

            //When
            val reminderList = reminderListViewModel.remindersList.value
            val noData = reminderListViewModel.showNoData.value

            //Then
            assertThat(reminderList, hasSize(equalTo(0)))
            assertThat(noData, `is`(true))
        }

    @Test
    fun invalidateShowNoData_whenNoDataInReminderList_returnShowNoDataMessageFalse() =
        mainCoroutineRule.runBlockingTest {

            //Given
            fakeDataSource.deleteAllReminders()
            fakeDataSource.saveReminder(buildInvalidateReminderDataItem())


            //When
            reminderListViewModel.loadReminders()
            val noData = reminderListViewModel.showNoData.value

            //Then
            assertThat(noData, `is`(false))
        }

    private fun buildReminderData() = listOf(
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

    private fun buildInvalidateReminderDataItem() =
        ReminderDTO(
            null, null, null, null, null
        )

}