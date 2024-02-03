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
import com.dacodev.organizeme.domain.calendar.GetIndexOfCalendarDayByDate
import com.dacodev.organizeme.domain.calendar.model.CalendarDay
import com.dacodev.organizeme.domain.task.GetTaskById
import com.dacodev.organizeme.domain.task.InsertTask
import com.dacodev.organizeme.domain.task.model.TaskItem
import com.dacodev.organizeme.util.Constants
import com.dacodev.organizeme.util.TaskDetailUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val context: Application,
    private val insertTask: InsertTask,
    private val getCalendarDays: GetCalendarDays,
    private val getCurrentDayOfCalendar: GetCurrentDayOfCalendar,
    private val getTaskById: GetTaskById,
    private val getIndexOfCalendarDayByDate: GetIndexOfCalendarDayByDate
) : ViewModel() {

    private var _calendarDays = MutableStateFlow<List<CalendarDay>>(emptyList())
    val calendarDays: StateFlow<List<CalendarDay>> get() = _calendarDays

    private var _indexOfDay = MutableStateFlow(0)
    val indexOfDay: StateFlow<Int> get() = _indexOfDay

    private var _task = MutableStateFlow<TaskItem?>(null)
    val task: StateFlow<TaskItem?> get() = _task

    private var _isDone = MutableStateFlow(false)
    val isDone: StateFlow<Boolean> get() = _isDone

    private var _title = MutableStateFlow("")
    val title: StateFlow<String> get() = _title

    private var _description = MutableStateFlow("")
    val description: StateFlow<String> get() = _description

    private var _deadLine = MutableStateFlow<String?>(null)
    val deadLine: StateFlow<String?> get() = _deadLine

    private var _alarmTimeInMillis = MutableStateFlow<Long?>(null)
    val alarmTimeInMillis: StateFlow<Long?> get() = _alarmTimeInMillis

    private val _uiEvent = Channel<TaskDetailUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var reminderIdOfTaskSchedule:Int? = null
    private var doNotRemindMeWasPressed = false

    fun onCreate(taskId: Int) {
        viewModelScope.launch {
            val calendarDays = getCalendarDays()
            _calendarDays.emit(calendarDays)
            if (taskId != -1) {
                getTaskById(taskId)?.let { task ->
                    _task.emit(task)
                    _title.emit(task.title)
                    _description.emit(task.description ?: "")
                    _deadLine.emit(task.deadLine)
                    _alarmTimeInMillis.emit(task.alarmTimeInMillis)
                    reminderIdOfTaskSchedule = task.reminderId
                    val indexDayOfCalendar = getIndexOfCalendarDayByDate(task.deadLine)
                    _indexOfDay.emit(indexDayOfCalendar)
                }
            } else {
                val currentDayOfCalendar = getCurrentDayOfCalendar()
                _indexOfDay.emit(currentDayOfCalendar)
            }
        }
    }

    fun onEvent(event: TaskDetailEvent) {
        when (event) {
            is TaskDetailEvent.OnSaveTaskClick -> viewModelScope.launch {
                viewModelScope.launch {
                    if (title.value.isNotEmpty()) {
                        val task = TaskItem(
                            id = task.value?.id,
                            title = title.value,
                            description = description.value,
                            isDone = isDone.value,
                            deadLine = deadLine.value ?: "",
                            alarmTimeInMillis = alarmTimeInMillis.value,
                            reminderId = if (alarmTimeInMillis.value != null) UUID.randomUUID()
                                .hashCode() else null
                        )
                        insertTask(task)
                        cancelReminderScheduled()
                        scheduleTaskReminder(task)
                        sendUiEvent(
                            TaskDetailUiEvent.ShowSnackBar(
                                context.getString(R.string.task_saved_text)
                            )
                        )
                        sendUiEvent(TaskDetailUiEvent.PopBackStack)
                    } else {
                        sendUiEvent(
                            TaskDetailUiEvent.ShowSnackBar(
                                context.getString(R.string.task_can_not_be_empty_text)
                            )
                        )
                    }
                }
            }

            is TaskDetailEvent.OnChangeDescription -> {
                viewModelScope.launch {
                    _description.emit(event.description)
                }
            }

            is TaskDetailEvent.OnChangeTitle -> {
                viewModelScope.launch {
                    _title.emit(event.title)
                }
            }

            is TaskDetailEvent.OnChangeAlarmTime -> {
                viewModelScope.launch {
                    doNotRemindMeWasPressed = event.alarmTime == null
                    _alarmTimeInMillis.emit(event.alarmTime)
                }
            }

            is TaskDetailEvent.OnChangeDeadLine -> {
                viewModelScope.launch {
                    _deadLine.emit(event.deadLine)
                }
            }

            TaskDetailEvent.OnBackPressedClick -> sendUiEvent(TaskDetailUiEvent.PopBackStack)
            is TaskDetailEvent.OnChangeDoneClick -> viewModelScope.launch { _isDone.emit(event.isDone) }
        }
    }

    private fun sendUiEvent(event: TaskDetailUiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
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

    private fun cancelReminderScheduled() {
        if (doNotRemindMeWasPressed && reminderIdOfTaskSchedule != null) {
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderIdOfTaskSchedule!!,
                Intent(context, TaskAlarmNotification::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val alarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            alarmManager.cancel(pendingIntent)
        }
    }

}