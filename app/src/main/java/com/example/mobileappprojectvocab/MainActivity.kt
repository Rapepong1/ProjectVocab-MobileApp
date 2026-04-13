package com.example.mobileappprojectvocab

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileappprojectvocab.ui.theme.MobileAppProjectVocabTheme

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
    var currentTab by remember { mutableStateOf(0) } // 0: All Words, 1: Quiz

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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Categories", style = MaterialTheme.typography.headlineSmall)
        LazyRow(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            item {
                FilterChip(
                    selected = selectedCategoryId == null,
                    onClick = { viewModel.selectCategory(null) },
                    label = { Text("All") },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            items(categories) { category ->
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
            items(words) { word ->
                WordItem(
                    word = word,
                    onFavoriteToggle = { viewModel.toggleFavorite(word.id) },
                    onDelete = { viewModel.deleteWord(word.id) },
                    onEdit = { editingWord = word }
                )
            }
        }
    }

    if (showAddCategoryDialog) {
        CategoryDialog(onDismiss = { showAddCategoryDialog = false }, onConfirm = { viewModel.addCategory(it) })
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
fun WordItem(word: Word, onFavoriteToggle: () -> Unit, onDelete: () -> Unit, onEdit: () -> Unit) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f).clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${word.word}+definition"))
                context.startActivity(intent)
            }) {
                Text(word.word, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                if (!word.pos.isNullOrBlank()) {
                    Text(word.pos, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Text(word.translation)
            }
            IconButton(onClick = onFavoriteToggle) {
                Icon(
                    if (word.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (word.isFavorite) Color.Red else Color.Gray
                )
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
        }
    }
}

@Composable
fun QuizScreen(viewModel: MainViewModel) {
    val quizWords by viewModel.quizWords.collectAsState()
    val currentIndex by viewModel.currentQuizIndex.collectAsState()
    val isRevealed by viewModel.isTranslationRevealed.collectAsState()
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

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (currentWord.isFavorite) {
                Text("★ Favorite Word ★", color = Color(0xFFDAA520), fontWeight = FontWeight.Bold)
            }
            Card(
                modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp),
                onClick = { viewModel.toggleRevealTranslation() }
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(currentWord.word, fontSize = 32.sp, fontWeight = FontWeight.Bold)
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

            Spacer(Modifier.height(16.dp))
            Text("Word ${currentIndex + 1} of ${quizWords.size}")

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
                // Simple category selector (In a real app, use a DropdownMenu)
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
