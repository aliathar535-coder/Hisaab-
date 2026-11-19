
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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

private data class Entry(
    val title: String,
    val amount: Double,
    val income: Boolean,
    val time: String
)

private data class PieceRate(
    val name: String,
    val rate: Double
)

   
private data class ScreenDesign(
    val enabled: Boolean = true,
    val color1: Long = 0x664D8CA6L,
    val color2: Long = 0x44334270L,
    val transparency: Float = 0.65f,
    val depth: Float = 12f,
    val glow: Float = 0.5f
)

private val designScreens = listOf(
    "home",
    "pieces",
    "history",
    "settings",
    "settings_menu"
)

 private data class PieceRecord( 
    val name: String,
    val rate: Double,
    val quantity: Int,
    val total: Double,
    val time: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HisaabApp()
        }
    }
}

private val bg = Color(0xFF07111F)
private val glass = Color(0xFF172B43)
private val mint = Color(0xFF65E6D2)
private val red = Color(0xFFFF929F)

@Composable
private fun HisaabApp() {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("hisaab_local", 0)
    }

    var entries by remember {
        mutableStateOf(loadEntries(prefs))
    }

    var rates by remember {
        mutableStateOf(loadRates(prefs))
    }

    var records by remember {
        mutableStateOf(loadPieces(prefs))
    }

   
var designs by remember {
    mutableStateOf(loadDesigns(prefs))
}

val updateScreenDesign: (String, ScreenDesign) -> Unit =
    { screen, design ->
        val updated = designs + (screen to design)
        designs = updated
        saveDesigns(prefs, updated)
    }
    
    var page by remember { mutableStateOf(0) }
    var dialog by remember { mutableStateOf("") }

    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(true) }

    var pieceName by remember { mutableStateOf("") }
    var pieceRate by remember { mutableStateOf("") }
    var selectedPiece by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }

    val income =
        entries.filter { it.income }.sumOf { it.amount } +
        records.sumOf { it.total }

    val expense =
        entries.filter { !it.income }.sumOf { it.amount }

    val balance = income - expense

    val selectedRate =
        rates.find { it.name == selectedPiece }?.rate ?: 0.0

    val qty = quantity.toIntOrNull() ?: 0
    val pieceTotal = selectedRate * qty

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = bg,
            surface = glass,
            primary = mint
        )
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            bg,
                            Color(0xFF102D48),
                            Color(0xFF201B43)
                        )
                    )
                )
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "HISAAB",
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Text(
                        SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a",
                            Locale.getDefault()
                        ).format(Date()),
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }

                Text(
                    "● OFFLINE",
                    color = mint,
                    fontSize = 11.sp
                )

                Spacer(Modifier.width(12.dp))

                IconButton(onClick = { page = 3 }) {
                    Icon(
                        Icons.Default.Settings,
                        "Settings",
                        tint = Color.White
                    )
                }
            }

            TabRow(
                selectedTabIndex = page,
                containerColor = Color.Transparent,
                contentColor = mint
            ) {
                listOf(
                    "होम",
                    "पीस",
                    "हिस्ट्री",
                    "सेटिंग"
                ).forEachIndexed { i, name ->
                    Tab(
                        selected = page == i,
                        onClick = { page = i },
                        text = {
                            Text(name, fontSize = 12.sp)
                        }
                    )
                }
            }

            when (page) {
                0 -> HomePage(
    design = designs["home"] ?: ScreenDesign(),
    income = income,
                    expense = expense,
                    balance = balance,
                    records = records,
                    onIncome = {
                        isIncome = true
                        title = ""
                        amount = ""
                        dialog = "entry"
                    },
                    onExpense = {
                        isIncome = false
                        title = ""
                        amount = ""
                        dialog = "entry"
                    }
                )

                1 -> PiecePage(
                    rates = rates,
                    records = records,
                    selectedPiece = selectedPiece,
                    onSelect = { selectedPiece = it },
                    quantity = quantity,
                    onQuantity = { quantity = it },
                    selectedRate = selectedRate,
                    pieceTotal = pieceTotal,
                    onSave = {
                        if (
                            selectedPiece.isNotBlank() &&
                            qty > 0 &&
                            selectedRate > 0
                        ) {
                            records = records + PieceRecord(
                                selectedPiece,
                                selectedRate,
                                qty,
                                pieceTotal,
                                now()
                            )
                            savePieces(prefs, records)
                            quantity = ""
                        }
                    },
                    onDeleteRate = { name ->
                        rates = rates.filterNot {
                            it.name == name
                        }
                        saveRates(prefs, rates)

                        if (selectedPiece == name) {
                            selectedPiece = ""
                        }
                    }
                )

                2 -> HistoryPage(
                    records = records,
                    entries = entries
                )

                3 -> SettingsPage(
    designs = designs,
    onDesignChange = updateScreenDesign,
    pieceName = pieceName,
                    onPieceName = { pieceName = it },
                    pieceRate = pieceRate,
                    onPieceRate = { pieceRate = it },
                    onSaveRate = {
                        val r = pieceRate.toDoubleOrNull()

                        if (
                            pieceName.isNotBlank() &&
                            r != null &&
                            r > 0
                        ) {
                            val updated = rates.filterNot {
                                it.name.equals(
                                    pieceName.trim(),
                                    true
                                )
                            }

                            rates = updated + PieceRate(
                                pieceName.trim(),
                                r
                            )

                            saveRates(prefs, rates)
                            pieceName = ""
                            pieceRate = ""
                        }
                    }
                )
            }
        }
    }
}    
@Composable
private fun HomePage(
    design: ScreenDesign,
    income: Double,
    expense: Double,
    balance: Double,
    records: List<PieceRecord>,
    onIncome: () -> Unit,
    onExpense: () -> Unit
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        item {
            GlassCard(design = design) {
                Text("कुल बैलेंस", color = Color.LightGray)
                Text(
                    "₹ ${money(balance)}",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (balance >= 0) "आपका बचा हुआ पैसा"
                    else "खर्च कमाई से अधिक है",
                    color = mint
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassCard(modifier = Modifier.weight(1f), design = design) {
                    Text("कुल कमाई", color = Color.LightGray)
                    Text(
                        "₹${money(income)}",
                        color = mint,
                        fontWeight = FontWeight.Bold
                    )
                }

                GlassCard(modifier = Modifier.weight(1f), design = design) {
                    Text("कुल खर्च", color = Color.LightGray)
                    Text(
                        "₹${money(expense)}",
                        color = red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Button(
                onClick = onIncome,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, null)
                Text("कमाई जोड़ें")
            }
        }

        item {
            Button(
                onClick = onExpense,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9A4355)
                )
            ) {
                Text("खर्च जोड़ें")
            }
        }

        item {
            Text(
                "हाल की पीस एंट्री",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(records.takeLast(5).reversed()) { record ->
            GlassCard(design = design) {
                Text(
                    record.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${record.quantity} पीस × ₹${money(record.rate)}",
                    color = Color.LightGray
                )
                Text(
                    "₹${money(record.total)}",
                    color = mint
                )
                Text(
                    record.time,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun PiecePage(
    rates: List<PieceRate>,
    records: List<PieceRecord>,
    selectedPiece: String,
    onSelect: (String) -> Unit,
    quantity: String,
    onQuantity: (String) -> Unit,
    selectedRate: Double,
    pieceTotal: Double,
    onSave: () -> Unit,
    onDeleteRate: (String) -> Unit
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            GlassCard {
                Text(
                    "पीस का हिसाब",
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "सेव किए हुए पीस का नाम चुनें",
                    color = Color.LightGray
                )

                Spacer(Modifier.height(12.dp))

                if (rates.isEmpty()) {
                    Text(
                        "पहले सेटिंग में पीस का नाम और रेट सेव करें।",
                        color = red
                    )
                } else {
                    rates.forEach { rate ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedPiece == rate.name,
                                onClick = {
                                    onSelect(rate.name)
                                }
                            )

                            Column(Modifier.weight(1f)) {
                                Text(
                                    rate.name,
                                    color = Color.White
                                )
                                Text(
                                    "रेट ₹${money(rate.rate)}",
                                    color = mint,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = quantity,
                    onValueChange = onQuantity,
                    label = {
                        Text("कितने पीस बनाए?")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "रेट: ₹${money(selectedRate)}",
                    color = Color.White
                )

                Text(
                    "कुल कमाई: ₹${money(pieceTotal)}",
                    color = mint,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(10.dp))

                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedPiece.isNotBlank() &&
                        quantity.toIntOrNull()?.let { it > 0 } == true &&
                        selectedRate > 0
                ) {
                    Text("पीस का हिसाब सेव करें")
                }
            }
        }

        item {
            Text(
                "सेव किए हुए पीस",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        items(rates) { rate ->
            GlassCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(rate.name, color = Color.White)
                        Text(
                            "₹${money(rate.rate)} प्रति पीस",
                            color = mint
                        )
                    }

                    TextButton(
                        onClick = {
                            onDeleteRate(rate.name)
                        }
                    ) {
                        Text("हटाएँ", color = red)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryPage(
    records: List<PieceRecord>,
    entries: List<Entry>
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                "पूरी हिस्ट्री",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(records.reversed()) { record ->
            GlassCard {
                Text(
                    record.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "${record.quantity} पीस × ₹${money(record.rate)}",
                    color = Color.LightGray
                )

                Text(
                    "कुल ₹${money(record.total)}",
                    color = mint,
                    fontSize = 19.sp
                )

                Text(
                    record.time,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }

        items(entries.reversed()) { entry ->
            GlassCard {
                Text(entry.title, color = Color.White)

                Text(
                    (if (entry.income) "+ " else "- ") +
                        "₹${money(entry.amount)}",
                    color = if (entry.income) mint else red
                )
                
                Text(
                    entry.time,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun SettingsPage(
    designs: Map<String, ScreenDesign>,
    onDesignChange: (String, ScreenDesign) -> Unit,
    pieceName: String,
    onPieceName: (String) -> Unit,
    pieceRate: String,
    onPieceRate: (String) -> Unit,
    onSaveRate: () -> Unit
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
               item {
            DesignStudio(
                designs = designs,
                onDesignChange = onDesignChange
            )
               } item {
            GlassCard {
                Text(
                    "सेटिंग",
                    color = Color.White,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "पीस के नाम और रेट यहाँ सेव करें।",
                    color = Color.LightGray
                )
            }
        }

        item {
            GlassCard {
                Text(
                    "नया पीस रेट जोड़ें",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = pieceName,
                    onValueChange = onPieceName,
                    label = { Text("पीस का नाम") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pieceRate,
                    onValueChange = onPieceRate,
                    label = { Text("एक पीस का रेट ₹") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = onSaveRate,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("रेट सेव करें")
                }
            }
        }

        item {
            GlassCard {
                Text(
                    "आगे के फीचर",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "फोटो स्टूडियो, घड़ी, डिजाइन स्टूडियो और बैकअप अगले चरणों में जोड़े जाएँगे।",
                    color = Color.LightGray
                )
            }
        }
    }
}

@Composable
private fun DesignStudio(
    designs: Map<String, ScreenDesign>,
    onDesignChange: (String, ScreenDesign) -> Unit
) {
    var selectedScreen by remember {
        mutableStateOf("home")
    }

    var menuExpanded by remember {
        mutableStateOf(false)
    }

    val design = designs[selectedScreen] ?: ScreenDesign()

    var colorOne by remember(selectedScreen, design.color1) {
        mutableStateOf(
            design.color1.toString(16).takeLast(6).uppercase()
        )
    }

    var colorTwo by remember(selectedScreen, design.color2) {
        mutableStateOf(
            design.color2.toString(16).takeLast(6).uppercase()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFF172B43),
                RoundedCornerShape(22.dp)
            )
            .border(
                1.dp,
                Color(0xFF70DDF5),
                RoundedCornerShape(22.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "3D LIQUID GLASS STUDIO",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            "किस स्क्रीन का डिजाइन बदलना है?",
            color = Color.LightGray
        )

        Button(
            onClick = { menuExpanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                when (selectedScreen) {
                    "home" -> "Home Dashboard"
                    "pieces" -> "Pieces Screen"
                    "history" -> "History Screen"
                    "settings" -> "Settings Screen"
                    else -> "Settings Menu / Icon"
                }
            )
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            designScreens.forEach { screen ->
                DropdownMenuItem(
                    text = {
                        Text(
                            when (screen) {
                                "home" -> "Home Dashboard"
                                "pieces" -> "Pieces Screen"
                                "history" -> "History Screen"
                                "settings" -> "Settings Screen"
                                else -> "Settings Menu / Icon"
                            }
                        )
                    },
                    onClick = {
                        selectedScreen = screen
                        menuExpanded = false
                    }
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "3D Glass चालू करें",
                color = Color.White,
                modifier = Modifier.weight(1f)
            )

            Switch(
                checked = design.enabled,
                onCheckedChange = {
                    onDesignChange(
                        selectedScreen,
                        design.copy(enabled = it)
                    )
                }
            )
        }

        Text("पहला रंग (HEX)", color = Color.White)

        OutlinedTextField(
            value = colorOne,
            onValueChange = {
                colorOne = it.uppercase().filter { c ->
                    c in "0123456789ABCDEF"
                }.take(6)

                if (colorOne.length == 6) {
                    val value = ("FF" + colorOne).toLong(16)
                    onDesignChange(
                        selectedScreen,
                        design.copy(color1 = value)
                    )
                }
            },
            label = { Text("जैसे FF4488") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text("दूसरा रंग (HEX)", color = Color.White)

        OutlinedTextField(
            value = colorTwo,
            onValueChange = {
                colorTwo = it.uppercase().filter { c ->
                    c in "0123456789ABCDEF"
                }.take(6)

                if (colorTwo.length == 6) {
                    val value = ("FF" + colorTwo).toLong(16)
                    onDesignChange(
                        selectedScreen,
                        design.copy(color2 = value)
                    )
                }
            },
            label = { Text("जैसे 3355AA") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text(
            "Transparency: ${(design.transparency * 100).toInt()}%",
            color = Color.White
        )

        Slider(
            value = design.transparency,
            onValueChange = {
                onDesignChange(
                    selectedScreen,
                    design.copy(transparency = it)
                )
            },
            valueRange = 0.1f..1f
        )

        Text(
            "3D Depth: ${design.depth.toInt()}",
            color = Color.White
        )

        Slider(
            value = design.depth,
            onValueChange = {
                onDesignChange(
                    selectedScreen,
                    design.copy(depth = it)
                )
            },
            valueRange = 0f..30f
        )

        Text(
            "Glow: ${(design.glow * 100).toInt()}%",
            color = Color.White
        )

        Slider(
            value = design.glow,
            onValueChange = {
                onDesignChange(
                    selectedScreen,
                    design.copy(glow = it)
                )
            },
            valueRange = 0f..1f
        )

        Text(
            "इस स्क्रीन की डिजाइन अलग सेव होती है।",
            color = Color(0xFF8FEAFF),
            fontSize = 12.sp
        )
    }
}
@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    design: ScreenDesign = ScreenDesign(),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(23.dp)

    val firstColor = if (design.enabled) {
        Color(design.color1).copy(
            alpha = design.transparency.coerceIn(0f, 1f)
        )
    } else {
        Color(0x664D8CA6)
    }

    val secondColor = if (design.enabled) {
        Color(design.color2).copy(
            alpha = design.transparency.coerceIn(0f, 1f)
        )
    } else {
        Color(0x44334270)
    }

    val glowAmount = design.glow.coerceIn(0f, 1f)

    Column(
        modifier
            .shadow(
                elevation = if (design.enabled) {
                    design.depth.coerceIn(0f, 30f).dp
                } else {
                    0.dp
                },
                shape = shape
            )
            .background(
                Brush.linearGradient(
                    listOf(
                        firstColor,
                        Color.White.copy(
                            alpha = if (design.enabled) {
                                0.12f * glowAmount
                            } else {
                                0f
                            }
                        ),
                        secondColor
                    )
                ),
                shape
            )
            .border(
                1.5.dp,
                Color(0xFFB9F3FF).copy(
                    alpha = if (design.enabled) {
                        0.25f + (glowAmount * 0.65f)
                    } else {
                        0.35f
                    }
                ),
                shape
            )
            .padding(16.dp),
        content = content
    )
}
private fun now(): String {
    return SimpleDateFormat(
        "dd MMM yyyy, hh:mm a",
        Locale.getDefault()
    ).format(Date())
}

private fun money(value: Double): String {
    return String.format(Locale.US, "%,.2f", value)
}

private fun loadEntries(
    p: android.content.SharedPreferences
): List<Entry> {
    return try {
        val a = JSONArray(
            p.getString("entries", "[]")
        )

        (0 until a.length()).map {
            val o = a.getJSONObject(it)

            Entry(
                o.getString("title"),
                o.getDouble("amount"),
                o.getBoolean("income"),
                o.getString("time")
            )
        }
    } catch (_: Exception) {
        emptyList()
    }
}

private fun saveEntries(
    p: android.content.SharedPreferences,
    list: List<Entry>
) {
    val a = JSONArray()

    list.forEach {
        a.put(
            JSONObject()
                .put("title", it.title)
                .put("amount", it.amount)
                .put("income", it.income)
                .put("time", it.time)
        )
    }

    p.edit()
        .putString("entries", a.toString())
        .apply()
}

private fun loadRates(
    p: android.content.SharedPreferences
): List<PieceRate> {
    return try {
        val a = JSONArray(
            p.getString("piece_rates", "[]")
        )

        (0 until a.length()).map {
            val o = a.getJSONObject(it)

            PieceRate(
                o.getString("name"),
                o.getDouble("rate")
            )
        }
    } catch (_: Exception) {
        emptyList()
    }
}

private fun saveRates(
    p: android.content.SharedPreferences,
    list: List<PieceRate>
) {
    val a = JSONArray()

    list.forEach {
        a.put(
            JSONObject()
                .put("name", it.name)
                .put("rate", it.rate)
        )
    }

    p.edit()
        .putString("piece_rates", a.toString())
        .apply()
}

private fun loadPieces(
    p: android.content.SharedPreferences
): List<PieceRecord> {
    return try {
        val a = JSONArray(
            p.getString("piece_records", "[]")
        )

        (0 until a.length()).map {
            val o = a.getJSONObject(it)

            PieceRecord(
                o.getString("name"),
                o.getDouble("rate"),
                o.getInt("quantity"),
                o.getDouble("total"),
                o.getString("time")
            )
        }
    } catch (_: Exception) {
        emptyList()
    }
}

private fun savePieces(
    p: android.content.SharedPreferences,
    list: List<PieceRecord>
) {
    val a = JSONArray()

    list.forEach {
        a.put(
            JSONObject()
                .put("name", it.name)
                .put("rate", it.rate)
                .put("quantity", it.quantity)
                .put("total", it.total)
                .put("time", it.time)
        )
    }

    p.edit()
        .putString("piece_records", a.toString())
        .apply()
}
private fun loadDesigns(
    prefs: android.content.SharedPreferences
): Map<String, ScreenDesign> {

    val json = JSONObject(
        prefs.getString("screen_designs", "{}") ?: "{}"
    )

    return designScreens.associateWith { screen ->
        val item = json.optJSONObject(screen)

        if (item == null) {
            ScreenDesign()
        } else {
            ScreenDesign(
                enabled = item.optBoolean("enabled", true),
                color1 = item.optLong(
                    "color1", 0x664D8CA6L
                ),
                color2 = item.optLong(
                    "color2", 0x44334270L
                ),
                transparency = item.optDouble(
                    "transparency", 0.65
                ).toFloat(),
                depth = item.optDouble(
                    "depth", 12.0
                ).toFloat(),
                glow = item.optDouble(
                    "glow", 0.5
                ).toFloat()
            )
        }
    }
}

private fun saveDesigns(
    prefs: android.content.SharedPreferences,
    designs: Map<String, ScreenDesign>
) {
    val json = JSONObject()

    designs.forEach { (screen, design) ->
        val item = JSONObject()

        item.put("enabled", design.enabled)
        item.put("color1", design.color1)
        item.put("color2", design.color2)
        item.put("transparency", design.transparency)
        item.put("depth", design.depth)
        item.put("glow", design.glow)

        json.put(screen, item)
    }

    prefs.edit()
        .putString("screen_designs", json.toString())
        .apply()
}
