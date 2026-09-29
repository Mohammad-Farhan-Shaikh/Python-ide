package com.example.ui.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

object PythonSyntaxHighlighter {

    val KeywordColor = Color(0xFFC678DD)     // Purple
    val BuiltinColor = Color(0xFF61AFEF)     // Cyan / Sky Blue
    val ConstantColor = Color(0xFFD19A66)    // Light Orange
    val StringColor = Color(0xFF98C379)      // Green
    val NumberColor = Color(0xFFE5C07B)      // Yellow / Amber
    val CommentColor = Color(0xFF7E8492)     // Slate Gray (Italic)
    val OperatorColor = Color(0xFF56B6C2)    // Teal
    val FunctionNameColor = Color(0xFF61AFEF)// Blue
    val DefaultColor = Color(0xFFABB2BF)     // Off-white / gray

    private val KEYWORDS = setOf(
        "def", "class", "import", "from", "as", "return", "if", "elif", "else",
        "for", "while", "break", "continue", "pass", "try", "except", "finally",
        "raise", "with", "in", "is", "not", "and", "or", "lambda", "yield",
        "global", "nonlocal", "assert", "async", "await", "del"
    )

    private val BUILTINS = setOf(
        "print", "input", "len", "range", "enumerate", "zip", "map", "filter",
        "open", "type", "isinstance", "int", "float", "str", "bool", "list",
        "dict", "set", "tuple", "sum", "min", "max", "abs", "round", "sorted",
        "reversed", "super", "help", "dir", "pow", "id", "format", "iter", "next"
    )

    private val CONSTANTS = setOf(
        "True", "False", "None"
    )

    fun highlight(text: String): AnnotatedString {
        val builder = AnnotatedString.Builder(text)
        val length = text.length
        var i = 0

        while (i < length) {
            val c = text[i]

            // 1. Comments: '#' to end of line
            if (c == '#') {
                val start = i
                while (i < length && text[i] != '\n') {
                    i++
                }
                builder.addStyle(
                    SpanStyle(color = CommentColor, fontStyle = FontStyle.Italic),
                    start,
                    i
                )
                continue
            }

            // 2. Triple-quoted strings
            if ((c == '"' || c == '\'') && i + 2 < length && text[i + 1] == c && text[i + 2] == c) {
                val quote = "$c$c$c"
                val start = i
                i += 3
                while (i + 2 < length) {
                    if (text[i] == c && text[i + 1] == c && text[i + 2] == c) {
                        i += 3
                        break
                    }
                    if (text[i] == '\\' && i + 1 < length) {
                        i += 2
                    } else {
                        i++
                    }
                }
                builder.addStyle(SpanStyle(color = StringColor), start, i.coerceAtMost(length))
                continue
            }

            // 3. Single-line strings
            if (c == '"' || c == '\'') {
                val quote = c
                val start = i
                i++
                while (i < length && text[i] != '\n') {
                    if (text[i] == '\\' && i + 1 < length) {
                        i += 2
                        continue
                    }
                    if (text[i] == quote) {
                        i++
                        break
                    }
                    i++
                }
                builder.addStyle(SpanStyle(color = StringColor), start, i.coerceAtMost(length))
                continue
            }

            // 4. Numbers (integers, floats, hex, binary)
            if (c.isDigit()) {
                val start = i
                while (i < length && (text[i].isLetterOrDigit() || text[i] == '.' || text[i] == '_')) {
                    i++
                }
                builder.addStyle(SpanStyle(color = NumberColor), start, i)
                continue
            }

            // 5. Identifiers / Keywords / Builtins
            if (c.isLetter() || c == '_') {
                val start = i
                while (i < length && (text[i].isLetterOrDigit() || text[i] == '_')) {
                    i++
                }
                val word = text.substring(start, i)
                when {
                    KEYWORDS.contains(word) -> {
                        builder.addStyle(
                            SpanStyle(color = KeywordColor, fontWeight = FontWeight.Bold),
                            start,
                            i
                        )
                    }
                    BUILTINS.contains(word) -> {
                        builder.addStyle(
                            SpanStyle(color = BuiltinColor, fontWeight = FontWeight.SemiBold),
                            start,
                            i
                        )
                    }
                    CONSTANTS.contains(word) -> {
                        builder.addStyle(
                            SpanStyle(color = ConstantColor, fontWeight = FontWeight.Bold),
                            start,
                            i
                        )
                    }
                }
                continue
            }

            // 6. Operators
            if (c in "+-*/%=<>!&|^~:") {
                builder.addStyle(SpanStyle(color = OperatorColor), i, i + 1)
            }

            i++
        }

        return builder.toAnnotatedString()
    }
}

class PythonVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val highlighted = PythonSyntaxHighlighter.highlight(text.text)
        return TransformedText(highlighted, OffsetMapping.Identity)
    }
}
