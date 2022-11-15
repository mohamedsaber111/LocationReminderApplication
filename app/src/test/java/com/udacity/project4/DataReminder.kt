package com.udacity.project4

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
object DataReminder {
    val items = arrayListOf(
        ReminderDTO(
            "Title 1",
            "Description 1",
            "Location 1",
            37.15,
            -110.8
        ),
        ReminderDTO(
            "Title 2",
            "Description 2",
            "Location 2",
            30.3,
            -500.8
        ),
        ReminderDTO(
            "title 3",
            "description 3",
            "location 3",
            80.9,
            -620.8
        )
    )

    val reminderDataItem = ReminderDataItem(
        "Title 4",
        "Description 4",
        "Location 4",
        88.8,
        -258.7
    )
}