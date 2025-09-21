# Todoアプリ 開発進捗ログ

## 2025-09-20

### 主要なファイルと役割

*   **`MainActivity.kt`**:
    *   アプリのメインの起動点となる `Activity` です。
    *   Jetpack Composeを使用してアプリのUI全体（主に `TodoScreen` Composable）を構築し、表示します。
    *   `TodoScreen` は、タスク一覧、**タスクフィルターボタン**、タスク追加ボタンなどの主要なUI要素を含みます。(変更点: フィルターボタン追加)
    *   ユーザーインタラクションの受付（タスク追加ダイアログの表示、**フィルターの選択**など）を行い、`TodoViewModel` へ処理を委譲します。(変更点: フィルター選択追加)
    *   `TodoScreen`、`TaskItem`（各タスクの表示用）、`AddTaskDialog`（タスク追加用ダイアログ）、**`FilterButtonsRow`（フィルターボタン表示用）** といった主要なComposable関数がこのファイル内で定義されています。(変更点: `FilterButtonsRow` 追加)

*   **`TodoViewModel.kt`**:
    *   `androidx.lifecycle.AndroidViewModel` を継承したクラスです。
    *   UIに関連するデータ（この場合はRoomデータベースから取得し、**フィルターされたタスクのリスト**）を保持し、管理する役割を担います。(変更点: フィルター処理追加)
    *   `AppDatabase` を通じて `TaskDao` を使用し、タスクの追加 (`addTask`)、完了状態の切り替え (`toggleTaskCompletion`)、削除 (`deleteTask`) といったデータベース操作をコルーチン (`viewModelScope`) で実行します。
    *   **現在のフィルター状態 (`currentFilter: StateFlow<FilterType>`) も管理し、変更するメソッド (`setFilter`) を提供します。** (新規追加)
    *   `TaskDao` から取得した `Flow<List<Task>>` と現在のフィルター状態を `combine` し、フィルタリングされた結果を `stateIn` を使って `StateFlow` (`tasks`) に変換し、UIにリアクティブに公開します。(変更点: `combine` とフィルター処理)
    *   `toggleTaskCompletion` と `deleteTask` メソッド内で、データベースからタスクリストの最新状態を取得して操作対象のタスクを検索するように修正し、フィルタリング状態での操作の堅牢性を向上させました。

*   **`Task.kt`**: (変更なし)
    *   個々のTodoタスクのデータを表現するためのRoomエンティティクラス (`@Entity data class Task`) です。
    *   各タスクが持つべき情報として、Roomが自動生成する一意な識別子 (`@PrimaryKey(autoGenerate = true) id: Int`)、タスクの内容を示すタイトル (`title: String`)、タスクが完了したかどうかを示す状態 (`isCompleted: Boolean`) をプロパティとして定義しています。

*   **`TaskDao.kt`**: (変更なし)
    *   RoomのData Access Object (DAO) インターフェース (`@Dao interface TaskDao`) です。
    *   `Task` エンティティに対するデータベース操作（全件取得、挿入、更新、削除）を行うためのメソッド（`getAllTasks`, `insertTask`, `updateTask`, `deleteTask`）を定義します。
    *   `getAllTasks` は `Flow<List<Task>>` を返し、データベースの変更をリアクティブに通知します。

*   **`AppDatabase.kt`**: (変更なし)
    *   `androidx.room.RoomDatabase` を継承した抽象クラス (`@Database abstract class AppDatabase`) です。
    *   データベースの構成（エンティティ、バージョンなど）を定義し、`TaskDao` のインスタンスを提供するファクトリメソッドを持ちます。
    *   シングルトンパターンでデータベースインスタンス (`task_database`) を管理します。

*   **`FilterType.kt` (新規追加)**:
    *   タスクのフィルタリング種別（`ALL`, `ACTIVE`, `COMPLETED`）を定義する `enum class` です。

### 完了したタスク

1.  **アプリケーション仕様のドキュメント化**:
    *   基本的な機能概要（タスク追加、一覧表示、完了/未完了切り替え、削除）と画面仕様（メイン画面、タスク追加UI）を定義した `AppSpecification.md` を作成しました。

2.  **データモデルの作成 (`Task.kt`)**:
    *   タスク情報を保持するための `Task` データクラスを定義しました。

3.  **ViewModelの実装 (`TodoViewModel.kt`)**:
    *   UIロジックと状態管理を行う `TodoViewModel` を実装しました。
    *   `lifecycle-viewmodel-compose` の依存関係を追加しました。

4.  **メイン画面UIの実装 (`MainActivity.kt` 内 `TodoScreen`)**:
    *   `TodoScreen` Composable関数を作成し、基本的なレイアウトを構築しました。
    *   `LazyColumn` でタスクリストを表示しました。

5.  **タスク追加機能の実装 (`MainActivity.kt` 内 `AddTaskDialog`)**:
    *   タスク名を入力し、新しいタスクを追加する機能を実装しました。

6.  **タスクアイテムUIの改善 (`MainActivity.kt` 内 `TaskItem`)**:
    *   チェックボックス、タスクタイトル、削除ボタンを含む各タスクの表示を改善しました。

7.  **データ永続化の実装 (Roomデータベース)**:
    *   Roomの依存関係追加、エンティティ定義、DAO作成、データベースクラス作成、ViewModel更新を行い、タスクデータが永続化されるようにしました。

8.  **Gitバージョン管理の導入**:
    *   プロジェクトをローカルGitリポジトリで初期化しました。
    *   Androidプロジェクト用の `.gitignore` ファイルを作成し設定しました。
    *   最初のコミットを行い、GitHub上にリモートリポジトリ (`https://github.com/ZOE-tech256/todo-app-for-android`) を作成してプッシュしました。

9.  **タスクフィルタリング機能の実装**: (新規追加セクション)
    *   **フィルタータイプの定義 (`FilterType.kt`)**:
        *   `FilterType` enumクラス (ALL, ACTIVE, COMPLETED) を作成しました。
    *   **ViewModelの更新 (`TodoViewModel.kt`)**:
        *   現在のフィルター状態を保持する `StateFlow` (`_currentFilter`) と、それを更新する `setFilter` メソッドを追加しました。
        *   公開するタスクリスト (`tasks`) が、`taskDao.getAllTasks()` と `_currentFilter` を `combine` し、選択されたフィルターに基づいて内容を動的に変更するようにロジックを更新しました。
        *   `toggleTaskCompletion` および `deleteTask` メソッド内で、データベースからタスクリストの最新状態を `firstOrNull()` を使って取得し、操作対象のタスクを検索するように修正しました。
    *   **UIの変更 (`MainActivity.kt` 内 `TodoScreen`)**:
        *   フィルターオプション（「すべて」「未完了」「完了済み」）を表示する `FilterButtonsRow` Composableを新たに追加しました。
        *   `TodoScreen` 内に `FilterButtonsRow` を配置し、`TodoViewModel` の `currentFilter` を監視して選択状態を反映し、フィルターボタンのクリックで `viewModel.setFilter()` が呼び出されるようにしました。
        *   選択されているフィルターボタンが視覚的に強調表示されるようにしました。
    *   **動作確認**: 各フィルターでのタスク表示、タスク操作（追加、完了/未完了、削除）が期待通りに動作することを確認しました。
