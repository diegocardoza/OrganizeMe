package com.dacodev.organizeme.di

import android.app.Application
import androidx.room.Room
import com.dacodev.organizeme.data.task.database.TaskDao
import com.dacodev.organizeme.data.task.database.TaskDatabase
import com.dacodev.organizeme.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Provides
    @Singleton
    fun provideTaskRoom(context: Application): TaskDatabase =
        Room.databaseBuilder(context,TaskDatabase::class.java,Constants.TASK_DATABASE).build()

    @Provides
    @Singleton
    fun provideTaskDao(db: TaskDatabase): TaskDao = db.getTaskDao()
}