package com.example.todo_app

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the Task entity.
 * このインターフェースは、Roomライブラリに対して、`Task`テーブルに対する
 * データベース操作（読み取り、書き込み、更新、削除など）をどのように行うかを定義します。
 * Roomがこのインターフェースの実装を自動生成します。
 */
@Dao // このインターフェースがRoomのDAOであることを示すアノテーション
interface TaskDao {

    /**
     * `tasks`テーブルから全てのタスクを取得し、IDの降順（新しいものが先頭）で並べ替えて返す。
     * 戻り値は `Flow<List<Task>>` であり、データベースの内容が変更されると自動的に
     * 新しいタスクのリストが通知されるリアクティブなストリーム。
     * UIはこのFlowを監視することで、常に最新のタスクリストを表示できる。
     */
    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<Task>>

    /**
     * 新しいタスクを `tasks` テーブルに挿入（追加）する。
     * `onConflict = OnConflictStrategy.REPLACE` は、もし同じIDのタスクが既に存在した場合（通常は新規追加では起こり得ないが）、
     * 既存のタスクを新しいタスクで置き換えることを指定する。
     * `suspend` キーワードは、この関数がコルーチン内で呼び出される必要があり、
     * データベース操作が完了するまで現在のコルーチンを一時停止することを示す（メインスレッドをブロックしない）。
     *
     * @param task 挿入するタスクオブジェクト。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    /**
     * 既存のタスク情報を更新する。
     * Roomは渡された `task` オブジェクトの主キー（`id`）を見て、どのタスクを更新するかを判断する。
     * `suspend` キーワードにより、この操作もコルーチン内で行われる。
     *
     * @param task 更新するタスクオブジェクト。
     */
    @Update
    suspend fun updateTask(task: Task)

    /**
     * 指定されたタスクをデータベースから削除する。
     * Roomは渡された `task` オブジェクトの主キー（`id`）を見て、どのタスクを削除するかを判断する。
     * `suspend` キーワードにより、この操作もコルーチン内で行われる。
     *
     * @param task 削除するタスクオブジェクト。
     */
    @Delete
    suspend fun deleteTask(task: Task)
}
