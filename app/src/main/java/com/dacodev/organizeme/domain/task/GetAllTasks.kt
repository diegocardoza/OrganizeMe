package com.dacodev.organizeme.domain.task

import com.dacodev.organizeme.data.task.TaskRepository
import com.dacodev.organizeme.data.task.model.TaskEntity
import com.dacodev.organizeme.domain.task.model.TaskItem
import com.dacodev.organizeme.domain.task.model.toTaskItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetAllTasks @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(): Flow<List<TaskItem>> =
        taskRepository.getAllTasks().map { list -> list.map { it.toTaskItem() } }
}