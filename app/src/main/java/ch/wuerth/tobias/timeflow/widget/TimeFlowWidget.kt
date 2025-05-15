package ch.wuerth.tobias.timeflow.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.GradientDrawable
import android.widget.RemoteViews
import androidx.core.content.edit
import androidx.core.graphics.createBitmap
import ch.wuerth.tobias.timeflow.MainActivity
import ch.wuerth.tobias.timeflow.R
import ch.wuerth.tobias.timeflow.data.TimeFlowDatabase
import ch.wuerth.tobias.timeflow.data.TimeFlowRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import java.time.temporal.ChronoUnit

class TimeFlowWidgetReceiver : AppWidgetProvider() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    companion object {
        private const val PREFS_NAME = "ch.wuerth.tobias.timeflow.widget.TimeFlowWidgetReceiver"
        private const val PREF_PREFIX_KEY = "appwidget_"
        var selectedTimeFlowId: Long = -1

        internal fun saveTimeFlowPref(context: Context, appWidgetId: Int, timeFlowId: Long) {
            context.getSharedPreferences(PREFS_NAME, 0).edit {
                putLong(PREF_PREFIX_KEY + appWidgetId, timeFlowId)
            }
        }

        internal fun loadTimeFlowPref(context: Context, appWidgetId: Int): Long {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            return prefs.getLong(PREF_PREFIX_KEY + appWidgetId, -1)
        }

        internal fun deleteTimeFlowPref(context: Context, appWidgetId: Int) {
            context.getSharedPreferences(PREFS_NAME, 0).edit {
                remove(PREF_PREFIX_KEY + appWidgetId)
            }
        }

        internal fun deleteAllWidgetsForTimeFlowId(context: Context, timeFlowId: Long) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, TimeFlowWidgetReceiver::class.java)
            )

            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            val prefEditor = prefs.edit()
            val widgetsToUpdate = mutableListOf<Int>()

            // Find all widgets that use this TimeFlow ID
            for (appWidgetId in appWidgetIds) {
                val savedId = prefs.getLong(PREF_PREFIX_KEY + appWidgetId, -1)
                if (savedId == timeFlowId) {
                    // Reset this widget's preference
                    prefEditor.remove(PREF_PREFIX_KEY + appWidgetId)
                    widgetsToUpdate.add(appWidgetId)
                }
            }

            // Apply all preference changes
            prefEditor.apply()

            // For Android 13+, we can't programmatically remove widgets
            // Instead, we'll update them to show "No TimeFlow Selected" message
            if (widgetsToUpdate.isNotEmpty()) {
                val instance = TimeFlowWidgetReceiver()
                instance.onUpdate(
                    context,
                    appWidgetManager,
                    widgetsToUpdate.toIntArray()
                )
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update each of the widgets with the remote adapter
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the widget is deleted, delete its associated preferences
        for (appWidgetId in appWidgetIds) {
            deleteTimeFlowPref(context, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.glance_default_loading_layout)

        // Fetch data in a coroutine
        scope.launch {
            try {
                val repository =
                    TimeFlowRepository(TimeFlowDatabase.getDatabase(context).timeFlowDao())
                val timeFlows = repository.allTimeFlows.first()

                // Get the saved timeflow ID for this specific widget
                val savedTimeFlowId = loadTimeFlowPref(context, appWidgetId)

                val selectedTimeFlow = if (savedTimeFlowId >= 0) {
                    // Use the saved ID specific to this widget
                    timeFlows.find { it.id == savedTimeFlowId }
                } else if (selectedTimeFlowId >= 0) {
                    // Fallback to the temporary ID (for first-time setup)
                    timeFlows.find { it.id == selectedTimeFlowId }
                } else {
                    // Default fallback
                    timeFlows.find { it.isActive() } ?: timeFlows.firstOrNull()
                }
                // Update the widget view with the timeflow data
                val widgetView = RemoteViews(context.packageName, R.layout.timeflow_widget)

                // Make the widget clickable to open the app
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                widgetView.setOnClickPendingIntent(R.id.widget_root_layout, pendingIntent)
                if (selectedTimeFlow != null) {
                    widgetView.setTextViewText(R.id.widget_title, selectedTimeFlow.title)

                    // Set progress bar progress (0-100)
                    val progress = (selectedTimeFlow.getProgress() * 100).toInt()
                    widgetView.setProgressBar(
                        R.id.widget_progress,
                        100,
                        progress,
                        false
                    )                    // Calculate days left
                    val now = Clock.System.now()
                    val daysLeft = ChronoUnit.DAYS.between(
                        now.toJavaInstant(),
                        selectedTimeFlow.toDateTime.toJavaInstant()
                    ).toInt()

                    // Display percentage and days left separately
                    widgetView.setTextViewText(R.id.widget_percentage, "$progress%")

                    // Set days left text
                    val daysLeftText = if (daysLeft > 0) {
                        "${daysLeft}d left"
                    } else if (daysLeft == 0) {
                        "Today"
                    } else {
                        "done"
                    }
                    widgetView.setTextViewText(R.id.widget_days_left, daysLeftText)

                    // Set custom background color
                    val customBackgroundDrawable = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 6f * context.resources.displayMetrics.density
                        setColor(selectedTimeFlow.color)
                    }

                    val backgroundBitmap = createBitmap(1000, 300)
                    val canvas = Canvas(backgroundBitmap)
                    customBackgroundDrawable.setBounds(0, 0, canvas.width, canvas.height)
                    customBackgroundDrawable.draw(canvas)

                    widgetView.setImageViewBitmap(R.id.widget_background, backgroundBitmap)
                } else {
                    widgetView.setTextViewText(R.id.widget_title, "No TimeFlow Selected")
                    widgetView.setProgressBar(R.id.widget_progress, 100, 0, false)
                    widgetView.setTextViewText(R.id.widget_percentage, "")
                    widgetView.setTextViewText(R.id.widget_days_left, "")
                }

                // Update the widget
                appWidgetManager.updateAppWidget(appWidgetId, widgetView)
            } catch (_: Exception) {
                // If there's an error, update with default view
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        // Set initial loading view while we fetch the data
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
