package ch.wuerth.tobias.timeflow.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ch.wuerth.tobias.timeflow.data.TimeFlowItem
import ch.wuerth.tobias.timeflow.data.TimeFlowRepository
import ch.wuerth.tobias.timeflow.widget.TimeFlowWidgetReceiver
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

class TimeFlowViewModel(
    application: Application,
    private val repository: TimeFlowRepository
) : AndroidViewModel(application) {

    val allTimeFlows: StateFlow<List<TimeFlowItem>> = repository.allTimeFlows.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun insertTimeFlow(title: String, fromDateTime: Instant, toDateTime: Instant, color: Int) {
        val timeFlowItem = TimeFlowItem(
            title = title,
            fromDateTime = fromDateTime,
            toDateTime = toDateTime,
            color = color
        )
        viewModelScope.launch {
            repository.insertTimeFlow(timeFlowItem)
        }
    }

    fun updateTimeFlow(timeFlowItem: TimeFlowItem) {
        viewModelScope.launch {
            repository.updateTimeFlow(timeFlowItem)
        }
    }

    fun deleteTimeFlow(timeFlowItem: TimeFlowItem) {
        // Remove widgets associated with this TimeFlow first
        val context = getApplication<Application>().applicationContext
        TimeFlowWidgetReceiver.deleteAllWidgetsForTimeFlowId(context, timeFlowItem.id)

        // Then delete the TimeFlow from database
        viewModelScope.launch {
            repository.deleteTimeFlow(timeFlowItem)
        }
    }

    class TimeFlowViewModelFactory(
        private val application: Application,
        private val repository: TimeFlowRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TimeFlowViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TimeFlowViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
