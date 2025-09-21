package com.example.todo_app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // 主キーを自動生成する場合、デフォルト値が必要
    val title: String,
    val isCompleted: Boolean = false
)
