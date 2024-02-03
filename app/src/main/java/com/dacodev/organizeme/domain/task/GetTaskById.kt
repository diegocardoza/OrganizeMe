package com.dacodev.organizeme.domain.task

import com.dacodev.organizeme.data.task.TaskRepository
import com.dacodev.organizeme.domain.task.model.TaskItem
import com.dacodev.organizeme.domain.task.model.toTaskItem
import javax.inject.Inject

class GetTaskById @Inject constructor(
    private val taskRepository: TaskRepository
) {

    suspend operator fun invoke(id: Int): TaskItem? =
        taskRepository.getTaskById(id).toTaskItem()

}