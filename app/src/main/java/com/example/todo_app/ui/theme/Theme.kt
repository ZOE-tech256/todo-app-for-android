package com.example.todo_app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color // Colorをインポート
import androidx.compose.ui.platform.LocalContext

// 既存のDarkColorScheme (今回は変更しませんが、将来的には新しいスタイルに合わせて調整できます)
private val DarkColorScheme = darkColorScheme(
    primary = Purple80, // これらは後で新しいカラースキームに合わせて更新可能
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF1C1B1F), // 例: ダークテーマの背景
    surface = Color(0xFF2C2B2F),   // 例: ダークテーマのカード背景
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5)
)

// 新しいカラースキーム (ライトテーマ用)
private val NewLightColorScheme = lightColorScheme(
    primary = AccentBlue,
    onPrimary = OnAccentBlue,
    primaryContainer = AccentBlue, // FABやアクティブな要素の背景などに使用検討
    onPrimaryContainer = OnAccentBlue,
    secondary = AccentBlue, // アプリ内で第二のアクセントとして使用する場合
    onSecondary = OnAccentBlue,
    tertiary = AccentBlue, // 第三のアクセント
    onTertiary = OnAccentBlue,
    error = Color(0xFFB00020), // 標準的なエラー色
    onError = Color.White,
    background = AppLightGray,    // アプリ全体の背景
    onBackground = PrimaryTextDark, // 背景上のテキスト色
    surface = CardWhite,          // カードやダイアログなどの表面の色
    onSurface = PrimaryTextDark,    // 表面上のテキスト色
    surfaceVariant = AppLightGray, // 例えば、非選択状態のタブの背景や、入力フィールドの枠線など、surfaceより若干目立たない部分
    onSurfaceVariant = SecondaryTextGray, // surfaceVariant上のテキスト色 (例: 非選択タブのテキスト)
    outline = SecondaryTextGray // 区切り線や枠線など
)

@Composable
fun Todo_AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic colorをデフォルトで無効にし、常にカスタムテーマを適用
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme // ダークテーマは既存のものを維持 (後で更新可能)
        else -> NewLightColorScheme  // ライトテーマには新しいカラースキームを適用
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Typographyは変更なし
        content = content
    )
}
