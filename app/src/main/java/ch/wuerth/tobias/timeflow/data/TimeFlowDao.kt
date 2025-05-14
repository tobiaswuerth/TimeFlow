package ch.wuerth.tobias.timeflow.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeFlowDao {
    @Query("SELECT * FROM timeflow_items ORDER BY fromDateTime ASC")
    fun getAllTimeFlows(): Flow<List<TimeFlowItem>>

    @Query("SELECT * FROM timeflow_items WHERE id = :id")
    suspend fun getTimeFlowById(id: Long): TimeFlowItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeFlow(timeFlowItem: TimeFlowItem): Long

    @Update
    suspend fun updateTimeFlow(timeFlowItem: TimeFlowItem)

    @Delete
    suspend fun deleteTimeFlow(timeFlowItem: TimeFlowItem)
}
