package com.dacodev.organizeme.ui.viewmodels

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dacodev.organizeme.R
import com.dacodev.organizeme.broadcast_receiver.TaskAlarmNotification
import com.dacodev.organizeme.domain.calendar.GetCalendarDays
import com.dacodev.organizeme.domain.calendar.GetCurrentDayOfCalendar
import com.dacodev.organizeme.domain.task.DeleteTask
import com.dacodev.organizeme.domain.task.GetAllTasks
import com.dacodev.organizeme.domain.task.InsertTask
import com.dacodev.organizeme.domain.task.UpdateTask
import com.dacodev.organizeme.domain.task.model.TaskItem
import com.dacodev.organizeme.util.Constants
import com.dacodev.organizeme.util.TaskListUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val context: Application,
    private val deleteTask: DeleteTask,
    private val updateTask: UpdateTask,
    private val insertTask: InsertTask,
    private val getCalendarDays: GetCalendarDays,
    private val getCurrentDayOfCalendar: GetCurrentDayOfCalendar,
    getAllTasks: GetAllTasks
) : ViewModel() {

    val calendarDays = flow {
        val days = getCalendarDays()
        emit(days)
    }

    val currentDay = flow {
        val day = getCurrentDayOfCalendar()
        emit(day)
    }

    val tasks = getAllTasks()

    private val _uiEvent = Channel<TaskListUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var deletedTask: TaskItem? = null

    fun onEvent(event: TaskListEvent) {
        when (event) {
            is TaskListEvent.OnAddEditTaskClick -> {
                sendTaskListUiEvent(TaskListUiEvent.NavigateToTaskDetail())
            }

            is TaskListEvent.OnDeleteTaskClick -> {
                deletedTask = event.task
                viewModelScope.launch {
                    deleteTask(event.task)
                }
                cancelReminder(event.task)
                sendTaskListUiEvent(
                    TaskListUiEvent.ShowSnackBar(
                        message = context.getString(R.string.task_deleted_text),
                        action = context.getString(R.string.undo_text)
                    )
                )
            }

            is TaskListEvent.OnDoneChangeClick -> {
                viewModelScope.launch {
                    updateTask(event.task.copy(isDone = event.isDone))
                }
            }

            is TaskListEvent.OnTaskClick -> {
                sendTaskListUiEvent(TaskListUiEvent.NavigateToTaskDetail(event.task.id))
            }

            is TaskListEvent.OnUndoDeleteTaskClick -> {
                deletedTask?.let {  task ->
                    viewModelScope.launch {
                        insertTask(task)
                        scheduleTaskReminder(task)
                    }
                    sendTaskListUiEvent(
                        TaskListUiEvent.ShowSnackBar(
                            message = context.getString(R.string.task_restored_text)
                        )
                    )
                }
            }
        }
    }

    private fun cancelReminder(task: TaskItem) {
        if (task.alarmTimeInMillis != null && task.reminderId != null) {
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                task.reminderId,
                Intent(context, TaskAlarmNotification::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val alarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            alarmManager.cancel(pendingIntent)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleTaskReminder(taskItem: TaskItem) {
        if (!taskItem.isDone && taskItem.alarmTimeInMillis != null && taskItem.reminderId != null) {
            if (System.currentTimeMillis() < taskItem.alarmTimeInMillis) {
                val intent = Intent(context, TaskAlarmNotification::class.java).apply {
                    putExtra(Constants.TASK_TITLE_EXTRA, taskItem.title)
                    putExtra(Constants.TASK_NOTIFICATION_ID_EXTRA, taskItem.reminderId)
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    taskItem.reminderId,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, taskItem.alarmTimeInMillis, pendingIntent
                )
            }
        }
    }

    private fun sendTaskListUiEvent(event: TaskListUiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }

}