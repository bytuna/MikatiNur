package com.example.mkat_nur.ui.risale

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RisaleWebViewScreen(
    bookId: String? = null,
    pageNumber: Int? = null,
    onBackClick: () -> Unit,
    onMenuClick: (() -> Unit)? = null
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    BackHandler {
        if (webViewRef?.canGoBack() == true) {
            webViewRef?.goBack()
        } else {
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Risale-i Nur") },
                navigationIcon = {
                    if (onMenuClick != null) {
                        IconButton(onClick = onMenuClick) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    } else {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B263B),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            factory = { context ->
                val root = FrameLayout(context).apply {
                    setBackgroundColor(android.graphics.Color.parseColor("#fdfcf9"))
                }

                val webView = WebView(context).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    
                    settings.apply {
                        @SuppressLint("SetJavaScriptEnabled")
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        databaseEnabled = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                    }

                    setLayerType(View.LAYER_TYPE_HARDWARE, null)

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                        }
                    }

                    // Mapping Android book IDs to web parameters if necessary
                    val mappedBookId = when (bookId) {
                        "Sözler" -> "sozler"
                        "Mektubat" -> "mektubat"
                        "Lem'alar" -> "lemalar"
                        "Şualar" -> "sualar"
                        "Mesnevi-i Nuriye" -> "mesnevi"
                        "İşarat-ül İ'caz" -> "isarat-ul-icaz"
                        "Barla Lahikası" -> "barla-lahikasi"
                        "Kastamonu Lahikası" -> "kastamonu-lahikasi"
                        "Emirdağ Lahikası 1" -> "emirdag-lahikasi-1"
                        "Emirdağ Lahikası 2" -> "emirdag-lahikasi-2"
                        "Sikke-i Tasdik-i Gaybi" -> "sikke-i-tasdik-i-gaybi"
                        "Tarihçe-i Hayat" -> "tarihce-i-hayat"
                        "Asa-yi Musa" -> "asa-yi-musa"
                        "İman ve Küfür Muvazeneleri" -> "iman-ve-kufur-muvazeneleri"
                        "Muhakemat" -> "muhakemat"
                        else -> bookId
                    }

                    val baseUrl = "https://www.mikatinur.com.tr/projects/risaleinur/"
                    val finalUrl = when {
                        mappedBookId != null && pageNumber != null -> "$baseUrl?book=$mappedBookId&page=$pageNumber"
                        mappedBookId != null -> "$baseUrl?book=$mappedBookId"
                        else -> baseUrl
                    }
                    loadUrl(finalUrl)
                    webViewRef = this
                }

                val progressBar = ProgressBar(context).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        android.view.Gravity.CENTER
                    )
                    visibility = View.GONE
                }

                root.addView(webView)
                root.addView(progressBar)
                
                // Track loading state to show/hide ProgressBar
                // We use a listener or just simple property if we have access to the ProgressBar instance later
                // In Compose, we can just use the 'isLoading' state to overlay a ProgressBar
                
                root
            },
            update = { root ->
                val progressBar = root.getChildAt(1) as ProgressBar
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        )
    }
}
