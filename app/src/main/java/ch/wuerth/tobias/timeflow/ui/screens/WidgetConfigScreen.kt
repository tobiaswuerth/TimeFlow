package ch.wuerth.tobias.timeflow.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.wuerth.tobias.timeflow.data.TimeFlowItem
import ch.wuerth.tobias.timeflow.ui.components.TimeFlowWidgetCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigScreen(
    timeFlows: List<TimeFlowItem>,
    onTimeFlowSelected: (TimeFlowItem) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select TimeFlow for Widget") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (timeFlows.isEmpty()) {
                Text(
                    text = "No TimeFlows available. Create one first!",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(timeFlows) { timeFlow ->
                        TimeFlowWidgetCard(
                            timeFlowItem = timeFlow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTimeFlowSelected(timeFlow) }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
