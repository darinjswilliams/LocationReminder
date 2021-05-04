package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

//    TODO: Add testing implementation to the RemindersDao.kt

    //Since your testing Architecture components
    //This will execute each task synchronsously
    var instanteExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    //subjectUnderTest_actionOrInput_resultState
    fun saveReminder_whenReminderIsPopulated_thenInsertIntoDatabase() = runBlockingTest {

        //Given
        val reminderDTO = ReminderDTO(
            "someTitle",
            "someDescription", "somelocation", 32.776665, -96.796989
        )
        database.reminderDao().saveReminder(reminderDTO)

        //When
        val reminderById = database.reminderDao().getReminderById(reminderDTO.id)

        //Then
        assertThat<ReminderDTO>(
            reminderById as ReminderDTO,
            notNullValue()
        )
        assertThat(reminderById?.id, `is`(reminderDTO.id))
        assertThat(reminderById?.title, `is`(reminderDTO.title))
        assertThat(reminderById?.location, `is`(reminderDTO.location))
        assertThat(reminderById?.latitude, `is`(reminderDTO.latitude))
        assertThat(reminderById?.longitude, `is`(reminderDTO.longitude))
    }
}