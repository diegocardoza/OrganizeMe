package com.dacodev.organizeme.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dacodev.organizeme.R
import com.dacodev.organizeme.databinding.ItemTaskBinding
import com.dacodev.organizeme.domain.task.model.TaskItem

class TaskListAdapter(
    private val taskList: List<TaskItem>,
    private val onTaskClick: (TaskItem) -> Unit,
    private val onDeleteTaskClick: (TaskItem) -> Unit,
    private val onDoneChangeClick: (TaskItem, Boolean) -> Unit
) : RecyclerView.Adapter<TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        TaskViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        )

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) =
        holder.render(taskList[position], onTaskClick, onDeleteTaskClick, onDoneChangeClick)

    override fun getItemCount() = taskList.size
}

class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemTaskBinding.bind(view)

    fun render(
        task: TaskItem,
        onTaskClick: (TaskItem) -> Unit,
        onDeleteTaskClick: (TaskItem) -> Unit,
        onDoneChangeClick: (TaskItem, Boolean) -> Unit
    ) {
        binding.title.text = task.title
        binding.description.text = task.description
        itemView.setOnClickListener {
            onTaskClick(task)
        }
        binding.ivDeleteTask.setOnClickListener {
            onDeleteTaskClick(task)
        }
        binding.cbIsDone.setOnCheckedChangeListener { _, _ ->
            onDoneChangeClick(task, true)
        }
    }

}
