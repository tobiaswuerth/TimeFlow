package ch.wuerth.tobias.timeflow.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import ch.wuerth.tobias.timeflow.R
import ch.wuerth.tobias.timeflow.data.TimeFlowDatabase
import ch.wuerth.tobias.timeflow.data.TimeFlowRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import java.time.temporal.ChronoUnit

class TimeFlowWidgetReceiver : AppWidgetProvider() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    
    companion object {
        var selectedTimeFlowId: Long = -1
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Update each of the widgets with the remote adapter
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.glance_default_loading_layout)
        
        // Fetch data in a coroutine
        scope.launch {
            try {
                val repository = TimeFlowRepository(TimeFlowDatabase.getDatabase(context).timeFlowDao())
                val timeFlows = repository.allTimeFlows.first()
                
                val selectedTimeFlow = if (selectedTimeFlowId >= 0) {
                    timeFlows.find { it.id == selectedTimeFlowId }
                } else {
                    timeFlows.find { it.isActive() } ?: timeFlows.firstOrNull()
                }
                
                // Update the widget view with the timeflow data
                val widgetView = RemoteViews(context.packageName, R.layout.timeflow_widget)
                
                if (selectedTimeFlow != null) {
                    widgetView.setTextViewText(R.id.widget_title, selectedTimeFlow.title)
                    widgetView.setTextViewText(R.id.widget_date_from,
                        selectedTimeFlow.formatDateTime(selectedTimeFlow.fromDateTime)
                    )
                    widgetView.setTextViewText(R.id.widget_date_to,
                        selectedTimeFlow.formatDateTime(selectedTimeFlow.toDateTime)
                    )
                    
                    // Set progress bar progress (0-100)
                    val progress = (selectedTimeFlow.getProgress() * 100).toInt()
                    widgetView.setProgressBar(R.id.widget_progress, 100, progress, false)
                    
                    // Calculate days left
                    val now = Clock.System.now()
                    val daysLeft = ChronoUnit.DAYS.between(
                        now.toJavaInstant(),
                        selectedTimeFlow.toDateTime.toJavaInstant()
                    ).toInt()
                    
                    // Display percentage and days left
                    val displayText = if (daysLeft > 0) {
                        "$progress% (${daysLeft}d left)"
                    } else {
                        "$progress%"
                    }
                    widgetView.setTextViewText(R.id.widget_percentage, displayText)
                } else {
                    widgetView.setTextViewText(R.id.widget_title, "No TimeFlow Selected")
                    widgetView.setTextViewText(R.id.widget_date_from, "N/A")
                    widgetView.setTextViewText(R.id.widget_date_to, "N/A")
                    widgetView.setProgressBar(R.id.widget_progress, 100, 0, false)
                    widgetView.setTextViewText(R.id.widget_percentage, "")
                }
                
                // Update the widget
                appWidgetManager.updateAppWidget(appWidgetId, widgetView)
            } catch (e: Exception) {
                // If there's an error, update with default view
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
        
        // Set initial loading view while we fetch the data
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
