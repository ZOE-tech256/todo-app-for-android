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

// 日付フォーマット用のヘルパー関数
fun Long.toFormattedDateString(): String {
    val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    return sdf.format(Date(this))
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Todo_AppTheme {
                TodoScreen()
            }
        }
    }
}

@Composable
fun TaskFilterTabs(
    currentFilter: FilterType,
    onFilterSelected: (FilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = FilterType.values()
    val selectedTabIndex = currentFilter.ordinal

    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                color = MaterialTheme.colorScheme.primary
            )
        }
    ) {
        filters.forEachIndexed { index, filterType ->
            val tabText = when (filterType) {
                FilterType.ALL -> "すべて"
                FilterType.ACTIVE -> "未完了"
                FilterType.COMPLETED -> "完了済"
            }
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onFilterSelected(filterType) },
                text = {
                    Text(
                        text = tabText,
                        color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    modifier: Modifier = Modifier,
    todoViewModel: TodoViewModel = viewModel()
) {
    val tasks by todoViewModel.tasks.collectAsStateWithLifecycle()
    val currentFilter by todoViewModel.currentFilter.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Todoアプリ") },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "タスク追加",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            TaskFilterTabs(
                currentFilter = currentFilter,
                onFilterSelected = { filterType -> todoViewModel.setFilter(filterType) }
            )
            LazyColumn(
                 modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                items(tasks, key = { task -> task.id }) { task ->
                    TaskItem(
                        task = task,
                        onToggleComplete = { todoViewModel.toggleTaskCompletion(task.id) },
                        onDelete = { todoViewModel.deleteTask(task.id) }, 
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }

        if (showDialog) {
            AddTaskDialog(
                onDismissRequest = { showDialog = false },
                onTaskAdd = { title, deadline -> // deadline を受け取るように変更
                    todoViewModel.addTask(title, deadline) // deadline を ViewModel に渡す
                    showDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // DatePicker と DatePickerDialog のために追加
@Composable
fun AddTaskDialog(
    onDismissRequest: () -> Unit,
    onTaskAdd: (title: String, deadline: Long?) -> Unit, // deadline パラメータを追加
    modifier: Modifier = Modifier
) {
    var taskTitle by remember { mutableStateOf("") }
    var deadlineMillis by remember { mutableStateOf<Long?>(null) } // 選択された期限日 (タイムスタンプ)
    var showDatePickerDialog by remember { mutableStateOf(false) } // DatePickerDialog の表示状態

    // DatePicker の状態を管理
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = deadlineMillis ?: System.currentTimeMillis() // 初期選択日
    )

    if (showDatePickerDialog) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        deadlineMillis = datePickerState.selectedDateMillis
                        showDatePickerDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("キャンセル")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = { Text("新しいタスクを追加") },
        text = {
            Column { // 複数の要素を縦に並べるために Column を使用
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    label = { Text("タスク名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp)) // タスク名と期限日の間にスペース

                // 期限日表示と選択ボタン
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePickerDialog = true } // このRowをクリックでダイアログ表示
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
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
        confirmButton = {
            Button(
                onClick = {
                    if (taskTitle.isNotBlank()) {
                        onTaskAdd(taskTitle, deadlineMillis) // deadlineMillis も渡す
                    }
                },
                enabled = taskTitle.isNotBlank()
            ) {
                Text("追加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("キャンセル")
            }
        }
    )
}

@Composable
fun TaskItem(
    task: Task,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit, 
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左側のタスク情報エリア (アイコン、タイトル、日付)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier
                        .clickable { onToggleComplete() } 
                        .padding(vertical = 4.dp), // クリック領域のためのパディング
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = if (task.isCompleted) "完了済み" else "未完了",
                        tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                // 期限日と完了日の表示
                // アイコンのインデントに合わせて少し右にずらす
                Row(modifier = Modifier.padding(start = 40.dp)) { // Icon size (24.dp) + padding (16.dp) = 40.dp
                    if (task.deadline != null) {
                        Text(
                            text = "期限: ${task.deadline.toFormattedDateString()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 8.dp) // 完了日との間にスペース
                        )
                    }
                    if (task.isCompleted && task.completionDate != null) {
                        Text(
                            text = "完了: ${task.completionDate!!.toFormattedDateString()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary // 完了日はプライマリーカラーなど
                        )
                    }
                }
            }

            // 右側の削除ボタン
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "削除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}


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
fun AddTaskDialogPreview() {
    Todo_AppTheme {
        AddTaskDialog(onDismissRequest = {}, onTaskAdd = { _, _ -> /* title, deadline */ })
    }
}

@Preview(showBackground = true)
@Composable
fun TaskItemPreview() {
    Todo_AppTheme {
        TaskItem(
            task = Task(1, "プレビュータスク", false, deadline = System.currentTimeMillis() + 86400000L), // 1日後を期限に
            onToggleComplete = {},
            onDelete = {} 
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TaskItemCompletedPreview() {
    Todo_AppTheme {
        TaskItem(
            task = Task(2, "完了済みプレビュータスク", true, deadline = System.currentTimeMillis() - 86400000L, completionDate = System.currentTimeMillis()), // 1日前に期限切れ、今日完了
            onToggleComplete = {},
            onDelete = {} 
        )
    }
}
