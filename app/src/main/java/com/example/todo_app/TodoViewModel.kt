package com.example.todo_app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoViewModel(application: Application) : AndroidViewModel(application) {

    private val taskDao = AppDatabase.getDatabase(application).taskDao()

    private val _currentFilter = MutableStateFlow(FilterType.ALL)
    val currentFilter: StateFlow<FilterType> = _currentFilter.asStateFlow()

    val tasks: StateFlow<List<Task>> = taskDao.getAllTasks()
        .combine(_currentFilter) { allTasks, filter ->
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

    fun addTask(title: String, deadline: Long? = null) {
        viewModelScope.launch {
            val newTask = Task(title = title, deadline = deadline)
            taskDao.insertTask(newTask)
        }
    }

    fun toggleTaskCompletion(taskId: Int) {
        viewModelScope.launch {
            val taskToUpdate = taskDao.getAllTasks().firstOrNull()?.find { it.id == taskId }
            taskToUpdate?.let {
                val newCompletionState = !it.isCompleted
                val newCompletionDate = if (newCompletionState) {
                    System.currentTimeMillis()
                } else {
                    null
                }
                taskDao.updateTask(
                    it.copy(
                        isCompleted = newCompletionState,
                        completionDate = newCompletionDate
                    )
                )
            }
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            val taskToDelete = taskDao.getAllTasks().firstOrNull()?.find { it.id == taskId }
            taskToDelete?.let {
                taskDao.deleteTask(it)
            }
        }
    }

    // ★ タスクの詳細（タイトルと期限日）を更新するメソッドを追加
    fun updateTaskDetails(taskId: Int, newTitle: String, newDeadline: Long?) {
        viewModelScope.launch {
            val taskToUpdate = taskDao.getAllTasks().firstOrNull()?.find { it.id == taskId }
            taskToUpdate?.let {
                // isCompleted と completionDate は変更せずに、title と deadline のみ更新
                taskDao.updateTask(
                    it.copy(
                        title = newTitle,
                        deadline = newDeadline
                    )
                )
            }
        }
    }
}
