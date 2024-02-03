package com.dacodev.organizeme.data.task.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dacodev.organizeme.util.Constants

@Entity(tableName = Constants.TASK_TABLE)
data class TaskEntity(

    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int? = null,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "isDone")
    val isDone: Boolean,

    @ColumnInfo(name = "deadLine")
    val deadLine: String,

    @ColumnInfo(name = "alarmTime")
    val alarmTime: Long? = null,

    @ColumnInfo(name = "reminderId")
    val reminderId: Int? = null
)