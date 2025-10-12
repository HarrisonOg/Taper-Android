# Taper-Android
Taper App

## Scheduling overview

Taper generates tapering plans with `ScheduleGenerator`, which interpolates between a habit's
starting and ending cadence while keeping each reminder inside the configured wake window. The
results feed into `AlarmScheduler`, which enqueues most events via WorkManager but automatically
switches to exact `AlarmManager` alarms on the final taper-to-zero day so reminders land on time
even during Doze.
