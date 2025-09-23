package com.example.todo_app

/**
 * タスクリストのフィルタリングに使用する種類を定義する列挙型クラス。
 * ユーザーがどの状態のタスクを見たいかを選択するために使われる。
 */
enum class FilterType {
    ALL,        // すべてのタスク
    ACTIVE,     // 未完了のタスク (isCompleted = false)
    COMPLETED   // 完了済みのタスク (isCompleted = true)
}
