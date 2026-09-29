package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.PythonFile
import com.example.ui.components.AutoCompleteBar
import com.example.ui.components.EditorTabBar
import com.example.ui.components.SearchReplaceBar
import com.example.ui.editor.CodeEditorView
import com.example.ui.editor.QuickKeyBar
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PythonCyan
import com.example.ui.theme.PythonYellow
import com.example.ui.theme.RunGreen
import com.example.ui.theme.StopRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    editorValue: TextFieldValue,
    onEditorValueChange: (TextFieldValue) -> Unit,
    activeFile: PythonFile?,
    allFiles: List<PythonFile>,
    onSelectFileTab: (PythonFile) -> Unit,
    onCloseFileTab: (PythonFile) -> Unit,
    onNewFileClick: () -> Unit,
    isRunning: Boolean,
    saveStatusMessage: String?,
    fontSize: Float,
    highlightLine: Int?,
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    showSearchBar: Boolean,
    findQuery: String,
    onFindQueryChange: (String) -> Unit,
    replaceQuery: String,
    onReplaceQueryChange: (String) -> Unit,
    searchMatchCount: Int,
    onToggleSearch: () -> Unit,
    onReplaceOne: () -> Unit,
    onReplaceAll: () -> Unit,
    onOpenSnippetsDialog: () -> Unit,
    onOpenChallengesDialog: () -> Unit,
    onFormatCodeClick: () -> Unit,
    onShareCodeClick: () -> Unit,
    onSocialShareClick: () -> Unit,
    onExportZipClick: () -> Unit,
    onRunClick: () -> Unit,
    onStopClick: () -> Unit,
    onOpenDrawer: () -> Unit,
    onInsertQuickKey: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMoreMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .testTag("editor_screen"),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    navigationIcon = {
                        // Hamburger Menu button opens Project Explorer Drawer
                        IconButton(
                            onClick = onOpenDrawer,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("editor_hamburger_menu")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Project Explorer",
                                tint = PythonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            activeFile?.let { currentFile ->
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkSurfaceVariant)
                                        .clickable { onOpenDrawer() }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .testTag("active_file_chip"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = androidx.compose.ui.res.painterResource(R.drawable.ic_python_logo),
                                        contentDescription = null,
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = currentFile.name,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PythonYellow
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Filled.ExpandMore,
                                        contentDescription = "Switch File",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = saveStatusMessage != null,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Text(
                                    text = saveStatusMessage ?: "",
                                    fontSize = 11.sp,
                                    color = RunGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    },
                    actions = {
                        // Search toggle button
                        IconButton(
                            onClick = onToggleSearch,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("editor_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = stringResource(R.string.search_code),
                                tint = if (showSearchBar) PythonCyan else TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // 3-dots overflow menu containing vertical tools
                        Box {
                            IconButton(
                                onClick = { showMoreMenu = true },
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("editor_overflow_menu_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "More Options",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false },
                                modifier = Modifier.background(DarkSurface)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("PEP8 Code Formatter", color = TextPrimary) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.AutoFixHigh,
                                            contentDescription = null,
                                            tint = PythonCyan,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        showMoreMenu = false
                                        onFormatCodeClick()
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Python Practice Exercises", color = TextPrimary) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.EmojiEvents,
                                            contentDescription = null,
                                            tint = PythonYellow,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        showMoreMenu = false
                                        onOpenChallengesDialog()
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Code Templates & Snippets", color = TextPrimary) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.DataObject,
                                            contentDescription = null,
                                            tint = PythonYellow,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        showMoreMenu = false
                                        onOpenSnippetsDialog()
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Connect (GitHub / LinkedIn)", color = TextPrimary) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Public,
                                            contentDescription = null,
                                            tint = PythonCyan,
                                            modifier = Modifier.size(20.dp)
                                         )
                                    },
                                    onClick = {
                                        showMoreMenu = false
                                        onSocialShareClick()
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Share Script (.py)", color = TextPrimary) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Share,
                                            contentDescription = null,
                                            tint = PythonCyan,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        showMoreMenu = false
                                        onShareCodeClick()
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Export All as .ZIP", color = TextPrimary) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Archive,
                                            contentDescription = null,
                                            tint = PythonYellow,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        showMoreMenu = false
                                        onExportZipClick()
                                    }
                                )

                                HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 4.dp))

                                DropdownMenuItem(
                                    text = { Text("Zoom In (Font +)", color = TextPrimary) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.ZoomIn,
                                            contentDescription = null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = { onZoomIn() }
                                )

                                DropdownMenuItem(
                                    text = { Text("Zoom Out (Font -)", color = TextPrimary) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.ZoomOut,
                                            contentDescription = null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = { onZoomOut() }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkSurface
                    )
                )

                // Multi-tab editor bar under top bar
                EditorTabBar(
                    files = allFiles,
                    activeFileId = activeFile?.id,
                    onSelectFile = onSelectFileTab,
                    onCloseFile = onCloseFileTab,
                    onNewFileClick = onNewFileClick
                )

                HorizontalDivider(color = DarkBorder, thickness = 1.dp)
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
            ) {
                // Auto-complete suggestion bar
                AutoCompleteBar(
                    suggestions = suggestions,
                    onSuggestionClick = onSuggestionClick
                )

                // Quick-Key row for fast symbol entry + Undo/Redo
                QuickKeyBar(
                    canUndo = canUndo,
                    canRedo = canRedo,
                    onUndoClick = onUndoClick,
                    onRedoClick = onRedoClick,
                    onKeyClick = onInsertQuickKey
                )

                // Divider line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(DarkBorder)
                )

                // Bottom Action Bar: Prominent Run Button
                Surface(
                    color = DarkSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        if (isRunning) {
                            Button(
                                onClick = onStopClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StopRed,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("editor_stop_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Stop,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.stop_code),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        } else {
                            Button(
                                onClick = onRunClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RunGreen,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("editor_run_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Run Code",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Find & Replace Bar when active
            AnimatedVisibility(visible = showSearchBar) {
                SearchReplaceBar(
                    findQuery = findQuery,
                    onFindQueryChange = onFindQueryChange,
                    replaceQuery = replaceQuery,
                    onReplaceQueryChange = onReplaceQueryChange,
                    matchCount = searchMatchCount,
                    onFindNext = {},
                    onReplaceOne = onReplaceOne,
                    onReplaceAll = onReplaceAll,
                    onClose = onToggleSearch
                )
            }

            // Full screen code editor
            CodeEditorView(
                editorValue = editorValue,
                onValueChange = onEditorValueChange,
                fontSize = fontSize,
                highlightLine = highlightLine,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
