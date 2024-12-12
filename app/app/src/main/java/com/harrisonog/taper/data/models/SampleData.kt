package com.harrisonog.taper.data.models

class SampleData {

    fun getSampleHabitList(): List<Habit> {
        return listOf(
            Habit(
                name = "Sample Habit 1",
                description = "Reduce habit description 1",
                notificationMessage = "Take 1 gum",
                startTaperAlarmsPerDay = 5,
                taperLength = 5,
            ),
            Habit(
                name = "Sample Habit 2",
                description = "Reduce habit description 2",
                notificationMessage = "Drink 1 soda",
                startTaperAlarmsPerDay = 3,
                taperLength = 3,
            ),
            Habit(
                name = "Sample Habit 3",
                description = "Increase habit description 1",
                notificationMessage = "Do 5 pushup",
                habitType = HabitType.INCREASE,
                startTaperAlarmsPerDay = 2,
                endTaperAlarmsPerDay = 10,
                taperLength = 6,
            ),
            Habit(
                name = "Sample Habit 4",
                description = "Increase habit description 1",
                notificationMessage = "Drink water",
                habitType = HabitType.INCREASE,
                startTaperAlarmsPerDay = 5,
                endTaperAlarmsPerDay = 10,
                taperLength = 5,
            )
        )
    }
}