package com.waxd.pos.fcmb.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.waxd.pos.fcmb.room.dao.UploadDao
import com.waxd.pos.fcmb.room.entity.UploadEntity

@Database(entities = [UploadEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun uploadDao(): UploadDao
}