package com.example.todo_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement // Arrangementのインポートを追加
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row // Rowのインポートを追加
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete // Deleteアイコンのインポートを追加
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox // Checkboxのインポートを追加
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton // IconButtonのインポートを追加
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment // Alignmentのインポートを追加
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration // TextDecorationのインポートを追加
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    modifier: Modifier = Modifier,
    todoViewModel: TodoViewModel = viewModel()
) {
    val tasks by todoViewModel.tasks.collectAsStateWithLifecycle()
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
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            items(tasks, key = { task -> task.id }) { task ->
                TaskItem(
                    task = task,
                    onToggleComplete = { todoViewModel.toggleTaskCompletion(task.id) },
                    onDelete = { todoViewModel.deleteTask(task.id) }
                )
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
