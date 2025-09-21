package com.example.todo_app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull // firstOrNull をインポート
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoViewModel(application: Application) : AndroidViewModel(application) {

    private val taskDao = AppDatabase.getDatabase(application).taskDao()

    // 現在のフィルター状態を保持するStateFlow
    private val _currentFilter = MutableStateFlow(FilterType.ALL)
    val currentFilter: StateFlow<FilterType> = _currentFilter.asStateFlow()

    // フィルターされたタスクリストを公開するStateFlow
    val tasks: StateFlow<List<Task>> = taskDao.getAllTasks()
        .combine(_currentFilter) { allTasks, filter -> // combineでタスクリストとフィルターを組み合わせる
            when (filter) {
                FilterType.ALL -> allTasks
                FilterType.ACTIVE -> allTasks.filter { !it.isCompleted }
                FilterType.COMPLETED -> allTasks.filter { it.isCompleted }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun setFilter(filterType: FilterType) {
        _currentFilter.value = filterType
    }

    fun addTask(title: String) {
        viewModelScope.launch {
            val newTask = Task(title = title)
            taskDao.insertTask(newTask)
        }
    }

    fun toggleTaskCompletion(taskId: Int) {
        viewModelScope.launch {
            // DAOから現在の全タスクリストを一度だけ取得して処理する
            val allTasksList = taskDao.getAllTasks().firstOrNull() ?: emptyList()
            val taskToUpdate = allTasksList.find { it.id == taskId }
            taskToUpdate?.let {
                taskDao.updateTask(it.copy(isCompleted = !it.isCompleted))
            }
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            // DAOから現在の全タスクリストを一度だけ取得して処理する
            val allTasksList = taskDao.getAllTasks().firstOrNull() ?: emptyList()
            val taskToDelete = allTasksList.find { it.id == taskId }
            taskToDelete?.let {
                taskDao.deleteTask(it)
            }
        }
    }
}
