package com.example.todo_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column // Column をインポート
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer // Spacer をインポート
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height // height をインポート
import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width // 必要に応じてwidthをインポート
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults // ButtonDefaults をインポート
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton // OutlinedButton をインポート
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
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

// 新しいComposable: フィルターボタンを表示する行
@Composable
fun FilterButtonsRow(
    currentFilter: FilterType,
    onFilterSelected: (FilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly // ボタンを等間隔に配置
    ) {
        FilterType.values().forEach { filterType ->
            val buttonColors = if (currentFilter == filterType) {
                ButtonDefaults.buttonColors( // 通常のButtonの場合
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                ButtonDefaults.outlinedButtonColors() // OutlinedButtonの場合の色に合わせるか、通常のButtonの色を調整
            }
            // OutlinedButton を使用して、選択されていないものは枠線のみにする
            if (currentFilter == filterType) {
                Button(
                    onClick = { onFilterSelected(filterType) },
                    colors = buttonColors // 選択されている場合のButtonの色を適用
                ) {
                    Text(filterType.name) // ALL, ACTIVE, COMPLETED
                }
            } else {
                OutlinedButton(
                    onClick = { onFilterSelected(filterType) }
                    // colors = buttonColors // 非選択時のOutlinedButtonの色はデフォルトで良い場合が多い
                ) {
                    Text(filterType.name)
                }
            }
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
    val currentFilter by todoViewModel.currentFilter.collectAsStateWithLifecycle() // 現在のフィルターを取得
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Todoアプリ") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "タスク追加")
            }
        }
    ) { innerPadding ->
        Column( // Columnでフィルターボタンとリストを縦に並べる
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp) // 左右のパディングをColumnに適用
        ) {
            FilterButtonsRow( // フィルターボタンを配置
                currentFilter = currentFilter,
                onFilterSelected = { filterType -> todoViewModel.setFilter(filterType) },
                modifier = Modifier.padding(top = 8.dp) // 上部に少しパディング
            )
            Spacer(modifier = Modifier.height(8.dp)) // フィルターボタンとリストの間に少しスペース

            LazyColumn( // LazyColumnは縦方向のパディングを削除 (Columnで管理するため)
                // modifier = Modifier.padding(vertical = 16.dp) // Columnに移動または調整
            ) {
                items(tasks, key = { task -> task.id }) { task ->
                    TaskItem(
                        task = task,
                        onToggleComplete = { todoViewModel.toggleTaskCompletion(task.id) },
                        onDelete = { todoViewModel.deleteTask(task.id) }
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleComplete() }
            )
            Text(
                text = task.title,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "削除")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FilterButtonsRowPreview() {
    Todo_AppTheme {
        FilterButtonsRow(currentFilter = FilterType.ALL, onFilterSelected = {})
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

// TaskItemのPreviewも更新または簡略化
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
