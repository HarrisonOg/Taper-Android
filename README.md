# Taper-Android
Taper App

## Scheduling reminders

Taper builds reminder plans in two steps:

1. `ScheduleGenerator` produces monotonically changing daily counts inside the
   user's configured waking window. Pass `AppSettings` explicitly during tests
   to reproduce edge cases such as daylight saving time.
2. `AlarmScheduler` fans the events out to the platform, preferring
   WorkManager when there are multiple reminders in a day and falling back to
   an exact alarm when a taper hits single reminders. Provide a
   `WorkRequestFactory` that tags requests with `AlarmScheduler.habitTag(habitId)`
   and a `PendingIntentFactory` that points at a manifest-registered broadcast
   receiver to handle the alarm. On Android 12+ the manifest must declare
   `android.permission.SCHEDULE_EXACT_ALARM`, and callers should fall back to
   WorkManager when `AlarmManager.canScheduleExactAlarms()` reports that the
   user has not granted exact alarm access.
