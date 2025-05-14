package ch.wuerth.tobias.timeflow.data

import kotlinx.coroutines.flow.Flow

class TimeFlowRepository(private val timeFlowDao: TimeFlowDao) {

    val allTimeFlows: Flow<List<TimeFlowItem>> = timeFlowDao.getAllTimeFlows()

    suspend fun insertTimeFlow(timeFlowItem: TimeFlowItem): Long {
        return timeFlowDao.insertTimeFlow(timeFlowItem)
    }

    suspend fun updateTimeFlow(timeFlowItem: TimeFlowItem) {
        timeFlowDao.updateTimeFlow(timeFlowItem)
    }

    suspend fun deleteTimeFlow(
        timeFlowItem: TimeFlowItem,
        context: android.content.Context? = null
    ) {
        timeFlowDao.deleteTimeFlow(timeFlowItem)

        // Remove any widgets associated with this TimeFlow
        context?.let {
            ch.wuerth.tobias.timeflow.widget.TimeFlowWidgetReceiver.deleteAllWidgetsForTimeFlowId(
                it,
                timeFlowItem.id
            )
        }
    }
}
