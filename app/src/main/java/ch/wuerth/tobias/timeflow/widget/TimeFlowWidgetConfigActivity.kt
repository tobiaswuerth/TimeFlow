package ch.wuerth.tobias.timeflow.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ch.wuerth.tobias.timeflow.TimeFlowApplication
import ch.wuerth.tobias.timeflow.ui.screens.WidgetConfigScreen
import ch.wuerth.tobias.timeflow.ui.theme.TimeFlowTheme

class TimeFlowWidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set the result to CANCELED in case the user backs out
        setResult(RESULT_CANCELED)
        
        // Get the widget ID from the intent
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        
        val repository = (application as TimeFlowApplication).repository
        
        setContent {
            TimeFlowTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val timeFlows by repository.allTimeFlows.collectAsState(initial = emptyList())
                    
                    WidgetConfigScreen(
                        timeFlows = timeFlows,                        onTimeFlowSelected = { selectedTimeFlow ->
                            // Save the selected TimeFlow ID for the widget in SharedPreferences
                            TimeFlowWidgetReceiver.selectedTimeFlowId = selectedTimeFlow.id
                            TimeFlowWidgetReceiver.saveTimeFlowPref(
                                this@TimeFlowWidgetConfigActivity,
                                appWidgetId,
                                selectedTimeFlow.id
                            )
                            
                            // Request a widget update
                            val appWidgetManager = AppWidgetManager.getInstance(this)
                            
                            // Trigger a widget update
                            val widgetProvider = TimeFlowWidgetReceiver()
                            widgetProvider.onUpdate(
                                this@TimeFlowWidgetConfigActivity,
                                appWidgetManager,
                                intArrayOf(appWidgetId)
                            )
                            
                            // Set the result and finish
                            val resultValue = Intent().apply {
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            }
                            setResult(RESULT_OK, resultValue)
                            finish()
                        },
                        onNavigateBack = { finish() }
                    )
                }
            }
        }
    }
}
