package com.dacodev.organizeme.broadcast_receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dacodev.organizeme.domain.task.GetTaskByReminderId
import com.dacodev.organizeme.domain.task.UpdateTask
import com.dacodev.organizeme.util.Constants
import com.dacodev.organizeme.util.Constants.ON_DISMISS_ACTION
import com.dacodev.organizeme.util.Constants.ON_MARK_AS_DONE_ACTION
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class TaskAlarmNotificationReceiver: BroadcastReceiver() {

    @Inject lateinit var getTaskByReminderId:GetTaskByReminderId

    @Inject lateinit var updateTask: UpdateTask

    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationId = intent?.getIntExtra(Constants.TASK_NOTIFICATION_ID_EXTRA,-1)
        when(intent?.action){
            ON_MARK_AS_DONE_ACTION -> {
                if (notificationId != null && notificationId != -1) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val task = getTaskByReminderId(notificationId)
                        if (task != null) {
                            updateTask(task.copy(isDone = true))
                        }
                        withContext(Dispatchers.Main) {
                            val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            notificationManager.cancel(notificationId)
                        }
                    }
                }
            }
            ON_DISMISS_ACTION -> {
                if (notificationId != null && notificationId != -1) {
                    val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(notificationId)
                }
            }
            else -> Unit
        }


    }
}