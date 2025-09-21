package com.example.todo_app

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Task::class], version = 2, exportSchema = false) // バージョンを2に上げる
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // バージョン1から2へのマイグレーション
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // tasksテーブルにdeadlineカラム (INTEGER NULLABLE) を追加
                db.execSQL("ALTER TABLE tasks ADD COLUMN deadline INTEGER DEFAULT NULL")
                // tasksテーブルにcompletionDateカラム (INTEGER NULLABLE) を追加
                db.execSQL("ALTER TABLE tasks ADD COLUMN completionDate INTEGER DEFAULT NULL")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "task_database"
                )
                .addMigrations(MIGRATION_1_2) // 作成したマイグレーションを追加
                // .fallbackToDestructiveMigration() // マイグレーションに失敗した場合のフォールバック (本番では非推奨)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
