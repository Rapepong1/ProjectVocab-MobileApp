package com.example.mobileappprojectvocab

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class MainViewModel : ViewModel() {
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _words = MutableStateFlow<List<Word>>(emptyList())
    val words: StateFlow<List<Word>> = _words.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.CREATED_AT)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _quizWords = MutableStateFlow<List<Word>>(emptyList())
    val quizWords: StateFlow<List<Word>> = _quizWords.asStateFlow()

    private val _currentQuizIndex = MutableStateFlow(0)
    val currentQuizIndex: StateFlow<Int> = _currentQuizIndex.asStateFlow()

    private val _isTranslationRevealed = MutableStateFlow(false)
    val isTranslationRevealed: StateFlow<Boolean> = _isTranslationRevealed.asStateFlow()

    enum class SortOrder { ALPHABETICAL, CREATED_AT }

    init {
        // Starter Pack
        val foodCategory = Category(name = "Food")
        val financeCategory = Category(name = "Finance")
        _categories.value = listOf(foodCategory, financeCategory)

        _words.value = listOf(
            Word(categoryId = foodCategory.id, word = "Apple", translation = "แอปเปิ้ล", pos = "Noun"),
            Word(categoryId = foodCategory.id, word = "Bread", translation = "ขนมปัง", pos = "Noun"),
            Word(categoryId = financeCategory.id, word = "Investment", translation = "การลงทุน", pos = "Noun"),
            Word(categoryId = financeCategory.id, word = "Interest", translation = "ดอกเบี้ย", pos = "Noun")
        )
    }

    fun selectCategory(id: String?) {
        _selectedCategoryId.value = id
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun getFilteredAndSortedWords(): List<Word> {
        var list = _words.value
        val categoryId = _selectedCategoryId.value
        if (categoryId != null) {
            list = list.filter { it.categoryId == categoryId }
        }

        return list.sortedWith(compareByDescending<Word> { it.isFavorite }
            .thenBy {
                when (_sortOrder.value) {
                    SortOrder.ALPHABETICAL -> it.word.lowercase()
                    SortOrder.CREATED_AT -> it.createdAt
                }
            })
    }

    // Category CRUD
    fun addCategory(name: String) {
        _categories.update { it + Category(name = name) }
    }

    fun editCategory(id: String, newName: String) {
        _categories.update { list ->
            list.map { if (it.id == id) it.copy(name = newName) else it }
        }
    }

    fun deleteCategory(id: String) {
        _categories.update { it.filter { it.id != id } }
        _words.update { it.filter { it.categoryId != id } }
        if (_selectedCategoryId.value == id) _selectedCategoryId.value = null
    }

    // Word CRUD
    fun addWord(categoryId: String, word: String, translation: String, pos: String?) {
        _words.update { it + Word(categoryId = categoryId, word = word, translation = translation, pos = pos) }
    }

    fun editWord(id: String, word: String, translation: String, pos: String?, categoryId: String) {
        _words.update { list ->
            list.map {
                if (it.id == id) it.copy(word = word, translation = translation, pos = pos, categoryId = categoryId)
                else it
            }
        }
    }

    fun deleteWord(id: String) {
        _words.update { it.filter { it.id != id } }
    }

    fun toggleFavorite(id: String) {
        _words.update { list ->
            list.map { if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it }
        }
    }

    // Quiz
    fun startQuiz() {
        var list = _words.value
        val categoryId = _selectedCategoryId.value
        if (categoryId != null) {
            list = list.filter { it.categoryId == categoryId }
        }
        _quizWords.value = list.shuffled()
        _currentQuizIndex.value = 0
        _isTranslationRevealed.value = false
    }

    fun nextQuizWord() {
        if (_currentQuizIndex.value < _quizWords.value.size - 1) {
            _currentQuizIndex.value++
            _isTranslationRevealed.value = false
        }
    }

    fun previousQuizWord() {
        if (_currentQuizIndex.value > 0) {
            _currentQuizIndex.value--
            _isTranslationRevealed.value = false
        }
    }

    fun toggleRevealTranslation() {
        _isTranslationRevealed.value = !_isTranslationRevealed.value
    }
}
