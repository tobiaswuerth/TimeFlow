package ch.wuerth.tobias.timeflow.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.wuerth.tobias.timeflow.data.TimeFlowItem
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun TimeFlowItemCard(
    timeFlowItem: TimeFlowItem,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = remember(timeFlowItem) { timeFlowItem.getProgress() }
    val fromDateTime = remember(timeFlowItem) {
        timeFlowItem.fromDateTime.toLocalDateTime(TimeZone.currentSystemDefault())
    }
    val toDateTime = remember(timeFlowItem) {
        timeFlowItem.toDateTime.toLocalDateTime(TimeZone.currentSystemDefault())
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(timeFlowItem.color).copy(
                alpha = when {
                    timeFlowItem.isActive() -> 1.0f
                    timeFlowItem.isPast() -> 0.6f
                    else -> 0.8f
                }
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = timeFlowItem.title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "From: ${fromDateTime.date} ${fromDateTime.time}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "To: ${toDateTime.date} ${toDateTime.time}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    timeFlowItem.isPast() -> MaterialTheme.colorScheme.outline
                    else -> MaterialTheme.colorScheme.primary
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}
