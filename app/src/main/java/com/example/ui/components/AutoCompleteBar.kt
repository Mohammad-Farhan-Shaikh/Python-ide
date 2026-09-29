package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PythonCyan
import com.example.ui.theme.PythonYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

object AutoCompleteEngine {
    private val standardKeywords = listOf(
        "def", "return", "import", "from", "as", "class",
        "if", "elif", "else", "for", "in", "while", "break", "continue",
        "try", "except", "finally", "with", "lambda", "yield",
        "print()", "len()", "range()", "plot()", "input()", "append()",
        "True", "False", "None"
    )

    fun getSuggestions(editorValue: TextFieldValue): List<String> {
        val text = editorValue.text
        val cursor = editorValue.selection.start
        if (cursor <= 0 || cursor > text.length) return emptyList()

        // Extract word currently being typed before cursor
        var start = cursor - 1
        while (start >= 0 && (text[start].isLetterOrDigit() || text[start] == '_')) {
            start--
        }
        start++

        val currentWord = text.substring(start, cursor)
        if (currentWord.length < 2) return emptyList()

        // Extract user identifiers from text (variables, function names)
        val identifierRegex = Regex("""\b[a-zA-Z_][a-zA-Z0-9_]*\b""")
        val userWords = identifierRegex.findAll(text)
            .map { it.value }
            .filter { it.length > 2 && it != currentWord }
            .distinct()
            .take(30)
            .toList()

        val allCandidates = (standardKeywords + userWords).distinct()

        return allCandidates.filter { candidate ->
            val cleanCand = candidate.removeSuffix("()")
            cleanCand.startsWith(currentWord, ignoreCase = true) && !cleanCand.equals(currentWord, ignoreCase = true)
        }.take(8)
    }

    fun applySuggestion(editorValue: TextFieldValue, suggestion: String): TextFieldValue {
        val text = editorValue.text
        val cursor = editorValue.selection.start
        var start = cursor - 1
        while (start >= 0 && (text[start].isLetterOrDigit() || text[start] == '_')) {
            start--
        }
        start++

        val before = text.substring(0, start)
        val after = text.substring(cursor)

        val insertText = when (suggestion) {
            "print()" -> "print("
            "len()" -> "len("
            "range()" -> "range("
            "plot()" -> "plot("
            "input()" -> "input("
            "append()" -> "append("
            "def" -> "def "
            "for" -> "for "
            "while" -> "while "
            "if" -> "if "
            "elif" -> "elif "
            "import" -> "import "
            "from" -> "from "
            "class" -> "class "
            else -> suggestion + " "
        }

        val newText = before + insertText + after
        val newCursor = start + insertText.length

        return TextFieldValue(
            text = newText,
            selection = androidx.compose.ui.text.TextRange(newCursor)
        )
    }
}

@Composable
fun AutoCompleteBar(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (suggestions.isEmpty()) return

    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp)
            .background(DarkSurface)
            .horizontalScroll(scrollState)
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .testTag("autocomplete_bar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "✨",
            fontSize = 12.sp,
            modifier = Modifier.padding(end = 2.dp)
        )

        suggestions.forEach { suggestion ->
            val isFunc = suggestion.endsWith("()")
            val isKeyword = suggestion in listOf("def", "return", "import", "class", "if", "for", "while")

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkSurfaceVariant)
                    .clickable { onSuggestionClick(suggestion) }
                    .padding(horizontal = 9.dp, vertical = 4.dp)
                    .testTag("suggestion_item_$suggestion")
            ) {
                Text(
                    text = suggestion,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        isFunc -> PythonYellow
                        isKeyword -> PythonCyan
                        else -> TextPrimary
                    }
                )
            }
        }
    }
}
