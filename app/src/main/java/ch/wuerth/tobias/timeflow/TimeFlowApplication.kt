package ch.wuerth.tobias.timeflow

import android.app.Application
import ch.wuerth.tobias.timeflow.data.TimeFlowDatabase
import ch.wuerth.tobias.timeflow.data.TimeFlowRepository

class TimeFlowApplication : Application() {
    private val database by lazy { TimeFlowDatabase.getDatabase(this) }
    val repository by lazy { TimeFlowRepository(database.timeFlowDao()) }
}
