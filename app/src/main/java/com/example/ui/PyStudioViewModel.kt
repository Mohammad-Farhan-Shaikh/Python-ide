package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.FileRepository
import com.example.data.PythonChallenge
import com.example.data.PythonFile
import com.example.data.PythonSnippet
import com.example.engine.OutputChunk
import com.example.engine.PythonRunner
import com.example.ui.components.AutoCompleteEngine
import com.example.ui.editor.CodeEditorHelper
import com.example.ui.editor.CodeFormatter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

enum class AppScreen {
    EDITOR,
    OUTPUT
}

class PyStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = FileRepository(database.pythonFileDao())
    val pythonRunner = PythonRunner(application)

    private val _currentScreen = MutableStateFlow(AppScreen.EDITOR)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    val allFiles: StateFlow<List<PythonFile>> = repository.allFiles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeFile: StateFlow<PythonFile?> = repository.activeFileFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _editorValue = MutableStateFlow(TextFieldValue(""))
    val editorValue: StateFlow<TextFieldValue> = _editorValue.asStateFlow()

    // Undo / Redo history stacks
    private val undoStack = ArrayDeque<String>()
    private val redoStack = ArrayDeque<String>()
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()
    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    // Font size setting (zoom in / zoom out)
    private val _fontSize = MutableStateFlow(13.5f)
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()

    // Auto-complete suggestions
    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    // Highlighted line from Smart Error Explainer
    private val _highlightLine = MutableStateFlow<Int?>(null)
    val highlightLine: StateFlow<Int?> = _highlightLine.asStateFlow()

    // Search and Replace
    private val _showSearchBar = MutableStateFlow(false)
    val showSearchBar: StateFlow<Boolean> = _showSearchBar.asStateFlow()
    private val _findQuery = MutableStateFlow("")
    val findQuery: StateFlow<String> = _findQuery.asStateFlow()
    private val _replaceQuery = MutableStateFlow("")
    val replaceQuery: StateFlow<String> = _replaceQuery.asStateFlow()
    private val _searchMatchCount = MutableStateFlow(0)
    val searchMatchCount: StateFlow<Int> = _searchMatchCount.asStateFlow()

    // Snippets / Templates Dialog
    private val _showSnippetsDialog = MutableStateFlow(false)
    val showSnippetsDialog: StateFlow<Boolean> = _showSnippetsDialog.asStateFlow()

    // Challenges / Exercises Dialog
    private val _showChallengesDialog = MutableStateFlow(false)
    val showChallengesDialog: StateFlow<Boolean> = _showChallengesDialog.asStateFlow()

    val isRunning: StateFlow<Boolean> = pythonRunner.isRunning
    val outputChunks: StateFlow<List<OutputChunk>> = pythonRunner.outputChunks
    val pendingInputPrompt: StateFlow<String?> = pythonRunner.pendingInputPrompt
    val lastErrorMessage: StateFlow<String?> = pythonRunner.lastErrorMessage
    val isEngineReady: StateFlow<Boolean> = pythonRunner.isEngineReady

    private val _showFilesDialog = MutableStateFlow(false)
    val showFilesDialog: StateFlow<Boolean> = _showFilesDialog.asStateFlow()

    private val _showNewFileDialog = MutableStateFlow(false)
    val showNewFileDialog: StateFlow<Boolean> = _showNewFileDialog.asStateFlow()

    private val _showNewFolderDialog = MutableStateFlow(false)
    val showNewFolderDialog: StateFlow<Boolean> = _showNewFolderDialog.asStateFlow()

    private val _showSocialShareDialog = MutableStateFlow(false)
    val showSocialShareDialog: StateFlow<Boolean> = _showSocialShareDialog.asStateFlow()

    private val _fileToRename = MutableStateFlow<PythonFile?>(null)
    val fileToRename: StateFlow<PythonFile?> = _fileToRename.asStateFlow()

    private val _fileToDelete = MutableStateFlow<PythonFile?>(null)
    val fileToDelete: StateFlow<PythonFile?> = _fileToDelete.asStateFlow()

    private val _saveStatusMessage = MutableStateFlow<String?>(null)
    val saveStatusMessage: StateFlow<String?> = _saveStatusMessage.asStateFlow()

    private var autoSaveJob: Job? = null
    private var lastActiveFileId: Long? = null

    init {
        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
        }

        viewModelScope.launch {
            activeFile.collect { file ->
                if (file != null && file.id != lastActiveFileId) {
                    lastActiveFileId = file.id
                    _editorValue.value = TextFieldValue(
                        text = file.content,
                        selection = TextRange(file.content.length.coerceAtMost(50))
                    )
                    undoStack.clear()
                    redoStack.clear()
                    updateUndoRedoStates()
                    _suggestions.value = emptyList()
                    _highlightLine.value = null
                }
            }
        }
    }

    private fun updateUndoRedoStates() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

    private fun pushUndoState(oldText: String) {
        if (undoStack.isEmpty() || undoStack.last() != oldText) {
            undoStack.addLast(oldText)
            if (undoStack.size > 50) {
                undoStack.removeFirst()
            }
            redoStack.clear()
            updateUndoRedoStates()
        }
    }

    fun onEditorValueChange(newValue: TextFieldValue) {
        val oldText = _editorValue.value.text
        if (oldText != newValue.text) {
            pushUndoState(oldText)
            _highlightLine.value = null
        }

        val updatedValue = CodeEditorHelper.handleAutoIndent(_editorValue.value, newValue)
        _editorValue.value = updatedValue
        _suggestions.value = AutoCompleteEngine.getSuggestions(updatedValue)
        updateSearchMatches(_findQuery.value, updatedValue.text)
        scheduleAutoSave(updatedValue.text)
    }

    fun applySuggestion(suggestion: String) {
        val current = _editorValue.value
        pushUndoState(current.text)
        val updated = AutoCompleteEngine.applySuggestion(current, suggestion)
        _editorValue.value = updated
        _suggestions.value = emptyList()
        scheduleAutoSave(updated.text)
    }

    fun formatCurrentCode() {
        val current = _editorValue.value.text
        if (current.isBlank()) return
        val formatted = CodeFormatter.formatPythonCode(current)
        if (formatted != current) {
            pushUndoState(current)
            _editorValue.value = TextFieldValue(
                text = formatted,
                selection = TextRange(formatted.length.coerceAtMost(50))
            )
            scheduleAutoSave(formatted)
            _saveStatusMessage.value = "Code formatted (PEP8)"
            viewModelScope.launch {
                delay(2000)
                if (_saveStatusMessage.value == "Code formatted (PEP8)") {
                    _saveStatusMessage.value = null
                }
            }
        }
    }

    fun jumpToLine(lineNumber: Int) {
        _highlightLine.value = lineNumber
        _currentScreen.value = AppScreen.EDITOR
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val currentText = _editorValue.value.text
        redoStack.addLast(currentText)

        val previousText = undoStack.removeLast()
        _editorValue.value = TextFieldValue(
            text = previousText,
            selection = TextRange(previousText.length.coerceAtMost(50))
        )
        updateUndoRedoStates()
        scheduleAutoSave(previousText)
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val currentText = _editorValue.value.text
        undoStack.addLast(currentText)

        val nextText = redoStack.removeLast()
        _editorValue.value = TextFieldValue(
            text = nextText,
            selection = TextRange(nextText.length.coerceAtMost(50))
        )
        updateUndoRedoStates()
        scheduleAutoSave(nextText)
    }

    fun zoomIn() {
        if (_fontSize.value < 22f) {
            _fontSize.value += 1.5f
        }
    }

    fun zoomOut() {
        if (_fontSize.value > 10.5f) {
            _fontSize.value -= 1.5f
        }
    }

    // Search and Replace Functions
    fun toggleSearchBar() {
        val next = !_showSearchBar.value
        _showSearchBar.value = next
        if (!next) {
            _findQuery.value = ""
            _replaceQuery.value = ""
            _searchMatchCount.value = 0
        }
    }

    fun onFindQueryChange(query: String) {
        _findQuery.value = query
        updateSearchMatches(query, _editorValue.value.text)
    }

    fun onReplaceQueryChange(query: String) {
        _replaceQuery.value = query
    }

    private fun updateSearchMatches(query: String, text: String) {
        if (query.isEmpty()) {
            _searchMatchCount.value = 0
            return
        }
        var count = 0
        var idx = 0
        while (true) {
            val next = text.indexOf(query, idx, ignoreCase = true)
            if (next == -1) break
            count++
            idx = next + query.length
        }
        _searchMatchCount.value = count
    }

    fun replaceOne() {
        val query = _findQuery.value
        if (query.isEmpty()) return
        val text = _editorValue.value.text
        val idx = text.indexOf(query, ignoreCase = true)
        if (idx != -1) {
            pushUndoState(text)
            val newText = text.substring(0, idx) + _replaceQuery.value + text.substring(idx + query.length)
            _editorValue.value = TextFieldValue(
                text = newText,
                selection = TextRange(idx + _replaceQuery.value.length)
            )
            updateSearchMatches(query, newText)
            scheduleAutoSave(newText)
        }
    }

    fun replaceAll() {
        val query = _findQuery.value
        if (query.isEmpty()) return
        val text = _editorValue.value.text
        pushUndoState(text)
        val newText = text.replace(query, _replaceQuery.value, ignoreCase = true)
        _editorValue.value = TextFieldValue(
            text = newText,
            selection = TextRange(newText.length.coerceAtMost(50))
        )
        updateSearchMatches(query, newText)
        scheduleAutoSave(newText)
    }

    // Snippets / Templates
    fun openSnippetsDialog() {
        _showSnippetsDialog.value = true
    }

    fun closeSnippetsDialog() {
        _showSnippetsDialog.value = false
    }

    fun openChallengesDialog() {
        _showChallengesDialog.value = true
    }

    fun closeChallengesDialog() {
        _showChallengesDialog.value = false
    }

    fun loadChallenge(challenge: PythonChallenge) {
        viewModelScope.launch {
            val fileName = challenge.title.lowercase().replace(" ", "_").replace(",", "") + ".py"
            repository.createNewFile(fileName, challenge.starterCode)
            _showChallengesDialog.value = false
            _currentScreen.value = AppScreen.EDITOR
        }
    }

    fun insertSnippet(snippet: PythonSnippet) {
        val current = _editorValue.value
        pushUndoState(current.text)
        val updated = CodeEditorHelper.insertText(current, "\n" + snippet.code + "\n")
        _editorValue.value = updated
        scheduleAutoSave(updated.text)
        _showSnippetsDialog.value = false
    }

    fun loadSnippetAsNewFile(snippet: PythonSnippet) {
        viewModelScope.launch {
            val fileName = snippet.title.lowercase().replace(" ", "_").replace(",", "") + ".py"
            repository.createNewFile(fileName, snippet.code)
            _showSnippetsDialog.value = false
            _currentScreen.value = AppScreen.EDITOR
        }
    }

    // Sharing & Clipboard Actions
    fun shareActiveCode() {
        val file = activeFile.value
        val code = _editorValue.value.text
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, file?.name ?: "script.py")
            putExtra(Intent.EXTRA_TEXT, code)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, "Share Python Script via").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        getApplication<Application>().startActivity(chooser)
    }

    fun shareFile(file: PythonFile) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, file.name)
            putExtra(Intent.EXTRA_TEXT, file.content)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, "Share ${file.name} via").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        getApplication<Application>().startActivity(chooser)
    }

    fun exportAllFilesAsZip() {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val files = allFiles.value
                if (files.isEmpty()) {
                    Toast.makeText(context, "No files to export", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val zipFile = java.io.File(context.cacheDir, "pystudio_project.zip")
                if (zipFile.exists()) {
                    zipFile.delete()
                }

                java.util.zip.ZipOutputStream(java.io.FileOutputStream(zipFile)).use { zos ->
                    for (file in files) {
                        val entry = java.util.zip.ZipEntry(file.name)
                        zos.putNextEntry(entry)
                        val bytes = file.content.toByteArray(Charsets.UTF_8)
                        zos.write(bytes, 0, bytes.size)
                        zos.closeEntry()
                    }
                }

                val zipUri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    zipFile
                )

                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/zip"
                    putExtra(Intent.EXTRA_STREAM, zipUri)
                    putExtra(Intent.EXTRA_SUBJECT, "PyStudio Project Files (.zip)")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                val chooser = Intent.createChooser(sendIntent, "Export Project .ZIP via").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "Failed to export ZIP: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun copyOutputToClipboard() {
        val chunks = outputChunks.value
        if (chunks.isEmpty()) return
        val fullOutput = chunks.joinToString("") { chunk ->
            when (chunk) {
                is OutputChunk.Standard -> chunk.text
                is OutputChunk.Error -> chunk.text
                is OutputChunk.System -> chunk.text
                is OutputChunk.UserInput -> chunk.text
                is OutputChunk.Plot -> "[Plot: ${chunk.title} (${chunk.type})]\n"
            }
        }
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("PyStudio Output", fullOutput)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(getApplication(), "Console output copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun importFileFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val contentResolver = context.contentResolver
                var fileName = "imported_script.py"

                val cursor = contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            fileName = it.getString(nameIndex) ?: "imported_script.py"
                        }
                    }
                }

                val stringBuilder = StringBuilder()
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line: String? = reader.readLine()
                        while (line != null) {
                            stringBuilder.append(line).append("\n")
                            line = reader.readLine()
                        }
                    }
                }

                repository.createNewFile(fileName, stringBuilder.toString())
                _showFilesDialog.value = false
                Toast.makeText(context, "Imported '$fileName' successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "Failed to import file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun scheduleAutoSave(content: String) {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(800)
            activeFile.value?.let { file ->
                repository.autoSaveContent(file.id, content)
                _saveStatusMessage.value = "Auto-saved"
                delay(1500)
                if (_saveStatusMessage.value == "Auto-saved") {
                    _saveStatusMessage.value = null
                }
            }
        }
    }

    fun manualSave() {
        val currentContent = _editorValue.value.text
        activeFile.value?.let { file ->
            viewModelScope.launch {
                repository.autoSaveContent(file.id, currentContent)
                _saveStatusMessage.value = "Saved successfully"
                delay(2000)
                if (_saveStatusMessage.value == "Saved successfully") {
                    _saveStatusMessage.value = null
                }
            }
        }
    }

    fun navigateToOutput() {
        _currentScreen.value = AppScreen.OUTPUT
    }

    fun navigateToEditor() {
        _currentScreen.value = AppScreen.EDITOR
    }

    fun setScreen(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun runCode() {
        val code = _editorValue.value.text
        activeFile.value?.let { file ->
            viewModelScope.launch {
                repository.autoSaveContent(file.id, code)
            }
        }
        pythonRunner.clearOutput()
        _currentScreen.value = AppScreen.OUTPUT
        pythonRunner.executeCode(code)
    }

    fun stopCode() {
        pythonRunner.stopExecution()
    }

    fun clearOutput() {
        pythonRunner.clearOutput()
    }

    fun submitInput(input: String) {
        pythonRunner.sendInput(input)
    }

    fun insertQuickKey(key: String) {
        val current = _editorValue.value
        pushUndoState(current.text)
        val updated = when (key) {
            "Tab" -> CodeEditorHelper.insertText(current, "    ")
            "(" -> CodeEditorHelper.insertPairOrText(current, "(", ")")
            "[" -> CodeEditorHelper.insertPairOrText(current, "[", "]")
            "{" -> CodeEditorHelper.insertPairOrText(current, "{", "}")
            "\"" -> CodeEditorHelper.insertPairOrText(current, "\"", "\"")
            "'" -> CodeEditorHelper.insertPairOrText(current, "'", "'")
            else -> CodeEditorHelper.insertText(current, key)
        }
        _editorValue.value = updated
        _suggestions.value = AutoCompleteEngine.getSuggestions(updated)
        scheduleAutoSave(updated.text)
    }

    fun openFilesDialog() {
        _showFilesDialog.value = true
    }

    fun closeFilesDialog() {
        _showFilesDialog.value = false
    }

    fun openNewFileDialog() {
        _showNewFileDialog.value = true
    }

    fun closeNewFileDialog() {
        _showNewFileDialog.value = false
    }

    fun openNewFolderDialog() {
        _showNewFolderDialog.value = true
    }

    fun closeNewFolderDialog() {
        _showNewFolderDialog.value = false
    }

    fun openSocialShareDialog() {
        _showSocialShareDialog.value = true
    }

    fun closeSocialShareDialog() {
        _showSocialShareDialog.value = false
    }

    fun createNewFolder(folderName: String) {
        val trimmed = folderName.trim().replace("/", "").replace("\\", "")
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            // In VS Code, creating a folder usually creates an initial script e.g. folder_name/main.py
            val defaultFilePath = "$trimmed/__init__.py"
            repository.createNewFile(defaultFilePath, "# Package / Module: $trimmed\n\ndef hello():\n    print(\"Hello from $trimmed!\")\n")
            _showNewFolderDialog.value = false
        }
    }

    fun createNewFile(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.createNewFile(trimmed)
            _showNewFileDialog.value = false
            _showFilesDialog.value = false
        }
    }

    fun switchToFile(file: PythonFile) {
        if (file.id == activeFile.value?.id) {
            _showFilesDialog.value = false
            return
        }
        activeFile.value?.let { curr ->
            viewModelScope.launch {
                repository.autoSaveContent(curr.id, _editorValue.value.text)
                repository.switchActiveFile(file.id)
                _showFilesDialog.value = false
            }
        } ?: run {
            viewModelScope.launch {
                repository.switchActiveFile(file.id)
                _showFilesDialog.value = false
            }
        }
    }

    fun startRenameFile(file: PythonFile) {
        _fileToRename.value = file
    }

    fun dismissRename() {
        _fileToRename.value = null
    }

    fun confirmRename(fileId: Long, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isNotEmpty()) {
            viewModelScope.launch {
                repository.renameFile(fileId, trimmed)
                _fileToRename.value = null
            }
        }
    }

    fun startDeleteFile(file: PythonFile) {
        _fileToDelete.value = file
    }

    fun dismissDelete() {
        _fileToDelete.value = null
    }

    fun confirmDelete(file: PythonFile) {
        viewModelScope.launch {
            repository.deleteFile(file)
            _fileToDelete.value = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        activeFile.value?.let { file ->
            viewModelScope.launch {
                repository.autoSaveContent(file.id, _editorValue.value.text)
            }
        }
        pythonRunner.destroy()
    }
}
