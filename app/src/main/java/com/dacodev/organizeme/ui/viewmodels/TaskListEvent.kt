package com.dacodev.organizeme.ui.viewmodels

import com.dacodev.organizeme.domain.task.model.TaskItem

sealed class TaskListEvent {
    data class OnTaskClick(val task:TaskItem): TaskListEvent()
    data class OnDoneChangeClick(val task: TaskItem, val isDone:Boolean): TaskListEvent()
    data class OnDeleteTaskClick(val task: TaskItem): TaskListEvent()
    data object OnUndoDeleteTaskClick: TaskListEvent()
    data object OnAddEditTaskClick: TaskListEvent()
}
