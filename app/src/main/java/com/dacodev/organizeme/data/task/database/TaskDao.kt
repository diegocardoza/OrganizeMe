package com.dacodev.organizeme.data.task.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomWarnings
import com.dacodev.organizeme.data.task.model.TaskEntity
import com.dacodev.organizeme.util.Constants.TASK_TABLE
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT * FROM $TASK_TABLE WHERE id = :id")
    suspend fun getTaskById(id: Int): TaskEntity

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT * FROM $TASK_TABLE WHERE reminderId = :reminderId")
    suspend fun getTaskByReminderId(reminderId: Int): TaskEntity?

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT * FROM $TASK_TABLE WHERE deadLine = :date")
    fun getTasksByDate(date: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM $TASK_TABLE")
    fun getAllTasks(): Flow<List<TaskEntity>>

}