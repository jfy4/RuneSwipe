// ─────────────────────────────────────────────────────────────────────────────
// app/src/main/java/com/example/runeswipe/ui/theme/Theme.kt
// ─────────────────────────────────────────────────────────────────────────────
package com.example.runeswipe.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun RuneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val dark = darkColorScheme()
    val light = lightColorScheme()
    MaterialTheme(
        colorScheme = if (darkTheme) dark else light,
        content = content
    )
}


// package com.example.runeswipe.ui.theme

// import androidx.compose.foundation.isSystemInDarkTheme
// import androidx.compose.material3.MaterialTheme
// import androidx.compose.material3.darkColorScheme
// import androidx.compose.material3.lightColorScheme
// import androidx.compose.runtime.Composable

// @Composable
// fun RuneTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
//     val dark = darkColorScheme()
//     val light = lightColorScheme()
//     MaterialTheme(colorScheme = if (darkTheme) dark else light, content = content)
// }
