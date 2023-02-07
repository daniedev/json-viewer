package github.daniedev.json_viewer


import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.LinkedList
import kotlin.math.roundToInt

val searchText = "na"

val searchIndex = mutableStateOf(-1)
val positionValueMap : MutableMap<String, SearchEntry> = linkedMapOf()
var currentSearchEntry: SearchEntry? = null
var searchPositionsList: LinkedList<Float>? = null
var isSearchInProgress = false

data class SearchEntry(var key: String, var position: Float, var isSelected: MutableState<Boolean>)

@Preview
@Composable
fun test() {
    val json = ""

    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val boxScrollState = rememberScrollState()

    Scaffold(Modifier.background(Color.White),
        topBar = {
            TopAppBar {
                Text(text = "JsonViewer")
                Spacer(modifier = Modifier.defaultMinSize(minWidth = 20.dp))
                IconButton(
                    onClick = {
                        //Log.d("api - search position list", searchPositionsList.toString())
                        //Log.d("api - search position first elememnt", searchPositionsList?.get(0).toString())
                        isSearchInProgress = true
                        if(searchPositionsList == null) {
                            searchPositionsList = LinkedList()
                            for (searchEntry in positionValueMap.values)
                                searchPositionsList?.add(searchEntry.position)
                        }
                        searchIndex.value--
                        if (searchIndex.value < 0) searchIndex.value = 0
                        currentSearchEntry?.isSelected?.value = false
                        currentSearchEntry = positionValueMap[positionValueMap.keys.elementAtOrNull(searchIndex.value)]
                        currentSearchEntry?.apply {
                            coroutineScope.launch {
                                isSelected.value = true
                                boxScrollState.scrollTo(position.roundToInt() - 700)
                                Log.d("api - map of all items", positionValueMap.toString())
                                Log.d("api - CE", currentSearchEntry.toString())
                                Log.d("api - search position list", searchPositionsList.toString())
                                Log.d("api - search position list size", searchPositionsList?.size.toString())
                            }
                        }
                        focusManager.clearFocus()
                    },
                    content = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Scroll to Previous"
                        )
                    })
                IconButton(
                    onClick = {
                        //Log.d("api - search position list", searchPositionsList.toString())
                        //Log.d("api - search position first elememnt", searchPositionsList?.get(0).toString())
                        isSearchInProgress = true
                        searchIndex.value++
                        if(searchPositionsList == null) {
                            searchPositionsList = LinkedList()
                            for (searchEntry in positionValueMap.values)
                                searchPositionsList?.add(searchEntry.position)
                        }
                        if (searchIndex.value > positionValueMap.size.minus(1)) searchIndex.value = positionValueMap.size.minus(1)
                        currentSearchEntry?.isSelected?.value = false
                        currentSearchEntry = positionValueMap[positionValueMap.keys.elementAtOrNull(searchIndex.value)]
                        currentSearchEntry?.apply {

                            coroutineScope.launch {
                                isSelected.value = true
                                boxScrollState.scrollTo(position.roundToInt() - 700)
                                Log.d("api - map of all items", positionValueMap.toString())
                                Log.d("api - map size", positionValueMap.size.toString())
                                Log.d("api - search position list", searchPositionsList.toString())
                                Log.d("api - search position list size", searchPositionsList?.size.toString())
                                Log.d("api - CE", currentSearchEntry.toString())
                            }
                        }
                        focusManager.clearFocus()
                    },
                    content = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Scroll to Next"
                        )
                    })

            }
        }
    ) {
        Column(Modifier.verticalScroll(boxScrollState)) {
            JsonContent(payload = json)
        }
    }
}

@Composable
fun JsonContent(payload: String) {
    val matchingItems =  payload.split(searchText, ignoreCase = true)
    searchIndex.value.takeUnless { matchingItems.isEmpty() }.let { 0 }
    val json = try {
        JSONObject(payload)
    } catch (e: Exception) {
        Log.e("XXXX", payload)
    }

    if (json is JSONObject) {
        Column(
            Modifier.padding(horizontal = 16.dp)
        ) {
            GetObject(json, "")
        }


        Box(Modifier.height(12.dp))
    } else
        Text(text = payload)
}

@Composable
fun ArrayCompose(jsonArray: JSONArray, key: String) {
    var isVisible by remember { mutableStateOf(true) }
    val itemCount = jsonArray.length()
    val animatedHeading by animateFloatAsState(if (isVisible) 0f else -90f)

    Box(modifier = Modifier.drawBehind {
        if (isVisible) {
            val strokeWidth = 1.dp.value * density
            drawLine(
                Color.LightGray,
                Offset(20f, 63f),
                Offset(20f, size.height - 50),
                strokeWidth
            )
        }
    }) {
        Column {
            Row(modifier = Modifier.clickable { isVisible = !isVisible }) {
                Image(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    "",
                    colorFilter = ColorFilter.tint(Color.DarkGray),
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .height(15.dp)
                        .width(15.dp)
                        .rotate(animatedHeading)
                )
                Text(
                    text = " $key",
                    style = typography.body1.copy(
                        background = if (key.contains(searchText, true)
                        ) Color.Yellow else Color.Transparent,
                    ),
                    modifier = Modifier.padding(start = 4.dp)
                )
                Text(text = ":  [")
                AnimatedVisibility(
                    visible = !isVisible,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Row {
                        Text(
                            "$itemCount items",
                            style = typography.body1
                        )
                        Text(
                            text = "]",
                            style = typography.body1,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }

            }

            AnimatedVisibility(visible = isVisible) {
                Column(Modifier.padding(start = 30.dp)) {

                    for (i in 0 until jsonArray.length()) {
                        when (jsonArray[i]) {
                            is Boolean,
                            is Double,
                            is Int,
                            is String,
                            is Long -> GetPrimitives(jsonArray[i], "")

                            is JSONObject -> Column { GetObject(jsonArray.getJSONObject(i), "") }
                            else -> GetPrimitives(jsonArray[i].javaClass.name, "xx")

                        }
                    }
                }
            }

            AnimatedVisibility(visible = isVisible) {
                Text(
                    text = "],",
                    style = typography.body1,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }

    }

}

@Composable
fun GetObject(jsonObject: JSONObject, key: String) {

    val keys = jsonObject.keys()
    val rootText = if (key.isEmpty()) "{" else " $key:  {"
    var isVisible by remember { mutableStateOf(true) }
    var itemsCountVisibility by remember { mutableStateOf(false) }
    val animatedHeading by animateFloatAsState(if (isVisible) 0f else -90f)
    val itemCount = Iterable { jsonObject.keys() }.count()
    Box(modifier = Modifier.drawBehind {
        val strokeWidth = 1.dp.value * density
        if (isVisible) {
            drawLine(
                Color.LightGray,
                Offset(20f, 63f),
                Offset(20f, size.height - 50),
                strokeWidth
            )
        }
    }) {
        Column {
            Row(modifier = Modifier.clickable {
                isVisible = !isVisible
                itemsCountVisibility = !itemsCountVisibility
            }) {
                Image(
                    imageVector = Icons.Filled.KeyboardArrowDown, "collapse object",
                    colorFilter = ColorFilter.tint(Color.DarkGray),
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .height(15.dp)
                        .width(15.dp)
                        .rotate(animatedHeading)
                )
                Text(rootText, style = typography.body1, modifier = Modifier.padding(start = 4.dp))
                AnimatedVisibility(visible = itemsCountVisibility) {
                    Row {
                        Text(
                            "$itemCount items",
                            modifier = Modifier.padding(horizontal = 4.dp),
                            style = typography.body1
                        )
                        Text(
                            text = "}",
                            style = typography.body1,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
            Log.d("XXXX", "getObject: $isVisible")


            if (isVisible) {
                Column(Modifier.padding(start = 30.dp)) {
                    keys.forEach {
                        when (jsonObject[it]) {
                            is Boolean,
                            is Double,
                            is Int,
                            is String,
                            is Long -> GetPrimitives(jsonObject[it], it)

                            is JSONObject -> Column {
                                GetObject(jsonObject.getJSONObject(it), it)
                            }
                            is JSONArray -> Column { ArrayCompose(jsonObject.getJSONArray(it), it) }

                            else -> GetPrimitives(jsonObject.javaClass.name, it)
                        }
                    }
                }
            }

            AnimatedVisibility(visible = isVisible) {
                Text(
                    text = "}",
                    style = typography.body1,
                    modifier = Modifier.padding(start = 3.dp)
                )
            }

        }
    }

}

@SuppressLint("UnrememberedMutableState")
@Composable
fun GetPrimitives(objectz: Any, key: String) {

    val (objectColour, value) = when (objectz) {
        is Boolean, is Int, is Double, is Long -> Pair(colors.secondary, objectz.toString())
        is String -> Pair(Color.Blue, "\"$objectz\"")
        else -> Pair(Color.DarkGray, objectz.toString())
    }

    val isSelected =
        if (searchPositionsList != null && searchPositionsList!!.size > 1 &&
            (key+value).contains(searchText, ignoreCase = true) && isSearchInProgress
        ) {
            val currentPosition: Float = searchPositionsList!!.first
            searchPositionsList?.removeFirst()
            searchPositionsList?.addLast(currentPosition)
            positionValueMap["$key:$value-$currentPosition"]?.isSelected ?: mutableStateOf(false)
        } else mutableStateOf(false)



    Text(
        annotatedString("$key : ", "$value,", objectColour, isSelected),
        modifier = Modifier
            .padding(vertical = 2.dp)
            .onGloballyPositioned {
                if (!isSearchInProgress && (key + value).contains(searchText, ignoreCase = true)) {
                    val positionInWindow = it.positionInRoot().y
                    val currentComposeUniqueId = "$key:$value-$positionInWindow"
                    positionValueMap[currentComposeUniqueId] = SearchEntry(
                        currentComposeUniqueId,
                        positionInWindow,
                        mutableStateOf(false)
                    )
                }
            }
    )
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun annotatedString(key: String, value: String, color: Color, isSelected: State<Boolean>): AnnotatedString {
    Log.d("recomp", "$key:$value")
    val builder = AnnotatedString.Builder()
    if (key.contains(searchText, ignoreCase = true) || value.contains(searchText, ignoreCase = true)) {
        val matchingKeys = key.split(searchText, ignoreCase = true)
        val matchingValues = value.split(searchText, ignoreCase = true)

        matchingKeys.forEachIndexed { index, s ->
            with(typography.body1) {
                builder.withStyle(
                    style = SpanStyle(fontFamily = fontFamily, fontSize = fontSize)
                ) {
                    append(s)
                }
                if (matchingKeys.size - 1 != index) {
                    builder.withStyle(
                        style = SpanStyle(
                            fontFamily = fontFamily,
                            fontSize = fontSize,
                            background = getMatchingBgColor(isSelected.value)
                        )
                    ) {
                        append(searchText)
                    }
                }
            }
        }

        matchingValues.forEachIndexed { index, s ->
            with(typography.body1) {
                builder.withStyle(
                    style = SpanStyle(fontFamily = fontFamily, fontSize = fontSize)
                ) {
                    append(s)
                }
                if (matchingValues.size - 1 != index) {
                    builder.withStyle(
                        style = SpanStyle(
                            fontFamily = fontFamily,
                            fontSize = fontSize,
                            background = getMatchingBgColor(isSelected.value),
                            color = color
                        )
                    ) {
                        append(searchText)
                    }
                }
            }
        }
    } else {
        with(typography.body1) {
            builder.withStyle(
                style = SpanStyle(
                    fontFamily = fontFamily,
                    fontSize = fontSize,
                )
            ) {
                append(key)
            }
            builder.withStyle(
                style = SpanStyle(
                    fontFamily = fontFamily,
                    color = color,
                    fontSize = fontSize,
                )
            ) {
                append(value)
            }
        }
    }
    return builder.toAnnotatedString()
}

fun getMatchingBgColor(isSelected: Boolean): Color {
    return if(isSelected ) Color(0xFFFFA500) else Color.Yellow
}
