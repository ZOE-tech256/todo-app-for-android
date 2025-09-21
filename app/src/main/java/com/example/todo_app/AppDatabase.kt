package com.example.todo_app

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        // @Volatile アノテーションは、この変数が複数のスレッドからアクセスされる可能性があり、
        // あるスレッドによる変更が即座に他のスレッドから見えるようにします。
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // INSTANCEがnullであれば、同期ブロック内でデータベースインスタンスを作成します。
            // これにより、複数のスレッドが同時にデータベースインスタンスを作成しようとするのを防ぎます。
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "task_database" // データベースファイル名
                )
                // ここでマイグレーション戦略を追加することも可能ですが、今回は省略します。
                // .fallbackToDestructiveMigration() // スキーマ変更時にデータを破棄して再作成（開発初期段階向け）
                .build()
                INSTANCE = instance
                // 作成したインスタンスを返す
                instance
            }
        }
    }
}
