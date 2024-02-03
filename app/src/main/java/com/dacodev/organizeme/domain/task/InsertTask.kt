package com.dacodev.organizeme.domain.task

import com.dacodev.organizeme.data.task.TaskRepository
import com.dacodev.organizeme.domain.task.model.TaskItem
import com.dacodev.organizeme.domain.task.model.toTaskEntity
import javax.inject.Inject

class InsertTask @Inject constructor(
    private val taskRepository: TaskRepository
) {

    suspend operator fun invoke(task: TaskItem) {
        taskRepository.insertTask(task.toTaskEntity())
    }

}