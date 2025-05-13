package ch.wuerth.tobias.timeflow.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Entity(tableName = "timeflow_items")
data class TimeFlowItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val fromDateTime: Instant,
    val toDateTime: Instant,
) {
    fun getProgress(): Float {
        val now = Clock.System.now()
        if (now < fromDateTime) return 0f
        if (now > toDateTime) return 1f
        
        val total = toDateTime.toEpochMilliseconds() - fromDateTime.toEpochMilliseconds()
        val elapsed = now.toEpochMilliseconds() - fromDateTime.toEpochMilliseconds()
        
        return (elapsed.toFloat() / total).coerceIn(0f, 1f)
    }
    
    fun isActive(): Boolean {
        val now = Clock.System.now()
        return now in fromDateTime..toDateTime
    }
    
    fun isPast(): Boolean {
        return Clock.System.now() > toDateTime
    }
    
    fun isFuture(): Boolean {
        return Clock.System.now() < fromDateTime
    }
    
    fun formatDateTime(instant: Instant): String {
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${localDateTime.date} ${localDateTime.time}"
    }
}
