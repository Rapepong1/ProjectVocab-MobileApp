package com.example.mobileappprojectvocab

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mobileappprojectvocab.ui.theme.MobileAppProjectVocabTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileAppProjectVocabTheme {
                VocabularyApp(viewModel)
            }
        }
    }
}

@Composable
fun VocabularyApp(viewModel: MainViewModel) {
    var currentTab by remember { mutableIntStateOf(0) } // 0: All Words, 1: Quiz

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.List, contentDescription = "Words") },
                    label = { Text("Words") }
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = {
                        currentTab = 1
                        viewModel.startQuiz()
                    },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Quiz") },
                    label = { Text("Quiz") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (currentTab == 0) {
                WordListScreen(viewModel)
            } else {
                QuizScreen(viewModel)
            }
        }
    }
}

@Composable
fun WordListScreen(viewModel: MainViewModel) {
    val categories by viewModel.categories.collectAsState()
    val words by viewModel.filteredWords.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddWordDialog by remember { mutableStateOf(false) }
    var editingWord by remember { mutableStateOf<Word?>(null) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    
    // State for Peek
    var peekingWord by remember { mutableStateOf<Word?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .blur(if (peekingWord != null) 15.dp else 0.dp) // Blur more when active
        ) {
            Text("Categories", style = MaterialTheme.typography.headlineSmall)
            LazyRow(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategoryId == "",
                        onClick = { viewModel.selectCategory("") },
                        label = { Text("All") },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                items(categories, key = { it.id }) { category ->
                    FilterChip(
                        selected = selectedCategoryId == category.id,
                        onClick = { viewModel.selectCategory(category.id) },
                        label = { Text(category.name) },
                        trailingIcon = {
                            Row {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    modifier = Modifier.size(16.dp).clickable { editingCategory = category }
                                )
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(16.dp).clickable { viewModel.deleteCategory(category.id) }
                                )
                            }
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                item {
                    IconButton(onClick = { showAddCategoryDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Category")
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sort: ", fontWeight = FontWeight.Bold)
                TextButton(onClick = {
                    viewModel.setSortOrder(
                        if (sortOrder == MainViewModel.SortOrder.CREATED_AT) MainViewModel.SortOrder.ALPHABETICAL
                        else MainViewModel.SortOrder.CREATED_AT
                    )
                }) {
                    Text(if (sortOrder == MainViewModel.SortOrder.CREATED_AT) "Time" else "A-Z")
                }
                Spacer(Modifier.weight(1f))
                Button(onClick = { showAddWordDialog = true }) {
                    Text("Add Word")
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(words, key = { it.id }) { word ->
                    WordItem(
                        word = word,
                        onFavoriteToggle = { viewModel.toggleFavorite(word.id) },
                        onDelete = { viewModel.deleteWord(word.id) },
                        onEdit = { editingWord = word },
                        onPeekRequest = { peekingWord = it }
                    )
                }
            }
        }

        // Peek Overlay with Dismiss Logic
        AnimatedVisibility(
            visible = peekingWord != null,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .pointerInput(Unit) {
                        detectTapGestures { peekingWord = null } // Tap outside to close
                    },
                contentAlignment = Alignment.Center
            ) {
                peekingWord?.let { word ->
                    // Prevent dismiss when clicking inside the card
                    Box(modifier = Modifier.pointerInput(Unit) { detectTapGestures { } }) {
                        WordPeekCard(word, onClose = { peekingWord = null })
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddCategoryDialog) {
        CategoryDialog(onDismiss = { showAddCategoryDialog = false }, onConfirm = { viewModel.addCategory(it); showAddCategoryDialog = false })
    }
    editingCategory?.let { category ->
        CategoryDialog(
            initialValue = category.name,
            onDismiss = { editingCategory = null },
            onConfirm = { viewModel.editCategory(category.id, it); editingCategory = null }
        )
    }
    if (showAddWordDialog) {
        WordDialog(
            categories = categories,
            onDismiss = { showAddWordDialog = false },
            onConfirm = { w, t, p, c -> viewModel.addWord(c, w, t, p); showAddWordDialog = false }
        )
    }
    editingWord?.let { word ->
        WordDialog(
            categories = categories,
            initialWord = word,
            onDismiss = { editingWord = null },
            onConfirm = { w, t, p, c -> viewModel.editWord(word.id, w, t, p, c); editingWord = null }
        )
    }
}

@Composable
fun WordItem(
    word: Word,
    onFavoriteToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onPeekRequest: (Word) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .pointerInput(word) {
                detectTapGestures(
                    onLongPress = { onPeekRequest(word) },
                    onTap = { /* สามารถเพิ่มฟังก์ชันดูคำแปลสั้นๆ ตรงนี้ได้ */ }
                )
            }
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(word.word, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onEdit, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp), tint = Color.Gray)
                    }
                }
                if (!word.pos.isNullOrBlank()) {
                    Text(word.pos, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Text(word.translation)
                Text("(กดค้างเพื่อดู Dictionary ค้างไว้)", fontSize = 10.sp, color = Color.Gray)
            }
            IconButton(onClick = onFavoriteToggle) {
                Icon(
                    if (word.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (word.isFavorite) Color.Red else Color.Gray
                )
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
        }
    }
}

@Composable
fun WordPeekCard(word: Word, onClose: () -> Unit) {
    val query = Uri.encode(word.word)
    val url = "https://dictionary.cambridge.org/dictionary/english-thai/$query"

    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .fillMaxHeight(0.8f),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(word.word, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(word.translation, style = MaterialTheme.typography.bodyMedium)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // WebView
            Box(modifier = Modifier.weight(1f)) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            webViewClient = WebViewClient()
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                cacheMode = WebSettings.LOAD_DEFAULT
                            }
                            loadUrl(url)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                Text(
                    "Loading Dictionary...",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            
            Box(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("กดที่พื้นหลังเพื่อปิด", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun QuizScreen(viewModel: MainViewModel) {
    val quizWords by viewModel.quizWords.collectAsState()
    val currentIndex by viewModel.currentQuizIndex.collectAsState()
    val isRevealed by viewModel.isTranslationRevealed.collectAsState()
    val isHintRevealed by viewModel.isHintRevealed.collectAsState()
    val isFinished by viewModel.isQuizFinished.collectAsState()

    if (quizWords.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No words in this category!")
        }
        return
    }

    if (isFinished) {
        QuizFinishedScreen(onRestart = { viewModel.startQuiz() })
    } else {
        val currentWord = quizWords[currentIndex]
        val progress = (currentIndex + 1).toFloat() / quizWords.size

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Progress", style = MaterialTheme.typography.labelMedium)
                    Text("${currentIndex + 1} / ${quizWords.size}", style = MaterialTheme.typography.labelMedium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (currentWord.isFavorite) {
                Text("★ Favorite Word ★", color = Color(0xFFDAA520), fontWeight = FontWeight.Bold)
            }
            
            Card(
                modifier = Modifier.fillMaxWidth().height(250.dp).padding(16.dp),
                onClick = { viewModel.toggleRevealTranslation() }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clickable { viewModel.toggleRevealHint() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Hint",
                            modifier = Modifier.size(18.dp),
                            tint = if (isHintRevealed) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Hint",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isHintRevealed) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }

                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(currentWord.word, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        
                        if (isHintRevealed && !isRevealed) {
                            Text(
                                "คำแปลขึ้นต้นด้วย: ${currentWord.translation.take(1)}...",
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        if (isRevealed) {
                            if (!currentWord.pos.isNullOrBlank()) {
                                Text(currentWord.pos, color = Color.Gray)
                            }
                            Text(currentWord.translation, fontSize = 24.sp)
                        } else {
                            Text("(Tap to reveal)", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { viewModel.previousQuizWord() }, enabled = currentIndex > 0) {
                    Text("Back")
                }
                Button(onClick = { viewModel.toggleRevealTranslation() }) {
                    Text(if (isRevealed) "Hide" else "Reveal")
                }
                Button(
                    onClick = { viewModel.nextQuizWord() }
                ) {
                    Text(if (currentIndex == quizWords.size - 1) "Finish" else "Next")
                }
            }

            Button(onClick = { viewModel.startQuiz() }, modifier = Modifier.padding(top = 16.dp)) {
                Text("Shuffle & Restart")
            }
        }
    }
}

@Composable
fun QuizFinishedScreen(onRestart: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "congrats")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + scaleIn()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale)
                        .background(Color(0xFFFFD700), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Congratulations!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "You've completed the quiz!",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onRestart) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Try Again")
                }
            }
        }
    }
}

@Composable
fun CategoryDialog(initialValue: String = "", onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Category") },
        text = { TextField(value = name, onValueChange = { name = it }, label = { Text("Name") }) },
        confirmButton = { Button(onClick = { onConfirm(name) }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun WordDialog(
    categories: List<Category>,
    initialWord: Word? = null,
    onDismiss: () -> Unit,
    onConfirm: (word: String, trans: String, pos: String?, catId: String) -> Unit
) {
    var word by remember { mutableStateOf(initialWord?.word ?: "") }
    var trans by remember { mutableStateOf(initialWord?.translation ?: "") }
    var pos by remember { mutableStateOf(initialWord?.pos ?: "") }
    var selectedCatId by remember { mutableStateOf(initialWord?.categoryId ?: categories.firstOrNull()?.id ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialWord == null) "Add Word" else "Edit Word") },
        text = {
            Column {
                TextField(value = word, onValueChange = { word = it }, label = { Text("Word") })
                TextField(value = pos, onValueChange = { pos = it }, label = { Text("POS (Optional)") })
                TextField(value = trans, onValueChange = { trans = it }, label = { Text("Translation") })
                Text("Category:", modifier = Modifier.padding(top = 8.dp))
                LazyRow {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCatId == cat.id,
                            onClick = { selectedCatId = cat.id },
                            label = { Text(cat.name) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (word.isNotBlank() && trans.isNotBlank() && selectedCatId.isNotBlank()) onConfirm(word, trans, pos, selectedCatId) }) {
                Text("OK")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
