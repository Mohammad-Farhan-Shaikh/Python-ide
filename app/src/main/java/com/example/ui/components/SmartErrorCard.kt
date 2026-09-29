package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PythonCyan
import com.example.ui.theme.PythonYellow
import com.example.ui.theme.StopRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class ParsedPythonError(
    val errorType: String,
    val lineNumber: Int?,
    val explanation: String,
    val suggestedFix: String
)

object ErrorExplainer {
    fun parse(rawError: String): ParsedPythonError? {
        val lines = rawError.lines()
        var lineNum: Int? = null

        // Look for line number: e.g. line 5 or line 12
        val lineRegex = Regex("""line (\d+)""")
        for (line in lines) {
            val match = lineRegex.find(line)
            if (match != null) {
                lineNum = match.groupValues[1].toIntOrNull()
            }
        }

        // Detect known error patterns
        return when {
            rawError.contains("IndentationError") -> {
                ParsedPythonError(
                    errorType = "IndentationError",
                    lineNumber = lineNum,
                    explanation = "Python uses strict indentation (spacing) to group code blocks. A line has incorrect spaces or tabs.",
                    suggestedFix = "Ensure indented lines inside functions, 'if', 'for', and 'while' blocks have exactly 4 spaces."
                )
            }
            rawError.contains("NameError") -> {
                val varRegex = Regex("""name '(\w+)' is not defined""")
                val varName = varRegex.find(rawError)?.groupValues?.get(1) ?: "variable"
                ParsedPythonError(
                    errorType = "NameError",
                    lineNumber = lineNum,
                    explanation = "You tried to use '$varName' before declaring it or making a typo.",
                    suggestedFix = "Check if '$varName' is spelled correctly and defined earlier in the code."
                )
            }
            rawError.contains("SyntaxError") -> {
                ParsedPythonError(
                    errorType = "SyntaxError",
                    lineNumber = lineNum,
                    explanation = "Python syntax rule was broken (often a missing colon ':', unmatched quote, or parenthesis).",
                    suggestedFix = "Check if the statement ends with ':' (for def, if, for) or has balanced brackets () and quotes."
                )
            }
            rawError.contains("IndexError") -> {
                ParsedPythonError(
                    errorType = "IndexError (List out of range)",
                    lineNumber = lineNum,
                    explanation = "Attempted to access a list element index that does not exist.",
                    suggestedFix = "Remember Python lists start at index 0 and end at len(list) - 1. Check list size."
                )
            }
            rawError.contains("TypeError") -> {
                ParsedPythonError(
                    errorType = "TypeError (Data type mismatch)",
                    lineNumber = lineNum,
                    explanation = "Operation applied to an incompatible data type (e.g. adding string + integer).",
                    suggestedFix = "Use int(x) or str(x) to convert types before concatenating or adding."
                )
            }
            rawError.contains("KeyError") -> {
                ParsedPythonError(
                    errorType = "KeyError (Dictionary)",
                    lineNumber = lineNum,
                    explanation = "Dictionary key was not found.",
                    suggestedFix = "Use my_dict.get('key', default) or verify that the key exists before accessing."
                )
            }
            rawError.contains("ZeroDivisionError") -> {
                ParsedPythonError(
                    errorType = "ZeroDivisionError",
                    lineNumber = lineNum,
                    explanation = "Math calculation tried to divide a number by 0.",
                    suggestedFix = "Check the divisor with an 'if divisor != 0:' check before dividing."
                )
            }
            else -> {
                if (lineNum != null) {
                    ParsedPythonError(
                        errorType = "Runtime Exception",
                        lineNumber = lineNum,
                        explanation = "An exception occurred while running the script.",
                        suggestedFix = "Inspect line $lineNum to verify arguments, logic, and types."
                    )
                } else null
            }
        }
    }
}

@Composable
fun SmartErrorCard(
    error: ParsedPythonError,
    onJumpToLine: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .testTag("smart_error_explainer_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, StopRed.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(StopRed.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lightbulb,
                        contentDescription = null,
                        tint = PythonYellow,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Smart Error Explainer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PythonYellow
                )

                Spacer(modifier = Modifier.weight(1f))

                if (error.lineNumber != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StopRed.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Line ${error.lineNumber}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = StopRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = error.explanation,
                fontSize = 12.5.sp,
                color = TextPrimary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Filled.AutoFixHigh,
                    contentDescription = null,
                    tint = PythonCyan,
                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = error.suggestedFix,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = PythonCyan,
                    lineHeight = 17.sp
                )
            }

            if (error.lineNumber != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { onJumpToLine(error.lineNumber) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PythonCyan,
                        contentColor = Color(0xFF003548)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("jump_to_error_line_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.TrendingFlat,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Jump to Line ${error.lineNumber} in Editor",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
