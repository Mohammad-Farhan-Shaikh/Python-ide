package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PythonCyan
import com.example.ui.theme.TextPrimary

@Composable
fun QuickKeyBar(
    canUndo: Boolean,
    canRedo: Boolean,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    onKeyClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val quickKeys = listOf(
        "Tab", ":", "(", ")", "[", "]", "{", "}", "\"", "'",
        "=", "+", "-", "*", "/", "%", "<", ">", "#", ".", "_"
    )

    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(DarkSurface)
            .horizontalScroll(scrollState)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("quick_key_bar"),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Undo Button
        Box(
            modifier = Modifier
                .heightIn(min = 44.dp)
                .widthIn(min = 44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (canUndo) DarkSurfaceVariant else DarkBorder.copy(alpha = 0.25f))
                .clickable(enabled = canUndo) { onUndoClick() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("quick_key_undo"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "↩ Undo",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (canUndo) PythonCyan else TextPrimary.copy(alpha = 0.3f)
            )
        }

        // Redo Button
        Box(
            modifier = Modifier
                .heightIn(min = 44.dp)
                .widthIn(min = 44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (canRedo) DarkSurfaceVariant else DarkBorder.copy(alpha = 0.25f))
                .clickable(enabled = canRedo) { onRedoClick() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("quick_key_redo"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "↪ Redo",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (canRedo) PythonCyan else TextPrimary.copy(alpha = 0.3f)
            )
        }

        quickKeys.forEach { key ->
            val isTab = key == "Tab"
            Box(
                modifier = Modifier
                    .heightIn(min = 44.dp)
                    .widthIn(min = if (isTab) 56.dp else 44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isTab) DarkSurfaceVariant else DarkBorder.copy(alpha = 0.6f))
                    .clickable { onKeyClick(key) }
                    .padding(horizontal = if (isTab) 10.dp else 8.dp, vertical = 6.dp)
                    .testTag("quick_key_$key"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = key,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = if (isTab) 13.sp else 16.sp,
                    color = if (isTab) PythonCyan else TextPrimary
                )
            }
        }
    }
}
