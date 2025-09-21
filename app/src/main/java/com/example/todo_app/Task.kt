package com.example.todo_app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    var isCompleted: Boolean = false, // var に変更して ViewModel で直接更新できるようにするのも一案ですが、今回は copy() を使う前提で val のまま進めます
    val deadline: Long? = null,      // 期限日 (タイムスタンプ)
    var completionDate: Long? = null // 完了日 (タイムスタンプ) - isCompleted が true になったときに設定
)
