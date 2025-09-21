package com.example.todo_app

enum class FilterType {
    ALL,        // すべてのタスク
    ACTIVE,     // 未完了のタスク (isCompleted = false)
    COMPLETED   // 完了済みのタスク (isCompleted = true)
}
