package com.example.mkat_nur.ui.risale

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

// Web projesinin Android'den veri almasını sağlayan köprü sınıfı
class WebAppInterface(private val bookId: String) {
    @JavascriptInterface
    fun getSelectedBook(): String = bookId
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RisaleWebViewScreen(
    bookId: String,
    onBackClick: () -> Unit
) {
    var webView by remember { mutableStateOf<WebView?>(null) }

    BackHandler {
        if (webView?.canGoBack() == true) webView?.goBack() else onBackClick()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(bookId.replace("-", " ").uppercase()) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(padding),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.apply {
                        @SuppressLint("SetJavaScriptEnabled")
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = true
                    }
                    
                    // KÖPRÜ BURADA KURULUYOR: Web projesi "AndroidBridge" adıyla bu sınıfa erişecek
                    addJavascriptInterface(WebAppInterface(bookId), "AndroidBridge")
                    
                    webViewClient = WebViewClient()
                    
                    // Standart yerel yükleme (Yol karmaşası yok)
                    loadUrl("file:///android_asset/risale_web/projects/risaleinur/index.html")
                    webView = this
                }
            }
        )
    }
}