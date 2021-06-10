package com.udacity.project4.locationreminders.savereminder


import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValueForTest
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.rule.MainCoroutineRule
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var fakeDataSource: FakeDataSource

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUpViewModel() {
        fakeDataSource = FakeDataSource()
        saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun validateAndSaveReminder_whenAllFieldsArePopulated_returnSaveReminder() =
        mainCoroutineRule.runBlockingTest {

            //Given
            val reminderData = buildReminderData()


            //When saving a reminder
            val validReminderItem = saveReminderViewModel.validateEnteredData(reminderData)
            assertThat(validReminderItem, `is`(true))

            saveReminderViewModel.saveReminder(reminderData)

            val reminderResult = fakeDataSource.getReminder(reminderData.id)
            reminderResult as Result.Success

            //Then
            assertThat(saveReminderViewModel.showLoading.getOrAwaitValueForTest(), `is`(false))
            assertThat(reminderResult.data.title, `is`(reminderData.title))
            assertThat(reminderResult.data.description, `is`(reminderData.description))
            assertThat(reminderResult.data.location, `is`(reminderData.location))
            assertThat(reminderResult.data.latitude, `is`(reminderData.latitude))
            assertThat(reminderResult.data.longitude, `is`(reminderData.longitude))
            assertThat(saveReminderViewModel.showToast.getOrAwaitValueForTest(), `is`("Reminder Saved !"))
        }


    @Test
    fun saveReminderResultSucess_whenGivenValidReminderItem_thenReturnSuccessMessage() =
        mainCoroutineRule.runBlockingTest {

            //Given
            val reminderData = buildReminderData()

            //When
            val saveReminderResult = saveReminderViewModel.validateEnteredData(reminderData)
            assertThat(saveReminderResult, `is`(true))

            //Then
            saveReminderViewModel.validateAndSaveReminder(reminderData)
            val reminderResult = fakeDataSource.getReminder(reminderData.id)
            reminderResult as Result.Success

            assertThat(
                saveReminderViewModel.showToast.getOrAwaitValueForTest(),
                `is`(equalTo("Reminder Saved !"))
            )
        }

    @Test
    fun saveReminderResultFalse_whenGivenInValidReminderId_thenReturnReminderNotFoundMessage() = runBlockingTest {
        //Given
        var reminderData = buildReminderData()

        //When
        val saveReminderResult = saveReminderViewModel.validateEnteredData(reminderData)
        assertThat(saveReminderResult, `is`(true))

        //Then
        reminderData.id = "17"

        val reminderResult = fakeDataSource.getReminder(reminderData.id)
        reminderResult as Result.Error


        assertThat(reminderResult.message, `is`(equalTo("Reminder Not Found for " + reminderData.id)))

    }

    @Test
    fun validateEnteredData_whenReminderItemIsValid_returnTrue() {
        //Given
        val reminderData = buildReminderData()

        //When
        val saveReminderResult = saveReminderViewModel.validateEnteredData(reminderData)

        //Then
        assertThat(saveReminderResult, `is`(true))
    }

    @Test
    fun validateTitle_whenReminderTileIsEmpty_returnFalse() {
        //Given
        val reminderData = buildReminderData()
        reminderData.title = null

        //When
        val saveReminderResult = saveReminderViewModel.validateEnteredData(reminderData)

        //Then
        assertThat(saveReminderResult, `is`(false))
    }

    @Test
    fun validateLocation_whenReminderLocationIsEmpty_returnFalse() {

        val reminderData = buildReminderData()
        reminderData.location = null

        //When
        val saveReminderResult = saveReminderViewModel.validateEnteredData(reminderData)

        //Then
        assertThat(saveReminderResult, `is`(false))

    }

    @Test
    fun onClear_whenReminderObjectIsSave_returnClearReminderItemOnNextSave() =
        mainCoroutineRule.runBlockingTest {

            //Given
            val reminderData = buildReminderData()

            //When
            saveReminderViewModel.validateAndSaveReminder(reminderData)
            val reminderResult = fakeDataSource.getReminder(reminderData.id)
            reminderResult as Result.Success
            assertThat(
                saveReminderViewModel.showToast.getOrAwaitValueForTest(),
                `is`(equalTo("Reminder Saved !"))
            )

            //then
            saveReminderViewModel.onClear()
            assertThat(saveReminderViewModel.latitude.getOrAwaitValueForTest(), `is`(nullValue()))


            fakeDataSource.deleteAllReminders()
            val reminderPastResult = fakeDataSource.getReminder(reminderData.id)
            val errorResult = reminderPastResult as Result.Error
            assertThat(reminderPastResult, `is`(errorResult))

        }


    private fun buildReminderData() = ReminderDataItem(
        "someTitle",
        "someDescription", "someLocation", 32.776665,
        -96.796989
    )
}



