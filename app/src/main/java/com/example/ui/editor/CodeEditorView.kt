package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CodeEditorBg
import com.example.ui.theme.CodeGutterBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.PythonCyan
import com.example.ui.theme.StopRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun CodeEditorView(
    editorValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    fontSize: Float = 13f,
    highlightLine: Int? = null,
    modifier: Modifier = Modifier
) {
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    val visualTransformation = remember { PythonVisualTransformation() }

    val lineCount = remember(editorValue.text) {
        editorValue.text.count { it == '\n' } + 1
    }

    val dynamicLineHeight = (fontSize * 1.54f).sp
    val dynamicFontSize = fontSize.sp

    // Auto-scroll to error line if requested
    LaunchedEffect(highlightLine) {
        if (highlightLine != null && highlightLine in 1..lineCount) {
            val targetScroll = (highlightLine - 1) * (fontSize * 2.2f).toInt()
            verticalScrollState.animateScrollTo(targetScroll)
        }
    }

    Box(
        modifier = modifier
            .background(CodeEditorBg)
            .testTag("code_editor_container")
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState)
        ) {
            // Line numbers column
            Column(
                modifier = Modifier
                    .background(CodeGutterBg)
                    .padding(horizontal = 8.dp, vertical = 12.dp)
                    .widthIn(min = 36.dp),
                horizontalAlignment = Alignment.End
            ) {
                for (i in 1..lineCount) {
                    val isErrorLine = highlightLine == i
                    Text(
                        text = "$i",
                        fontFamily = FontFamily.Monospace,
                        fontSize = dynamicFontSize,
                        lineHeight = dynamicLineHeight,
                        color = if (isErrorLine) StopRed else TextMuted
                    )
                }
            }

            // Divider between gutter and code
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(DarkBorder)
            )

            // Code input area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScrollState)
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                if (editorValue.text.isEmpty()) {
                    Text(
                        text = "# Start writing Python code here...\nprint('Hello World')",
                        fontFamily = FontFamily.Monospace,
                        fontSize = dynamicFontSize,
                        lineHeight = dynamicLineHeight,
                        color = TextMuted.copy(alpha = 0.5f)
                    )
                }

                BasicTextField(
                    value = editorValue,
                    onValueChange = onValueChange,
                    visualTransformation = visualTransformation,
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = dynamicFontSize,
                        lineHeight = dynamicLineHeight,
                        color = TextPrimary
                    ),
                    cursorBrush = SolidColor(PythonCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("code_editor_input")
                )
            }
        }
    }
}
