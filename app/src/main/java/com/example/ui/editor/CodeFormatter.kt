package com.example.ui.editor

object CodeFormatter {
    fun formatPythonCode(code: String): String {
        if (code.isBlank()) return code

        val lines = code.lines()
        val formattedLines = mutableListOf<String>()

        var currentIndentLevel = 0
        val indentUnit = "    " // 4 spaces

        for (rawLine in lines) {
            val trimmed = rawLine.trim()

            if (trimmed.isEmpty()) {
                // Keep max one consecutive empty line
                if (formattedLines.isNotEmpty() && formattedLines.last().isNotEmpty()) {
                    formattedLines.add("")
                }
                continue
            }

            // Check if line should be unindented (e.g. elif, else, except, finally, return, pass)
            val shouldUnindent = trimmed.startsWith("elif ") ||
                    trimmed.startsWith("elif:") ||
                    trimmed == "else:" ||
                    trimmed.startsWith("except") ||
                    trimmed == "finally:"

            var lineIndent = currentIndentLevel
            if (shouldUnindent && lineIndent > 0) {
                lineIndent -= 1
            }

            // Format standard operators with spaces (e.g. x=5 -> x = 5)
            val spacedLine = formatOperators(trimmed)

            formattedLines.add(indentUnit.repeat(lineIndent) + spacedLine)

            // Calculate next indent level
            if (trimmed.endsWith(":")) {
                currentIndentLevel = lineIndent + 1
            } else if (trimmed.startsWith("return") || trimmed == "break" || trimmed == "continue" || trimmed == "pass") {
                // Soft reduction
                if (currentIndentLevel > 0 && !trimmed.endsWith(":")) {
                    // keep current or adjust if appropriate
                }
            }
        }

        return formattedLines.joinToString("\n")
    }

    private fun formatOperators(line: String): String {
        // Skip comments and strings from aggressive formatting
        if (line.startsWith("#")) return line

        var result = line

        // Ensure single space after commas
        result = result.replace(Regex(""",\s*"""), ", ")

        // Ensure binary operators have spaces if they don't have (avoiding ==, <=, >=, != corruption)
        result = result.replace(Regex("""(?<![=!<>+\-*/])=(?![=])"""), " = ")
        result = result.replace(Regex("""\s+=\s+"""), " = ")

        result = result.replace(Regex("""(?<![=!<>+\-*/])\+(?![+=])"""), " + ")
        result = result.replace(Regex("""\s+\+\s+"""), " + ")

        // Clean extra internal spaces around parentheses
        result = result.replace(Regex("""\(\s+"""), "(")
        result = result.replace(Regex("""\s+\)"""), ")")
        result = result.replace(Regex("""\[\s+"""), "[")
        result = result.replace(Regex("""\s+\]"""), "]")

        return result
    }
}
