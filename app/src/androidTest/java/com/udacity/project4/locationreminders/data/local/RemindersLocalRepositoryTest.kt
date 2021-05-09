package com.udacity.project4.locationreminders.data.local

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.hasSize
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    //    TODO: Add testing implementation to the RemindersLocalRepository.kt

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    //represent data from database
    private lateinit var reminderLocalRepo: RemindersLocalRepository
    private lateinit var reminderDatabase: RemindersDatabase


    @Before
    fun initDb() {
        reminderDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        reminderLocalRepo =
            RemindersLocalRepository(reminderDatabase.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDb() = reminderDatabase.close()


    @Test
    //subjectUnderTest_actionOrInput_resultState
    fun getReminders_whenSuccess_returnListOfReminders() =  runBlocking(){

            //Given
            reminderLocalRepo.saveReminder(buildReminder())
            reminderLocalRepo.saveReminder(buildReminder())



            //When
            val results = reminderLocalRepo.getReminders()
            val  reminderList = results as Result.Success


        //Then
            assertThat(reminderList, not(emptyArray<ReminderDTO>()))
            assertThat(reminderList.data, hasSize(greaterThanOrEqualTo(1)))
        }


    @Test
    fun saveReminder_whenValidReminderRecord_InsertRecordIntoDatabase() = runBlocking() {


        //Given
        val reminder = buildReminder()


        //When
        reminderLocalRepo.saveReminder(reminder)
        val reminderRecord = reminderLocalRepo.getReminder(reminder.id)
        reminderRecord as Result.Success

        //Then
        assertThat(reminder.id, `is`(reminderRecord.data.id) )
    }


    @Test
    //subjectUnderTest_actionOrInput_resultState
    fun deleteReminders_whenRemindersAreDelete_returnReminderNotFoundMessage() = runBlocking {

        //Given
        val reminder = buildReminder()
        reminderLocalRepo.saveReminder(reminder)


        //When
        reminderLocalRepo.deleteAllReminders()

        val reminderRecord = reminderLocalRepo.getReminder(reminder.id)
        reminderRecord as Result.Error

        //Then
        assertThat(reminderRecord.message, `is`("Reminder not found!"))


    }


    @Test
    //subjectUnderTest_actionOrInput_resultState
    fun getReminder_whenReminderInserted_returnReminder() = runBlocking{

        //Given
        val reminder = buildReminder()


        //When
        reminderLocalRepo.saveReminder(reminder)

        val reminderRecord = reminderLocalRepo.getReminder(reminder.id)
        reminderRecord as Result.Success


        assertThat(reminderRecord.data.id, `is`(reminder.id))


    }

    private fun buildRemindersList() = arrayListOf(
        ReminderDTO(
            "sometitle",
            "someDescription",
            "someLocation",
            36.94593,
            -35.67789
        ),
        ReminderDTO(
            "sometitle",
            "someDescription",
            "someLocation",
            39.94593,
            -34.34567
        ),
        ReminderDTO(
            "sometitle",
            "someDescription",
            "someLocation",
            38.94593,
            -34.83245
        ),
        ReminderDTO(
            "sometitle",
            "someDescription",
            "someLocation",
            37.94593,
            -34.12345
        ),
        ReminderDTO(
            "sometitle",
            "someDescription",
            "someLocation",
            35.94593,
            -34.34897
        ),

        )


    private fun buildReminder() = ReminderDTO(
            "sometitle",
            "someDescription",
            "someLocation",
            36.94593,
            -35.67789
        )
}