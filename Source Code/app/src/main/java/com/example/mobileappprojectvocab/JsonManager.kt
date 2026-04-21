package com.example.mobileappprojectvocab

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class DataManager(context: Context) {
    private val prefs = context.getSharedPreferences("vocab_prefs", Context.MODE_PRIVATE)

    fun saveCategories(categories: List<Category>) {
        val array = JSONArray()
        categories.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("name", it.name)
            array.put(obj)
        }
        prefs.edit().putString("categories", array.toString()).apply()
    }

    fun loadCategories(): List<Category>? {
        val json = prefs.getString("categories", null) ?: return null
        val list = mutableListOf<Category>()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(Category(obj.getString("id"), obj.getString("name")))
        }
        return list
    }

    fun saveWords(words: List<Word>) {
        val array = JSONArray()
        words.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("categoryId", it.categoryId)
            obj.put("word", it.word)
            obj.put("pos", it.pos ?: "")
            obj.put("translation", it.translation)
            obj.put("isFavorite", it.isFavorite)
            obj.put("createdAt", it.createdAt)
            array.put(obj)
        }
        prefs.edit().putString("words", array.toString()).apply()
    }

    fun loadWords(): List<Word>? {
        val json = prefs.getString("words", null) ?: return null
        val list = mutableListOf<Word>()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(Word(
                id = obj.getString("id"),
                categoryId = obj.getString("categoryId"),
                word = obj.getString("word"),
                pos = obj.optString("pos").ifEmpty { null },
                translation = obj.getString("translation"),
                isFavorite = obj.getBoolean("isFavorite"),
                createdAt = obj.getLong("createdAt")
            ))
        }
        return list
    }

    fun saveDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean("dark_mode", isDark).apply()
    }

    fun loadDarkMode(): Boolean? {
        if (!prefs.contains("dark_mode")) return null
        return prefs.getBoolean("dark_mode", false)
    }

    fun getWidgetOffset(): Int {
        return prefs.getInt("widget_offset", 0)
    }

    fun saveWidgetOffset(offset: Int) {
        prefs.edit().putInt("widget_offset", offset).apply()
    }
}
