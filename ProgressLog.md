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

## 2025-09-21 

### UI/UX改善と表示テキストの日本語化

*   **全体的なカラースキームの変更**:
    *   参考画像に合わせた新しい色（`AppLightGray`, `CardWhite`, `AccentBlue` など）を `Color.kt` に定義しました。
    *   定義した新しい色を `Theme.kt` の `NewLightColorScheme` に適用し、アプリ全体のテーマとして設定しました。 ライトテーマが常に適用されるように `dynamicColor` はデフォルトで `false` に変更しました。
*   **トップアプリバーの刷新**:
    *   `CenterAlignedTopAppBar` を使用し、タイトルを中央揃えに変更しました。
    *   アプリのタイトルを「Tasks」から「Todoアプリ」に日本語化しました (`MainActivity.kt` 内 `TodoScreen`)。
    *   従来のフローティングアクションボタン（FAB）を廃止し、タスク追加機能をトップアプリバー右端の `IconButton`（「+」アイコン）に変更しました。
    *   トップアプリバーの背景色をテーマの `surface`（白）、タイトル色を `onSurface`（濃いグレー）に設定しました。
*   **フィルター表示のタブ化と日本語化**:
    *   従来のフィルターボタンを表示していた `FilterButtonsRow` Composable を削除し、新たに `TabRow` と `Tab` を使用した `TaskFilterTabs` Composable を作成しました (`MainActivity.kt`)。
    *   タブのテキストを以下のように日本語化しました:
        *   "ALL" → "すべて"
        *   "ACTIVE" → "未完了"
        *   "COMPLETED" → "完了済"
    *   選択されたタブにはプライマリーカラー（`AccentBlue`）のインジケーターとテキスト色を適用しました。
*   **タスクアイテムのカードスタイル化とインタラクション改善**:
    *   各タスクアイテム (`TaskItem` Composable) を `Card` で囲み、角丸 (`RoundedCornerShape(8.dp)`) と影 (`elevation`) を適用しました。カードの背景色はテーマの `surface`（白）です。
    *   従来の `Checkbox` を廃止し、タスクの完了状態を示すアイコン（未完了: `Icons.Outlined.RadioButtonUnchecked`、完了: `Icons.Filled.CheckCircle`）をタスクタイトルの前に配置しました。アイコンの色も状態に応じて変わります。
    *   タスクの完了/未完了の切り替えは、タスクタイトルと状態アイコンを含む領域のクリックで行うように変更しました。
    *   タスク削除用の `IconButton`（`Icons.Filled.Delete`）を各カードの右端に再配置し、削除機能を維持しました。アイコンの色はテーマの `error` カラーに設定しました。
*   **依存関係の追加**:
    *   `Icons.Outlined.RadioButtonUnchecked` アイコンを使用するために、`androidx.compose.material:material-icons-extended` (バージョン `1.7.8`) の依存関係を `gradle/libs.versions.toml` 及び `app/build.gradle.kts` に追加しました。
*   **関連ファイルの更新概要**:
    *   `MainActivity.kt`: `TodoScreen`, `TaskFilterTabs`, `TaskItem` の大幅な変更。
    *   `com.example.todo_app.ui.theme/Color.kt`: 新しい色の定義。
    *   `com.example.todo_app.ui.theme/Theme.kt`: `NewLightColorScheme` の定義と適用。
    *   `app/build.gradle.kts`: `material-icons-extended` 依存関係の追加。
    *   `gradle/libs.versions.toml`: `material-icons-extended` のバージョンとライブラリ定義の追加。

## 2025-09-21 (今日の日付に置き換えてください)

### タスクへの期限日と完了日の追加

*   **データモデルの更新 (`Task.kt`)**:
    *   `Task` データクラスに、期限日を保存する `deadline: Long?` と完了日を保存する `completionDate: Long?` プロパティ（どちらもNULL許容のタイムスタンプ）を追加しました。
*   **データベーススキーマの更新とマイグレーション (`AppDatabase.kt`)**:
    *   Roomデータベースのバージョンを `1` から `2` に更新しました。
    *   バージョン1から2へのマイグレーションパス (`MIGRATION_1_2`) を作成し、`tasks` テーブルに `deadline` (INTEGER NULLABLE) と `completionDate` (INTEGER NULLABLE) カラムを追加するSQLを定義しました。
    *   データベースビルダーに `.addMigrations(MIGRATION_1_2)` を追加して、スキーマ更新を適用しました。
*   **ViewModelの更新 (`TodoViewModel.kt`)**:
    *   `addTask` メソッドのシグネチャを `addTask(title: String, deadline: Long? = null)` に変更し、タスク追加時にオプションで期限日を受け取れるようにしました。新しいタスク作成時に `deadline` を設定します。
    *   `toggleTaskCompletion` メソッドを更新し、タスクの完了状態が変更された際に `completionDate` を自動的に設定またはクリアするようにしました:
        *   完了時: `completionDate` に現在のタイムスタンプを設定。
        *   未完了に戻した時: `completionDate` を `null` に設定。
*   **UIの更新 (`MainActivity.kt`)**:
    *   **日付フォーマットヘルパー関数追加**: `Long` 型のタイムスタンプを "yyyy/MM/dd" 形式の文字列に変換する `toFormattedDateString()` 拡張関数をトップレベルに追加しました。
    *   **タスク追加ダイアログの機能拡張 (`AddTaskDialog`)**:
        *   `onTaskAdd` コールバックを `(String, Long?) -> Unit` に変更し、期限日も渡せるようにしました。
        *   `DatePicker` と `DatePickerDialog` (Material 3) を使用して、ユーザーが期限日を選択できるUIを追加しました。
        *   選択された期限日はダイアログ内に表示され、タスク追加時にViewModelへ渡されます。
        *   必要な `@ExperimentalMaterial3Api` アノテーションと関連import文を追加しました。
    *   **タスクアイテム表示の更新 (`TaskItem`)**:
        *   タスクのタイトル下に、設定されていれば「期限: (日付)」および「完了: (日付)」を表示するように変更しました。
        *   日付表示は `bodySmall` スタイルを使用し、視覚的な階層をつけました。
        *   日付表示部分のインデント調整を行いました。
    *   **`TodoScreen` の呼び出し更新**: `AddTaskDialog` の呼び出し箇所で、新しい `onTaskAdd` シグネチャに合わせて `todoViewModel.addTask(title, deadline)` を呼び出すように修正しました。
    *   **プレビューの更新**: `AddTaskDialogPreview` と `TaskItemPreview` (`TaskItemCompletedPreview` を含む) に、新しいパラメータやサンプルデータを反映させました。
*   **動作確認**: 期限日の設定、タスクアイテムへの期限日と完了日の表示、完了状態変更に伴う完了日の更新が期待通りに動作することを確認しました。

