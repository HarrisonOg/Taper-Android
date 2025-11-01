# Taper-Android
Taper App

## Scheduling reminders

The `ScheduleGenerator` turns taper plans into day-by-day reminders that always stay inside the
user's wake window, even across daylight saving changes. When it is time to actually wake the user,
`AlarmScheduler` prefers WorkManager so Android can optimise background execution, but
automatically falls back to exact alarms when a habit is tapering to zero so Doze mode cannot delay
the final reminders. Make sure the app declares the `SCHEDULE_EXACT_ALARM` permission on Android 14
and newer if you want the fallback to fire without manual user approval.
