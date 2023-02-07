package github.daniedev.json_viewer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.google.gson.Gson
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


fun parseJson(json: String): Map<String, Any> {
    return Gson().fromJson(json, object : TypeToken<Map<String, Any>>() {}.type)
}

fun convertToJson(map: Map<*, *>): String {
    val gson = Gson()
    return gson.toJson(map)
}

@Composable
fun JsonViewer(json: String) {
    val parsedJson = parseJson(json)
    Column {
        for ((key, value) in parsedJson.entries) {
            val expanded = remember { mutableStateOf(false) }
                Row(Modifier.clickable { expanded.value = !expanded.value  }) {
                    Text(text = "$key: ")
                    when (value) {
                        is String -> Text(text = "$value")
                        is Map<*, *> -> {
                            if (expanded.value) {
                                Text(text = "{ ")
                                JsonViewer(convertToJson(value))
                                Text(text = " }")
                            } else {
                                Text(text = "{...}")
                            }
                        }
                        is List<*> -> {
                            if (expanded.value) {
                                JsonListViewer(value)
                            } else {
                                Text(text = "[...]")
                            }
                        }
                        else -> Text(text = "$value")
                    }
                }
            }
        }
    }

@Composable
fun JsonListViewer(list: List<*>) {
    Column {
        for (value in list) {
            when (value) {
                is String -> Text(text = "$value")
                is Map<*, *> -> {
                    Text(text = "{ ")
                    JsonViewer(convertToJson(value))
                    Text(text = " }")
                }
                is List<*> -> {
                    Text(text = "[")
                    JsonListViewer(value)
                    Text(text = "]")
                }
                else -> Text(text = "$value")
            }
        }
    }
}

@Composable
fun OptimizedJsonViewer(json: String) {
    val parsedJson = parseJson(json)
    val expanded = remember { mutableStateOf(mapOf<String, Boolean>()) }
    Column {
        for ((key, value) in parsedJson.entries) {
                Row(Modifier.clickable { expanded.value = expanded.value.toMutableMap().apply {
                    this[key] = !this.getOrDefault(key, false)
                }  }) {
                    Text(text = "$key: ")
                    when (value) {
                        is String -> Text(text = "$value")
                        is Map<*, *> -> {
                            if (expanded.value.getOrDefault(key, false)) {
                                Text(text = "{ ")
                                OptimizedJsonViewer(convertToJson(value))
                                Text(text = " }")
                            } else {
                                Text(text = "{...}")
                            }
                        }
                        is List<*> -> {
                            if (expanded.value.getOrDefault(key, false)) {
                                OptimizedJsonListViewer(value)
                            } else {
                                Text(text = "[...]")
                            }
                        }
                        else -> Text(text = "$value")
                    }
                }
            }
        }
    }


@Composable
fun OptimizedJsonListViewer(list: List<*>) {
    Column {
        for (value in list) {
            when (value) {
                is String -> Text(text = "$value")
                is Map<*, *> -> {
                    Text(text = "{ ")
                    OptimizedJsonViewer(convertToJson(value))
                    Text(text = " }")
                }
                is List<*> -> {
                    Text(text = "[")
                    OptimizedJsonListViewer(value)
                    Text(text = "]")
                }
                else -> Text(text = "$value")
            }
        }
    }
}


