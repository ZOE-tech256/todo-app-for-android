# Todoアプリ 開発進捗ログ

## 2025-09-20

### 主要なファイルと役割

*   **`MainActivity.kt`**:
    *   アプリのメインの起動点となる `Activity` です。
    *   Jetpack Composeを使用してアプリのUI全体（主に `TodoScreen` Composable）を構築し、表示します。
    *   `TodoScreen` は、タスク一覧、タスク追加ボタンなどの主要なUI要素を含みます。
    *   ユーザーインタラクションの受付（タスク追加ダイアログの表示など）を行い、`TodoViewModel` へ処理を委譲します。
    *   `TodoScreen`、`TaskItem`（各タスクの表示用）、`AddTaskDialog`（タスク追加用ダイアログ）といった主要なComposable関数がこのファイル内で定義されています。

*   **`TodoViewModel.kt`**:
    *   `androidx.lifecycle.AndroidViewModel` を継承したクラスです。(変更点: `ViewModel` -> `AndroidViewModel`)
    *   UIに関連するデータ（この場合はRoomデータベースから取得したタスクのリスト）を保持し、管理する役割を担います。(変更点: メモリ内リスト -> Roomデータベース)
    *   `AppDatabase` を通じて `TaskDao` を使用し、タスクの追加 (`addTask`)、完了状態の切り替え (`toggleTaskCompletion`)、削除 (`deleteTask`) といったデータベース操作をコルーチン (`viewModelScope`) で実行します。(変更点: 操作対象と方法)
    *   `TaskDao` から取得した `Flow<List<Task>>` を `stateIn` を使って `StateFlow` に変換し、タスクリストの状態をUI（`TodoScreen`）にリアクティブに公開します。

*   **`Task.kt`**:
    *   個々のTodoタスクのデータを表現するためのRoomエンティティクラス (`@Entity data class Task`) です。(変更点: Roomエンティティ化)
    *   各タスクが持つべき情報として、Roomが自動生成する一意な識別子 (`@PrimaryKey(autoGenerate = true) id: Int`)、タスクの内容を示すタイトル (`title: String`)、タスクが完了したかどうかを示す状態 (`isCompleted: Boolean`) をプロパティとして定義しています。

*   **`TaskDao.kt` (新規追加)**:
    *   RoomのData Access Object (DAO) インターフェース (`@Dao interface TaskDao`) です。
    *   `Task` エンティティに対するデータベース操作（全件取得、挿入、更新、削除）を行うためのメソッド（`getAllTasks`, `insertTask`, `updateTask`, `deleteTask`）を定義します。
    *   `getAllTasks` は `Flow<List<Task>>` を返し、データベースの変更をリアクティブに通知します。

*   **`AppDatabase.kt` (新規追加)**:
    *   `androidx.room.RoomDatabase` を継承した抽象クラス (`@Database abstract class AppDatabase`) です。
    *   データベースの構成（エンティティ、バージョンなど）を定義し、`TaskDao` のインスタンスを提供するファクトリメソッドを持ちます。
    *   シングルトンパターンでデータベースインスタンス (`task_database`) を管理します。

### 完了したタスク

1.  **アプリケーション仕様のドキュメント化**:
    *   基本的な機能概要（タスク追加、一覧表示、完了/未完了切り替え、削除）と画面仕様（メイン画面、タスク追加UI）を定義した `AppSpecification.md` を作成しました。

2.  **データモデルの作成 (`Task.kt`)**:
    *   上記「主要なファイルと役割」で説明した、タスク情報を保持するための `Task` データクラスを定義しました。

3.  **ViewModelの実装 (`TodoViewModel.kt`)**:
    *   上記「主要なファイルと役割」で説明した、UIロジックと状態管理を行う `TodoViewModel` を実装しました。
    *   タスクリストの管理（初期データ含む）、タスクの追加・完了状態変更・削除の各メソッドを実装しました。
    *   `lifecycle-viewmodel-compose` の依存関係を `build.gradle.kts` および `gradle/libs.versions.toml` に追加し、プロジェクトを同期しました。

4.  **メイン画面UIの実装 (`MainActivity.kt` 内 `TodoScreen`)**:
    *   `TodoScreen` Composable関数を作成し、`Scaffold` を使用してアプリの基本レイアウト（トップアプリバー、タスクリスト表示エリア、フローティングアクションボタン）を構築しました。
    *   `LazyColumn` を使用して `TodoViewModel` から取得したタスクのリストを表示するようにしました。ViewModelの `tasks` (`StateFlow`) を `collectAsStateWithLifecycle` を使って購読し、リストの変更がUIに自動的に反映されるようにしました。

5.  **タスク追加機能の実装 (`MainActivity.kt` 内 `AddTaskDialog`)**:
    *   `TodoScreen` 内のフローティングアクションボタン（FAB）がクリックされた際に、タスク名を入力するための `AlertDialog` を表示する `AddTaskDialog` Composableを作成しました。
    *   入力されたタスク名を `TodoViewModel` の `addTask` メソッドに渡し、新しいタスクとしてリストに追加する機能を実装しました。

6.  **タスクアイテムUIの改善 (`MainActivity.kt` 内 `TaskItem`)**:
    *   各タスクを表示するための `TaskItem` Composableを改善しました。
    *   `Row` を使用して、チェックボックス、タスクタイトル、削除ボタンを横に並べて表示しました。
    *   `Checkbox` を追加し、その状態をタスクの `isCompleted` プロパティに連動させ、チェック状態の変更時に `TodoViewModel` の `toggleTaskCompletion` メソッドが呼び出されるようにしました。
    *   `IconButton`（削除アイコン付き）を追加し、クリック時に `TodoViewModel` の `deleteTask` メソッドが呼び出されるようにしました。
    *   完了済みのタスクのタイトルには取り消し線 (`TextDecoration.LineThrough`) が表示されるようにスタイルを調整しました。

7.  **データ永続化の実装 (Roomデータベース)**: (新規追加セクション)
    *   **依存関係の追加**:
        *   `gradle/libs.versions.toml` にRoom (`room = "2.8.0"`) とKSP (`ksp = "2.0.21-1.0.21"`) のバージョンおよびライブラリ定義を追加しました。
        *   プロジェクトレベルの `build.gradle.kts` にKSPプラグイン (`alias(libs.plugins.kotlin.ksp) apply false`) を追加しました。
        *   モジュールレベル (`app`) の `build.gradle.kts` にKSPプラグインを適用し、Roomのランタイム、KTX、コンパイラの依存関係を追加しました。
        *   Gradle Syncを実行し、依存関係を解決しました。
    *   **エンティティの定義 (`Task.kt`)**:
        *   `Task` データクラスに `@Entity(tableName = "tasks")` アノテーションを付与し、Roomのエンティティとして定義しました。
        *   `id` プロパティに `@PrimaryKey(autoGenerate = true)` を付与し、自動生成される主キーとして設定しました。
    *   **DAOの作成 (`TaskDao.kt`)**:
        *   `@Dao` アノテーションを持つ `TaskDao` インターフェースを作成しました。
        *   タスクの全件取得 (`getAllTasks(): Flow<List<Task>>`)、挿入 (`insertTask(task: Task)`)、更新 (`updateTask(task: Task)`)、削除 (`deleteTask(task: Task)`) を行うための suspend 関数および Flow を返すメソッドを定義しました。
    *   **データベースクラスの作成 (`AppDatabase.kt`)**:
        *   `@Database(entities = [Task::class], version = 1, exportSchema = false)` アノテーションを持つ `AppDatabase` 抽象クラス (RoomDatabaseを継承) を作成しました。
        *   `TaskDao` を返す抽象メソッドと、データベースインスタンスをシングルトンで提供するコンパニオンオブジェクトを実装しました。
    *   **ViewModelの更新 (`TodoViewModel.kt`)**:
        *   `AndroidViewModel` を継承するように変更し、コンストラクタで `Application` コンテキストを受け取るようにしました。
        *   `AppDatabase` を介して `TaskDao` のインスタンスを取得しました。
        *   タスクリスト (`tasks: StateFlow<List<Task>>`) が `taskDao.getAllTasks()` を `stateIn` を使って監視するように変更しました。
        *   タスクの追加、更新、削除のロジックを、`viewModelScope` 内で `TaskDao` の対応するメソッドを呼び出すように変更しました。
    *   **動作確認**: アプリを起動し、タスクの追加、変更、削除後、アプリを再起動してもデータが永続化されていることを確認しました。
