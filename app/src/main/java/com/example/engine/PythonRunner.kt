package com.example.engine

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

sealed class OutputChunk {
    data class Standard(val text: String) : OutputChunk()
    data class Error(val text: String) : OutputChunk()
    data class System(val text: String) : OutputChunk()
    data class UserInput(val text: String) : OutputChunk()
    data class Plot(val title: String, val type: String, val xLabels: List<String>, val yValues: List<Float>) : OutputChunk()
}

class PythonRunner(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var webView: WebView? = null
    private var executionCounter = 0

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _isEngineReady = MutableStateFlow(false)
    val isEngineReady: StateFlow<Boolean> = _isEngineReady.asStateFlow()

    private val _outputChunks = MutableStateFlow<List<OutputChunk>>(emptyList())
    val outputChunks: StateFlow<List<OutputChunk>> = _outputChunks.asStateFlow()

    private val _pendingInputPrompt = MutableStateFlow<String?>(null)
    val pendingInputPrompt: StateFlow<String?> = _pendingInputPrompt.asStateFlow()

    private val _lastErrorMessage = MutableStateFlow<String?>(null)
    val lastErrorMessage: StateFlow<String?> = _lastErrorMessage.asStateFlow()

    private var pendingCodeToRun: String? = null
    private var timeoutRunnable: Runnable? = null

    init {
        mainHandler.post {
            initWebView()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        val wv = WebView(context.applicationContext)
        val settings = wv.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.cacheMode = WebSettings.LOAD_NO_CACHE

        wv.webChromeClient = WebChromeClient()
        wv.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                _isEngineReady.value = true
                pendingCodeToRun?.let { code ->
                    pendingCodeToRun = null
                    runCodeInternal(code)
                }
            }
        }

        wv.addJavascriptInterface(object {
            @JavascriptInterface
            fun onEngineReady() {
                mainHandler.post {
                    _isEngineReady.value = true
                }
            }

            @JavascriptInterface
            fun onStarted() {
                mainHandler.post {
                    _isRunning.value = true
                    _pendingInputPrompt.value = null
                    _lastErrorMessage.value = null
                    scheduleExecutionTimeout()
                }
            }

            @JavascriptInterface
            fun onOutput(text: String, isError: Boolean) {
                mainHandler.post {
                    val chunk = if (isError) OutputChunk.Error(text) else OutputChunk.Standard(text)
                    appendOutput(chunk)
                }
            }

            @JavascriptInterface
            fun onPlot(title: String, type: String, xJson: String, yJson: String) {
                mainHandler.post {
                    try {
                        val xArray = JSONArray(xJson)
                        val yArray = JSONArray(yJson)
                        val xLabels = mutableListOf<String>()
                        val yValues = mutableListOf<Float>()

                        for (i in 0 until xArray.length()) {
                            xLabels.add(xArray.getString(i))
                        }
                        for (i in 0 until yArray.length()) {
                            yValues.add(yArray.getDouble(i).toFloat())
                        }

                        appendOutput(OutputChunk.Plot(title, type, xLabels, yValues))
                    } catch (e: Exception) {
                        appendOutput(OutputChunk.Standard("[Plot data rendered]\n"))
                    }
                }
            }

            @JavascriptInterface
            fun onRequestInput(prompt: String) {
                mainHandler.post {
                    cancelTimeout()
                    if (prompt.isNotEmpty()) {
                        appendOutput(OutputChunk.Standard(prompt))
                    }
                    _pendingInputPrompt.value = prompt
                }
            }

            @JavascriptInterface
            fun onError(errorMessage: String) {
                mainHandler.post {
                    cancelTimeout()
                    _lastErrorMessage.value = errorMessage
                    appendOutput(OutputChunk.Error("\n$errorMessage\n"))
                }
            }

            @JavascriptInterface
            fun onFinished(exitCode: Int, elapsedMs: Long) {
                mainHandler.post {
                    cancelTimeout()
                    _isRunning.value = false
                    _pendingInputPrompt.value = null
                    val statusText = if (exitCode == 0) {
                        "\n--- Process finished in ${elapsedMs}ms (exit code 0) ---"
                    } else {
                        "\n--- Process finished with error in ${elapsedMs}ms (exit code $exitCode) ---"
                    }
                    appendOutput(OutputChunk.System(statusText))
                }
            }
        }, "AndroidBridge")

        wv.loadUrl("file:///android_asset/python/runner.html")
        webView = wv
    }

    private fun scheduleExecutionTimeout() {
        cancelTimeout()
        // Protection: 15-second infinite loop guard
        timeoutRunnable = Runnable {
            if (_isRunning.value && _pendingInputPrompt.value == null) {
                appendOutput(OutputChunk.Error("\n⚠️ Loop Timeout Protection: Script ran for >15 seconds (possible infinite loop). Auto-stopping execution.\n"))
                stopExecution()
            }
        }
        mainHandler.postDelayed(timeoutRunnable!!, 15000)
    }

    private fun cancelTimeout() {
        timeoutRunnable?.let {
            mainHandler.removeCallbacks(it)
            timeoutRunnable = null
        }
    }

    private fun appendOutput(chunk: OutputChunk) {
        _outputChunks.value = _outputChunks.value + chunk
    }

    fun clearOutput() {
        _outputChunks.value = emptyList()
        _pendingInputPrompt.value = null
        _lastErrorMessage.value = null
    }

    fun executeCode(code: String) {
        if (_isRunning.value) {
            stopExecution()
        }

        if (!_isEngineReady.value) {
            pendingCodeToRun = code
            return
        }

        runCodeInternal(code)
    }

    private fun runCodeInternal(code: String) {
        mainHandler.post {
            executionCounter++
            _isRunning.value = true
            _pendingInputPrompt.value = null
            _lastErrorMessage.value = null

            val jsonCode = JSONObject.quote(code)
            val js = "window.runPythonCode($jsonCode, $executionCounter);"
            webView?.evaluateJavascript(js, null)
        }
    }

    fun sendInput(input: String) {
        mainHandler.post {
            appendOutput(OutputChunk.UserInput("$input\n"))
            _pendingInputPrompt.value = null
            scheduleExecutionTimeout()
            val jsonInput = JSONObject.quote(input)
            val js = "window.provideInput($jsonInput);"
            webView?.evaluateJavascript(js, null)
        }
    }

    fun stopExecution() {
        mainHandler.post {
            cancelTimeout()
            _isRunning.value = false
            _pendingInputPrompt.value = null
            appendOutput(OutputChunk.System("\n[Execution stopped by user]"))

            try {
                webView?.evaluateJavascript("window.stopExecution();", null)
            } catch (e: Exception) {
                // Ignore
            }

            webView?.loadUrl("file:///android_asset/python/runner.html")
        }
    }

    fun destroy() {
        mainHandler.post {
            cancelTimeout()
            try {
                webView?.stopLoading()
                webView?.destroy()
                webView = null
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
