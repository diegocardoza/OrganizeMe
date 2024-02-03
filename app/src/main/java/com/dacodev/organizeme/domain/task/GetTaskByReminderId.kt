package com.dacodev.organizeme.domain.task

import com.dacodev.organizeme.data.task.TaskRepository
import com.dacodev.organizeme.domain.task.model.toTaskItem
import javax.inject.Inject

class GetTaskByReminderId @Inject constructor(
    private val repository: TaskRepository
) {

    suspend operator fun invoke(reminderId: Int) = repository.getTaskByReminderId(reminderId)?.toTaskItem()

}