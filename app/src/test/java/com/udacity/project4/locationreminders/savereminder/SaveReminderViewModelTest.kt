package com.udacity.project4.locationreminders.savereminder


import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.rule.MainCoroutineRule
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
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

    //TODO: provide testing to the SaveReminderView and its live data objects
    //TODO: write test for validateAndSaveReminder, saveReminder, validateEnteredData, onClear

    @Test
    //subjectUnderTest_actionOrInput_resultState
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
            assertThat(saveReminderViewModel.showLoading.value, `is`(false))
            assertThat(reminderResult.data.title, `is`(reminderData.title))
            assertThat(reminderResult.data.description, `is`(reminderData.description))
            assertThat(reminderResult.data.location, `is`(reminderData.location))
            assertThat(reminderResult.data.latitude, `is`(reminderData.latitude))
            assertThat(reminderResult.data.longitude, `is`(reminderData.longitude))
            assertThat(saveReminderViewModel.showToast.value, `is`("Reminder Saved !"))
        }





    @Test
    //subjectUnderTest_actionOrInput_resultState
    fun saveReminder_whenGivenValidReminderItem_thenSaveReminderItemReturnTrue() =
        mainCoroutineRule.runBlockingTest {

            //Given
            val reminderData = buildReminderData()

            //When
            val saveReminderResult = saveReminderViewModel.validateEnteredData(reminderData)
            assertThat(saveReminderResult,`is`(true))

            //Then
            saveReminderViewModel.validateAndSaveReminder(reminderData)
            val reminderResult = fakeDataSource.getReminder(reminderData.id)
            reminderResult as Result.Success


            assertThat(saveReminderViewModel.showToast.value,
                `is`(equalTo("Reminder Saved !")))
        }


    @Test
    //subjectUnderTest_actionOrInput_resultState
    fun saveReminder_whenReminderItemIsNotValid_thenReturnFalse()
    = mainCoroutineRule.runBlockingTest {

        //Given
        val reminderData = buildReminderData()

         reminderData.title = null

        //When
        val saveReminderResult = saveReminderViewModel.validateEnteredData(reminderData)

        //Then
        assertThat(saveReminderResult,`is`(false))
    }

    @Test
    //subjectUnderTest_actionOrInput_resultState
    fun validateEnteredData_whenReminderItemIsValid_returnTrue() {
        TODO("Not yet implemented")
    }

    @Test
    //subjectUnderTest_actionOrInput_resultState
    fun validateEnteredData_whenReminderItemsNotValid_returnFalse() {
        TODO("Not yet implemented")
    }

    @Test
    //subjectUnderTest_actionOrInput_resultState
    fun onClear_whenReminderObjectIsSave_returnClearReminderItemOnNextSave() = mainCoroutineRule.runBlockingTest {

        //Given
        val reminderData = buildReminderData()

        //When
        saveReminderViewModel.validateAndSaveReminder(reminderData)
        val reminderResult = fakeDataSource.getReminder(reminderData.id)
        reminderResult as Result.Success
        assertThat(saveReminderViewModel.showToast.value,
            `is`(equalTo("Reminder Saved !")))

        //then
        saveReminderViewModel.onClear()
        assertThat(saveReminderViewModel.latitude.value, `is`(nullValue()))


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



