package ch.wuerth.tobias.timeflow.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.RemoteViews
import ch.wuerth.tobias.timeflow.MainActivity
import ch.wuerth.tobias.timeflow.R
import ch.wuerth.tobias.timeflow.TimeFlowApplication
import ch.wuerth.tobias.timeflow.data.TimeFlowItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class StackedTimeFlowWidgetReceiver : AppWidgetProvider() {

    companion object {
        private const val MAX_ITEMS_TO_SHOW = 5

        fun updateWidgets(context: Context) {
            val intent = Intent(context, StackedTimeFlowWidgetReceiver::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, StackedTimeFlowWidgetReceiver::class.java))
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.timeflow_stacked_widget)
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widget_root_layout, pendingIntent)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = (context.applicationContext as TimeFlowApplication).repository
                val timeFlows = repository.allTimeFlows.first()
                val sortedTimeFlows = timeFlows.sortedBy { item -> calculateDaysLeft(item) }
                val limitedTimeFlows = sortedTimeFlows.take(MAX_ITEMS_TO_SHOW)

                withContext(Dispatchers.Main) {
                    views.removeAllViews(R.id.timeflow_container)

                    if (limitedTimeFlows.isEmpty()) {
                        val emptyView = RemoteViews(context.packageName, R.layout.timeflow_item)
                        emptyView.setTextViewText(R.id.item_title, "No TimeFlows available")
                        emptyView.setTextViewText(R.id.item_date, "")
                        emptyView.setTextViewText(R.id.item_percentage, "")
                        emptyView.setTextViewText(R.id.item_days_left, "")
                        emptyView.setProgressBar(R.id.item_progress, 100, 0, false)
                        views.addView(R.id.timeflow_container, emptyView)
                    } else {
                        for (timeFlow in limitedTimeFlows) {
                            val itemView = createTimeFlowItemView(context, timeFlow)
                            views.addView(R.id.timeflow_container, itemView)
                        }
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (e: Exception) {
                e.printStackTrace()

                Handler(Looper.getMainLooper()).post {
                    val errorView = RemoteViews(context.packageName, R.layout.timeflow_item)
                    errorView.setTextViewText(R.id.item_title, "Error loading TimeFlows")
                    errorView.setTextViewText(R.id.item_date, "")
                    errorView.setTextViewText(R.id.item_percentage, "")
                    errorView.setTextViewText(R.id.item_days_left, "")
                    errorView.setProgressBar(R.id.item_progress, 100, 0, false)

                    views.removeAllViews(R.id.timeflow_container)
                    views.addView(R.id.timeflow_container, errorView)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }

    private fun createTimeFlowItemView(context: Context, timeFlow: TimeFlowItem): RemoteViews {
        val itemView = RemoteViews(context.packageName, R.layout.timeflow_item)
        val percentage = calculateTimeFlowPercentage(timeFlow)
        val daysLeft = calculateDaysLeft(timeFlow)
        val formattedEndDate = timeFlow.formatDateTime(timeFlow.toDateTime)

        itemView.setTextViewText(R.id.item_title, timeFlow.title)
        itemView.setTextViewText(R.id.item_date, formattedEndDate)
        itemView.setTextViewText(R.id.item_percentage, "${(percentage * 100).roundToInt()}%")
        itemView.setTextViewText(
            R.id.item_days_left,
            "$daysLeft ${if (daysLeft == 1) "day" else "days"} left"
        )
        itemView.setProgressBar(R.id.item_progress, 100, (percentage * 100).roundToInt(), false)

        return itemView
    }

    private fun calculateTimeFlowPercentage(timeFlow: TimeFlowItem): Float {
        return timeFlow.getProgress()
    }

    private fun calculateDaysLeft(timeFlow: TimeFlowItem): Int {
        val now = Clock.System.now()
        if (now > timeFlow.toDateTime) return 0

        val diff = timeFlow.toDateTime.toEpochMilliseconds() - now.toEpochMilliseconds()
        return TimeUnit.MILLISECONDS.toDays(diff).toInt() + 1
    }
}
