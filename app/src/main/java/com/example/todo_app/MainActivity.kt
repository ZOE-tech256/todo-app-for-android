package com.example.todo_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable // Cardのクリックイベント用
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape // 角丸用
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle // 完了済みアイコン
import androidx.compose.material.icons.filled.DateRange // DateRange アイコン用
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit // 編集アイコン用
import androidx.compose.material.icons.outlined.RadioButtonUnchecked // 未完了アイコン
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button // AddTaskDialog で使用
import androidx.compose.material3.Card // Cardをインポート
import androidx.compose.material3.CardDefaults // CardDefaultsをインポート
import androidx.compose.material3.CenterAlignedTopAppBar // CenterAlignedTopAppBar をインポート
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab // Tab をインポート
import androidx.compose.material3.TabRow // TabRow をインポート
import androidx.compose.material3.TabRowDefaults // TabRowDefaults をインポート
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset // tabIndicatorOffset をインポート
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton // AddTaskDialog で使用
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration // Textの取り消し線に使う可能性がまだあるので残す
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todo_app.ui.theme.Todo_AppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Long型のタイムスタンプを "yyyy/MM/dd" 形式の文字列に変換する拡張関数。
 * @return フォーマットされた日付文字列
 */
fun Long.toFormattedDateString(): String {
    // SimpleDateFormat は日付や時刻のフォーマット（書式設定）を行うクラス
    // "yyyy/MM/dd" は西暦4桁/月2桁/日2桁の形式を指定
    // Locale.getDefault() はユーザーの地域の標準的な書式を使用する設定
    val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    // Date(this) で Long型のタイムスタンプ (ミリ秒) を Dateオブジェクトに変換し、フォーマットする
    return sdf.format(Date(this))
}

/**
 * このアプリのメインアクティビティ。
 * アプリが起動したときに最初に呼び出される画面。
 */
class MainActivity : ComponentActivity() {
    /**
     * Activityが作成されたときに呼び出されるメソッド。
     * ここで画面の初期設定やUIの描画を行う。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge() は画面全体にUIを表示できるようにする設定 (ステータスバーなども含めて描画領域とする)
        enableEdgeToEdge()
        // setContent で、このActivityの画面内容を Composable関数で定義する
        setContent {
            // Todo_AppTheme はアプリ全体のデザインテーマを定義している (色、フォントなど)
            Todo_AppTheme {
                // TodoScreen Composable を呼び出して、Todoアプリのメイン画面を表示する
                TodoScreen()
            }
        }
    }
}

/**
 * タスクをフィルタリングするためのタブを表示するComposable関数。
 * 「すべて」「未完了」「完了済」のタブを提供する。
 *
 * @param currentFilter 現在選択されているフィルターの種類。
 * @param onFilterSelected フィルターが選択されたときに呼び出されるコールバック関数。
 * @param modifier このComposableの見た目や動作をカスタマイズするためのModifier。
 */
@Composable
fun TaskFilterTabs(
    currentFilter: FilterType, // 現在のフィルター状態 (例: ALL, ACTIVE, COMPLETED)
    onFilterSelected: (FilterType) -> Unit, // タブがクリックされたときの処理
    modifier: Modifier = Modifier // UIの装飾やレイアウト調整用
) {
    // FilterType enum からすべてのフィルタータイプを取得 (ALL, ACTIVE, COMPLETED)
    val filters = FilterType.values()
    // 現在選択されているタブのインデックス (順番) を取得
    val selectedTabIndex = currentFilter.ordinal

    // TabRow はタブの行を表示するためのComposable
    TabRow(
        selectedTabIndex = selectedTabIndex, // 現在選択中のタブを指定
        modifier = modifier.fillMaxWidth(), // 横幅いっぱいに表示
        containerColor = MaterialTheme.colorScheme.surface, // 背景色をテーマの表面色に設定
        // indicator は選択中のタブを示す下線
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]), // 選択中タブの位置にインジケータを配置
                color = MaterialTheme.colorScheme.primary // インジケータの色をテーマのプライマリ色に設定
            )
        }
    ) {
        // 各フィルタータイプに対してタブを作成
        filters.forEachIndexed { index, filterType ->
            // タブに表示するテキストを決定
            val tabText = when (filterType) {
                FilterType.ALL -> "すべて"
                FilterType.ACTIVE -> "未完了"
                FilterType.COMPLETED -> "完了済"
            }
            // Tab は個々のタブアイテムを表示するComposable
            Tab(
                selected = selectedTabIndex == index, // このタブが選択中かどうか
                onClick = { onFilterSelected(filterType) }, // クリックされたら onFilterSelected を呼び出す
                text = { // タブに表示するテキスト内容
                    Text(
                        text = tabText,
                        // 選択されていればプライマリ色、そうでなければ通常の色
                        color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }
}

/**
 * Todoアプリのメイン画面を表示するComposable関数。
 * 上部バー、タスクフィルタータブ、タスク一覧、追加/編集ダイアログなどを管理する。
 *
 * @param modifier このComposableの見た目や動作をカスタマイズするためのModifier。
 * @param todoViewModel Todoタスクのデータを管理するViewModel。
 */
@OptIn(ExperimentalMaterial3Api::class) // Experimental API (実験的な機能) を使用することを示すアノテーション
@Composable
fun TodoScreen(
    modifier: Modifier = Modifier, // UIの装飾やレイアウト調整用
    // viewModel() で TodoViewModel のインスタンスを取得。画面回転などがあっても状態が保持される。
    todoViewModel: TodoViewModel = viewModel()
) {
    // ViewModelからタスク一覧を監視し、変更があればUIを再描画する
    // collectAsStateWithLifecycle はライフサイクルを考慮してStateFlowから状態を安全に収集する
    val tasks by todoViewModel.tasks.collectAsStateWithLifecycle()
    // ViewModelから現在のフィルター状態を監視
    val currentFilter by todoViewModel.currentFilter.collectAsStateWithLifecycle()

    // remember はComposableの状態を記憶するための関数。mutableStateOf で変更可能な状態を作成。
    // タスク追加ダイアログを表示するかどうかの状態 (trueなら表示)
    var showAddTaskDialog by remember { mutableStateOf(false) }
    // タスク編集ダイアログを表示するかどうかの状態 (trueなら表示)
    var showEditTaskDialog by remember { mutableStateOf(false) }
    // 編集対象のタスクを保持する状態 (nullの場合は編集対象なし)
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    // Scaffold はマテリアルデザインの基本的な画面構造（トップバー、コンテンツエリアなど）を提供するComposable
    Scaffold(
        modifier = modifier.fillMaxSize(), // 画面全体に表示
        topBar = { // 画面上部のアプリケーションバー
            CenterAlignedTopAppBar(
                title = { Text("Todoアプリ") }, // 中央揃えのタイトル
                actions = { // 右側に配置されるアクションボタン
                    IconButton(onClick = { // アイコンボタンがクリックされたときの処理
                        taskToEdit = null // 新規追加なので、編集対象タスクはクリア
                        showAddTaskDialog = true // 追加ダイアログを表示する
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Add, // 表示するアイコン (プラスマーク)
                            contentDescription = "タスク追加", // スクリーンリーダー用の説明
                            tint = MaterialTheme.colorScheme.primary // アイコンの色
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface, // 背景色
                    titleContentColor = MaterialTheme.colorScheme.onSurface // タイトルの色
                )
            )
        }
    ) { innerPadding -> // Scaffoldのコンテンツエリア。innerPadding にはトップバーなどが占める領域の情報が入る。
        // Column は子要素を縦方向に並べるComposable
        Column(
            modifier = Modifier.padding(innerPadding) // Scaffoldのコンテンツ領域に適切にパディングを適用
        ) {
            // タスクフィルタータブを表示
            TaskFilterTabs(
                currentFilter = currentFilter, // 現在のフィルターを渡す
                onFilterSelected = { filterType -> todoViewModel.setFilter(filterType) } // フィルター選択時にViewModelを更新
            )
            // LazyColumn は大量のアイテムを効率的に表示するためのリスト (スクロール可能)
            LazyColumn(
                 modifier = Modifier.padding(horizontal = 16.dp) // リストの左右にパディング
            ) {
                // tasks リストの各タスクに対して TaskItem を表示
                // key を指定することで、リストアイテムの再配置やアニメーションが効率的に行われる
                items(tasks, key = { task -> task.id }) { task ->
                    TaskItem(
                        task = task, // 表示するタスクデータ
                        // タスクの完了状態を切り替える処理をViewModelに依頼
                        onToggleComplete = { todoViewModel.toggleTaskCompletion(task.id) },
                        // タスクを削除する処理をViewModelに依頼
                        onDelete = { todoViewModel.deleteTask(task.id) },
                        // タスク編集を開始する処理
                        onEditClick = {
                            taskToEdit = task // 編集対象のタスクをセット
                            showEditTaskDialog = true // 編集ダイアログを表示
                        },
                        modifier = Modifier.padding(vertical = 6.dp) // 各アイテムの上下にパディング
                    )
                }
            }
        }

        // タスク追加ダイアログの表示制御
        if (showAddTaskDialog) {
            TaskDialog(
                existingTask = null, // 新規追加なので既存タスクはなし (null)
                onDismissRequest = { showAddTaskDialog = false }, // ダイアログが閉じるよう要求されたときの処理
                // 確認ボタンが押されたときの処理 (IDは新規なのでnull、タイトルと期限日を渡す)
                onConfirm = { _, title, deadline ->
                    todoViewModel.addTask(title, deadline) // ViewModelにタスク追加を依頼
                    showAddTaskDialog = false // ダイアログを閉じる
                }
            )
        }

        // タスク編集ダイアログの表示制御
        // taskToEdit が null でない (編集対象のタスクが存在する) 場合のみ考慮
        taskToEdit?.let { currentTaskToEdit ->
            if (showEditTaskDialog) {
                TaskDialog(
                    existingTask = currentTaskToEdit, // 編集対象のタスクを渡す
                    onDismissRequest = { showEditTaskDialog = false }, // ダイアログが閉じるよう要求されたときの処理
                    // 確認ボタンが押されたときの処理 (タスクID、新しいタイトル、新しい期限日を渡す)
                    onConfirm = { id, title, deadline ->
                        // id!! は id が null でないことを保証する (編集時は必ずIDがあるため)
                        todoViewModel.updateTaskDetails(id!!, title, deadline) // ViewModelにタスク更新を依頼
                        showEditTaskDialog = false // ダイアログを閉じる
                    }
                )
            }
        }
    }
}

/**
 * タスクの追加または編集を行うためのダイアログを表示するComposable関数。
 * existingTask パラメータの有無によって、追加モードと編集モードが切り替わる。
 *
 * @param existingTask 編集対象のタスク。nullの場合は新規追加モード。
 * @param onDismissRequest ダイアログを閉じるよう要求されたときに呼び出されるコールバック。
 * @param onConfirm 確認ボタンが押されたときに呼び出されるコールバック。タスクのID(編集時のみ)、タイトル、期限日を渡す。
 * @param modifier このComposableの見た目や動作をカスタマイズするためのModifier。
 */
@OptIn(ExperimentalMaterial3Api::class) // DatePickerなどの実験的APIを使用
@Composable
fun TaskDialog(
    existingTask: Task? = null, // 編集モードの場合、ここに既存タスク情報が入る
    onDismissRequest: () -> Unit, // ダイアログを閉じるリクエスト
    onConfirm: (id: Int?, title: String, deadline: Long?) -> Unit, // 保存・追加ボタンの処理
    modifier: Modifier = Modifier
) {
    // ダイアログ内のタスクタイトル入力フィールドの状態
    var taskTitle by remember { mutableStateOf("") }
    // ダイアログ内の期限日選択フィールドの状態 (Long型のタイムスタンプ)
    var deadlineMillis by remember { mutableStateOf<Long?>(null) }
    // DatePickerDialog (日付選択カレンダー) を表示するかどうかの状態
    var showDatePickerDialog by remember { mutableStateOf(false) }

    // LaunchedEffect は特定のキー (ここでは existingTask) が変更されたときに副作用 (この場合は初期値設定) を実行する
    // existingTask が変更されるたびに (つまりダイアログが開かれるたびに) 以下の処理が走る
    LaunchedEffect(existingTask) {
        if (existingTask != null) { // 編集モードの場合
            taskTitle = existingTask.title // フィールドに既存タスクのタイトルを設定
            deadlineMillis = existingTask.deadline // フィールドに既存タスクの期限日を設定
        } else { // 新規追加モードの場合
            taskTitle = "" // タイトルを空にする
            deadlineMillis = null // 期限日を未設定 (null) にする
        }
    }

    // DatePicker (日付選択カレンダー) の状態を管理
    val datePickerState = rememberDatePickerState(
        // 初期選択日: 既存の期限日があればそれ、なければ現在のシステム時刻
        initialSelectedDateMillis = deadlineMillis ?: (existingTask?.deadline ?: System.currentTimeMillis())
    )

    // 日付選択カレンダーダイアログの表示制御
    if (showDatePickerDialog) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false }, // カレンダーが閉じるよう要求された
            confirmButton = { // 「OK」ボタン
                TextButton(
                    onClick = {
                        deadlineMillis = datePickerState.selectedDateMillis // 選択された日付を deadlineMillis に設定
                        showDatePickerDialog = false // カレンダーを閉じる
                    }
                ) { Text("OK") }
            },
            dismissButton = { // 「キャンセル」ボタン
                TextButton(onClick = { showDatePickerDialog = false }) { Text("キャンセル") }
            }
        ) {
            DatePicker(state = datePickerState) // 日付選択カレンダー本体
        }
    }

    // AlertDialog は汎用的な警告・情報表示ダイアログ
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest, // ダイアログ外クリックや戻るボタンで閉じるリクエスト
        // ダイアログのタイトル (編集モードか追加モードかで変更)
        title = { Text(if (existingTask != null) "タスクを編集" else "新しいタスクを追加") },
        text = { // ダイアログの本文エリア
            Column { // 縦に要素を並べる
                OutlinedTextField(
                    value = taskTitle, // 表示するテキスト (現在の taskTitle の値)
                    onValueChange = { taskTitle = it }, // テキストが変更されたら taskTitle を更新
                    label = { Text("タスク名") }, // 入力フィールドのラベル
                    singleLine = true, // 入力を1行に制限
                    modifier = Modifier.fillMaxWidth() // 横幅いっぱいに表示
                )
                Spacer(modifier = Modifier.height(16.dp)) // 縦方向のスペース
                // 期限日表示と選択トリガーの行
                Row(
                    modifier = Modifier
                        .fillMaxWidth() // 横幅いっぱい
                        .clickable { showDatePickerDialog = true } // この行をクリックで日付選択カレンダー表示
                        .padding(vertical = 8.dp), // 上下のパディング
                    verticalAlignment = Alignment.CenterVertically, // 子要素を垂直方向中央揃え
                    horizontalArrangement = Arrangement.SpaceBetween // 子要素を左右両端に配置
                ) {
                    Text(
                        // deadlineMillis があればフォーマットして表示、なければ "期限日を設定"
                        text = deadlineMillis?.toFormattedDateString() ?: "期限日を設定",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Icon(
                        imageVector = Icons.Filled.DateRange, // カレンダーアイコン
                        contentDescription = "期限日を選択",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = { // 「保存」または「追加」ボタン
            Button(
                onClick = {
                    if (taskTitle.isNotBlank()) { // タスク名が空でなければ
                        // onConfirm コールバックを呼び出し (編集時はIDあり、追加時はIDなし)
                        onConfirm(existingTask?.id, taskTitle, deadlineMillis)
                    }
                },
                enabled = taskTitle.isNotBlank() // タスク名が空ならボタンを無効化
            ) {
                // ボタンのテキスト (編集モードか追加モードかで変更)
                Text(if (existingTask != null) "保存" else "追加")
            }
        },
        dismissButton = { // 「キャンセル」ボタン
            TextButton(onClick = onDismissRequest) { Text("キャンセル") }
        }
    )
}

/**
 * 個々のタスクアイテムを表示するComposable関数。
 * タスクのタイトル、完了状態、期限日、完了日、編集・削除ボタンなどを含む。
 *
 * @param task 表示するタスクのデータ。
 * @param onToggleComplete タスクの完了状態を切り替えるときに呼び出されるコールバック。
 * @param onDelete タスクを削除するときに呼び出されるコールバック。
 * @param onEditClick タスクを編集するときに呼び出されるコールバック。
 * @param modifier このComposableの見た目や動作をカスタマイズするためのModifier。
 */
@Composable
fun TaskItem(
    task: Task, // 表示するタスクオブジェクト
    onToggleComplete: () -> Unit, // 完了状態切り替え時の処理
    onDelete: () -> Unit, // 削除ボタンクリック時の処理
    onEditClick: () -> Unit, // 編集ボタンクリック時の処理
    modifier: Modifier = Modifier
) {
    // Card は情報をカード形式で表示するためのComposable (影や角丸など)
    Card(
        modifier = modifier
            .fillMaxWidth(), // 横幅いっぱい
        shape = RoundedCornerShape(8.dp), // 角を丸める (半径8dp)
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // カードの影の高さ
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // 背景色
    ) {
        // Row は子要素を横方向に並べるComposable
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp), // カード内部のパディング
            verticalAlignment = Alignment.CenterVertically // 子要素を垂直方向中央揃え
        ) {
            // 左側のタスク情報エリア (アイコン、タイトル、日付など)
            Column(
                modifier = Modifier.weight(1f) // このColumnが横方向の余ったスペースをすべて使用する
            ) {
                // 完了状態アイコンとタスクタイトルの行
                Row(
                    modifier = Modifier
                        .clickable { onToggleComplete() } // この行のクリックで完了状態を切り替え
                        .padding(vertical = 4.dp), // クリック領域のためのパディング
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        // タスクが完了していればチェックマーク、未完了なら円形アイコン
                        imageVector = if (task.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = if (task.isCompleted) "完了済み" else "未完了",
                        // アイコンの色も完了状態に応じて変更
                        tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 16.dp) // 右側にパディング（タイトルとの間隔）
                    )
                    Text(
                        text = task.title, // タスクのタイトル
                        style = MaterialTheme.typography.bodyLarge, // テキストスタイル
                        color = MaterialTheme.colorScheme.onSurface // テキストの色
                    )
                }
                // 期限日と完了日の表示エリア
                // アイコンのインデント (40dp) に合わせて少し右にずらして表示
                Row(modifier = Modifier.padding(start = 40.dp)) {
                    // 期限日が存在する場合のみ表示
                    if (task.deadline != null) {
                        Text(
                            text = "期限: ${task.deadline.toFormattedDateString()}", // フォーマットされた期限日
                            style = MaterialTheme.typography.bodySmall, // 少し小さいテキストスタイル
                            color = MaterialTheme.colorScheme.onSurfaceVariant, // やや薄い色
                            modifier = Modifier.padding(end = 8.dp) // 右隣に完了日が表示される場合の間隔
                        )
                    }
                    // タスクが完了しており、かつ完了日が存在する場合のみ表示
                    if (task.isCompleted && task.completionDate != null) {
                        Text(
                            // completionDate!! は completionDate が null でないことを保証
                            text = "完了: ${task.completionDate!!.toFormattedDateString()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary // 完了日はプライマリ色
                        )
                    }
                }
            }

            // 右側のアクションボタン (編集と削除)
            IconButton(onClick = onEditClick) { // 編集ボタン
                Icon(
                    Icons.Filled.Edit, // 編集アイコン
                    contentDescription = "編集",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant // 通常のアイコン色
                )
            }
            IconButton(onClick = onDelete) { // 削除ボタン
                Icon(
                    Icons.Filled.Delete, // ゴミ箱アイコン
                    contentDescription = "削除",
                    tint = MaterialTheme.colorScheme.error // エラー色 (赤系)
                )
            }
        }
    }
}

// --- 以下はプレビュー用のComposable関数 --- //
// これらの関数はAndroid Studioのプレビュー機能でUIを確認するために使用される
// @Preview アノテーションをつけることで、ビルドせずにUIの見た目を確認できる

@Preview(showBackground = true)
@Composable
fun TaskFilterTabsPreview() {
    Todo_AppTheme {
        TaskFilterTabs(currentFilter = FilterType.ALL, onFilterSelected = {})
    }
}

@Preview(showBackground = true)
@Composable
fun TodoScreenPreview() {
    Todo_AppTheme {
        TodoScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun TaskDialogPreview_AddMode() {
    Todo_AppTheme {
        TaskDialog(
            existingTask = null,
            onDismissRequest = {},
            onConfirm = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TaskDialogPreview_EditMode() {
    Todo_AppTheme {
        TaskDialog(
            existingTask = Task(id = 1, title = "既存のタスク", deadline = System.currentTimeMillis() + 86400000L),
            onDismissRequest = {},
            onConfirm = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TaskItemPreview() {
    Todo_AppTheme {
        TaskItem(
            task = Task(1, "プレビュータスク", false, deadline = System.currentTimeMillis() + 86400000L),
            onToggleComplete = {},
            onDelete = {}, 
            onEditClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TaskItemCompletedPreview() {
    Todo_AppTheme {
        TaskItem(
            task = Task(2, "完了済みプレビュータスク", true, deadline = System.currentTimeMillis() - 86400000L, completionDate = System.currentTimeMillis()),
            onToggleComplete = {},
            onDelete = {}, 
            onEditClick = {}
        )
    }
}
