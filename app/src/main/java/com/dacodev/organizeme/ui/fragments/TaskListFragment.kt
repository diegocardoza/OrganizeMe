package com.dacodev.organizeme.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dacodev.organizeme.R
import com.dacodev.organizeme.databinding.FragmentTaskListBinding
import com.dacodev.organizeme.domain.calendar.model.toCalendarDayForRecycler
import com.dacodev.organizeme.domain.task.model.TaskItem
import com.dacodev.organizeme.ui.adapters.CalendarAdapter
import com.dacodev.organizeme.ui.adapters.CompletedTasksAdapter
import com.dacodev.organizeme.ui.adapters.TaskListAdapter
import com.dacodev.organizeme.ui.adapters.model.CalendarDayForRecycler
import com.dacodev.organizeme.ui.viewmodels.TaskListEvent
import com.dacodev.organizeme.ui.viewmodels.TaskListViewModel
import com.dacodev.organizeme.util.TaskListUiEvent
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskListViewModel by viewModels()

    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var taskListAdapter: TaskListAdapter
    private lateinit var completedTasksAdapter: CompletedTasksAdapter

    private var tasks: List<TaskItem> = emptyList()
    private lateinit var calendarDays: List<CalendarDayForRecycler>

    private var index0fCurrentDay = 0
    private var indexOfCalendar = 0
    private var previousItemSelected = 0

    private var dateOfCalendar: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUiState()
    }

    private fun initUiState() {
        setUpFlowCollects()
        setUpListeners()
    }

    private fun setUpFlowCollects() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.calendarDays.collect { calendarDays ->
                        val calendarDaysForRecycler =
                            calendarDays.map { it.toCalendarDayForRecycler() }
                        setUpCalendarDays(calendarDaysForRecycler)
                    }
                }
                launch {
                    viewModel.currentDay.collect { currentDay ->
                        index0fCurrentDay = currentDay
                        setUpCurrentDay(currentDay)
                    }
                }
                launch {
                    viewModel.uiEvent.collect { uiEvent ->
                        when (uiEvent) {
                            is TaskListUiEvent.NavigateToTaskDetail -> navigateToTaskDetail(uiEvent)
                            is TaskListUiEvent.PopBackStack -> findNavController().popBackStack()
                            is TaskListUiEvent.ShowSnackBar -> showSnackBar(uiEvent)
                        }
                    }
                }
                launch {
                    viewModel.tasks.collect { tasks ->
                        this@TaskListFragment.tasks = tasks
                        filterTasksForDate(calendarDays[indexOfCalendar])
                    }
                }
            }
        }
    }

    private fun setUpListeners() {
        binding.btnAddTask.setOnClickListener {
            viewModel.onEvent(TaskListEvent.OnAddEditTaskClick)
        }
    }

    private fun getTasksText(): String {
        val calendarDay = calendarDays[indexOfCalendar]
        if (index0fCurrentDay == indexOfCalendar) {
            return getString(R.string.today_tasks_text)
        } else if (index0fCurrentDay - 1 == indexOfCalendar) {
            return getString(R.string.yesterdays_tasks_text)
        } else if (index0fCurrentDay + 1 == indexOfCalendar) {
            return getString(R.string.tomorrows_tasks_text)
        } else {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, calendarDay.year)
            calendar.set(Calendar.MONTH, calendarDay.month - 1)
            calendar.set(Calendar.DAY_OF_MONTH, calendarDay.dayOfMonth)

            val numberOfDayText =
                SimpleDateFormat("dd", Locale.getDefault()).format(calendar.timeInMillis).toInt()
            val month = SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.timeInMillis)

            return when (Locale.getDefault().language) {
                "en" -> getString(
                    R.string.tasks_for_day_of_month_in_english,
                    month,
                    numberOfDayText
                )

                "es" -> getString(
                    R.string.tasks_for_day_of_month_in_spanish,
                    numberOfDayText,
                    month
                )

                else -> getString(
                    R.string.tasks_for_day_of_month_in_english,
                    month,
                    numberOfDayText
                )
            }
        }
    }

    private fun filterTasksForDate(calendarDay: CalendarDayForRecycler) {
        val year = calendarDay.year.toString()
        val month =
            if (calendarDay.month < 10) "0${calendarDay.month}" else calendarDay.month.toString()
        val day =
            if (calendarDay.dayOfMonth < 10) "0${calendarDay.dayOfMonth}" else calendarDay.dayOfMonth.toString()

        dateOfCalendar = "$year-$month-$day"
        val filteredTasks = tasks.filter { it.deadLine == dateOfCalendar }
        updateToTaskRecyclers(filteredTasks)
    }

    private fun setUpCalendarDays(allDays: List<CalendarDayForRecycler>) {
        calendarDays = allDays
        setUpViewPager(allDays)
    }

    private fun setUpCurrentDay(currentDay: Int) {
        indexOfCalendar = currentDay
        calendarAdapter.markItemAsSelected(previousItemSelected, currentDay)
        binding.todayTasks.text = getTasksText()
        previousItemSelected = currentDay
        filterTasksForDate(calendarDays[indexOfCalendar])
        if (binding.vpCalendar.adapter != null) {
            binding.vpCalendar.scrollToPosition(indexOfCalendar)
        }
    }

    private fun showSnackBar(uiEvent: TaskListUiEvent.ShowSnackBar) {
        Snackbar.make(binding.btnAddTask, uiEvent.message, Snackbar.LENGTH_SHORT).apply {
            setAction(uiEvent.action) {
                viewModel.onEvent(TaskListEvent.OnUndoDeleteTaskClick)
            }
            show()
        }
    }

    private fun navigateToTaskDetail(uiEvent: TaskListUiEvent.NavigateToTaskDetail) {
        if (uiEvent.id != null) {
            findNavController().navigate(
                TaskListFragmentDirections
                    .actionTodoListFragmentToTaskDetailFragment(uiEvent.id)
            )
        } else {
            findNavController().navigate(
                TaskListFragmentDirections
                    .actionTodoListFragmentToTaskDetailFragment()
            )
        }
    }

    private fun setUpTaskRecyclers() {
        taskListAdapter = TaskListAdapter(
            emptyList(),
            { task ->
                viewModel.onEvent(TaskListEvent.OnTaskClick(task))
            }, { task ->
                viewModel.onEvent(TaskListEvent.OnDeleteTaskClick(task))
            }, { task, isChecked ->
                viewModel.onEvent(TaskListEvent.OnDoneChangeClick(task, isChecked))
            })
        binding.rvTaskList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTaskList.adapter = taskListAdapter

        completedTasksAdapter = CompletedTasksAdapter(
            emptyList(),
            { task ->
                viewModel.onEvent(TaskListEvent.OnTaskClick(task))
            }, { task ->
                viewModel.onEvent(TaskListEvent.OnDeleteTaskClick(task))
            }, { task, isChecked ->
                viewModel.onEvent(TaskListEvent.OnDoneChangeClick(task, isChecked))
            })
        binding.rvCompletedTaskList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCompletedTaskList.adapter = completedTasksAdapter
    }

    private fun updateToTaskRecyclers(tasks: List<TaskItem>) {
        if (tasks.isNotEmpty()) {
            binding.notTasksFoundContainer.isVisible = false
            binding.taskContainer.isVisible = true
            val noTasksCompleted = tasks.filter { !it.isDone }
            if (noTasksCompleted.isNotEmpty()) {
                binding.notTasksForCompletedFoundContainer.isVisible = false
                binding.rvTaskList.isVisible = true

                binding.rvTaskList.layoutManager = LinearLayoutManager(requireContext())
                binding.rvTaskList.adapter = TaskListAdapter(
                    noTasksCompleted,
                    { task ->
                        viewModel.onEvent(TaskListEvent.OnTaskClick(task))
                    }, { task ->
                        viewModel.onEvent(TaskListEvent.OnDeleteTaskClick(task))
                    }, { task, _ ->
                        viewModel.onEvent(TaskListEvent.OnDoneChangeClick(task, true))
                    })
            } else {
                binding.notTasksForCompletedFoundContainer.isVisible = true
                binding.rvTaskList.isVisible = false
            }
            val completedTask = tasks.filter { it.isDone }
            if (completedTask.isNotEmpty()) {
                binding.notTasksCompletedFoundContainer.isVisible = false
                binding.rvCompletedTaskList.isVisible = true

                binding.rvCompletedTaskList.layoutManager = LinearLayoutManager(requireContext())
                binding.rvCompletedTaskList.adapter = CompletedTasksAdapter(
                    completedTask,
                    { task ->
                        viewModel.onEvent(TaskListEvent.OnTaskClick(task))
                    }, { task ->
                        viewModel.onEvent(TaskListEvent.OnDeleteTaskClick(task))
                    }, { task, _ ->
                        viewModel.onEvent(TaskListEvent.OnDoneChangeClick(task, false))
                    })
            } else {
                binding.notTasksCompletedFoundContainer.isVisible = true
                binding.rvCompletedTaskList.isVisible = false
            }
        } else {
            binding.notTasksFoundContainer.isVisible = true
            binding.taskContainer.isVisible = false
        }
    }

    private fun setUpViewPager(daysOfYear: List<CalendarDayForRecycler>) {
        calendarAdapter = CalendarAdapter(daysOfYear) { position ->
            setUpCurrentDay(position)
        }
        binding.vpCalendar.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.vpCalendar.adapter = calendarAdapter
    }
}