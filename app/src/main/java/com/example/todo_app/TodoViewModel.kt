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

    // addTaskメソッドにdeadlineパラメータを追加
    fun addTask(title: String, deadline: Long? = null) {
        viewModelScope.launch {
            // deadline を持つ Task オブジェクトを作成
            val newTask = Task(title = title, deadline = deadline)
            taskDao.insertTask(newTask)
        }
    }

    fun toggleTaskCompletion(taskId: Int) {
        viewModelScope.launch {
            val allTasksList = taskDao.getAllTasks().firstOrNull() ?: emptyList()
            val taskToUpdate = allTasksList.find { it.id == taskId }
            taskToUpdate?.let {
                val newCompletionState = !it.isCompleted
                val newCompletionDate = if (newCompletionState) {
                    System.currentTimeMillis() // 完了したら現在時刻を設定
                } else {
                    null // 未完了に戻したらnullを設定
                }
                taskDao.updateTask(
                    it.copy(
                        isCompleted = newCompletionState,
                        completionDate = newCompletionDate // completionDateを更新
                    )
                )
            }
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            val allTasksList = taskDao.getAllTasks().firstOrNull() ?: emptyList()
            val taskToDelete = allTasksList.find { it.id == taskId }
            taskToDelete?.let {
                taskDao.deleteTask(it)
            }
        }
    }
}
