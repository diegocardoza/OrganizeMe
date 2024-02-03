package com.dacodev.organizeme.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.dacodev.organizeme.R
import com.dacodev.organizeme.databinding.FragmentTaskDetailBinding
import com.dacodev.organizeme.domain.calendar.model.toCalendarDayForRecycler
import com.dacodev.organizeme.ui.adapters.CalendarAdapter
import com.dacodev.organizeme.ui.adapters.model.CalendarDayForRecycler
import com.dacodev.organizeme.ui.viewmodels.TaskDetailEvent
import com.dacodev.organizeme.ui.viewmodels.TaskDetailViewModel
import com.dacodev.organizeme.util.TaskDetailUiEvent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class TaskDetailFragment : Fragment() {

    private val args: TaskDetailFragmentArgs by navArgs()

    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskDetailViewModel by viewModels()

    private lateinit var daysOfYear: List<CalendarDayForRecycler>

    private var indexOfCalendar = 0
    private var previousItemSelected = 0

    private lateinit var calendarAdapter: CalendarAdapter

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                showDateTimePicker()
            } else {
                showShouldAcceptPermissionDialog()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUiState()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun initUiState() {
        setUpTask()
        setUpFlows()
        setUpListeners()
    }

    private fun setUpTask() {
        viewModel.onCreate(args.taskId)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setUpListeners() {
        binding.ivBackPressed.setOnClickListener {
            viewModel.onEvent(TaskDetailEvent.OnBackPressedClick)
        }
        binding.btnSaveTask.setOnClickListener {
            viewModel.onEvent(TaskDetailEvent.OnSaveTaskClick)
        }
        binding.taskTitleEditText.doAfterTextChanged { newTitle ->
            viewModel.onEvent(TaskDetailEvent.OnChangeTitle(newTitle.toString()))
        }
        binding.taskDescriptionEditText.doAfterTextChanged { newDescription ->
            viewModel.onEvent(TaskDetailEvent.OnChangeDescription(newDescription.toString()))
        }
        binding.cbTaskIsDone.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onEvent(
                TaskDetailEvent.OnChangeDoneClick(isChecked)
            )
        }
        binding.btnSelectDateAndTime.setOnClickListener {
            if (hasNotificationPermission(requireContext())) {
                showDateTimePicker()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        binding.btnDoNotRemindMe.setOnClickListener {
            viewModel.onEvent(TaskDetailEvent.OnChangeAlarmTime(null))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUpFlows() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.calendarDays.collect { calendarDays ->
                        val calendarDaysForRecycler = calendarDays.map { it.toCalendarDayForRecycler() }
                        setUpDaysOfYear(calendarDaysForRecycler)
                    }
                }
                launch {
                    viewModel.indexOfDay.collect { currentDay ->
                        setUpCurrentDay(currentDay)
                    }
                }
                launch {
                    viewModel.task.collect { task ->
                        if (task != null) {
                            binding.toolbarTitle.text =
                                requireContext().getString(R.string.edit_task_text)
                            binding.cbTaskIsDone.isChecked = task.isDone
                            binding.taskTitleEditText.text =
                                Editable.Factory.getInstance().newEditable(task.title)
                            binding.taskDescriptionEditText.text =
                                Editable.Factory.getInstance().newEditable(task.description ?: "")
                        } else {
                            binding.toolbarTitle.text =
                                requireContext().getString(R.string.add_task_text)
                        }
                    }
                }
                launch {
                    viewModel.uiEvent.collect { event ->
                        when (event) {
                            is TaskDetailUiEvent.PopBackStack -> findNavController().popBackStack()
                            is TaskDetailUiEvent.ShowSnackBar -> showSnackBar(event)
                        }
                    }
                }
                launch {
                    viewModel.deadLine.collect { deadLine ->
                        if (deadLine != null) {
                            binding.calendarTitle.text =
                                requireContext().getString(R.string.select_the_date_text) + " " + deadLine
                        }
                    }
                }
                launch {
                    viewModel.isDone.collect { isDone ->
                        binding.cbTaskIsDone.isChecked = isDone
                    }
                }
                launch {
                    viewModel.alarmTimeInMillis.collect { alarmTimeInMillis ->
                        if (alarmTimeInMillis != null) {
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = alarmTimeInMillis

                            binding.remindMeTaskText.text = calendarToDateText(calendar)
                            binding.btnDoNotRemindMe.isVisible = true
                        } else {
                            binding.remindMeTaskText.text =
                                requireContext().getString(R.string.do_not_remind_me_task_text)
                            binding.btnDoNotRemindMe.isVisible = false
                        }
                    }
                }
            }
        }
    }

    private fun setUpDaysOfYear(allDaysOfYear: List<CalendarDayForRecycler>) {
        daysOfYear = allDaysOfYear
        setUpViewPager(allDaysOfYear)
    }

    private fun setUpCurrentDay(currentDay: Int) {
        indexOfCalendar = currentDay
        calendarAdapter.markItemAsSelected(previousItemSelected,currentDay)
        previousItemSelected = currentDay
        onChangeDeadLine(currentDay)
        if (binding.vpCalendar.adapter != null) {
            binding.vpCalendar.scrollToPosition(indexOfCalendar)
        }
    }

    private fun setUpViewPager(daysOfYear: List<CalendarDayForRecycler>) {
        calendarAdapter = CalendarAdapter(daysOfYear){ position ->
            setUpCurrentDay(position)
        }
        binding.vpCalendar.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.vpCalendar.adapter = calendarAdapter
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()

        showDatePicker(calendar)
    }

    private fun showShouldAcceptPermissionDialog() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setIcon(R.drawable.ic_suggestion)
            setTitle(getString(R.string.suggestion_text))
            setMessage(getString(R.string.you_must_accept_to_permission_text))
            setPositiveButton(getString(R.string.ok_text)) { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }

    private fun showDatePicker(calendar: Calendar) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                calendar.set(selectedYear, selectedMonth, selectedDayOfMonth)
                showTimePicker(calendar)
            },
            year,
            month,
            dayOfMonth
        )
        datePickerDialog.show()
    }

    private fun showTimePicker(dateCalendar: Calendar) {
        val hour = dateCalendar.get(Calendar.HOUR_OF_DAY)
        val minute = dateCalendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHourOfDay, selectedMinute ->
                dateCalendar.set(Calendar.HOUR_OF_DAY, selectedHourOfDay)
                dateCalendar.set(Calendar.MINUTE, selectedMinute)
                dateCalendar.set(Calendar.SECOND, 0)
                val remindMeAtText = calendarToDateText(dateCalendar)
                viewModel.onEvent(TaskDetailEvent.OnChangeAlarmTime(dateCalendar.timeInMillis))
                binding.remindMeTaskText.text = remindMeAtText
            },
            hour,
            minute,
            false
        )
        timePickerDialog.show()
    }

    private fun onChangeDeadLine(position: Int) {
        val calendarDay = daysOfYear[position]
        val year = calendarDay.year.toString()
        val month =
            if (calendarDay.month < 10) "0${calendarDay.month}" else calendarDay.month.toString()
        val day =
            if (calendarDay.dayOfMonth < 10) "0${calendarDay.dayOfMonth}" else calendarDay.dayOfMonth.toString()
        val deadLine = "$year-$month-$day"
        viewModel.onEvent(TaskDetailEvent.OnChangeDeadLine(deadLine))
    }

    private fun showSnackBar(event: TaskDetailUiEvent.ShowSnackBar) {
        Snackbar.make(binding.btnSaveTask, event.message, Snackbar.LENGTH_SHORT).show()
    }

    private fun calendarToDateText(
        dateSelected: Calendar,
        format: String = "EEEE, MMM"
    ): String {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val currentDate = Calendar.getInstance()

        val remindMeText = if (dateSelected.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR) &&
            dateSelected.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH) &&
            dateSelected.get(Calendar.DAY_OF_MONTH) == currentDate.get(Calendar.DAY_OF_MONTH)
        ) {
            requireContext().getString(
                R.string.remind_me_today_at_text,
                timeFormat.format(dateSelected.time)
            )
        } else if (dateSelected.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR) &&
            dateSelected.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH) &&
            dateSelected.get(Calendar.DAY_OF_MONTH) == currentDate.get(Calendar.DAY_OF_MONTH) - 1
        ) {
            requireContext().getString(
                R.string.remind_me_yesterday_at_text,
                timeFormat.format(dateSelected.time)
            )
        } else if (dateSelected.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR) &&
            dateSelected.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH) &&
            dateSelected.get(Calendar.DAY_OF_MONTH) == currentDate.get(Calendar.DAY_OF_MONTH) + 1
        ) {
            requireContext().getString(
                R.string.remind_me_tomorrow_at_text,
                timeFormat.format(dateSelected.time)
            )
        } else {
            requireContext().getString(
                R.string.remind_me_at_text,
                dateFormat.format(dateSelected.time),
                timeFormat.format(dateSelected.time)
            )
        }
        return remindMeText
    }

    private fun hasNotificationPermission(context: Context) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

}