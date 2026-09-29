package com.example.ui.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PythonFile
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PythonCyan
import com.example.ui.theme.PythonYellow
import com.example.ui.theme.StopRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ProjectFileDrawerSheet(
    files: List<PythonFile>,
    activeFileId: Long?,
    onSelectFile: (PythonFile) -> Unit,
    onNewFileClick: () -> Unit,
    onNewFolderClick: () -> Unit,
    onRenameClick: (PythonFile) -> Unit,
    onDeleteClick: (PythonFile) -> Unit,
    onImportClick: () -> Unit,
    onExportZipClick: () -> Unit,
    onShareFileClick: (PythonFile) -> Unit,
    onOpenChallengesClick: () -> Unit,
    onSocialShareClick: () -> Unit,
    onInlineCreateFile: (String) -> Unit = {},
    onInlineCreateFolder: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }

    // Map to keep track of expanded/collapsed folders like in VS Code
    val folderExpandedMap = remember { mutableStateMapOf<String, Boolean>() }
    var isWorkspaceExpanded by remember { mutableStateOf(true) }

    // Inline creation state inside Explorer tree (VS Code style!)
    var isCreatingInlineFile by remember { mutableStateOf(false) }
    var isCreatingInlineFolder by remember { mutableStateOf(false) }
    var inlineTargetFolder by remember { mutableStateOf<String?>(null) } // null = root
    var inlineInputText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    // Group files by directory
    val (groupedFolders, rootFiles) = remember(files) {
        val folderMap = mutableMapOf<String, MutableList<PythonFile>>()
        val rootList = mutableListOf<PythonFile>()

        for (file in files) {
            val parts = file.name.split("/")
            if (parts.size > 1) {
                val folderName = parts.dropLast(1).joinToString("/")
                folderMap.getOrPut(folderName) { mutableListOf() }.add(file)
            } else {
                rootList.add(file)
            }
        }
        Pair(folderMap, rootList)
    }

    // Auto-focus when inline input is opened
    LaunchedEffect(isCreatingInlineFile, isCreatingInlineFolder) {
        if (isCreatingInlineFile || isCreatingInlineFolder) {
            try {
                focusRequester.requestFocus()
            } catch (e: Exception) {}
        }
    }

    ModalDrawerSheet(
        modifier = modifier
            .width(320.dp)
            .fillMaxHeight()
            .testTag("project_drawer_sheet"),
        drawerContainerColor = Color(0xFF1E1E1E), // Authentic VS Code sidebar dark
        drawerContentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 12.dp)
        ) {
            // VS Code Sidebar Top Title: EXPLORER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "EXPLORER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFBBBBBB),
                    letterSpacing = 1.sp
                )

                // 3 dots more button in Explorer title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onSocialShareClick,
                        modifier = Modifier.size(28.dp).testTag("explorer_github_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Public,
                            contentDescription = "GitHub & LinkedIn",
                            tint = PythonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // VS Code Project Section Header: v PYSTUDIO-WORKSPACE [📄+] [📁+] [🔄] [⊟]
            Surface(
                color = Color(0xFF252526),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Arrow + Project Title
                    Row(
                        modifier = Modifier
                            .clickable { isWorkspaceExpanded = !isWorkspaceExpanded }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isWorkspaceExpanded) Icons.Filled.ExpandMore else Icons.Filled.ChevronRight,
                            contentDescription = if (isWorkspaceExpanded) "Collapse" else "Expand",
                            tint = Color(0xFFCCCCCC),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "WORKSPACE",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFFE1E1E1)
                        )
                    }

                    // VS Code exact action icons: New File, New Folder, Refresh, Collapse All
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        // 1. New File Icon (📄+)
                        IconButton(
                            onClick = {
                                inlineTargetFolder = null
                                inlineInputText = ""
                                isCreatingInlineFolder = false
                                isCreatingInlineFile = true
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("vscode_action_new_file")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.NoteAdd,
                                contentDescription = "New File",
                                tint = PythonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // 2. New Folder Icon (📁+)
                        IconButton(
                            onClick = {
                                inlineTargetFolder = null
                                inlineInputText = ""
                                isCreatingInlineFile = false
                                isCreatingInlineFolder = true
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("vscode_action_new_folder")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CreateNewFolder,
                                contentDescription = "New Folder",
                                tint = PythonYellow,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // 3. Import Button
                        IconButton(
                            onClick = onImportClick,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("vscode_action_import")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FileUpload,
                                contentDescription = "Import",
                                tint = Color(0xFFCCCCCC),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        // 4. Collapse/Expand All Folders (⊟)
                        IconButton(
                            onClick = {
                                val allExpanded = groupedFolders.keys.all { folderExpandedMap[it] == true }
                                groupedFolders.keys.forEach { folderExpandedMap[it] = !allExpanded }
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("vscode_action_collapse_all")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.UnfoldLess,
                                contentDescription = "Collapse All",
                                tint = Color(0xFFCCCCCC),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Tree View Area (Like VS Code)
            if (isWorkspaceExpanded) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    // Inline input box at root level if user clicked New File or New Folder at root
                    if ((isCreatingInlineFile || isCreatingInlineFolder) && inlineTargetFolder == null) {
                        item(key = "inline_root_creator") {
                            InlineTreeInputRow(
                                isFolder = isCreatingInlineFolder,
                                value = inlineInputText,
                                onValueChange = { inlineInputText = it },
                                focusRequester = focusRequester,
                                onConfirm = {
                                    val trimmed = inlineInputText.trim()
                                    if (trimmed.isNotEmpty()) {
                                        if (isCreatingInlineFolder) {
                                            onInlineCreateFolder(trimmed)
                                        } else {
                                            onInlineCreateFile(trimmed)
                                        }
                                    }
                                    isCreatingInlineFile = false
                                    isCreatingInlineFolder = false
                                    inlineInputText = ""
                                },
                                onCancel = {
                                    isCreatingInlineFile = false
                                    isCreatingInlineFolder = false
                                    inlineInputText = ""
                                },
                                indentLevel = 0
                            )
                        }
                    }

                    // 1. Folders in Workspace Tree
                    groupedFolders.forEach { (folderName, folderFiles) ->
                        val isExpanded = folderExpandedMap[folderName] ?: true

                        item(key = "tree_folder_$folderName") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(28.dp)
                                    .clickable {
                                        folderExpandedMap[folderName] = !isExpanded
                                    }
                                    .padding(start = 12.dp, end = 6.dp)
                                    .testTag("folder_row_$folderName"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Filled.ExpandMore else Icons.Filled.ChevronRight,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                                    tint = Color(0xFF909090),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (isExpanded) Icons.Filled.FolderOpen else Icons.Filled.Folder,
                                    contentDescription = null,
                                    tint = Color(0xFFDCAE5A), // VS code folder yellow
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = folderName,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = Color(0xFFCCCCCC),
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                // Add File inside this specific folder button (+)
                                IconButton(
                                    onClick = {
                                        inlineTargetFolder = folderName
                                        inlineInputText = ""
                                        isCreatingInlineFolder = false
                                        isCreatingInlineFile = true
                                        folderExpandedMap[folderName] = true
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.NoteAdd,
                                        contentDescription = "New File in $folderName",
                                        tint = Color(0xFF858585),
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }

                        // Files inside this folder when expanded
                        if (isExpanded) {
                            // Inline input if adding file directly inside this folder
                            if (isCreatingInlineFile && inlineTargetFolder == folderName) {
                                item(key = "inline_folder_creator_$folderName") {
                                    InlineTreeInputRow(
                                        isFolder = false,
                                        value = inlineInputText,
                                        onValueChange = { inlineInputText = it },
                                        focusRequester = focusRequester,
                                        onConfirm = {
                                            val trimmed = inlineInputText.trim()
                                            if (trimmed.isNotEmpty()) {
                                                onInlineCreateFile("$folderName/$trimmed")
                                            }
                                            isCreatingInlineFile = false
                                            inlineInputText = ""
                                            inlineTargetFolder = null
                                        },
                                        onCancel = {
                                            isCreatingInlineFile = false
                                            inlineInputText = ""
                                            inlineTargetFolder = null
                                        },
                                        indentLevel = 1
                                    )
                                }
                            }

                            items(folderFiles, key = { it.id }) { file ->
                                val isActive = file.id == activeFileId
                                val shortName = file.name.substringAfterLast("/")

                                TreeFileRow(
                                    file = file,
                                    displayName = shortName,
                                    isActive = isActive,
                                    indentLevel = 1,
                                    filesCount = files.size,
                                    onSelectFile = onSelectFile,
                                    onShareFileClick = onShareFileClick,
                                    onRenameClick = onRenameClick,
                                    onDeleteClick = onDeleteClick
                                )
                            }
                        }
                    }

                    // 2. Root files (outside folders)
                    items(rootFiles, key = { it.id }) { file ->
                        val isActive = file.id == activeFileId

                        TreeFileRow(
                            file = file,
                            displayName = file.name,
                            isActive = isActive,
                            indentLevel = 0,
                            filesCount = files.size,
                            onSelectFile = onSelectFile,
                            onShareFileClick = onShareFileClick,
                            onRenameClick = onRenameClick,
                            onDeleteClick = onDeleteClick
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            // Bottom Status / Tools Bar
            HorizontalDivider(color = Color(0xFF2D2D2D), thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onExportZipClick,
                    shape = RoundedCornerShape(6.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFCCCCCC)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3C3C3C)),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .testTag("drawer_export_zip_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Archive,
                        contentDescription = null,
                        tint = PythonYellow,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Export .ZIP",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedButton(
                    onClick = onOpenChallengesClick,
                    shape = RoundedCornerShape(6.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = PythonCyan
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PythonCyan.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .testTag("drawer_challenges_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = PythonCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Exercises",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TreeFileRow(
    file: PythonFile,
    displayName: String,
    isActive: Boolean,
    indentLevel: Int,
    filesCount: Int,
    onSelectFile: (PythonFile) -> Unit,
    onShareFileClick: (PythonFile) -> Unit,
    onRenameClick: (PythonFile) -> Unit,
    onDeleteClick: (PythonFile) -> Unit
) {
    var showItemMenu by remember { mutableStateOf(false) }

    val startPadding = if (indentLevel == 1) 32.dp else 18.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(if (isActive) Color(0xFF094771) else Color.Transparent) // VS Code active blue highlight
            .clickable { onSelectFile(file) }
            .padding(start = startPadding, end = 6.dp)
            .testTag("drawer_file_item_${file.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Python file logo/icon
        if (displayName.endsWith(".py")) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(com.example.R.drawable.ic_python_logo),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(15.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Description,
                contentDescription = null,
                tint = Color(0xFFE38C00),
                modifier = Modifier.size(15.dp)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = displayName,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            color = if (isActive) Color.White else Color(0xFFCCCCCC),
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        // Item Options Menu
        Box {
            IconButton(
                onClick = { showItemMenu = true },
                modifier = Modifier
                    .size(24.dp)
                    .testTag("drawer_file_more_${file.id}")
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Options",
                    tint = if (isActive) Color.White else Color(0xFF858585),
                    modifier = Modifier.size(14.dp)
                )
            }

            DropdownMenu(
                expanded = showItemMenu,
                onDismissRequest = { showItemMenu = false },
                modifier = Modifier.background(DarkSurface)
            ) {
                DropdownMenuItem(
                    text = { Text("Share Script", color = TextPrimary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = null,
                            tint = PythonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    onClick = {
                        showItemMenu = false
                        onShareFileClick(file)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Rename", color = TextPrimary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    onClick = {
                        showItemMenu = false
                        onRenameClick(file)
                    }
                )
                if (filesCount > 1) {
                    DropdownMenuItem(
                        text = { Text("Delete", color = StopRed) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = null,
                                tint = StopRed,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            showItemMenu = false
                            onDeleteClick(file)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun InlineTreeInputRow(
    isFolder: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    indentLevel: Int
) {
    val startPadding = if (indentLevel == 1) 30.dp else 14.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .background(Color(0xFF2A2D2E))
            .border(1.dp, PythonCyan, RoundedCornerShape(2.dp))
            .padding(start = startPadding, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isFolder) Icons.Filled.Folder else Icons.Filled.Description,
            contentDescription = null,
            tint = if (isFolder) PythonYellow else PythonCyan,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.5.sp,
                color = Color.White
            ),
            cursorBrush = SolidColor(PythonCyan),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onConfirm() }),
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .testTag("inline_tree_text_input")
        )

        IconButton(
            onClick = onConfirm,
            modifier = Modifier.size(24.dp).testTag("inline_confirm_btn")
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Confirm",
                tint = PythonCyan,
                modifier = Modifier.size(16.dp)
            )
        }

        IconButton(
            onClick = onCancel,
            modifier = Modifier.size(24.dp).testTag("inline_cancel_btn")
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Cancel",
                tint = Color(0xFF909090),
                modifier = Modifier.size(15.dp)
            )
        }
    }
}
