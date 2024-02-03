package com.dacodev.organizeme.ui.viewmodels

sealed class TaskDetailEvent {
    data class OnChangeTitle(val title: String): TaskDetailEvent()
    data class OnChangeDescription(val description: String): TaskDetailEvent()
    data class OnChangeDeadLine(val deadLine: String): TaskDetailEvent()
    data class OnChangeAlarmTime(val alarmTime: Long?): TaskDetailEvent()
    data class OnChangeDoneClick(val isDone:Boolean): TaskDetailEvent()
    data object OnBackPressedClick: TaskDetailEvent()
    data object OnSaveTaskClick: TaskDetailEvent()
}
