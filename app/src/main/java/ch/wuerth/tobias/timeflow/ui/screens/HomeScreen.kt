package ch.wuerth.tobias.timeflow.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
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
import androidx.compose.ui.zIndex
import ch.wuerth.tobias.timeflow.R
import ch.wuerth.tobias.timeflow.data.TimeFlowItem
import ch.wuerth.tobias.timeflow.ui.components.TimeFlowDialog
import ch.wuerth.tobias.timeflow.ui.components.TimeFlowWidgetCard
import kotlinx.datetime.Instant

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    androidx.compose.material.ExperimentalMaterialApi::class
)
@Composable
fun HomeScreen(
    timeFlows: List<TimeFlowItem>,
    onAddTimeFlow: (String, Instant, Instant) -> Unit,
    onEditTimeFlow: (TimeFlowItem) -> Unit,
    onDeleteTimeFlow: (TimeFlowItem) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTimeFlow by remember { mutableStateOf<TimeFlowItem?>(null) }
    rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = androidx.compose.ui.graphics.Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = androidx.compose.ui.graphics.Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_timeflow))
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
                    text = stringResource(R.string.no_timeflows_yet),
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = timeFlows,
                        key = { it.id }
                    ) { timeFlow ->
                        Spacer(modifier = Modifier.height(8.dp))

                        val dismissState = rememberDismissState(
                            confirmStateChange = { dismissValue ->
                                if (dismissValue == DismissValue.DismissedToStart ||
                                    dismissValue == DismissValue.DismissedToEnd
                                ) {
                                    // Delete the item when fully swiped
                                    onDeleteTimeFlow(timeFlow)
                                    true
                                } else {
                                    false
                                }
                            }
                        )

                        SwipeToDismiss(
                            state = dismissState,
                            directions = setOf(
                                DismissDirection.StartToEnd,
                                DismissDirection.EndToStart
                            ),
                            background = {
                                val color = MaterialTheme.colorScheme.error
                                val alignment = Alignment.CenterEnd

                                // Only show the background when actually being dismissed
                                if (dismissState.dismissDirection != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(color, RoundedCornerShape(12.dp))
                                            .zIndex(-1f),
                                        contentAlignment = alignment
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.delete),
                                            tint = androidx.compose.ui.graphics.Color.White,
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )
                                    }
                                }
                            },
                            dismissContent = {
                                TimeFlowWidgetCard(
                                    timeFlowItem = timeFlow,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = { },
                                            onLongClick = { selectedTimeFlow = timeFlow }
                                        )
                                )
                            }
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
