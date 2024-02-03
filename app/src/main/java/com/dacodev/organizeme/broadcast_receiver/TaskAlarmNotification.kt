package com.dacodev.organizeme.broadcast_receiver

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.dacodev.organizeme.R
import com.dacodev.organizeme.ui.activities.MainActivity
import com.dacodev.organizeme.util.Constants.ON_DISMISS_ACTION
import com.dacodev.organizeme.util.Constants.ON_MARK_AS_DONE_ACTION
import com.dacodev.organizeme.util.Constants.TASK_NOTIFICATION_ID_EXTRA
import com.dacodev.organizeme.util.Constants.TASK_TITLE_EXTRA

class TaskAlarmNotification : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "Reminder"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val titleExtraText = intent?.getStringExtra(TASK_TITLE_EXTRA)
        val notificationIdExtra = intent?.getIntExtra(TASK_NOTIFICATION_ID_EXTRA,-1) ?: -1
        if (titleExtraText != null) {
            showNotification(context, titleExtraText, notificationIdExtra)
        }
    }

    @SuppressLint("MissingPermission", "ResourceAsColor")
    private fun showNotification(context: Context, title: String, notificationIdExtra: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context,MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent
            .getActivity(context,notificationIdExtra, intent, PendingIntent.FLAG_IMMUTABLE)

        val intentForMarkAsDoneAction = Intent(context,TaskAlarmNotificationReceiver::class.java).apply {
            putExtra(TASK_NOTIFICATION_ID_EXTRA, notificationIdExtra)
            action = ON_MARK_AS_DONE_ACTION
        }
        val pendingIntentMarkAsDoneAction = PendingIntent
            .getBroadcast(context,notificationIdExtra, intentForMarkAsDoneAction, PendingIntent.FLAG_IMMUTABLE)

        val intentForDismissAction = Intent(context,TaskAlarmNotificationReceiver::class.java).apply {
            putExtra(TASK_NOTIFICATION_ID_EXTRA, notificationIdExtra)
            action = ON_DISMISS_ACTION
        }
        val pendingIntentDismissAction = PendingIntent
            .getBroadcast(context,notificationIdExtra, intentForDismissAction, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.your_reminder_text))
            .setContentText(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(title))
            .addAction(0,context.getString(R.string.mark_as_done_text), pendingIntentMarkAsDoneAction)
            .addAction(0, context.getString(R.string.dismiss_text),pendingIntentDismissAction)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(R.color.md_theme_primary)
            .setOngoing(true)
            .build()
        if (hasNotificationPermission(context)) {
            notificationManager.notify(notificationIdExtra, notification)
        }
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