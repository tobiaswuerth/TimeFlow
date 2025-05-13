package ch.wuerth.tobias.timeflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ch.wuerth.tobias.timeflow.data.TimeFlowItem
import ch.wuerth.tobias.timeflow.data.TimeFlowRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class TimeFlowViewModel(private val repository: TimeFlowRepository) : ViewModel() {
    
    val allTimeFlows: StateFlow<List<TimeFlowItem>> = repository.allTimeFlows.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    
    fun insertTimeFlow(title: String, fromDateTime: Instant, toDateTime: Instant) {
        val timeFlowItem = TimeFlowItem(
            title = title,
            fromDateTime = fromDateTime,
            toDateTime = toDateTime
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
        viewModelScope.launch {
            repository.deleteTimeFlow(timeFlowItem)
        }
    }
    
    class TimeFlowViewModelFactory(private val repository: TimeFlowRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TimeFlowViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TimeFlowViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
