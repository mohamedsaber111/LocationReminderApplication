package com.udacity.project4.data

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//in unit test we does not use RemindersLocalRepository() class with real code so we create fake one to use it in local test
//we inherit from ReminderDataSource() interface and create new FakeDataSource()

//reminders list(val reminders : MutableList<ReminderDTO>) >> represents all data in my FakeDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO> = mutableListOf()):
    ReminderDataSource {

    //create boolean flag that says whether or not the FakeDataSource should return an error
    //error handling if has any error
    private var shouldReturnError = false

    fun setShouldReturnError(shouldReturn: Boolean) {

        this.shouldReturnError = shouldReturn
    }

    //get reminderList
    override suspend fun getReminders(): Result<List<ReminderDTO>> {

        //if we have error and we need to test it with error handling we should make the data source return error even if it is not empty to test error
        if (shouldReturnError) {
            return Result.Error("return testing error")
        }
        //return success result with my reminders
        reminders.let { return Result.Success(reminders) }

    }

    //Get a reminder by its id
    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        //if has error return error
        if(shouldReturnError){
            return Result.Error("Reminder not found")
        }
        val reminder = reminders?.find {
            it.id == id
        }
        return if (reminder!=null){
            //if reminder not null return Success with reminder
            Result.Success(reminder)
        } else{
            //if else return error with message Reminder not found
            Result.Error("Reminder not found")
        }


    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        //save reminders
        reminders.add(reminder)
    }

    override suspend fun deleteAllReminders() {
        //delete reminders
        reminders.clear()
    }

}