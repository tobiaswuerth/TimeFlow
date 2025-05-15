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
        timeFlowItem: TimeFlowItem
    ) {
        timeFlowDao.deleteTimeFlow(timeFlowItem)
    }
}
