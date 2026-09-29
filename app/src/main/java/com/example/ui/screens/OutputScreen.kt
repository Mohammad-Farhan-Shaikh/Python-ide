package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.OutputChunk
import com.example.ui.components.ConsolePlotView
import com.example.ui.components.ErrorExplainer
import com.example.ui.components.SmartErrorCard
import com.example.ui.console.InputPromptBox
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PythonCyan
import com.example.ui.theme.PythonYellow
import com.example.ui.theme.RunGreen
import com.example.ui.theme.StopRed
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutputScreen(
    fileName: String,
    outputChunks: List<OutputChunk>,
    isRunning: Boolean,
    pendingInputPrompt: String?,
    lastErrorMessage: String?,
    fontSize: Float = 13f,
    onBackToEditor: () -> Unit,
    onRunAgain: () -> Unit,
    onStopCode: () -> Unit,
    onClearOutput: () -> Unit,
    onCopyOutput: () -> Unit,
    onSubmitInput: (String) -> Unit,
    onJumpToErrorLine: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto-scroll when new output arrives
    LaunchedEffect(outputChunks.size, pendingInputPrompt) {
        if (outputChunks.isNotEmpty()) {
            listState.animateScrollToItem(outputChunks.size - 1)
        }
    }

    val parsedError = lastErrorMessage?.let { ErrorExplainer.parse(it) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .testTag("output_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Terminal,
                            contentDescription = null,
                            tint = PythonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Output Console",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = fileName,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = PythonYellow
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackToEditor,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("back_to_editor_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_to_editor),
                            tint = PythonCyan
                        )
                    }
                },
                actions = {
                    // Running status indicator pill
                    if (isRunning) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(RunGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp,
                                color = RunGreen
                            )
                            Text(
                                text = stringResource(R.string.running),
                                fontSize = 11.sp,
                                color = RunGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Copy All Output Button
                    IconButton(
                        onClick = onCopyOutput,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("output_screen_copy_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = stringResource(R.string.copy_output),
                            tint = PythonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Clear button
                    IconButton(
                        onClick = onClearOutput,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("output_screen_clear_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CleaningServices,
                            contentDescription = stringResource(R.string.clear_console),
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        bottomBar = {
            Surface(
                color = DarkSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(DarkBorder)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onBackToEditor,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkSurfaceVariant,
                                contentColor = PythonCyan
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("output_edit_code_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Code,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Edit Code",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        if (isRunning) {
                            Button(
                                onClick = onStopCode,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StopRed,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("output_stop_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Stop,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.stop_code),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            Button(
                                onClick = onRunAgain,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RunGreen,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("output_run_again_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.re_run),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = TerminalBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                if (outputChunks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Terminal,
                                contentDescription = null,
                                tint = DarkBorder,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Output is empty.",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextMuted
                            )
                            Text(
                                text = "Press 'Re-Run' or go back to Edit Code and press Run.",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = TextMuted.copy(alpha = 0.8f)
                            )
                        }
                    }
                } else {
                    val dynamicOutFontSize = fontSize.sp
                    val dynamicOutLineHeight = (fontSize * 1.52f).sp

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("output_chunks_list")
                    ) {
                        items(outputChunks) { chunk ->
                            when (chunk) {
                                is OutputChunk.Standard -> {
                                    Text(
                                        text = chunk.text,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = dynamicOutFontSize,
                                        lineHeight = dynamicOutLineHeight,
                                        color = Color(0xFFE2E8F0)
                                    )
                                }
                                is OutputChunk.Error -> {
                                    Text(
                                        text = chunk.text,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = dynamicOutFontSize,
                                        lineHeight = dynamicOutLineHeight,
                                        fontWeight = FontWeight.Bold,
                                        color = StopRed
                                    )
                                }
                                is OutputChunk.System -> {
                                    Text(
                                        text = chunk.text,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = (fontSize - 1f).coerceAtLeast(10f).sp,
                                        lineHeight = dynamicOutLineHeight,
                                        color = PythonCyan
                                    )
                                }
                                is OutputChunk.UserInput -> {
                                    Text(
                                        text = chunk.text,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = dynamicOutFontSize,
                                        lineHeight = dynamicOutLineHeight,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PythonYellow
                                    )
                                }
                                is OutputChunk.Plot -> {
                                    ConsolePlotView(
                                        title = chunk.title,
                                        type = chunk.type,
                                        xLabels = chunk.xLabels,
                                        yValues = chunk.yValues
                                    )
                                }
                            }
                        }

                        // Render Smart Error Explainer Card if an error occurred
                        if (parsedError != null) {
                            item {
                                SmartErrorCard(
                                    error = parsedError,
                                    onJumpToLine = onJumpToErrorLine
                                )
                            }
                        }
                    }
                }
            }

            // Input prompt bar if input() is currently requested
            AnimatedVisibility(visible = pendingInputPrompt != null) {
                InputPromptBox(
                    prompt = pendingInputPrompt ?: "",
                    onSubmit = onSubmitInput
                )
            }
        }
    }
}
