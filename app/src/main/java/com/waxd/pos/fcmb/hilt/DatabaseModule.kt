package com.waxd.pos.fcmb.hilt

import android.content.Context
import androidx.room.Room
import com.waxd.pos.fcmb.room.AppDatabase
import com.waxd.pos.fcmb.room.dao.UploadDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDb(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "uploads_waxd.db").build()

    @Provides
    fun provideUploadDao(db: AppDatabase): UploadDao = db.uploadDao()
}