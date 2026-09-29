package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ChallengesDialog
import com.example.ui.components.SnippetsDialog
import com.example.ui.components.SocialShareDialog
import com.example.ui.drawer.ProjectFileDrawerSheet
import com.example.ui.files.DeleteConfirmDialog
import com.example.ui.files.NewFileDialog
import com.example.ui.files.NewFolderDialog
import com.example.ui.files.RenameFileDialog
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.OutputScreen
import kotlinx.coroutines.launch

@Composable
fun PyStudioScreen(
    viewModel: PyStudioViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val editorValue by viewModel.editorValue.collectAsStateWithLifecycle()
    val allFiles by viewModel.allFiles.collectAsStateWithLifecycle()
    val activeFile by viewModel.activeFile.collectAsStateWithLifecycle()
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()
    val outputChunks by viewModel.outputChunks.collectAsStateWithLifecycle()
    val pendingInputPrompt by viewModel.pendingInputPrompt.collectAsStateWithLifecycle()
    val lastErrorMessage by viewModel.lastErrorMessage.collectAsStateWithLifecycle()

    val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
    val canRedo by viewModel.canRedo.collectAsStateWithLifecycle()
    val fontSize by viewModel.fontSize.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    val highlightLine by viewModel.highlightLine.collectAsStateWithLifecycle()

    val showSearchBar by viewModel.showSearchBar.collectAsStateWithLifecycle()
    val findQuery by viewModel.findQuery.collectAsStateWithLifecycle()
    val replaceQuery by viewModel.replaceQuery.collectAsStateWithLifecycle()
    val searchMatchCount by viewModel.searchMatchCount.collectAsStateWithLifecycle()
    val showSnippetsDialog by viewModel.showSnippetsDialog.collectAsStateWithLifecycle()
    val showChallengesDialog by viewModel.showChallengesDialog.collectAsStateWithLifecycle()

    val showNewFileDialog by viewModel.showNewFileDialog.collectAsStateWithLifecycle()
    val showNewFolderDialog by viewModel.showNewFolderDialog.collectAsStateWithLifecycle()
    val showSocialShareDialog by viewModel.showSocialShareDialog.collectAsStateWithLifecycle()
    val fileToRename by viewModel.fileToRename.collectAsStateWithLifecycle()
    val fileToDelete by viewModel.fileToDelete.collectAsStateWithLifecycle()
    val saveStatusMessage by viewModel.saveStatusMessage.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.importFileFromUri(it)
        }
    }

    BackHandler(enabled = drawerState.isOpen || currentScreen == AppScreen.OUTPUT) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else if (currentScreen == AppScreen.OUTPUT) {
            viewModel.navigateToEditor()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentScreen == AppScreen.EDITOR,
        drawerContent = {
            ProjectFileDrawerSheet(
                files = allFiles,
                activeFileId = activeFile?.id,
                onSelectFile = { file ->
                    viewModel.switchToFile(file)
                    coroutineScope.launch { drawerState.close() }
                },
                onNewFileClick = {
                    coroutineScope.launch { drawerState.close() }
                    viewModel.openNewFileDialog()
                },
                onNewFolderClick = {
                    coroutineScope.launch { drawerState.close() }
                    viewModel.openNewFolderDialog()
                },
                onRenameClick = { file ->
                    coroutineScope.launch { drawerState.close() }
                    viewModel.startRenameFile(file)
                },
                onDeleteClick = { file ->
                    coroutineScope.launch { drawerState.close() }
                    viewModel.startDeleteFile(file)
                },
                onImportClick = {
                    coroutineScope.launch { drawerState.close() }
                    filePickerLauncher.launch(arrayOf("*/*"))
                },
                onExportZipClick = {
                    coroutineScope.launch { drawerState.close() }
                    viewModel.exportAllFilesAsZip()
                },
                onShareFileClick = { file ->
                    viewModel.shareFile(file)
                },
                onOpenChallengesClick = {
                    coroutineScope.launch { drawerState.close() }
                    viewModel.openChallengesDialog()
                },
                onSocialShareClick = {
                    coroutineScope.launch { drawerState.close() }
                    viewModel.openSocialShareDialog()
                },
                onInlineCreateFile = { fileName ->
                    viewModel.createNewFile(fileName)
                },
                onInlineCreateFolder = { folderName ->
                    viewModel.createNewFolder(folderName)
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                if (targetState == AppScreen.OUTPUT) {
                    (slideInHorizontally { width -> width } + fadeIn())
                        .togetherWith(slideOutHorizontally { width -> -width / 3 } + fadeOut())
                } else {
                    (slideInHorizontally { width -> -width / 3 } + fadeIn())
                        .togetherWith(slideOutHorizontally { width -> width } + fadeOut())
                }
            },
            label = "ScreenTransition",
            modifier = Modifier.fillMaxSize()
        ) { screen ->
            when (screen) {
                AppScreen.EDITOR -> {
                    EditorScreen(
                        editorValue = editorValue,
                        onEditorValueChange = { viewModel.onEditorValueChange(it) },
                        activeFile = activeFile,
                        allFiles = allFiles,
                        onSelectFileTab = { viewModel.switchToFile(it) },
                        onCloseFileTab = { viewModel.startDeleteFile(it) },
                        onNewFileClick = { viewModel.openNewFileDialog() },
                        isRunning = isRunning,
                        saveStatusMessage = saveStatusMessage,
                        fontSize = fontSize,
                        highlightLine = highlightLine,
                        suggestions = suggestions,
                        onSuggestionClick = { viewModel.applySuggestion(it) },
                        onZoomIn = { viewModel.zoomIn() },
                        onZoomOut = { viewModel.zoomOut() },
                        canUndo = canUndo,
                        canRedo = canRedo,
                        onUndoClick = { viewModel.undo() },
                        onRedoClick = { viewModel.redo() },
                        showSearchBar = showSearchBar,
                        findQuery = findQuery,
                        onFindQueryChange = { viewModel.onFindQueryChange(it) },
                        replaceQuery = replaceQuery,
                        onReplaceQueryChange = { viewModel.onReplaceQueryChange(it) },
                        searchMatchCount = searchMatchCount,
                        onToggleSearch = { viewModel.toggleSearchBar() },
                        onReplaceOne = { viewModel.replaceOne() },
                        onReplaceAll = { viewModel.replaceAll() },
                        onOpenSnippetsDialog = { viewModel.openSnippetsDialog() },
                        onOpenChallengesDialog = { viewModel.openChallengesDialog() },
                        onFormatCodeClick = { viewModel.formatCurrentCode() },
                        onShareCodeClick = { viewModel.shareActiveCode() },
                        onSocialShareClick = { viewModel.openSocialShareDialog() },
                        onExportZipClick = { viewModel.exportAllFilesAsZip() },
                        onRunClick = { viewModel.runCode() },
                        onStopClick = { viewModel.stopCode() },
                        onOpenDrawer = {
                            coroutineScope.launch { drawerState.open() }
                        },
                        onInsertQuickKey = { viewModel.insertQuickKey(it) }
                    )
                }
                AppScreen.OUTPUT -> {
                    OutputScreen(
                        fileName = activeFile?.name ?: "script.py",
                        outputChunks = outputChunks,
                        isRunning = isRunning,
                        pendingInputPrompt = pendingInputPrompt,
                        lastErrorMessage = lastErrorMessage,
                        fontSize = fontSize,
                        onBackToEditor = { viewModel.navigateToEditor() },
                        onRunAgain = { viewModel.runCode() },
                        onStopCode = { viewModel.stopCode() },
                        onClearOutput = { viewModel.clearOutput() },
                        onCopyOutput = { viewModel.copyOutputToClipboard() },
                        onSubmitInput = { viewModel.submitInput(it) },
                        onJumpToErrorLine = { line -> viewModel.jumpToLine(line) }
                    )
                }
            }
        }
    }

    // Dialogs
    if (showSnippetsDialog) {
        SnippetsDialog(
            onInsertSnippet = { snippet -> viewModel.insertSnippet(snippet) },
            onLoadAsNewFile = { snippet -> viewModel.loadSnippetAsNewFile(snippet) },
            onDismiss = { viewModel.closeSnippetsDialog() }
        )
    }

    if (showChallengesDialog) {
        ChallengesDialog(
            onLoadChallenge = { challenge -> viewModel.loadChallenge(challenge) },
            onDismiss = { viewModel.closeChallengesDialog() }
        )
    }

    if (showNewFileDialog) {
        NewFileDialog(
            onConfirm = { viewModel.createNewFile(it) },
            onDismiss = { viewModel.closeNewFileDialog() }
        )
    }

    if (showNewFolderDialog) {
        NewFolderDialog(
            onConfirm = { viewModel.createNewFolder(it) },
            onDismiss = { viewModel.closeNewFolderDialog() }
        )
    }

    if (showSocialShareDialog) {
        SocialShareDialog(
            activeFile = activeFile,
            onDismiss = { viewModel.closeSocialShareDialog() }
        )
    }

    fileToRename?.let { target ->
        RenameFileDialog(
            file = target,
            onConfirm = { id, newName -> viewModel.confirmRename(id, newName) },
            onDismiss = { viewModel.dismissRename() }
        )
    }

    fileToDelete?.let { target ->
        DeleteConfirmDialog(
            file = target,
            onConfirm = { viewModel.confirmDelete(it) },
            onDismiss = { viewModel.dismissDelete() }
        )
    }
}
