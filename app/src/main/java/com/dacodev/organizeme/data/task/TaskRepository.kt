package com.dacodev.organizeme.data.task

import com.dacodev.organizeme.data.task.database.TaskDao
import com.dacodev.organizeme.data.task.model.TaskEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {

    suspend fun insertTask(task:TaskEntity) {
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: TaskEntity) {
        taskDao.insertTask(task)
    }

    suspend fun deleteTask(task: TaskEntity) {
        taskDao.deleteTask(task)
    }

    suspend fun getTaskById(id: Int): TaskEntity = taskDao.getTaskById(id)

    suspend fun getTaskByReminderId(reminderId: Int):TaskEntity? = taskDao.getTaskByReminderId(reminderId)

    fun getTasksByDate(date:String): Flow<List<TaskEntity>> = taskDao.getTasksByDate(date)

    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()
}