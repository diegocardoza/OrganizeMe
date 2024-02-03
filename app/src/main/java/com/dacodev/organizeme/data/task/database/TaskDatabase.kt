package com.dacodev.organizeme.data.task.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dacodev.organizeme.data.task.model.TaskEntity

@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TaskDatabase: RoomDatabase() {

    abstract fun getTaskDao(): TaskDao

}