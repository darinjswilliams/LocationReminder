package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

class FakeDataSource (var reminderServiceData: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        reminderServiceData?.let{ return Result.Success(ArrayList(it))}
        return  Result.Error("no data")
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