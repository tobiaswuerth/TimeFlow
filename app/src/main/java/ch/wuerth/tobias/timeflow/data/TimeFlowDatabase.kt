package ch.wuerth.tobias.timeflow.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.datetime.Instant

@Database(entities = [TimeFlowItem::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TimeFlowDatabase : RoomDatabase() {
    
    abstract fun timeFlowDao(): TimeFlowDao
    
    companion object {
        @Volatile
        private var INSTANCE: TimeFlowDatabase? = null
        
        fun getDatabase(context: Context): TimeFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TimeFlowDatabase::class.java,
                    "timeflow_database"
                )
                    .fallbackToDestructiveMigration(false)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.fromEpochMilliseconds(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilliseconds()
    }
}
