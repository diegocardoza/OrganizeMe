package com.dacodev.organizeme.ui.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dacodev.organizeme.R
import com.dacodev.organizeme.databinding.ItemCompletedTaskBinding
import com.dacodev.organizeme.domain.task.model.TaskItem

class CompletedTasksAdapter(
    private val completedTasksList: List<TaskItem>,
    private val onTaskClick: (TaskItem) -> Unit,
    private val onDeleteTaskClick: (TaskItem) -> Unit,
    private val onDoneChangeClick: (TaskItem, Boolean) -> Unit
) : RecyclerView.Adapter<CompletedTasksViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CompletedTasksViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_completed_task, parent, false)
        )

    override fun getItemCount() = completedTasksList.size

    override fun onBindViewHolder(holder: CompletedTasksViewHolder, position: Int) =
        holder.render(
            completedTasksList[position],
            onTaskClick,
            onDeleteTaskClick,
            onDoneChangeClick
        )
}

class CompletedTasksViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemCompletedTaskBinding.bind(view)

    fun render(
        task: TaskItem,
        onTaskClick: (TaskItem) -> Unit,
        onDeleteTaskClick: (TaskItem) -> Unit,
        onDoneChangeClick: (TaskItem, Boolean) -> Unit
    ) {
        binding.title.text = task.title
        binding.title.apply {
            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }
        binding.description.text = task.description
        itemView.setOnClickListener {
            onTaskClick(task)
        }
        binding.ivDeleteTask.setOnClickListener {
            onDeleteTaskClick(task)
        }
        binding.cbIsDone.setOnCheckedChangeListener { _, _ ->
            onDoneChangeClick(task, false)
        }
    }

}
