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

/**
 * TodoアプリのUIロジックを担当するViewModel。
 * UI (ActivityやComposable) からのイベントを処理し、必要なデータをUIに提供する。
 * データベース (Room) とのやり取りもこのクラスが仲介する。
 * AndroidViewModelを継承しているため、Applicationコンテキストにアクセスできる。
 */
class TodoViewModel(application: Application) : AndroidViewModel(application) {

    // AppDatabaseからTaskDaoのインスタンスを取得。
    // TaskDaoを通じてデータベース操作を行う。
    private val taskDao = AppDatabase.getDatabase(application).taskDao()

    // 現在選択されているフィルターの種類を保持する。
    // MutableStateFlowは変更可能なStateFlowで、ViewModel内部でのみ値を変更する。
    // 初期値は FilterType.ALL (すべてのタスクを表示)。
    private val _currentFilter = MutableStateFlow(FilterType.ALL)
    // UI (Composable) に公開する読み取り専用のStateFlow。
    // _currentFilterの変更をUIがリアクティブに監視できるようにする。
    val currentFilter: StateFlow<FilterType> = _currentFilter.asStateFlow()

    // UIに表示するタスクのリストを保持するStateFlow。
    // Roomから取得した全タスクリスト (_taskDao.getAllTasks()) と
    // 現在のフィルター (_currentFilter) を `combine` して、
    // フィルター適用後のタスクリストを生成する。
    val tasks: StateFlow<List<Task>> = taskDao.getAllTasks() // Roomから全タスクをFlowとして取得
        .combine(_currentFilter) { allTasks, filter -> // 全タスクリストと現在のフィルターを組み合わせる
            // フィルターの種類に応じてタスクを絞り込む
            when (filter) {
                FilterType.ALL -> allTasks // 「すべて」なら全てのタスクを返す
                FilterType.ACTIVE -> allTasks.filter { !it.isCompleted } // 「未完了」なら未完了タスクのみ
                FilterType.COMPLETED -> allTasks.filter { it.isCompleted } // 「完了済」なら完了タスクのみ
            }
        }
        // stateIn を使って、FlowをStateFlowに変換する。
        // viewModelScope: このViewModelのライフサイクルと連動するコルーチンスコープ。
        // SharingStarted.WhileSubscribed(5000L): UIがこのStateFlowを監視している間だけFlowをアクティブにし、
        //                                         監視が5秒間なくなったらFlowを停止する。(リソース節約)
        // initialValue: Flowが最初に発行するまでの初期値。
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    /**
     * 表示するタスクのフィルター種類を設定する。
     * @param filterType 設定するフィルターの種類 (ALL, ACTIVE, COMPLETED)。
     */
    fun setFilter(filterType: FilterType) {
        // _currentFilterの値を更新することで、`tasks` StateFlowも自動的に再計算される。
        _currentFilter.value = filterType
    }

    /**
     * 新しいタスクをデータベースに追加する。
     * @param title タスクのタイトル。
     * @param deadline タスクの期限日 (タイムスタンプ形式、null許容)。
     */
    fun addTask(title: String, deadline: Long? = null) {
        // viewModelScope.launch はViewModelのライフサイクルで管理されるコルーチンを開始する。
        // データベース操作などの時間のかかる可能性のある処理は、メインスレッドをブロックしないようにコルーチンで行う。
        viewModelScope.launch {
            val newTask = Task(title = title, deadline = deadline) // 新しいTaskオブジェクトを作成
            taskDao.insertTask(newTask) // 作成したタスクをデータベースに挿入
        }
    }

    /**
     * 指定されたIDのタスクの完了状態を切り替える。
     * 完了なら未完了に、未完了なら完了にし、完了日も更新する。
     * @param taskId 完了状態を切り替えるタスクのID。
     */
    fun toggleTaskCompletion(taskId: Int) {
        viewModelScope.launch {
            // taskDao.getAllTasks().firstOrNull() で現在の全タスクリストを取得し、
            // find { it.id == taskId } で該当するタスクを探す。
            val taskToUpdate = taskDao.getAllTasks().firstOrNull()?.find { it.id == taskId }
            // taskToUpdate が null でない (該当タスクが見つかった) 場合のみ処理を実行
            taskToUpdate?.let {
                val newCompletionState = !it.isCompleted // 現在の完了状態を反転させる
                // 新しい完了状態に応じて完了日を設定
                val newCompletionDate = if (newCompletionState) {
                    System.currentTimeMillis() // 完了状態になったら、現在時刻を完了日として設定
                } else {
                    null // 未完了状態に戻したら、完了日をnullに設定
                }
                // taskDao.updateTask() でデータベース内のタスクを更新。
                // it.copy() を使って、元のタスクオブジェクトの一部プロパティのみを変更した新しいオブジェクトを作成。
                taskDao.updateTask(
                    it.copy(
                        isCompleted = newCompletionState,
                        completionDate = newCompletionDate
                    )
                )
            }
        }
    }

    /**
     * 指定されたIDのタスクをデータベースから削除する。
     * @param taskId 削除するタスクのID。
     */
    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            val taskToDelete = taskDao.getAllTasks().firstOrNull()?.find { it.id == taskId }
            taskToDelete?.let {
                taskDao.deleteTask(it) // 該当タスクをデータベースから削除
            }
        }
    }

    /**
     * 指定されたIDのタスクのタイトルと期限日を更新する。
     * 完了状態や完了日はこのメソッドでは変更しない。
     * @param taskId 更新するタスクのID。
     * @param newTitle 新しいタスクのタイトル。
     * @param newDeadline 新しい期限日 (タイムスタンプ形式、null許容)。
     */
    fun updateTaskDetails(taskId: Int, newTitle: String, newDeadline: Long?) {
        viewModelScope.launch {
            val taskToUpdate = taskDao.getAllTasks().firstOrNull()?.find { it.id == taskId }
            taskToUpdate?.let {
                taskDao.updateTask(
                    it.copy(
                        title = newTitle, // タイトルを新しい値で更新
                        deadline = newDeadline // 期限日を新しい値で更新
                    )
                )
            }
        }
    }
}
