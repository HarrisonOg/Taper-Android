package com.harrisonog.taper_android.data.db

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class Converters {
    @TypeConverter fun localDateToString(v: LocalDate?): String? = v?.toString()

    @TypeConverter fun stringToLocalDate(v: String?): LocalDate? = v?.let(LocalDate::parse)

    @TypeConverter fun localTimeToString(v: LocalTime?): String? = v?.toString()
    @TypeConverter fun stringToLocalTime(v: String?): LocalTime? = v?.let(LocalTime::parse)

    @TypeConverter fun instantToLong(v: Instant?): Long? = v?.toEpochMilli()
    @TypeConverter fun longToInstant(v: Long?): Instant? = v?.let { Instant.ofEpochMilli(it) }
}