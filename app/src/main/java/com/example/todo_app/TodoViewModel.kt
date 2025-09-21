package com.example.todo_app

import android.app.Application // Applicationをインポート
import androidx.lifecycle.AndroidViewModel // AndroidViewModelをインポート
import androidx.lifecycle.viewModelScope // viewModelScopeをインポート
import kotlinx.coroutines.flow.SharingStarted // SharingStartedをインポート
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn // stateInをインポート
import kotlinx.coroutines.launch // launchをインポート

class TodoViewModel(application: Application) : AndroidViewModel(application) { // AndroidViewModelを継承

    // TaskDaoのインスタンスを取得
    private val taskDao = AppDatabase.getDatabase(application).taskDao()

    // データベースからタスクリストをFlowとして取得し、StateFlowに変換
    // UIがアクティブな間だけデータベースの監視を開始し、
    // 5秒間UIが非アクティブになったら監視を停止します。
    // 初期値は空のリストです。
    val tasks: StateFlow<List<Task>> = taskDao.getAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L), // 5秒間サブスクライブされなかったら停止
            initialValue = emptyList() // 初期値
        )

    fun addTask(title: String) {
        viewModelScope.launch {
            // idはRoomが自動生成するので、ここでは指定しない
            // isCompletedはTaskデータクラスのデフォルト値(false)が使われる
            val newTask = Task(title = title)
            taskDao.insertTask(newTask)
        }
    }

    fun toggleTaskCompletion(taskId: Int) {
        viewModelScope.launch {
            // 現在のStateFlowの値からタスクを探す
            val taskToUpdate = tasks.value.find { it.id == taskId }
            taskToUpdate?.let {
                // 見つかったタスクの完了状態を反転させて更新
                taskDao.updateTask(it.copy(isCompleted = !it.isCompleted))
            }
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            // 現在のStateFlowの値からタスクを探す
            val taskToDelete = tasks.value.find { it.id == taskId }
            taskToDelete?.let {
                // 見つかったタスクを削除
                taskDao.deleteTask(it)
            }
        }
    }
}
