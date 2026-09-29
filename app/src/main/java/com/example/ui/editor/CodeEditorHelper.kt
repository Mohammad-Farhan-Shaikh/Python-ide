package com.example.ui.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

object CodeEditorHelper {

    fun handleAutoIndent(oldValue: TextFieldValue, newValue: TextFieldValue): TextFieldValue {
        val oldText = oldValue.text
        val newText = newValue.text

        // Check if exactly one character was added and it is a newline '\n'
        val oldSel = oldValue.selection.start
        val newSel = newValue.selection.start

        if (newText.length == oldText.length + 1 && newSel == oldSel + 1 && newSel > 0 && newText[newSel - 1] == '\n') {
            val newlinePos = newSel - 1

            // Find the start of the line preceding this newline
            var prevLineStart = newlinePos - 1
            while (prevLineStart >= 0 && newText[prevLineStart] != '\n') {
                prevLineStart--
            }
            prevLineStart++ // move past '\n' or to 0

            val prevLine = newText.substring(prevLineStart, newlinePos)

            // Calculate leading spaces / tabs
            val leadingWhitespace = prevLine.takeWhile { it == ' ' || it == '\t' }
            val trimmedPrev = prevLine.trim()

            val extraIndent = if (trimmedPrev.endsWith(":")) "    " else ""
            val totalIndent = leadingWhitespace + extraIndent

            if (totalIndent.isNotEmpty()) {
                val before = newText.substring(0, newlinePos + 1)
                val after = newText.substring(newlinePos + 1)
                val updatedText = before + totalIndent + after
                val newCursor = newlinePos + 1 + totalIndent.length
                return TextFieldValue(
                    text = updatedText,
                    selection = TextRange(newCursor)
                )
            }
        }

        return newValue
    }

    fun insertText(current: TextFieldValue, stringToInsert: String): TextFieldValue {
        val text = current.text
        val selection = current.selection
        val start = selection.min
        val end = selection.max

        val newText = text.substring(0, start) + stringToInsert + text.substring(end)
        val newCursor = start + stringToInsert.length
        return TextFieldValue(
            text = newText,
            selection = TextRange(newCursor)
        )
    }

    fun insertPairOrText(current: TextFieldValue, open: String, close: String): TextFieldValue {
        val text = current.text
        val selection = current.selection
        val start = selection.min
        val end = selection.max

        return if (start != end) {
            // Wrap selected text
            val selectedText = text.substring(start, end)
            val wrapped = open + selectedText + close
            val newText = text.substring(0, start) + wrapped + text.substring(end)
            TextFieldValue(
                text = newText,
                selection = TextRange(start + open.length, end + open.length)
            )
        } else {
            // Insert both and place cursor in middle
            val newText = text.substring(0, start) + open + close + text.substring(end)
            TextFieldValue(
                text = newText,
                selection = TextRange(start + open.length)
            )
        }
    }
}
