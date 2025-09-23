package com.example.todo_app

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * アプリ内で扱う個々のTodoタスクのデータ構造を定義するクラス。
 * Roomデータベースのエンティティ (テーブルの行に相当) としても機能する。
 *
 * @property id タスクの一意な識別子。データベースによって自動生成される。
 * @property title タスクの内容・タイトル。
 * @property isCompleted タスクが完了したかどうかを示すフラグ (trueなら完了、falseなら未完了)。
 * @property deadline タスクの期限日。Long型のタイムスタンプ (エポックからのミリ秒) で保存。未設定の場合はnull。
 * @property completionDate タスクが完了した日時。Long型のタイムスタンプで保存。未完了または完了日時が未設定の場合はnull。
 */
@Entity(tableName = "tasks") // このクラスが "tasks" という名前のテーブルに対応することを示す
data class Task(
    @PrimaryKey(autoGenerate = true) // idプロパティを主キーとし、Roomが自動で値を生成するように設定
    val id: Int = 0, // タスクの一意なID (データベースが自動採番)
    val title: String, // タスクのタイトル
    var isCompleted: Boolean = false, // タスクが完了したかどうか (初期値は未完了)
    val deadline: Long? = null,      // タスクの期限日 (タイムスタンプ形式、null許容)
    var completionDate: Long? = null // タスクの完了日 (タイムスタンプ形式、null許容)
)
