package com.example.decisionapp.network

import org.json.JSONArray

/**
 * Converts JSONArray ["a","b","c"] → List<String>
 */
fun JSONArray.toStringList(): List<String> {
    val list = mutableListOf<String>()
    for (i in 0 until length()) {
        list.add(getString(i))
    }
    return list
}