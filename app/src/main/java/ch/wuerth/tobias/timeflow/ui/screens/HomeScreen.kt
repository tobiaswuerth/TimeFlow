package ch.wuerth.tobias.timeflow.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.wuerth.tobias.timeflow.R
import ch.wuerth.tobias.timeflow.data.TimeFlowItem
import ch.wuerth.tobias.timeflow.ui.components.TimeFlowDialog
import ch.wuerth.tobias.timeflow.ui.components.TimeFlowItemCard
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    timeFlows: List<TimeFlowItem>,
    onAddTimeFlow: (String, Instant, Instant) -> Unit,
    onEditTimeFlow: (TimeFlowItem) -> Unit,
    onDeleteTimeFlow: (TimeFlowItem) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTimeFlow by remember { mutableStateOf<TimeFlowItem?>(null) }
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TimeFlow") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add TimeFlow")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (timeFlows.isEmpty()) {
                Text(
                    text = "No TimeFlows yet. Create one by tapping the + button!",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {                    items(
                        items = timeFlows,
                        key = { it.id }
                    ) { timeFlow ->
                        TimeFlowItemCard(
                            timeFlowItem = timeFlow,
                            onLongClick = { selectedTimeFlow = timeFlow },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .combinedClickable(
                                    onClick = { },
                                    onLongClick = { selectedTimeFlow = timeFlow }
                                )
                        )
                    }
                }
            }
        }
    }
    
    // Add Dialog
    AnimatedVisibility(
        visible = showAddDialog,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        TimeFlowDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = onAddTimeFlow
        )
    }
    
    // Edit Dialog
    AnimatedVisibility(
        visible = selectedTimeFlow != null,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        selectedTimeFlow?.let { timeFlow ->
            TimeFlowDialog(
                timeFlowItem = timeFlow,
                onDismiss = { selectedTimeFlow = null },
                onConfirm = { title, fromDateTime, toDateTime ->
                    val updatedTimeFlow = timeFlow.copy(
                        title = title,
                        fromDateTime = fromDateTime,
                        toDateTime = toDateTime
                    )
                    onEditTimeFlow(updatedTimeFlow)
                }
            )
        }
    }
}
