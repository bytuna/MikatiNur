package com.example.mkat_nur.ui.risale

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mkat_nur.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RisaleScreen(
    viewModel: RisaleViewModel = viewModel(),
    onMenuClick: () -> Unit
) {
    val currentPage by viewModel.currentPage.collectAsState()
    val isInitializing by viewModel.isInitializing.collectAsState()
    val bookNames by viewModel.bookNames.collectAsState(initial = emptyList())
    val fihrist by viewModel.fihrist.collectAsState()
    val scrollTarget by viewModel.pendingScrollTarget.collectAsState()
    
    val scrollState = rememberScrollState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    var activeBook by remember { mutableStateOf<String?>(null) }
    var activeFootnoteContent by remember { mutableStateOf<String?>(null) }
    var showPageDialog by remember { mutableStateOf(false) }

    // Hangi ana başlıkların (Seviye 1) açık olduğunu tutar
    val expandedSections = remember { mutableStateMapOf<String, Boolean>() }

    val arabicFont = FontFamily(Font(R.font.uthman_taha))

    // Sayfa değiştiğinde en üste kaydır
    LaunchedEffect(currentPage) {
        scrollState.scrollTo(0)
    }

    // Fihrist öğelerini ana başlıklar (seviye 1) ve altındakiler olarak grupla
    val groupedFihrist = remember(fihrist) {
        val groups = mutableListOf<Pair<RisaleFihristItem, List<RisaleFihristItem>>>()
        var currentParent: RisaleFihristItem? = null
        var currentChildren = mutableListOf<RisaleFihristItem>()

        fihrist.forEach { item ->
            if (item.seviye == 1) {
                if (currentParent != null) {
                    groups.add(currentParent!! to currentChildren)
                }
                currentParent = item
                currentChildren = mutableListOf()
            } else {
                currentChildren.add(item)
            }
        }
        if (currentParent != null) {
            groups.add(currentParent!! to currentChildren)
        }
        groups
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(320.dp),
                drawerContainerColor = Color(0xFFFCF5E5)
            ) {
                Text(
                    "Fihrist",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    groupedFihrist.forEach { (parent, children) ->
                        val isExpanded = expandedSections[parent.baslik] ?: false

                        Column {
                            // Ana Başlık (Seviye 1)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        if (children.isEmpty()) {
                                            scope.launch { drawerState.close() }
                                            viewModel.goToPage(parent.sayfaNo, parent.baslik)
                                        } else {
                                            expandedSections[parent.baslik] = !isExpanded
                                            // Başlığa git
                                            viewModel.goToPage(parent.sayfaNo, parent.baslik)
                                        }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = parent.baslik,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20),
                                    modifier = Modifier.weight(1f)
                                )
                                if (children.isNotEmpty()) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = Color(0xFF1B5E20)
                                    )
                                }
                            }

                            // Alt Başlıklar (Açılır/Kapanır)
                            if (children.isNotEmpty()) {
                                AnimatedVisibility(visible = isExpanded) {
                                    Column {
                                        children.forEach { child ->
                                            Text(
                                                text = child.baslik,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        scope.launch { drawerState.close() }
                                                        viewModel.goToPage(child.sayfaNo, child.baslik)
                                                    }
                                                    .padding(
                                                        start = (16 * child.seviye).dp, 
                                                        end = 16.dp, 
                                                        top = 8.dp, 
                                                        bottom = 8.dp
                                                    ),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp)
                                    }
                                }
                            } else {
                                HorizontalDivider(thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        },
        gesturesEnabled = currentPage != null
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (currentPage != null) currentPage!!.kitapAdi else if (activeBook != null) activeBook!! else "RİSALE-İ NUR",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (currentPage != null) {
                                Badge(
                                    containerColor = Color(0xFFB71C1C),
                                    contentColor = Color.White,
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .clickable { showPageDialog = true }
                                ) {
                                    Text("Sayfa: ${currentPage?.sayfaNo}")
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        if (currentPage != null || activeBook != null) {
                            IconButton(onClick = { 
                                if (currentPage != null) {
                                    viewModel.loadPage("", -1) 
                                }
                                activeBook = null 
                            }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        } else {
                            IconButton(onClick = onMenuClick) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    },
                    actions = {
                        if (currentPage != null) {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.FormatListBulleted, contentDescription = "Fihrist")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            },
            bottomBar = {
                if (currentPage != null) {
                    BottomAppBar(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { viewModel.previousPage() }) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = null)
                                Text("Geri")
                            }
                            
                            Text(
                                "Sayfa ${currentPage?.sayfaNo}",
                                modifier = Modifier.clickable { showPageDialog = true },
                                style = MaterialTheme.typography.labelLarge
                            )

                            TextButton(onClick = { viewModel.nextPage() }) {
                                Text("İleri")
                                Icon(Icons.Default.ChevronRight, contentDescription = null)
                            }
                        }
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (isInitializing) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (currentPage != null) {
                    RisaleReaderComponent(
                        content = currentPage!!.sayfaIcerigi,
                        hasiyeler = currentPage!!.hasiyeler,
                        fontSize = 18f,
                        backgroundColor = Color(0xFFFCF5E5), // Sepia background
                        textColor = Color(0xFF212121),
                        arabicFont = arabicFont,
                        scrollState = scrollState,
                        scrollTarget = scrollTarget,
                        onScrollTargetComplete = { viewModel.clearScrollTarget() },
                        onFootnoteClick = { activeFootnoteContent = it },
                        onWordLongClick = { word ->
                            // Sorgu yapılacak
                        }
                    )
                } else if (activeBook != null) {
                    // Seçilen kitabın son kaldığı sayfayı yükle
                    LaunchedEffect(activeBook) {
                        val lastPage = viewModel.getLastPage(activeBook!!)
                        viewModel.loadPage(activeBook!!, lastPage)
                    }
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    // Kitap listesi
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Külliyat",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        if (bookNames.isEmpty()) {
                            Text("Kütüphane yükleniyor veya boş...")
                        }

                        bookNames.forEach { bookName ->
                            Card(
                                onClick = { activeBook = bookName },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20))
                            ) {
                                Text(
                                    text = bookName,
                                    color = Color.White,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Sayfaya Git Dialog
    if (showPageDialog) {
        var pageText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPageDialog = false },
            title = { Text("Sayfaya Git") },
            text = {
                OutlinedTextField(
                    value = pageText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) pageText = it },
                    label = { Text("Sayfa Numarası") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pageNum = pageText.toIntOrNull()
                        if (pageNum != null) {
                            viewModel.goToPage(pageNum)
                        }
                        showPageDialog = false
                    }
                ) {
                    Text("Git")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPageDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // Footnote BottomSheet
    if (activeFootnoteContent != null) {
        ModalBottomSheet(
            onDismissRequest = { activeFootnoteContent = null }
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    "Dipnot",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB71C1C)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = activeFootnoteContent!!,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
