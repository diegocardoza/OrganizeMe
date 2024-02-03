package com.dacodev.organizeme.domain.task.model

import com.dacodev.organizeme.data.task.model.TaskEntity

data class TaskItem(
    val id: Int? = null,
    val title: String,
    val description: String? = null,
    val isDone: Boolean = false,
    val deadLine: String,
    val alarmTimeInMillis: Long? = null,
    val reminderId: Int? = null
)

fun TaskItem.toTaskEntity(): TaskEntity =
    TaskEntity(id, title, description, isDone, deadLine, alarmTimeInMillis, reminderId)

fun TaskEntity.toTaskItem(): TaskItem =
    TaskItem(id, title, description, isDone, deadLine, alarmTime, reminderId)
