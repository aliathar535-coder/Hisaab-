package com.hisaab.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class Entry(val title: String, val amount: Double, val income: Boolean, val time: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HisaabApp() }
    }
}

@Composable
private fun HisaabApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("hisaab_local", 0) }
    var entries by remember {
        mutableStateOf(loadEntries(prefs))
    }
    var showDialog by remember { mutableStateOf(false) }
    var addIncome by remember { mutableStateOf(true) }
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    val incomeTotal = entries.filter { it.income }.sumOf { it.amount }
    val expenseTotal = entries.filter { !it.income }.sumOf { it.amount }
    val balance = incomeTotal - expenseTotal

    MaterialTheme(colorScheme = darkColorScheme(
        background = Color(0xFF07111F),
        surface = Color(0xFF14243A),
        primary = Color(0xFF65E6D2),
        onBackground = Color.White,
        onSurface = Color.White
    )) {
        Box(
            Modifier.fillMaxSize().background(
                Brush.linearGradient(listOf(Color(0xFF07111F), Color(0xFF102B42), Color(0xFF171B39)))
            )
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("HISAAB", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                            Text("आपका रोज़ का पूरा हिसाब", color = Color(0xFFB8C9D9), fontSize = 13.sp)
                        }
                        Text("● OFFLINE", color = Color(0xFF65E6D2), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                item {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Text("कुल बैलेंस", color = Color(0xFFB8C9D9), fontSize = 14.sp)
                        Spacer(Modifier.height(7.dp))
                        Text("₹ ${money(balance)}", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                        Text(if (balance >= 0) "आपका मौजूदा बचा हुआ पैसा" else "खर्च कमाई से अधिक है",
                            color = Color(0xFF65E6D2), fontSize = 12.sp)
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassCard(Modifier.weight(1f)) {
                            Text("कुल कमाई", color = Color(0xFFB8C9D9), fontSize = 13.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("₹ ${money(incomeTotal)}", color = Color(0xFF65E6D2), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        GlassCard(Modifier.weight(1f)) {
                            Text("कुल खर्च", color = Color(0xFFB8C9D9), fontSize = 13.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("₹ ${money(expenseTotal)}", color = Color(0xFFFF9B9B), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { addIncome = true; title = ""; amount = ""; showDialog = true },
                            modifier = Modifier.weight(1f).height(54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF167D72)),
                            shape = RoundedCornerShape(18.dp)
                        ) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("कमाई जोड़ें") }
                        Button(
                            onClick = { addIncome = false; title = ""; amount = ""; showDialog = true },
                            modifier = Modifier.weight(1f).height(54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9A4355)),
                            shape = RoundedCornerShape(18.dp)
                        ) { Icon(Icons.Default.Remove, null); Spacer(Modifier.width(6.dp)); Text("खर्च जोड़ें") }
                    }
                }
                item {
                    Text("हाल की एंट्री", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                if (entries.isEmpty()) {
                    item { GlassCard(Modifier.fillMaxWidth()) {
                        Text("अभी कोई एंट्री नहीं है। ऊपर से कमाई या खर्च जोड़ें।", color = Color(0xFFB8C9D9))
                    } }
                } else {
                    items(entries.reversed()) { entry ->
                        GlassCard(Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(entry.title, color = Color.White, fontWeight = FontWeight.SemiBold)
                                    Text(entry.time, color = Color(0xFF9FB2C6), fontSize = 11.sp)
                                }
                                Text(
                                    (if (entry.income) "+ " else "− ") + "₹ ${money(entry.amount)}",
                                    color = if (entry.income) Color(0xFF65E6D2) else Color(0xFFFF9B9B),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                item {
                    Text("आपका डेटा इसी फोन में सेव होता है। इंटरनेट की जरूरत नहीं।",
                        color = Color(0xFF8FA5BA), fontSize = 11.sp)
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (addIncome) "नई कमाई" else "नया खर्च") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it },
                        label = { Text("विवरण") }, singleLine = true)
                    OutlinedTextField(value = amount, onValueChange = { amount = it },
                        label = { Text("रकम (₹)") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val value = amount.toDoubleOrNull()
                    if (title.isNotBlank() && value != null && value > 0) {
                        val date = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale("hi", "IN")).format(Date())
                        entries = entries + Entry(title.trim(), value, addIncome, date)
                        saveEntries(prefs, entries)
                        showDialog = false
                    }
                }) { Text("सेव करें") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("रद्द करें") } }
        )
    }
}

@Composable
private fun GlassCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .background(
                Brush.linearGradient(listOf(Color(0x553F7894), Color(0x332C416D), Color(0x443C4778))),
                RoundedCornerShape(24.dp)
            )
            .border(1.dp, Color(0x668BD7E5), RoundedCornerShape(24.dp))
            .padding(17.dp),
        content = content
    )
}

private fun money(value: Double): String = String.format(Locale("en", "IN"), "%,.2f", value)

private fun loadEntries(prefs: android.content.SharedPreferences): List<Entry> {
    val raw = prefs.getString("entries", "[]") ?: "[]"
    return try {
        val array = JSONArray(raw)
        (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            Entry(o.getString("title"), o.getDouble("amount"), o.getBoolean("income"), o.getString("time"))
        }
    } catch (_: Exception) { emptyList() }
}

private fun saveEntries(prefs: android.content.SharedPreferences, entries: List<Entry>) {
    val array = JSONArray()
    entries.forEach { e ->
        array.put(JSONObject().put("title", e.title).put("amount", e.amount)
            .put("income", e.income).put("time", e.time))
    }
    prefs.edit().putString("entries", array.toString()).apply()
}
