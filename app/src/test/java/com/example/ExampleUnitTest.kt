package com.example

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.example.data.ChallengeLibrary
import com.example.ui.components.AutoCompleteEngine
import com.example.ui.components.ErrorExplainer
import com.example.ui.editor.CodeEditorHelper
import com.example.ui.editor.CodeFormatter
import com.example.ui.editor.PythonSyntaxHighlighter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testAutoIndentAfterColon() {
        val oldText = "def hello():"
        val oldVal = TextFieldValue(oldText, selection = TextRange(oldText.length))
        val newVal = TextFieldValue(oldText + "\n", selection = TextRange(oldText.length + 1))

        val result = CodeEditorHelper.handleAutoIndent(oldVal, newVal)
        assertEquals("def hello():\n    ", result.text)
        assertEquals("def hello():\n    ".length, result.selection.start)
    }

    @Test
    fun testAutoIndentMaintainIndentation() {
        val oldText = "    x = 10"
        val oldVal = TextFieldValue(oldText, selection = TextRange(oldText.length))
        val newVal = TextFieldValue(oldText + "\n", selection = TextRange(oldText.length + 1))

        val result = CodeEditorHelper.handleAutoIndent(oldVal, newVal)
        assertEquals("    x = 10\n    ", result.text)
    }

    @Test
    fun testQuickKeyInsertPair() {
        val current = TextFieldValue("item", selection = TextRange(0, 4))
        val result = CodeEditorHelper.insertPairOrText(current, "(", ")")
        assertEquals("(item)", result.text)
    }

    @Test
    fun testQuickKeyInsertText() {
        val current = TextFieldValue("x = ", selection = TextRange(4))
        val result = CodeEditorHelper.insertText(current, "5")
        assertEquals("x = 5", result.text)
    }

    @Test
    fun testSyntaxHighlighterProducesAnnotatedString() {
        val pythonCode = "def test():\n    print('Hello')\n    return True\n"
        val annotated = PythonSyntaxHighlighter.highlight(pythonCode)
        assertEquals(pythonCode, annotated.text)
        assertTrue(annotated.spanStyles.isNotEmpty())
    }

    @Test
    fun testScreenEnumValues() {
        val screens = com.example.ui.AppScreen.values()
        assertEquals(2, screens.size)
        assertEquals(com.example.ui.AppScreen.EDITOR, screens[0])
        assertEquals(com.example.ui.AppScreen.OUTPUT, screens[1])
    }

    @Test
    fun testSnippetLibraryLoaded() {
        val snippets = com.example.data.SnippetLibrary.snippets
        assertTrue(snippets.isNotEmpty())
        assertTrue(snippets.any { it.title.contains("Fibonacci") })
    }

    @Test
    fun testFindReplaceLogic() {
        val initial = "value = 10\nprint(value)"
        val replaced = initial.replace("value", "number", ignoreCase = true)
        assertEquals("number = 10\nprint(number)", replaced)
    }

    @Test
    fun testSmartErrorExplainer() {
        val rawErr = "File \"<stdin>\", line 4\n    print(x)\nIndentationError: unexpected indent"
        val parsed = ErrorExplainer.parse(rawErr)
        assertNotNull(parsed)
        assertEquals("IndentationError", parsed?.errorType)
        assertEquals(4, parsed?.lineNumber)
        assertTrue(parsed?.suggestedFix?.contains("4 spaces") == true)
    }

    @Test
    fun testAutoCompleteSuggestions() {
        val code = "pri"
        val tf = TextFieldValue(code, selection = TextRange(3))
        val suggestions = AutoCompleteEngine.getSuggestions(tf)
        assertTrue(suggestions.contains("print()"))
    }

    @Test
    fun testCodeFormatter() {
        val unformatted = "x=5+10\nprint(x)"
        val formatted = CodeFormatter.formatPythonCode(unformatted)
        assertTrue(formatted.contains("x = 5 + 10"))
    }

    @Test
    fun testChallengeLibrary() {
        val list = ChallengeLibrary.challenges
        assertTrue(list.isNotEmpty())
        assertTrue(list.any { it.title.contains("Palindrome") })
    }

    @Test
    fun testFolderPathGrouping() {
        val samplePath = "models/neural_net.py"
        val parts = samplePath.split("/")
        assertEquals(2, parts.size)
        val folder = parts.dropLast(1).joinToString("/")
        val fileName = parts.last()
        assertEquals("models", folder)
        assertEquals("neural_net.py", fileName)
    }
}
