package com.udacity.project4.locationreminders.savereminder

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var fakeDataSource: FakeDataSource

    @Before
    fun setUp() {
        fakeDataSource = FakeDataSource()
        saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    //TODO: provide testing to the SaveReminderView and its live data objects
    //TODO: write test for validateAndSaveReminder, saveReminder, validateEnteredData, onClear

    @Test
    //subjectUnderTest_actionOrInput_resultState
    fun validateAndSaveReminder_whenAllFieldsArePopulated_returnSaveViewModel() {

        //Given
        val reminderData = ReminderDataItem("someTitle",
            "someDescription", "someLocation", 32.776665,
            -96.796989, "101")

        //When saving a reminder
        saveReminderViewModel.saveReminder(reminderData)

        //Then
    }


}



