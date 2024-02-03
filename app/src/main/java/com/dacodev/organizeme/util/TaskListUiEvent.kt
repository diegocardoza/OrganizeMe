package com.dacodev.organizeme.util


sealed class TaskListUiEvent {
    data object PopBackStack: TaskListUiEvent()
    data class NavigateToTaskDetail(val id: Int? = null): TaskListUiEvent()
    data class ShowSnackBar(
        val message: String,
        val action: String? = null
    ): TaskListUiEvent()
}
