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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.RadioButtonUnchecked // 未完了アイコン
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button // AddTaskDialog で使用
import androidx.compose.material3.Card // Cardをインポート
import androidx.compose.material3.CardDefaults // CardDefaultsをインポート
import androidx.compose.material3.CenterAlignedTopAppBar // CenterAlignedTopAppBar をインポート
// import androidx.compose.material3.Checkbox // TaskItemから削除
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

// TaskFilterTabs Composable の Tab テキストを変更
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
            val tabText = when (filterType) { // when 式でテキストを決定
                FilterType.ALL -> "すべて"
                FilterType.ACTIVE -> "未完了"
                FilterType.COMPLETED -> "完了済"
            }
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onFilterSelected(filterType) },
                text = {
                    Text(
                        text = tabText, // 変更後のテキストを使用
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
                onTaskAdd = { title ->
                    todoViewModel.addTask(title)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismissRequest: () -> Unit,
    onTaskAdd: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var taskTitle by remember { mutableStateOf("") }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = { Text("新しいタスクを追加") },
        text = {
            OutlinedTextField(
                value = taskTitle,
                onValueChange = { taskTitle = it },
                label = { Text("タスク名") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (taskTitle.isNotBlank()) {
                        onTaskAdd(taskTitle)
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
            Row(
                modifier = Modifier
                    .weight(1f) 
                    .clickable { onToggleComplete() } 
                    .padding(vertical = 4.dp), 
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
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = null
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
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
        AddTaskDialog(onDismissRequest = {}, onTaskAdd = {})
    }
}

@Preview(showBackground = true)
@Composable
fun TaskItemPreview() {
    Todo_AppTheme {
        TaskItem(
            task = Task(1, "プレビュータスク", false),
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
            task = Task(2, "完了済みプレビュータスク", true),
            onToggleComplete = {},
            onDelete = {} 
        )
    }
}
