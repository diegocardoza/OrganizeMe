package com.dacodev.organizeme.util

sealed class TaskDetailUiEvent {
    data object PopBackStack : TaskDetailUiEvent()
    data class ShowSnackBar(val message: String): TaskDetailUiEvent()
}
