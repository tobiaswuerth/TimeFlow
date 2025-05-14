package ch.wuerth.tobias.timeflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.wuerth.tobias.timeflow.ui.screens.HomeScreen
import ch.wuerth.tobias.timeflow.ui.theme.TimeFlowTheme
import ch.wuerth.tobias.timeflow.ui.viewmodel.TimeFlowViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val repository = (application as TimeFlowApplication).repository
        
        setContent {
            TimeFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val timeFlowViewModel: TimeFlowViewModel = viewModel(
                        factory = TimeFlowViewModel.TimeFlowViewModelFactory(application, repository)
                    )
                    
                    val timeFlows by timeFlowViewModel.allTimeFlows.collectAsState()
                    
                    HomeScreen(
                        timeFlows = timeFlows,
                        onAddTimeFlow = { title, fromDateTime, toDateTime, color ->
                            timeFlowViewModel.insertTimeFlow(title, fromDateTime, toDateTime, color)
                        },
                        onEditTimeFlow = { timeFlowItem ->
                            timeFlowViewModel.updateTimeFlow(timeFlowItem)
                        },
                        onDeleteTimeFlow = { timeFlowItem ->
                            timeFlowViewModel.deleteTimeFlow(timeFlowItem)
                        }
                    )
                }
            }
        }
    }
}