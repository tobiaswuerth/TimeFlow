package ch.wuerth.tobias.timeflow.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.wuerth.tobias.timeflow.data.TimeFlowItem
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime
import java.time.temporal.ChronoUnit

@Composable
fun TimeFlowWidgetCard(
    timeFlowItem: TimeFlowItem,
    modifier: Modifier = Modifier
) {
    val progress = remember(timeFlowItem) { timeFlowItem.getProgress() }
    remember(timeFlowItem) {
        timeFlowItem.fromDateTime.toLocalDateTime(TimeZone.currentSystemDefault())
    }
    remember(timeFlowItem) {
        timeFlowItem.toDateTime.toLocalDateTime(TimeZone.currentSystemDefault())
    }

    // Calculate days left
    val now = remember { Clock.System.now() }
    val daysLeft = remember(timeFlowItem) {
        ChronoUnit.DAYS.between(
            now.toJavaInstant(),
            timeFlowItem.toDateTime.toJavaInstant()
        ).toInt()
    }

    // Create display text
    val displayText = remember(progress, daysLeft) {
        val progressPercentage = (progress * 100).toInt()
        if (daysLeft > 0) {
            "$progressPercentage% (${daysLeft}d left)"
        } else {
            "$progressPercentage%"
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    timeFlowItem.isPast() -> MaterialTheme.colorScheme.outline
                    else -> MaterialTheme.colorScheme.primary
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeFlowItem.formatDateTime(timeFlowItem.fromDateTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = displayText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = timeFlowItem.formatDateTime(timeFlowItem.toDateTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                )
            }
        }
    }
}
