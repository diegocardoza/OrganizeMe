package com.dacodev.organizeme.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dacodev.organizeme.R
import com.dacodev.organizeme.databinding.ItemDayOfMonthBinding
import com.dacodev.organizeme.ui.adapters.model.CalendarDayForRecycler

class CalendarAdapter(
    private val calendarDays: List<CalendarDayForRecycler>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<CalendarViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return CalendarViewHolder(layoutInflater.inflate(R.layout.item_day_of_month, parent, false))
    }

    override fun getItemCount(): Int = calendarDays.size

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.render(calendarDays[position], holder.itemView.context, onItemClick, position)
    }

    fun markItemAsSelected(previousSelected: Int, newSelected: Int) {
        calendarDays[previousSelected].isSelected = false
        notifyItemChanged(previousSelected)
        calendarDays[newSelected].isSelected = true
        notifyItemChanged(newSelected)
    }
}

class CalendarViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemDayOfMonthBinding.bind(view)

    fun render(
        day: CalendarDayForRecycler,
        context: Context,
        onItemClick: (Int) -> Unit,
        position: Int
    ) {
        itemView.setOnClickListener {
            onItemClick(position)
        }
        binding.card.strokeWidth =
            if (day.isSelected) context.resources.getDimension(com.intuit.sdp.R.dimen._3sdp)
                .toInt() else context.resources.getDimension(com.intuit.sdp.R.dimen._1sdp).toInt()
        binding.card.strokeColor =
            if (day.isSelected) context.getColor(R.color.md_theme_primary) else context.getColor(R.color.md_theme_primaryContainer)
        binding.year.text = day.year.toString()
        val monthText = context.getString(
            when (day.month) {
                1 -> R.string.january_text
                2 -> R.string.february_text
                3 -> R.string.march_text
                4 -> R.string.april_text
                5 -> R.string.may_text
                6 -> R.string.june_text
                7 -> R.string.july_text
                8 -> R.string.august_text
                9 -> R.string.september_text
                10 -> R.string.october_text
                11 -> R.string.november_text
                12 -> R.string.december_text
                else -> R.string.january_text
            }
        )
        binding.month.text = monthText
        binding.dayOfMonth.text = if (day.dayOfMonth < 10) "0${day.dayOfMonth}"
        else day.dayOfMonth.toString()
        val dayOfWeekText = context.getString(
            when (day.dayOfWeek) {
                1 -> R.string.monday_text
                2 -> R.string.tuesday_text
                3 -> R.string.wednesday_text
                4 -> R.string.thursday_text
                5 -> R.string.friday_text
                6 -> R.string.saturday_text
                7 -> R.string.sunday_text
                else -> R.string.monday_text
            }
        )
        binding.dayOfWeek.text = dayOfWeekText
    }

}