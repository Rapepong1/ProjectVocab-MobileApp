package com.example.mobileappprojectvocab

import android.net.Uri
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mobileappprojectvocab.ui.theme.MobileAppProjectVocabTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val darkTheme = isDarkMode ?: isSystemInDarkTheme()
            
            MobileAppProjectVocabTheme(darkTheme = darkTheme) {
                VocabularyApp(viewModel)
            }
        }
    }
}

@Composable
fun VocabularyApp(viewModel: MainViewModel) {
    var currentTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier.shadow(16.dp)
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.List, contentDescription = "Words") },
                    label = { Text("Vocabulary", fontWeight = if(currentTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = {
                        currentTab = 1
                        viewModel.startQuiz()
                    },
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Quiz") },
                    label = { Text("Quiz", fontWeight = if(currentTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> width } + fadeOut()
                    }
                },
                label = "TabTransition"
            ) { targetTab ->
                if (targetTab == 0) {
                    WordListScreen(viewModel)
                } else {
                    QuizScreen(viewModel)
                }
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
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val darkTheme = isDarkMode ?: isSystemInDarkTheme()

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddWordDialog by remember { mutableStateOf(false) }
    var editingWord by remember { mutableStateOf<Word?>(null) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var deletingWordId by remember { mutableStateOf<String?>(null) }
    var deletingCategoryId by remember { mutableStateOf<String?>(null) }
    
    var peekingWord by remember { mutableStateOf<Word?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .blur(if (peekingWord != null) 12.dp else 0.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Word", 
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        "Master your vocabulary daily",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.padding(end = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            if (darkTheme) "Light Mode" else "Dark Mode",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    FloatingActionButton(
                        onClick = { showAddWordDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Word")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "CATEGORIES", 
                style = MaterialTheme.typography.labelLarge, 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 1.2.sp
            )
            
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    CategoryChip(
                        name = "All",
                        selected = selectedCategoryId == "",
                        onClick = { viewModel.selectCategory("") }
                    )
                }
                items(categories, key = { it.id }) { category ->
                    CategoryChip(
                        name = category.name,
                        selected = selectedCategoryId == category.id,
                        onClick = { viewModel.selectCategory(category.id) },
                        onEdit = { editingCategory = category },
                        onDelete = { deletingCategoryId = category.id }
                    )
                }
                item {
                    IconButton(
                        onClick = { showAddCategoryDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New Category", modifier = Modifier.size(20.dp))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.clickable {
                        viewModel.setSortOrder(
                            if (sortOrder == MainViewModel.SortOrder.CREATED_AT) MainViewModel.SortOrder.ALPHABETICAL
                            else MainViewModel.SortOrder.CREATED_AT
                        )
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (sortOrder == MainViewModel.SortOrder.CREATED_AT) "Recently Added" else "Alphabetical",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                
                Text(
                    "${words.size} words total",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(words, key = { it.id }) { word ->
                    WordItem(
                        word = word,
                        onFavoriteToggle = { viewModel.toggleFavorite(word.id) },
                        onDelete = { deletingWordId = word.id },
                        onEdit = { editingWord = word },
                        onPeekRequest = { peekingWord = it }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = peekingWord != null,
            enter = fadeIn() + scaleIn(initialScale = 0.95f),
            exit = fadeOut() + scaleOut(targetScale = 0.95f),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .pointerInput(Unit) { detectTapGestures { peekingWord = null } },
                contentAlignment = Alignment.Center
            ) {
                peekingWord?.let { word ->
                    Box(modifier = Modifier.pointerInput(Unit) { detectTapGestures { } }) {
                        WordPeekCard(word, onClose = { peekingWord = null })
                    }
                }
            }
        }
    }

    // Confirm Word Delete Dialog
    deletingWordId?.let { wordId ->
        ConfirmDeleteDialog(
            title = "Delete Word?",
            message = "Are you sure you want to remove this word from your list?",
            onDismiss = { deletingWordId = null },
            onConfirm = { viewModel.deleteWord(wordId); deletingWordId = null }
        )
    }

    // Confirm Category Delete Dialog
    deletingCategoryId?.let { catId ->
        ConfirmDeleteDialog(
            title = "Delete Category?",
            message = "Removing this category will NOT delete the words inside it. Are you sure?",
            onDismiss = { deletingCategoryId = null },
            onConfirm = { viewModel.deleteCategory(catId); deletingCategoryId = null }
        )
    }

    if (showAddCategoryDialog) CategoryDialog(onDismiss = { showAddCategoryDialog = false }, onConfirm = { viewModel.addCategory(it); showAddCategoryDialog = false })
    editingCategory?.let { category -> CategoryDialog(initialValue = category.name, onDismiss = { editingCategory = null }, onConfirm = { viewModel.editCategory(category.id, it); editingCategory = null }) }
    if (showAddWordDialog) WordDialog(categories = categories, onDismiss = { showAddWordDialog = false }, onConfirm = { w, t, p, c -> viewModel.addWord(c, w, t, p); showAddWordDialog = false })
    editingWord?.let { word -> WordDialog(categories = categories, initialWord = word, onDismiss = { editingWord = null }, onConfirm = { w, t, p, c -> viewModel.editWord(word.id, w, t, p, c); editingWord = null }) }
}

@Composable
fun ConfirmDeleteDialog(title: String, message: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun CategoryChip(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    val backgroundColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(name, fontWeight = if(selected) FontWeight.Bold else FontWeight.Medium, color = contentColor)
            if (onEdit != null && onDelete != null && selected) {
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.Edit, null, Modifier.size(14.dp).clickable { onEdit() }, tint = contentColor)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.Delete, null, Modifier.size(14.dp).clickable { onDelete() }, tint = contentColor)
            }
        }
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
            .pointerInput(word) { detectTapGestures(onLongPress = { onPeekRequest(word) }) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(word.word, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                    if (word.isFavorite) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.Star, null, Modifier.size(14.dp), tint = Color(0xFFFFB300))
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                if (!word.pos.isNullOrBlank()) {
                    Text(
                        word.pos, 
                        style = MaterialTheme.typography.labelSmall, 
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
                
                Text(
                    word.translation, 
                    style = MaterialTheme.typography.titleMedium, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp,
                    fontSize = 19.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                Text("Hold to see Dictionary", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) { 
                    Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), modifier = Modifier.size(14.dp)) 
                }
                IconButton(onClick = onFavoriteToggle, modifier = Modifier.size(28.dp)) { 
                    Icon(if (word.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = if (word.isFavorite) Color(0xFFEF5350) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), modifier = Modifier.size(16.dp)) 
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) { 
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.4f), modifier = Modifier.size(14.dp)) 
                }
            }
        }
    }
}

@Composable
fun WordPeekCard(word: Word, onClose: () -> Unit) {
    val query = Uri.encode(word.word)
    val url = "https://dictionary.cambridge.org/dictionary/english-thai/$query"

    Card(
        modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.85f),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(20.dp)
            ) {
                Column {
                    Text(word.word, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    Text(word.translation, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                }
                IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopEnd)) {
                    Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onPrimary)
                }
            }

            Box(modifier = Modifier.weight(1f).background(Color.White)) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            webViewClient = WebViewClient()
                            settings.apply { javaScriptEnabled = true; domStorageEnabled = true }
                            loadUrl(url)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp), strokeWidth = 3.dp)
                }
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
        EmptyStateScreen("Add some words to start quiz!")
        return
    }

    if (isFinished) {
        QuizFinishedScreen(onRestart = { viewModel.startQuiz() })
    } else {
        val currentWord = quizWords[currentIndex]
        val progress = (currentIndex + 1).toFloat() / quizWords.size

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Quiz Mode", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.weight(1f).height(10.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(Modifier.width(16.dp))
                Text("${currentIndex + 1}/${quizWords.size}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            }

            Spacer(modifier = Modifier.height(24.dp))

            val cardScale by animateFloatAsState(if (isRevealed) 1.02f else 1f, label = "cardScale")
            
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp) 
                    .scale(cardScale)
                    .clickable { viewModel.toggleRevealTranslation() },
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if(isRevealed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                    TextButton(
                        onClick = { viewModel.toggleRevealHint() },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(Icons.Outlined.Info, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Hint", fontSize = 12.sp)
                    }

                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            currentWord.word, 
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            lineHeight = 38.sp
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))

                        if (isRevealed) {
                            Text(currentWord.translation, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                            if (!currentWord.pos.isNullOrBlank()) {
                                Text(currentWord.pos, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.outline)
                            }
                        } else {
                            if (isHintRevealed) {
                                Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(12.dp)) {
                                    Text("Starts with: ${currentWord.translation.take(1)}...", modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.bodyMedium)
                                }
                            } else {
                                Text("TAP TO REVEAL", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f), letterSpacing = 2.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                FilledTonalButton(
                    onClick = { viewModel.previousQuizWord() }, 
                    enabled = currentIndex > 0,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(Icons.Default.KeyboardArrowLeft, null)
                    Text("Back")
                }
                
                Button(
                    onClick = { viewModel.nextQuizWord() },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(if (currentIndex == quizWords.size - 1) "FINISH" else "NEXT")
                    Icon(Icons.Default.KeyboardArrowRight, null)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = { viewModel.startQuiz() }) {
                Icon(Icons.Default.Refresh, null, Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Shuffle & Restart")
            }
        }
    }
}

@Composable
fun EmptyStateScreen(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Info, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(Modifier.height(16.dp))
            Text(message, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun QuizFinishedScreen(onRestart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎉", fontSize = 80.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Brilliant!", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
        Text("You've mastered this set.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(Icons.Default.Refresh, null)
            Spacer(Modifier.width(12.dp))
            Text("Try Another Round", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CategoryDialog(initialValue: String = "", onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Category Name") },
        text = { OutlinedTextField(value = name, onValueChange = { name = it }, placeholder = { Text("e.g. Travel, Work") }, shape = RoundedCornerShape(12.dp), singleLine = true) },
        confirmButton = { Button(onClick = { onConfirm(name) }) { Text("Save") } },
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
        title = { Text(if (initialWord == null) "New Word" else "Edit Word", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = word, onValueChange = { word = it }, label = { Text("English Word") }, shape = RoundedCornerShape(12.dp), singleLine = true)
                OutlinedTextField(value = pos, onValueChange = { pos = it }, label = { Text("P.O.S. (e.g. n., v., adj.)") }, shape = RoundedCornerShape(12.dp), singleLine = true)
                OutlinedTextField(value = trans, onValueChange = { trans = it }, label = { Text("Thai Translation") }, shape = RoundedCornerShape(12.dp), singleLine = true)
                
                Text("Category", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCatId == cat.id,
                            onClick = { selectedCatId = cat.id },
                            label = { Text(cat.name) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (word.isNotBlank() && trans.isNotBlank() && selectedCatId.isNotBlank()) onConfirm(word, trans, pos, selectedCatId) }) {
                Text("Add Word")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
