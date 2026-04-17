package com.example.mobileappprojectvocab

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dataManager = DataManager(application)
    
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _words = MutableStateFlow<List<Word>>(emptyList())
    val words: StateFlow<List<Word>> = _words.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow("")
    val selectedCategoryId: StateFlow<String> = _selectedCategoryId.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.CREATED_AT)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _quizWords = MutableStateFlow<List<Word>>(emptyList())
    val quizWords: StateFlow<List<Word>> = _quizWords.asStateFlow()

    private val _currentQuizIndex = MutableStateFlow(0)
    val currentQuizIndex: StateFlow<Int> = _currentQuizIndex.asStateFlow()

    private val _isTranslationRevealed = MutableStateFlow(false)
    val isTranslationRevealed: StateFlow<Boolean> = _isTranslationRevealed.asStateFlow()

    private val _isHintRevealed = MutableStateFlow(false)
    val isHintRevealed: StateFlow<Boolean> = _isHintRevealed.asStateFlow()

    private val _isQuizFinished = MutableStateFlow(false)
    val isQuizFinished: StateFlow<Boolean> = _isQuizFinished.asStateFlow()

    enum class SortOrder { ALPHABETICAL, CREATED_AT }

    val filteredWords: StateFlow<List<Word>> = combine(
        _words,
        _selectedCategoryId,
        _sortOrder
    ) { words, selectedId, sortOrder ->
        var list = if (selectedId.isNotEmpty()) {
            words.filter { it.categoryId == selectedId }
        } else {
            words
        }

        list.sortedWith(compareByDescending<Word> { it.isFavorite }
            .thenBy {
                when (sortOrder) {
                    SortOrder.ALPHABETICAL -> it.word.lowercase()
                    SortOrder.CREATED_AT -> it.createdAt
                }
            })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadData()
    }

    private fun loadData() {
        val savedCats = dataManager.loadCategories()
        val savedWords = dataManager.loadWords()

        if (savedCats == null || savedWords == null) {
            // First time - load Starter Pack with FIXED IDs
            val cat1 = Category(id = "cat_business", name = "Workplace & Business")
            val cat2 = Category(id = "cat_academic", name = "Academic & Research")
            val cat3 = Category(id = "cat_personality", name = "Personality & Behavior")
            val cat4 = Category(id = "cat_society", name = "Society & Environment")
            val cat5 = Category(id = "cat_tech", name = "Technology & Digital")
            val cat6 = Category(id = "cat_uncategorized", name = "Uncategorized (ไม่ระบุ)")
            
            val initialCats = listOf(cat6, cat1, cat2, cat3, cat4, cat5)
            val initialWords = listOf(
                Word(categoryId = cat1.id, word = "Collaborate", translation = "ร่วมมือกันทำงาน", pos = "Verb"),
                Word(categoryId = cat1.id, word = "Implement", translation = "นำไปปฏิบัติ/เริ่มใช้", pos = "Verb"),
                Word(categoryId = cat1.id, word = "Mitigate", translation = "บรรเทา/ลดผลกระทบ", pos = "Verb"),
                Word(categoryId = cat1.id, word = "Objective", translation = "วัตถุประสงค์", pos = "Noun"),
                Word(categoryId = cat1.id, word = "Feasibility", translation = "ความเป็นไปได้", pos = "Noun"),

                Word(categoryId = cat2.id, word = "Analysis", translation = "การวิเคราะห์", pos = "Noun"),
                Word(categoryId = cat2.id, word = "Hypothesis", translation = "สมมติฐาน", pos = "Noun"),
                Word(categoryId = cat2.id, word = "Empirical", translation = "เชิงประจักษ์", pos = "Adjective"),
                Word(categoryId = cat2.id, word = "Significant", translation = "สำคัญอย่างมีนัยสำคัญ", pos = "Adjective"),
                Word(categoryId = cat2.id, word = "Paradigm", translation = "กระบวนทัศน์/แบบอย่าง", pos = "Noun"),

                Word(categoryId = cat3.id, word = "Resilient", translation = "ยืดหยุ่น/คืนสภาพเดิมเร็ว", pos = "Adjective"),
                Word(categoryId = cat3.id, word = "Meticulous", translation = "พิถีพิถัน", pos = "Adjective"),
                Word(categoryId = cat3.id, word = "Versatile", translation = "อเนกประสงค์/รอบด้าน", pos = "Adjective"),
                Word(categoryId = cat3.id, word = "Ambiguous", translation = "กำกวม/คลุมเครือ", pos = "Adjective"),
                Word(categoryId = cat3.id, word = "Empathy", translation = "ความเห็นอกเห็นใจ", pos = "Noun"),

                Word(categoryId = cat4.id, word = "Sustainable", translation = "ยั่งยืน", pos = "Adjective"),
                Word(categoryId = cat4.id, word = "Diversity", translation = "ความหลากหลาย", pos = "Noun"),
                Word(categoryId = cat4.id, word = "Advocate", translation = "สนับสนุน/ผู้ให้การสนับสนุน", pos = "Verb/Noun"),
                Word(categoryId = cat4.id, word = "Consequence", translation = "ผลที่ตามมา", pos = "Noun"),
                Word(categoryId = cat4.id, word = "Prosperity", translation = "ความมั่งคั่ง/เจริญรุ่งเรือง", pos = "Noun"),

                Word(categoryId = cat5.id, word = "Automation", translation = "ระบบอัตโนมัติ", pos = "Noun"),
                Word(categoryId = cat5.id, word = "Integration", translation = "การรวมเข้าด้วยกัน", pos = "Noun"),
                Word(categoryId = cat5.id, word = "Infrastructure", translation = "โครงสร้างพื้นฐาน", pos = "Noun"),
                Word(categoryId = cat5.id, word = "Accessibility", translation = "การเข้าถึงได้ง่าย", pos = "Noun"),
                Word(categoryId = cat5.id, word = "Cybersecurity", translation = "ความปลอดภัยทางไซเบอร์", pos = "Noun")
            )
            _categories.value = initialCats
            _words.value = initialWords
            saveToDisk()
        } else {
            _categories.value = savedCats
            _words.value = savedWords
        }
    }

    private fun saveToDisk() {
        dataManager.saveCategories(_categories.value)
        dataManager.saveWords(_words.value)
    }

    fun selectCategory(id: String) {
        _selectedCategoryId.value = id
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    // Category CRUD
    fun addCategory(name: String) {
        _categories.update { it + Category(name = name) }
        saveToDisk()
    }

    fun editCategory(id: String, newName: String) {
        _categories.update { list ->
            list.map { if (it.id == id) it.copy(name = newName) else it }
        }
        saveToDisk()
    }

    fun deleteCategory(id: String) {
        _categories.update { it.filter { it.id != id } }
        _words.update { it.filter { it.categoryId != id } }
        if (_selectedCategoryId.value == id) _selectedCategoryId.value = ""
        saveToDisk()
    }

    // Word CRUD
    fun addWord(categoryId: String, word: String, translation: String, pos: String?) {
        _words.update { it + Word(categoryId = categoryId, word = word, translation = translation, pos = pos) }
        saveToDisk()
    }

    fun editWord(id: String, word: String, translation: String, pos: String?, categoryId: String) {
        _words.update { list ->
            list.map {
                if (it.id == id) it.copy(word = word, translation = translation, pos = pos, categoryId = categoryId)
                else it
            }
        }
        saveToDisk()
    }

    fun deleteWord(id: String) {
        _words.update { it.filter { it.id != id } }
        saveToDisk()
    }

    fun toggleFavorite(id: String) {
        _words.update { list ->
            list.map { if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it }
        }
        saveToDisk()
    }

    // Quiz - Updated logic to handle empty words properly
    fun startQuiz() {
        val allWords = _words.value
        val categoryId = _selectedCategoryId.value
        
        val listToQuiz = if (categoryId.isNotEmpty()) {
            allWords.filter { it.categoryId == categoryId }
        } else {
            allWords
        }

        if (listToQuiz.isNotEmpty()) {
            _quizWords.value = listToQuiz.shuffled()
            _currentQuizIndex.value = 0
            _isTranslationRevealed.value = false
            _isHintRevealed.value = false
            _isQuizFinished.value = false
        } else {
            _quizWords.value = emptyList()
            _isQuizFinished.value = false
        }
    }

    fun nextQuizWord() {
        if (_quizWords.value.isEmpty()) return

        if (_currentQuizIndex.value < _quizWords.value.size - 1) {
            _currentQuizIndex.value++
            _isTranslationRevealed.value = false
            _isHintRevealed.value = false
        } else {
            _isQuizFinished.value = true
        }
    }

    fun previousQuizWord() {
        if (_currentQuizIndex.value > 0) {
            _currentQuizIndex.value--
            _isTranslationRevealed.value = false
            _isHintRevealed.value = false
            _isQuizFinished.value = false
        }
    }

    fun toggleRevealTranslation() {
        _isTranslationRevealed.value = !_isTranslationRevealed.value
    }

    fun toggleRevealHint() {
        _isHintRevealed.value = !_isHintRevealed.value
    }
}
