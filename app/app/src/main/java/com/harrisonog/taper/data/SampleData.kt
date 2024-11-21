package com.harrisonog.taper.data

import java.time.LocalDateTime

class SampleData {

    fun getSampleHabitList(): List<Habit> {
        return listOf(
            Habit(
                name = "Sample Habit 1",
                description = "Reduce habit description 1",
                notificationMessage = "Take 1 gum",
                startTaperAlarmsPerDay = 5,
                taperLength = TaperLength(5, TaperLengthTimeScale.WEEKS),
            ),
            Habit(
                name = "Sample Habit 2",
                description = "Reduce habit description 2",
                notificationMessage = "Drink 1 soda",
                startTaperAlarmsPerDay = 3,
                taperLength = TaperLength(3, TaperLengthTimeScale.WEEKS),
            ),
            Habit(
                name = "Sample Habit 3",
                description = "Increase habit description 1",
                notificationMessage = "Do 5 pushup",
                habitType = HabitType.INCREASE,
                startTaperAlarmsPerDay = 2,
                endTaperAlarmsPerDay = 10,
                taperLength = TaperLength(6, TaperLengthTimeScale.WEEKS),
            ),
            Habit(
                name = "Sample Habit 4",
                description = "Increase habit description 1",
                notificationMessage = "Drink water",
                habitType = HabitType.INCREASE,
                startTaperAlarmsPerDay = 5,
                endTaperAlarmsPerDay = 10,
                taperLength = TaperLength(5, TaperLengthTimeScale.WEEKS),
            )
        )
    }
}