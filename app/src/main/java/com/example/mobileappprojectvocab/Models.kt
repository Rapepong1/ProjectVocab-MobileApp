package com.example.mobileappprojectvocab

import java.util.UUID

data class Category(
    val id: String = UUID.randomUUID().toString(),
    val name: String
)

data class Word(
    val id: String = UUID.randomUUID().toString(),
    val categoryId: String,
    val word: String,
    val pos: String? = null,
    val translation: String,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
