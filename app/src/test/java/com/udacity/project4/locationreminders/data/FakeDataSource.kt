package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminderServiceData: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

    private var shouldReturnError = false

    fun setShouldReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (shouldReturnError) {
            Result.Error("Reminders not found")
        } else {
            reminderServiceData.let {
                Result.Success(ArrayList(it))
            }

        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderServiceData?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {

        return when(val reminderItemFound = reminderServiceData?.find { it.id == id }){
               null -> Result.Error("Reminder Not Found for $id")

                else -> Result.Success(reminderItemFound)
        }
    }

    override suspend fun deleteAllReminders() {
    reminderServiceData = mutableListOf()
    }



}