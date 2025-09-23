package com.example.todo_app

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Roomデータベースのメインクラス。
 * このクラスは、データベースの構成を定義し、DAOへのアクセスポイントを提供する。
 * `@Database` アノテーションで、このクラスがRoomデータベースであることを示し、
 * 含まれるエンティティ (テーブル)、データベースのバージョン、スキーマエクスポート設定などを指定する。
 *
 * @property entities データベースに含まれるエンティティ (テーブルに対応するクラス) のリスト。
 * @property version データベースの現在のバージョン。スキーマ変更時にはバージョンを上げる必要がある。
 * @property exportSchema スキーマ情報をファイルにエクスポートするかどうか。デフォルトはtrueだが、ここではfalseに設定。
 *                      trueにするとビルド時にスキーマファイルが出力され、バージョン管理に役立つ。
 */
@Database(entities = [Task::class], version = 2, exportSchema = false) // バージョンを2に更新済み
abstract class AppDatabase : RoomDatabase() { // RoomDatabaseを継承する抽象クラスとして定義

    /**
     * `TaskDao` の抽象メソッド。
     * Roomがこのメソッドの実装を自動生成し、`TaskDao`のインスタンスを返すようにする。
     * このメソッドを通じて、ViewModelなどはデータベース操作を行うDAOを取得する。
     *
     * @return TaskDaoのインスタンス
     */
    abstract fun taskDao(): TaskDao

    // companion object はJavaのstaticメンバーに似たもので、クラス名から直接アクセスできるメンバーを定義するブロック。
    companion object {
        // @Volatile アノテーションは、この変数が複数のスレッドからアクセスされる可能性があり、
        // あるスレッドによる変更が他のスレッドから即座に見えるようにするためのもの。
        // INSTANCE変数は、AppDatabaseのシングルトンインスタンスを保持する。
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // データベースのマイグレーション処理を定義。
        // マイグレーションは、データベースのスキーマ (テーブル構造など) が変更された際に、
        // 既存のデータを保持したまま新しいスキーマに更新するための手順。
        // バージョン1からバージョン2へのマイグレーションを定義している。
        val MIGRATION_1_2 = object : Migration(1, 2) {
            /**
             * スキーマをバージョン1から2に更新する具体的なSQL処理を記述する。
             * @param db SupportSQLiteDatabaseオブジェクト。SQLの実行に使用する。
             */
            override fun migrate(db: SupportSQLiteDatabase) {
                // `tasks` テーブルに `deadline` カラム (INTEGER型, NULL許容) を追加するSQL文。
                db.execSQL("ALTER TABLE tasks ADD COLUMN deadline INTEGER DEFAULT NULL")
                // `tasks` テーブルに `completionDate` カラム (INTEGER型, NULL許容) を追加するSQL文。
                db.execSQL("ALTER TABLE tasks ADD COLUMN completionDate INTEGER DEFAULT NULL")
            }
        }

        /**
         * `AppDatabase` のシングルトンインスタンスを取得するためのメソッド。
         * シングルトンパターンにより、アプリ全体でデータベースのインスタンスが一つだけ生成されることを保証する。
         *
         * @param context アプリケーションコンテキスト。データベースの初期化に必要。
         * @return `AppDatabase` のシングルトンインスタンス。
         */
        fun getDatabase(context: Context): AppDatabase {
            // INSTANCEがnullなら、まだインスタンスが作成されていない。
            // nullでなければ、既存のインスタンスを返す。
            return INSTANCE ?: synchronized(this) { // synchronizedブロックでスレッドセーフなインスタンス生成を保証
                // Room.databaseBuilder() を使ってデータベースインスタンスを構築する。
                val instance = Room.databaseBuilder(
                    context.applicationContext, // アプリケーションコンテキスト
                    AppDatabase::class.java,    // データベースクラスの参照
                    "task_database"         // データベースファイル名
                )
                .addMigrations(MIGRATION_1_2) // 定義したマイグレーション処理を追加
                // .fallbackToDestructiveMigration() // マイグレーションパスが見つからない場合に、既存のデータを破棄して再生成する。
                                                // 開発中は便利だが、本番アプリでは通常使用を避けるべき。
                .build() // データベースインスタンスを構築
                INSTANCE = instance // 生成したインスタンスをINSTANCE変数に保持
                instance // 生成したインスタンスを返す
            }
        }
    }
}
