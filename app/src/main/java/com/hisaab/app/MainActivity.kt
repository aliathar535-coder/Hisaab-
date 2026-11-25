
package com.hisaab.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
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
    val colors: List<Long> = listOf(
        0xFF4D8CA6L,
        0xFF334270L
    ),
    val transparency: Float = 0.65f,
    val depth: Float = 12f,
    val glow: Float = 0.5f,
    val gradientAngle: Float = 45f,
    val gradientType: String = "linear",
    val shading: Float = 0.35f,
    val colorPositions: List<Float> = listOf(0f, 0.25f, 0.5f, 0.75f, 1f)
)

private data class HistoryCardStyle(
    val backgroundColor: Long = 0xFF263B59L,
    val textColor: Long = 0xFFFFFFFFL,
    val secondaryTextColor: Long = 0xFFCFD8E8L,
    val accentColor: Long = 0xFF65E6D2L,
    val fontSize: Float = 16f,
    val fontStyle: String = "bold",
    val cornerRadius: Float = 20f,
    val widthPercent: Float = 100f,
    val heightDp: Float = 0f,
    val horizontalPadding: Float = 16f,
    val verticalPadding: Float = 16f,
    val borderColor: Long = 0xFF65E6D2L,
    val borderWidth: Float = 1f,
    val gradientColors: List<Long> = listOf(
        0xFF263B59L,
        0xFF315A80L,
        0xFF4169A1L,
        0xFF6756A5L,
        0xFF65E6D2L
    ),
    val gradientAngle: Float = 45f
)

private data class MainCardStyle(
    val widthPercent: Float = 100f,
    val heightDp: Float = 0f,
    val backgroundColor: Long = 0xFF263B59L,
    val textColor: Long = 0xFFFFFFFFL,
    val fontSize: Float = 16f,
    val fontStyle: String = "bold",
    val text3D: Boolean = true,
    val cornerRadius: Float = 25f,
    val gradientColors: List<Long> = listOf(
        0xFF263B59L,
        0xFF315A80L,
        0xFF4169A1L,
        0xFF6756A5L,
        0xFF65E6D2L
    )
)

private data class DesignPreset(
    val id: String,
    val name: String,
    val category: String,
    val colors: List<Long>,
    val effect: String,
    val angle: Float = 135f,
    val depth: Float = 12f,
    val glow: Float = 0.4f
)

private val designCategories = listOf(
    "Material",
    "Texture",
    "3D Shape & Surface",
    "Light & Effects",
    "Gradient",
    "Colors",
    "Shading",
    "Border & Edge",
    "Shadow",
    "Glass Effects",
    "Metallic Effects"
)
private val designPresets = designCategories.flatMapIndexed { categoryIndex, category ->
    (1..30).map { index ->
        val hue1 = ((index * 37 + categoryIndex * 29) % 360).toFloat()
        val hue2 = (hue1 + 55f) % 360f

        val color1 = android.graphics.Color.HSVToColor(
            floatArrayOf(hue1, 0.72f, 0.95f)
        ).toLong() and 0xFFFFFFFFL

        val color2 = android.graphics.Color.HSVToColor(
            floatArrayOf(hue2, 0.68f, 0.78f)
        ).toLong() and 0xFFFFFFFFL

        DesignPreset(
            id = "${categoryIndex}_${index}",
            name = "$category ${index.toString().padStart(2, '0')}",
            category = category,
            colors = listOf(color1, color2),
            effect = category,
            angle = (index * 12f) % 360f,
            depth = 5f + (index % 6) * 5f,
            glow = (index % 10) / 10f
        )
    }
}


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
    val time: String,
    val photoPath: String = ""
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


var pieceHistoryStyle by remember {
    mutableStateOf(
        loadHistoryCardStyle(prefs, "piece_history_style")
    )
}

var entryHistoryStyle by remember {
    mutableStateOf(
        loadHistoryCardStyle(prefs, "entry_history_style")
    )
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
    
var entryPieceName by remember { mutableStateOf("") }
var entryQuantity by remember { mutableStateOf("") }
var entryRateMenuExpanded by remember { mutableStateOf(false) }

var piecePhoto by remember { mutableStateOf<Bitmap?>(null) }
var piecePhotoUri by remember { mutableStateOf<Uri?>(null) }

val galleryLauncher =
    rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        piecePhotoUri = uri
        piecePhoto = null
    }

val cameraLauncher =
    rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        piecePhoto = bitmap
        piecePhotoUri = null
    }
    
    val income =
        entries.filter { it.income }.sumOf { it.amount } +
        records.sumOf { it.total }

    val expense =
        entries.filter { !it.income }.sumOf { it.amount }

    val balance = income - expense

    val selectedRate =
        rates.find { it.name == selectedPiece }?.rate ?: 0.0
    
val entrySelectedRate =
    rates.find { it.name == entryPieceName }?.rate ?: 0.0

val entryQty = entryQuantity.toIntOrNull() ?: 0
val entryTotal = entrySelectedRate * entryQty
    
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

                IconButton(onClick = {
    page = if (page == 3) 0 else 3
}) {
                    Icon(
                        Icons.Default.Settings,
                        "Settings",
                        tint = Color.White
                    )
                }
            }

            
Box(
    Modifier
        .fillMaxSize()
        .pointerInput(page) {
            var drag = 0f

            detectHorizontalDragGestures(
                onHorizontalDrag = { change, amount ->
                    drag += amount
                    change.consume()
                },
                onDragEnd = {
                    if (drag < -80f) {
                        page = (page + 1).coerceAtMost(2)
                    } else if (drag > 80f) {
                        page = (page - 1).coerceAtLeast(0)
                    }
                    drag = 0f
                },
                onDragCancel = {
                    drag = 0f
                }
            )
        }
) {
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
                       entryPieceName = ""
entryQuantity = ""
piecePhoto = null
piecePhotoUri = null
                        dialog = "entry"
                    },
                    onExpense = {
                        isIncome = false
                        title = ""
                        amount = ""
                        dialog = "entry"
                    }
                )

                2 -> PiecePage(
                    design = designs["pieces"] ?: ScreenDesign(),
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


1 -> HistoryPage(
    design = designs["history"] ?: ScreenDesign(),
    pieceCardStyle = pieceHistoryStyle,
    entryCardStyle = entryHistoryStyle,
    
    onPieceStyleChange = { style ->
        pieceHistoryStyle = style
        saveHistoryCardStyle(
            prefs,
            "piece_history_style",
            style
        )
    },
    onEntryStyleChange = { style ->
        entryHistoryStyle = style
        saveHistoryCardStyle(
            prefs,
            "entry_history_style",
            style
        )
    },
    
    records = records,
    entries = entries,
    onDeleteRecord = { index ->
        records = records.filterIndexed { i, _ -> i != index }
        savePieces(prefs, records)
    },
    onDeleteEntry = { index ->
        entries = entries.filterIndexed { i, _ -> i != index }
        saveEntries(prefs, entries)
    },
    onClearAll = {
        records = emptyList()
        entries = emptyList()
        savePieces(prefs, records)
        saveEntries(prefs, entries)
    }
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
                it.name.equals(pieceName.trim(), true)
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
  
if (dialog == "entry") {
    AlertDialog(
        onDismissRequest = {
            dialog = ""
        },
        title = {
            Text(
                if (isIncome) "कमाई जोड़ें" else "खर्च जोड़ें",
                color = Color.White
            )
        },
        
        text = {
            Column {
                if (isIncome) {
                    Box {
                        OutlinedButton(
                            onClick = {
                                entryRateMenuExpanded = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (entryPieceName.isBlank())
                                    "पीस का नाम चुनें"
                                else entryPieceName
                            )
                        }

                        DropdownMenu(
                            expanded = entryRateMenuExpanded,
                            onDismissRequest = {
                                entryRateMenuExpanded = false
                            }
                        ) {
                            rates.forEach { rate ->
                                DropdownMenuItem(
                                    text = {
                                        Text("${rate.name} — ₹${money(rate.rate)}")
                                    },
                                    onClick = {
                                        entryPieceName = rate.name
                                        entryRateMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = entryQuantity,
                        onValueChange = {
                            entryQuantity = it.filter { c -> c.isDigit() }
                        },
                        label = { Text("पीस की संख्या") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        "रेट: ₹${money(entrySelectedRate)}",
                        color = Color.White
                    )

                    Text(
                        "कुल कमाई: ₹${money(entryTotal)}",
                        color = mint,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                galleryLauncher.launch("image/*")
                            }
                        ) {
                            Text("गैलरी")
                        }

                        Button(
                            onClick = {
                                
                         cameraLauncher.launch(null)

                            }
                        ) {
                            Text("कैमरा")
                        }
                    }

                    piecePhoto?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "पीस फोटो",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                    if (piecePhotoUri != null) {
                        Text("गैलरी की फोटो चुनी गई है", color = mint)
                    }

                } else {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("किस चीज का खर्च है?") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("रकम दर्ज करें (₹)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = if (isIncome) {
                    entryPieceName.isNotBlank() &&
                    entryQty > 0 &&
                    entrySelectedRate > 0
                } else {
                    title.isNotBlank() &&
                    (amount.toDoubleOrNull() ?: 0.0) > 0
                },
                onClick = {
                    if (isIncome) {
                        val savedPhoto = savePiecePhoto(
                            context,
                            piecePhoto,
                            piecePhotoUri
                        )

                        val newRecord = PieceRecord(
                            name = entryPieceName,
                            rate = entrySelectedRate,
                            quantity = entryQty,
                            total = entryTotal,
                            time = now(),
                            photoPath = savedPhoto
                        )

                        records = records + newRecord
                        savePieces(prefs, records)

                        entryPieceName = ""
                        entryQuantity = ""
                        piecePhoto = null
                        piecePhotoUri = null

                    } else {
                        val value = amount.toDoubleOrNull()

                        if (value != null && value > 0) {
                            entries = entries + Entry(
                                title = title.trim(),
                                amount = value,
                                income = false,
                                time = now()
                            )

                            saveEntries(prefs, entries)
                            title = ""
                            amount = ""
                        }
                    }

                    dialog = ""
                }
            ) {
                Text("सेव करें")
            }
        },
        
        dismissButton = {
            TextButton(
                onClick = {
                    dialog = ""
                }
            ) {
                Text("रद्द करें")
            }
        },
        containerColor = glass,
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
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
    design: ScreenDesign,
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
            GlassCard(design = design) {
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
            GlassCard(design = design) {
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
    design: ScreenDesign,
    
    pieceCardStyle: HistoryCardStyle,
    entryCardStyle: HistoryCardStyle,
    
            records: List<PieceRecord>,
    entries: List<Entry>,
    onPieceStyleChange: (HistoryCardStyle) -> Unit,
    onEntryStyleChange: (HistoryCardStyle) -> Unit,
    onDeleteRecord: (Int) -> Unit,
    onDeleteEntry: (Int) -> Unit,
    onClearAll: () -> Unit
) {
    var deleteRecordIndex by remember { mutableStateOf<Int?>(null) }
    var deleteEntryIndex by remember { mutableStateOf<Int?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var zoomPhotoPath by remember { mutableStateOf<String?>(null) }
    var editingHistoryStyle by remember { mutableStateOf<String?>(null) }

    BackHandler(enabled = zoomPhotoPath != null) {
        zoomPhotoPath = null
    }
val historyListState = rememberLazyListState()
    LazyColumn(
       state = historyListState,
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = 110.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "पूरी हिस्ट्री",
                color = Color.White,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { showClearConfirm = true },
                enabled = records.isNotEmpty() || entries.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9A4355)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("पूरी हिस्ट्री साफ करें")
            }
        }

        itemsIndexed(
            records.indices.reversed().toList(),
            key = { _, index ->
                "piece_${records[index].time}_${records[index].name}_$index"
            }
        ) { _, index ->
            val record = records[index]

val historyPhoto by produceState<androidx.compose.ui.graphics.ImageBitmap?>(
    initialValue = null,
    key1 = record.photoPath
) {
    value = if (record.photoPath.isNotBlank()) {
        withContext(Dispatchers.IO) {
            BitmapFactory.decodeFile(
                record.photoPath
            )?.asImageBitmap()
        }
    } else {
        null
    }
}

            HistoryStyledCard(style = pieceCardStyle) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            Modifier.weight(1f)
                        ) {
                            Text(
                                record.name,
                                color = Color(pieceCardStyle.textColor),

                                fontWeight = FontWeight.Bold,
                                fontSize = pieceCardStyle.fontSize.sp
                            )

                            Text(
                                "${record.quantity} पीस × ₹${money(record.rate)}",
                            color = Color(pieceCardStyle.secondaryTextColor)
                            )

                            Text(
                                "कुल ₹${money(record.total)}",
                            color = Color(pieceCardStyle.accentColor),
                            fontSize = 19.sp
                            )

                            Text(
                                record.time,
                            color = Color(pieceCardStyle.secondaryTextColor),
                            fontSize = 11.sp
                            )
                        }

                                               TextButton(
                            onClick = {
                                editingHistoryStyle = "piece"
                            }
                        ) {
                            Text("⚙ डिजाइन", color = Color.Cyan)
                                               }
                        TextButton(
                            onClick = {
                                deleteRecordIndex = index
                            }
                        ) {
                            Text("🗑 हटाएँ", color = red)
                        }
                    }

                    historyPhoto?.let { photo ->
                        Spacer(Modifier.height(10.dp))

                        Image(
                            bitmap = photo,
                            contentDescription = "फोटो बड़ी करके देखें",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clickable {
                                    zoomPhotoPath = record.photoPath
                                },
                            contentScale = ContentScale.Crop
                        )

                        Text(
                            "फोटो बड़ी देखने के लिए टच करें",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        itemsIndexed(
            entries.indices.reversed().toList(),
            key = { _, index ->
                "entry_${entries[index].time}_${entries[index].title}_$index"
            }
        ) { _, index ->
            val entry = entries[index]

            HistoryStyledCard(style = entryCardStyle) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        Modifier.weight(1f)
                    ) {
                        Text(
                            entry.title,
                            color = Color(entryCardStyle.textColor),
                            fontSize = entryCardStyle.fontSize.sp
                        )

                        Text(
                            (if (entry.income) "+ " else "- ") +
                                "₹${money(entry.amount)}",
                            color = Color(entryCardStyle.accentColor)
                        )

                        Text(
                            entry.time,
                          color = Color(entryCardStyle.secondaryTextColor),
                           fontSize = 11.sp
                        )
                    }

                                       TextButton(
                        onClick = {
                            editingHistoryStyle = "entry"
                        }
                    ) {
                        Text("⚙ डिजाइन", color = Color.Cyan)
                                       }
                    TextButton(
                        onClick = {
                            deleteEntryIndex = index
                        }
                    ) {
                        Text("🗑 हटाएँ", color = red)
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
            Text(
                "— हिस्ट्री समाप्त —",
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }

    if (zoomPhotoPath != null) {
        val zoomBitmap = remember(zoomPhotoPath) {
            zoomPhotoPath?.let {
                BitmapFactory.decodeFile(it)?.asImageBitmap()
            }
        }

        if (zoomBitmap != null) {
            Dialog(
                onDismissRequest = {
                    zoomPhotoPath = null
                },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.97f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = zoomBitmap,
                        contentDescription = "बड़ी फोटो",
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.85f)
                            .padding(8.dp),
                        contentScale = ContentScale.Fit
                    )

                    TextButton(
                        onClick = {
                            zoomPhotoPath = null
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        Text(
                            "✕",
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        "बंद करने के लिए ✕ या मोबाइल का Back दबाएँ",
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

        if (editingHistoryStyle != null) {
        val isPiece = editingHistoryStyle == "piece"
        val currentStyle = if (isPiece) pieceCardStyle else entryCardStyle

        var bgHex by remember(editingHistoryStyle) {
            mutableStateOf("#%08X".format(currentStyle.backgroundColor))
        }
        var textHex by remember(editingHistoryStyle) {
            mutableStateOf("#%08X".format(currentStyle.textColor))
        }
        var secondaryHex by remember(editingHistoryStyle) {
            mutableStateOf("#%08X".format(currentStyle.secondaryTextColor))
        }
        var accentHex by remember(editingHistoryStyle) {
            mutableStateOf("#%08X".format(currentStyle.accentColor))
        }
        var gradientHex1 by remember(editingHistoryStyle) {
    mutableStateOf("#%08X".format(currentStyle.gradientColors.getOrElse(0) { 0xFF263B59L }))
}
var gradientHex2 by remember(editingHistoryStyle) {
    mutableStateOf("#%08X".format(currentStyle.gradientColors.getOrElse(1) { 0xFF315A80L }))
}
var gradientHex3 by remember(editingHistoryStyle) {
    mutableStateOf("#%08X".format(currentStyle.gradientColors.getOrElse(2) { 0xFF4169A1L }))
}
var gradientHex4 by remember(editingHistoryStyle) {
    mutableStateOf("#%08X".format(currentStyle.gradientColors.getOrElse(3) { 0xFF6756A5L }))
}
var gradientHex5 by remember(editingHistoryStyle) {
    mutableStateOf("#%08X".format(currentStyle.gradientColors.getOrElse(4) { 0xFF65E6D2L }))
}
var gradientAngle by remember(editingHistoryStyle) {
    mutableFloatStateOf(currentStyle.gradientAngle)
}
var activeGradientPicker by remember(editingHistoryStyle) {
    mutableStateOf(-1)
}

var pickerHue by remember {
    mutableFloatStateOf(210f)
}

var pickerSaturation by remember {
    mutableFloatStateOf(0.8f)
}

var pickerBrightness by remember {
    mutableFloatStateOf(0.9f)
}
        var cardWidth by remember(editingHistoryStyle) {
    mutableFloatStateOf(currentStyle.widthPercent)
}

var cardHeight by remember(editingHistoryStyle) {
    mutableFloatStateOf(
        currentStyle.heightDp.coerceIn(20f, 300f)
    )
}
var cardVerticalPadding by remember(editingHistoryStyle) {
    mutableFloatStateOf(currentStyle.verticalPadding.coerceIn(0f, 20f))
}
var colorError by remember(editingHistoryStyle) {
    mutableStateOf("")
}

        AlertDialog(
            onDismissRequest = { editingHistoryStyle = null },
            title = { Text("हिस्ट्री कार्ड डिजाइन") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("बैकग्राउंड रंग")
                    OutlinedTextField(
                        value = bgHex,
                        onValueChange = { bgHex = it },
                        label = { Text("HEX रंग") },
                        singleLine = true
                    )
                    Text("मुख्य टेक्स्ट रंग")
                    OutlinedTextField(
                        value = textHex,
                        onValueChange = { textHex = it },
                        label = { Text("HEX रंग") },
                        singleLine = true
                    )
                    Text("छोटे टेक्स्ट का रंग")
OutlinedTextField(
    value = secondaryHex,
    onValueChange = { secondaryHex = it },
    label = { Text("HEX रंग") },
    singleLine = true
)

Text("एक्सेंट रंग")
OutlinedTextField(
    value = accentHex,
    onValueChange = { accentHex = it },
    label = { Text("HEX रंग") },
    singleLine = true
)

Text("उदाहरण: #FF3366CC")

Text("पाँच रंगों का मिक्स ग्रेडिएंट")

Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(6.dp)
) {
    repeat(5) { index ->
        val color = when (index) {
            0 -> gradientHex1
            1 -> gradientHex2
            2 -> gradientHex3
            3 -> gradientHex4
            else -> gradientHex5
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .background(
                    runCatching {
                        Color(color.removePrefix("#").toLong(16) or
                            if (color.removePrefix("#").length == 6)
                                0xFF000000L else 0L)
                    }.getOrDefault(Color.DarkGray),
                    RoundedCornerShape(16.dp)
                )
                .border(
                    2.dp,
                    if (activeGradientPicker == index)
                        Color.White else Color.Gray,
                    RoundedCornerShape(16.dp)
                )
                .clickable {
    val selectedHex = when (index) {
        0 -> gradientHex1
        1 -> gradientHex2
        2 -> gradientHex3
        3 -> gradientHex4
        else -> gradientHex5
    }

    val parsedColor = runCatching {
        android.graphics.Color.parseColor(selectedHex)
    }.getOrDefault(android.graphics.Color.DKGRAY)

    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(parsedColor, hsv)

    pickerHue = hsv[0]
    pickerSaturation = hsv[1]
    pickerBrightness = hsv[2]

    activeGradientPicker = index
},
            contentAlignment = Alignment.Center
        ) {
            Text(
    text = if (activeGradientPicker == index) "✓" else "${index + 1}",
    color = Color.White,
    fontSize = 20.sp,
    fontWeight = FontWeight.Bold
)
        }
    }
}

OutlinedTextField(
    value = gradientHex1,
    onValueChange = { gradientHex1 = it },
    label = { Text("रंग 1 HEX") },
    singleLine = true
)
OutlinedTextField(
    value = gradientHex2,
    onValueChange = { gradientHex2 = it },
    label = { Text("रंग 2 HEX") },
    singleLine = true
)
OutlinedTextField(
    value = gradientHex3,
    onValueChange = { gradientHex3 = it },
    label = { Text("रंग 3 HEX") },
    singleLine = true
)
OutlinedTextField(
    value = gradientHex4,
    onValueChange = { gradientHex4 = it },
    label = { Text("रंग 4 HEX") },
    singleLine = true
)
OutlinedTextField(
    value = gradientHex5,
    onValueChange = { gradientHex5 = it },
    label = { Text("रंग 5 HEX") },
    singleLine = true
)

Text("ग्रेडिएंट का कोण: ${gradientAngle.toInt()}°")
Slider(
    value = gradientAngle,
    onValueChange = { gradientAngle = it },
    valueRange = 0f..360f
)
Text(
    "LIVE CARD PREVIEW",
    color = Color(0xFF65E6D2),
    fontWeight = FontWeight.Bold
)

val previewTextColor = runCatching {
    Color(android.graphics.Color.parseColor(textHex))
}.getOrDefault(Color.White)

val previewSecondaryColor = runCatching {
    Color(android.graphics.Color.parseColor(secondaryHex))
}.getOrDefault(Color.LightGray)

val previewAccentColor = runCatching {
    Color(android.graphics.Color.parseColor(accentHex))
}.getOrDefault(Color.Yellow)

Box(
    modifier = Modifier
        .fillMaxWidth(cardWidth.coerceIn(20f, 100f) / 100f)
        .height(cardHeight.coerceIn(90f, 220f).dp)
        .drawWithCache {
            val colors = listOf(
                gradientHex1,
                gradientHex2,
                gradientHex3,
                gradientHex4,
                gradientHex5
            ).map {
                val hex = it.trim().removePrefix("#")
                val value = runCatching {
                    if (hex.length == 6) {
                        0xFF000000L or hex.toLong(16)
                    } else {
                        hex.toLong(16)
                    }
                }.getOrDefault(0xFF263B59L)

                Color(value)
            }

            val angle = Math.toRadians(gradientAngle.toDouble())
            val cx = size.width / 2f
            val cy = size.height / 2f
            val dx = kotlin.math.cos(angle).toFloat() * size.width / 2f
            val dy = kotlin.math.sin(angle).toFloat() * size.height / 2f

            val brush = Brush.linearGradient(
                colors = colors,
                start = Offset(cx - dx, cy - dy),
                end = Offset(cx + dx, cy + dy)
            )

            onDrawBehind {
                drawRoundRect(
                    brush = brush,
                    cornerRadius = CornerRadius(18.dp.toPx())
                )
            }
        }
        .padding(12.dp)
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            "नमूना हिस्ट्री कार्ड",
            color = previewTextColor,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
        )

        Text(
            "12 पीस × ₹50",
            color = previewSecondaryColor
        )

        Text(
            "कुल रकम ₹600",
            color = previewAccentColor,
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp
        )

        Text(
            "23-11-2026  •  नमूना रिकॉर्ड",
            color = previewSecondaryColor,
            fontSize = 11.sp
        )
    }
}        

Spacer(
    modifier = Modifier
        .fillMaxWidth()
        .height(1.dp)
        .background(Color.Gray)
)

Text("कार्ड की चौड़ाई: ${cardWidth.toInt()}%")
Slider(
    value = cardWidth,
    onValueChange = { cardWidth = it },
    valueRange = 10f..100f
)

Text("कार्ड की ऊँचाई: ${cardHeight.toInt()} px")
Slider(
    value = cardHeight,
    onValueChange = { cardHeight = it },
    valueRange = 20f..300f
)
Text("ऊपर-नीचे खाली जगह: ${cardVerticalPadding.toInt()} px")
Slider(
    value = cardVerticalPadding,
    onValueChange = { cardVerticalPadding = it },
    valueRange = 0f..20f
)
if (colorError.isNotEmpty()) {
    Text(
        text = colorError,
        color = Color(0xFFFF6B6B)
    )
}
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    try {
                        fun readColor(value: String): Long {
    var hex = value.trim().removePrefix("#")

    if (hex.length == 6) {
        hex = "FF$hex"
    }

    require(hex.length == 8 && hex.all {
        it in "0123456789ABCDEFabcdef"
    })

    return hex.toLong(16)
}

val updated = currentStyle.copy(
    backgroundColor = readColor(bgHex),
    textColor = readColor(textHex),
    accentColor = readColor(accentHex),
    secondaryTextColor = readColor(secondaryHex),
    widthPercent = cardWidth,
    heightDp = cardHeight,
    verticalPadding = cardVerticalPadding,
    gradientColors = listOf(
        readColor(gradientHex1),
        readColor(gradientHex2),
        readColor(gradientHex3),
        readColor(gradientHex4),
        readColor(gradientHex5)
    ),
    gradientAngle = gradientAngle
)

                        if (isPiece) {
                            onPieceStyleChange(updated)
                        } else {
                            onEntryStyleChange(updated)
                        }
                        editingHistoryStyle = null
                    } catch (_: Exception) {
    colorError = "HEX रंग सही नहीं है। उदाहरण: #FF3366CC"
}
                }) {
                    Text("सेव करें")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    editingHistoryStyle = null
                }) {
                    Text("रद्द करें")
                }
            }
        )
        if (activeGradientPicker >= 0) {
    AlertDialog(
        onDismissRequest = {
            activeGradientPicker = -1
        },
        title = {
            Text("रंग ${activeGradientPicker + 1} चुनें")
        },
        text = {
            Column {
                val selectedColor = Color(
                    android.graphics.Color.HSVToColor(
                        floatArrayOf(
                            pickerHue,
                            pickerSaturation,
                            pickerBrightness
                        )
                    )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(65.dp)
                        .background(
                            selectedColor,
                            RoundedCornerShape(12.dp)
                        )
                )
                val presetColors = listOf(
    "#FFFF0000", "#FFFF8000", "#FFFFFF00",
    "#FF00FF00", "#FF00FFFF", "#FF0080FF",
    "#FF0000FF", "#FF8000FF", "#FFFF00FF",
    "#FFFF69B4", "#FFFFFFFF", "#FF202020"
)

Text("तैयार रंग चुनें")

presetColors.chunked(6).forEach { colorRow ->
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        colorRow.forEach { hex ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .background(
                        Color(android.graphics.Color.parseColor(hex)),
                        RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        val hsv = FloatArray(3)
                        android.graphics.Color.colorToHSV(
                            android.graphics.Color.parseColor(hex),
                            hsv
                        )
                        pickerHue = hsv[0]
                        pickerSaturation = hsv[1]
                        pickerBrightness = hsv[2]
                    }
            )
        }
    }
}

                Text("रंग की टोन")
                Slider(
                    value = pickerHue,
                    onValueChange = { pickerHue = it },
                    valueRange = 0f..360f
                )

                Text("रंग की गहराई")
                Slider(
                    value = pickerSaturation,
                    onValueChange = { pickerSaturation = it },
                    valueRange = 0f..1f
                )

                Text("चमक")
                Slider(
                    value = pickerBrightness,
                    onValueChange = { pickerBrightness = it },
                    valueRange = 0f..1f
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val picked = android.graphics.Color
                        .HSVToColor(
                            floatArrayOf(
                                pickerHue,
                                pickerSaturation,
                                pickerBrightness
                            )
                        ).toLong() and 0xFFFFFFFFL

                    val hex = "#%08X".format(picked)

                    when (activeGradientPicker) {
                        0 -> gradientHex1 = hex
                        1 -> gradientHex2 = hex
                        2 -> gradientHex3 = hex
                        3 -> gradientHex4 = hex
                        4 -> gradientHex5 = hex
                    }

                    activeGradientPicker = -1
                }
            ) {
                Text("रंग लगाएँ")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    activeGradientPicker = -1
                }
            ) {
                Text("रद्द करें")
            }
        }
    )
        }
        }
    if (deleteRecordIndex != null) {
        AlertDialog(
            onDismissRequest = {
                deleteRecordIndex = null
            },
            title = { Text("पीस हिस्ट्री हटाएँ?") },
            text = {
                Text("क्या आप यह पीस एंट्री डिलीट करना चाहते हैं?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteRecordIndex?.let(onDeleteRecord)
                        deleteRecordIndex = null
                    }
                ) {
                    Text("हाँ, हटाएँ")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        deleteRecordIndex = null
                    }
                ) {
                    Text("नहीं")
                }
            }
        )
    }

    if (deleteEntryIndex != null) {
        AlertDialog(
            onDismissRequest = {
                deleteEntryIndex = null
            },
            title = { Text("हिस्ट्री हटाएँ?") },
            text = {
                Text("क्या आप यह कमाई/खर्च की एंट्री डिलीट करना चाहते हैं?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteEntryIndex?.let(onDeleteEntry)
                        deleteEntryIndex = null
                    }
                ) {
                    Text("हाँ, हटाएँ")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        deleteEntryIndex = null
                    }
                ) {
                    Text("नहीं")
                }
            }
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = {
                showClearConfirm = false
            },
            title = { Text("पूरी हिस्ट्री साफ करें?") },
            text = {
                Text(
                    "सभी पीस एंट्री, कमाई और खर्च की हिस्ट्री डिलीट हो जाएगी। क्या आप पक्का चाहते हैं?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAll()
                        showClearConfirm = false
                    }
                ) {
                    Text("हाँ, सब साफ करें")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showClearConfirm = false
                    }
                ) {
                    Text("रद्द करें")
                }
            }
        )
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
            GlassCard(design = designs["settings"] ?: ScreenDesign()) {
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
            GlassCard(design = designs["settings"] ?: ScreenDesign()) {
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
    DesignStudio(
        designs = designs,
        onDesignChange = onDesignChange
    )
}

        item {
            GlassCard(design = designs["settings"] ?: ScreenDesign()) {
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

    var selectedCategory by remember {
        mutableStateOf("Material")
    }
   
    var selectedPreset by remember {
    mutableStateOf<DesignPreset?>(null)
    }

    var menuExpanded by remember {
        mutableStateOf(false)
    }
    
    var selectedGradientStyle by remember {
        mutableStateOf<DesignPreset?>(null)
    }

    val gradientFamilies = listOf(
        "Linear" to "linear",
        "Radial" to "radial",
        "Sweep" to "sweep",
        "Diagonal" to "diagonal",
        "Horizontal" to "horizontal",
        "Vertical" to "vertical",
        "Mirror" to "mirror",
        "Reflected" to "reflected",
        "Diamond" to "diamond",
        "Spiral" to "spiral",
        "Mesh" to "mesh",
        "Aurora" to "aurora",
        "Liquid" to "liquid",
        "Prism" to "prism",
        "Multi-Stop" to "multi-stop",
        "Wave" to "wave",
        "Vortex" to "vortex",
        "Neon" to "neon",
        "Galaxy" to "galaxy",
        "Sunset" to "sunset",
        "Ocean" to "ocean",
        "Flame" to "flame",
        "Rainbow" to "rainbow",
        "Chrome" to "chrome",
        "Pearl" to "pearl"
    )

    val gradientStyles = remember {
        (1..50).map { index ->
            val family = gradientFamilies[(index - 1) / 2]
            val variant = if (index % 2 == 0) 2 else 1

            val hue1 = ((index * 31) % 360).toFloat()
            val hue2 = (hue1 + 65f) % 360f
            val hue3 = (hue1 + 155f) % 360f

            val colors = listOf(hue1, hue2, hue3).map { hue ->
                android.graphics.Color.HSVToColor(
                    floatArrayOf(hue, 0.78f, 0.95f)
                ).toLong() and 0xFFFFFFFFL
            }

            DesignPreset(
                id = "gradient_$index",
                name = "${family.first} $variant",
                category = "Gradient",
                colors = colors,
                effect = family.second,
                angle = ((index * 17) % 360).toFloat(),
                depth = 8f + (index % 5) * 4f,
                glow = (index % 10) / 10f
            )
        }
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
    
    var selectedColorPreset by remember {
        mutableStateOf<List<Long>?>(null)
    }


var manualColors by remember(selectedScreen, design.colors) {
    mutableStateOf(
        (
            design.colors.take(5) +
            listOf(
                0xFFFF3B5CL,
                0xFFFFB52EL,
                0xFF35D6A0L,
                0xFF398BFFL,
                0xFFA76BFFL
            )
        ).take(5)
    )
}

var selectedManualColor by remember {
    mutableIntStateOf(0)
}

var manualColorDialogOpen by remember {
    mutableStateOf(false)
}

var hue by remember { mutableFloatStateOf(210f) }
var saturation by remember { mutableFloatStateOf(0.72f) }
var brightness by remember { mutableFloatStateOf(0.92f) }

    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0x26172B43),
                RoundedCornerShape(22.dp)
            )
            .border(
                1.dp,
                Color(0x5570DDF5),
                RoundedCornerShape(22.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
                Text(
            "DESIGN CATEGORIES",
            color = Color(0xFF8FEAFF),
            fontWeight = FontWeight.Bold
        )

        ScrollableTabRow(
            selectedTabIndex = designCategories.indexOf(selectedCategory),
            containerColor = Color.Transparent,
            contentColor = Color(0xFF65E6D2),
            edgePadding = 0.dp
        ) {
            designCategories.forEachIndexed { index, category ->
                Tab(
                    selected = selectedCategory == category,
                    onClick = {
                        selectedCategory = category
                    },
                    text = {
                        Text(
                            category,
                            fontSize = 11.sp
                        )
                    }
                )
            }
        }
     
        if (selectedCategory == "Gradient") {
        Text(
            "50 GRADIENT STYLES",
            color = Color(0xFF8FEAFF),
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
        )

        Text(
            "किसी भी बॉक्स को टच करके उसका Preview चुनें।",
            color = Color.LightGray,
            fontSize = 12.sp
        )

        gradientStyles.chunked(5).forEach { styleRow ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                styleRow.forEach { style ->
                    val sampleBrush = when (style.effect) {
                        "radial" -> Brush.radialGradient(
                            style.colors.map { Color(it) }
                        )
                        "sweep" -> Brush.sweepGradient(
                            style.colors.map { Color(it) }
                        )
                        else -> Brush.linearGradient(
                            style.colors.map { Color(it) }
                        )
                    }

                    Card(
                        onClick = {
                            selectedGradientStyle = style
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(sampleBrush),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                style.id.substringAfter("_"),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        
        selectedGradientStyle?.let { style ->
            Text(
                "PREVIEW: ${style.name}",
                color = Color(0xFF8FEAFF),
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .background(
                        when (style.effect) {
                            "radial" -> Brush.radialGradient(
                                colors = style.colors.map { Color(it) }
                            )
                            "sweep" -> Brush.sweepGradient(
                                colors = style.colors.map { Color(it) }
                            )
                            else -> Brush.linearGradient(
                                colors = style.colors.map { Color(it) }
                            )
                        },
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    style.name,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = {
                    onDesignChange(
                        selectedScreen,
                        design.copy(
                            color1 = style.colors[0],
                            color2 = style.colors[1],
                            colors = style.colors,
                            gradientType = style.effect,
                            gradientAngle = style.angle,
                            depth = style.depth,
                            glow = style.glow
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("इस Gradient को APPLY करें")
            }
        }
        }

        Text(
            "चुनी हुई कैटेगरी: $selectedCategory",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        if (selectedCategory != "Gradient" && selectedCategory != "Colors") {
        Text(
    "DESIGN GALLERY — 30 STYLES",
    color = Color(0xFF8FEAFF),
    fontWeight = FontWeight.Bold
)

designPresets
    .filter { it.category == selectedCategory }
    .chunked(5)
    .forEach { presetRow ->

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presetRow.forEach { preset ->

                Card(
                    onClick = {
                        selectedPreset = preset
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(65.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    preset.colors.map { Color(it) }
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            preset.name.substringAfterLast(" "),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            repeat(5 - presetRow.size) {
                Spacer(
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

selectedPreset?.let { preset ->

    Text(
        "SELECTED: ${preset.name}",
        color = Color(0xFF8FEAFF),
        fontWeight = FontWeight.Bold
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        preset.colors.map { Color(it) }
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                preset.name,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    Button(
        onClick = {
            onDesignChange(
                selectedScreen,
                design.copy(
                    color1 = preset.colors[0],
                    color2 = preset.colors[1],
                    colors = preset.colors,
                    gradientAngle = preset.angle,
                    depth = preset.depth,
                    glow = preset.glow
                )
            )
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("इस स्क्रीन पर APPLY करें")
    }
}
}
       if (selectedCategory == "Colors") {
        Text(
            "READY-MADE COLOR GALLERY — 30",
            color = Color(0xFF8FEAFF),
            fontWeight = FontWeight.Bold
        )

        Text(
            "रंग के नमूने को टच करके Preview देखें।",
            color = Color.LightGray,
            fontSize = 12.sp
        )

        colorGallery.chunked(5).forEachIndexed { rowIndex, rowColors ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowColors.forEachIndexed { columnIndex, colors ->
                    val number = rowIndex * 5 + columnIndex + 1

                    Card(
                        onClick = {
                            selectedColorPreset = colors
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(55.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors.map { Color(it) }
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "$number",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                repeat(5 - rowColors.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }

        selectedColorPreset?.let { colors ->
            Text(
                "COLOR PREVIEW",
                color = Color(0xFF8FEAFF),
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .background(
                        Brush.linearGradient(
                            colors.map { Color(it) }
                        ),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "आपका चुना हुआ रंग",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = {
                    onDesignChange(
                        selectedScreen,
                        design.copy(
                            color1 = colors[0],
                            color2 = colors[1],
                            colors = colors
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("READY-MADE COLOR APPLY करें")
            }
        }

        Divider(color = Color(0xFF52718A))
        
Text(
    "MANUAL COLOUR STUDIO — 5 COLOURS",
    color = Color(0xFF8FEAFF),
    fontWeight = FontWeight.Bold
)

Text(
    "हर बॉक्स का रंग अलग-अलग बदल सकते हो।",
    color = Color.LightGray,
    fontSize = 12.sp
)

Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(6.dp)
) {
    manualColors.forEachIndexed { index, value ->
        Card(
            onClick = {
                selectedManualColor = index

                val hsv = FloatArray(3)
                android.graphics.Color.colorToHSV(
                    value.toInt(),
                    hsv
                )

                hue = hsv[0]
                saturation = hsv[1]
                brightness = hsv[2]
                manualColorDialogOpen = true
            },
            modifier = Modifier
                .weight(1f)
                .height(58.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(value)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${index + 1}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}

if (manualColorDialogOpen) {
    val pickedColor =
        android.graphics.Color.HSVToColor(
            floatArrayOf(hue, saturation, brightness)
        ).toLong() and 0xFFFFFFFFL

    AlertDialog(
        onDismissRequest = {
            manualColorDialogOpen = false
        },
        title = {
            Text("रंग ${selectedManualColor + 1} बदलें")
        },
        text = {
            Column {
                Text("Hue: ${hue.toInt()}°")
                Slider(
                    value = hue,
                    onValueChange = { hue = it },
                    valueRange = 0f..360f
                )

                Text("Saturation: ${(saturation * 100).toInt()}%")
                Slider(
                    value = saturation,
                    onValueChange = { saturation = it },
                    valueRange = 0f..1f
                )

                Text("Brightness: ${(brightness * 100).toInt()}%")
                Slider(
                    value = brightness,
                    onValueChange = { brightness = it },
                    valueRange = 0f..1f
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(65.dp)
                        .background(
                            Color(pickedColor),
                            RoundedCornerShape(12.dp)
                        )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    manualColors =
                        manualColors.toMutableList().also {
                            it[selectedManualColor] = pickedColor
                        }
                    manualColorDialogOpen = false
                }
            ) {
                Text("रंग सेव करें")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    manualColorDialogOpen = false
                }
            ) {
                Text("रद्द करें")
            }
        }
    )
}

Button(
    onClick = {
        onDesignChange(
            selectedScreen,
            design.copy(
                color1 = manualColors[0],
                color2 = manualColors[1],
                colors = manualColors
            )
        )
    },
    modifier = Modifier.fillMaxWidth()
) {
    Text("पाँचों रंग APPLY करें")
}


Text(
    "GRADIENT DIRECTION: ${design.gradientAngle.toInt()}°",
    color = Color(0xFF8FEAFF),
    fontWeight = FontWeight.Bold
)

Slider(
    value = design.gradientAngle,
    onValueChange = {
        onDesignChange(
            selectedScreen,
            design.copy(gradientAngle = it)
        )
    },
    valueRange = 0f..360f
)

Text(
    "GRADIENT INTENSITY: ${(design.shading * 100).toInt()}%",
    color = Color(0xFF8FEAFF),
    fontWeight = FontWeight.Bold
)

Slider(
    value = design.shading,
    onValueChange = {
        onDesignChange(
            selectedScreen,
            design.copy(shading = it)
        )
    },
    valueRange = 0f..1f
)
Text(
    "FIVE COLOUR POSITION CONTROL",
    color = Color(0xFF8FEAFF),
    fontWeight = FontWeight.Bold
)

(0 until 5).forEach { index ->
    val position = design.colorPositions.getOrElse(index) {
        index / 4f
    }

    Text(
        "रंग ${index + 1}: ${(position * 100).toInt()}%",
        color = Color.White
    )

    Slider(
        value = position,
        onValueChange = { newPosition ->
            val positions = (0 until 5).map { i ->
                if (i == index) newPosition
                else design.colorPositions.getOrElse(i) {
                    i / 4f
                }
            }
            onDesignChange(
                selectedScreen,
                design.copy(colorPositions = positions)
            )
        },
        valueRange = 0f..1f
    )
}
           Divider(color = Color(0xFF52718A))

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
    "GRADIENT ANGLE: ${design.gradientAngle.toInt()}°",
    color = Color.White,
    fontWeight = FontWeight.Bold
)

Slider(
    value = design.gradientAngle,
    onValueChange = {
        onDesignChange(
            selectedScreen,
            design.copy(gradientAngle = it)
        )
    },
    valueRange = 0f..360f
)

Text(
    "3D SHADING: ${(design.shading * 100).toInt()}%",
    color = Color.White,
    fontWeight = FontWeight.Bold
)

Slider(
    value = design.shading,
    onValueChange = {
        onDesignChange(
            selectedScreen,
            design.copy(shading = it)
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
    }
private fun makeGradientBrush(
    type: String,
    colors: List<Color>,
    positions: List<Float>,
    start: Offset,
    end: Offset,
    center: Offset,
    width: Float,
    height: Float
): Brush {
    val safeColors = colors.ifEmpty {
        listOf(Color.Blue, Color.Cyan)
    }

    val stops = safeColors.mapIndexed { index, color ->
        val position = positions.getOrElse(index) {
            if (safeColors.size <= 1) 0f
            else index.toFloat() / (safeColors.size - 1)
        }.coerceIn(0f, 1f)

        position to color
    }.sortedBy { it.first }.toTypedArray()

    return when (type) {
        "radial", "diamond" -> Brush.radialGradient(
            colorStops = stops,
            center = center,
            radius = maxOf(width, height) / 1.5f
        )

        "sweep" -> Brush.sweepGradient(
            colorStops = stops,
            center = center
        )

        "horizontal" -> Brush.linearGradient(
            colorStops = stops,
            start = Offset(0f, height / 2f),
            end = Offset(width, height / 2f)
        )

        "vertical" -> Brush.linearGradient(
            colorStops = stops,
            start = Offset(width / 2f, 0f),
            end = Offset(width / 2f, height)
        )

        "mirror" -> Brush.linearGradient(
            colors = safeColors + safeColors.reversed(),
            start = start,
            end = end
        )

        "reflected" -> Brush.linearGradient(
            colors = safeColors.reversed(),
            start = start,
            end = end
        )

        else -> Brush.linearGradient(
            colorStops = stops,
            start = start,
            end = end
        )
    }
}

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    design: ScreenDesign = ScreenDesign(),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(25.dp)
    val enabled = design.enabled

    val glow = design.glow.coerceIn(0f, 1f)
  val depth = if (enabled) {
    design.depth.coerceIn(12f, 30f)
} else {
    0f
  }

    // OFF: keep user's selected colors solid.
    // ON: translucent glass color.
    val cardColors = design.colors.ifEmpty {
        listOf(0xFF263B59L, 0xFF17243D)
    }.map { value ->
        val original = Color(value)
        original.copy(
            alpha = if (enabled) {
                (1f - design.transparency.coerceIn(0f, 1f) * 0.72f)
                    .coerceIn(0.22f, 0.82f)
            } else {
                1f
            }
        )
    }

    val safeColors = if (cardColors.size == 1) {
        listOf(cardColors.first(), cardColors.first())
    } else {
        cardColors
    }

    Column(
        modifier
            .shadow(
                elevation = depth.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.65f),
                spotColor = if (enabled) {
                    Color(0xFF65E8FF).copy(alpha = 0.35f + glow * 0.4f)
                } else {
                    Color.Black.copy(alpha = 0.35f)
                }
            )
            .drawWithCache {
                val corner = CornerRadius(25.dp.toPx())
                val center = Offset(size.width / 2f, size.height / 2f)

                val angle = Math.toRadians(
                    design.gradientAngle.toDouble()
                )

                val dx = kotlin.math.cos(angle).toFloat() * size.width / 2f
                val dy = kotlin.math.sin(angle).toFloat() * size.height / 2f

                val baseBrush = makeGradientBrush(
                    type = design.gradientType,
                    colors = safeColors,
                    start = Offset(center.x - dx, center.y - dy),
                    positions = design.colorPositions,
                    end = Offset(center.x + dx, center.y + dy),
                    center = center,
                    width = size.width,
                    height = size.height
                )

                val darkSide = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF020817).copy(alpha = 0.85f),
                        Color(0xFF101C35).copy(alpha = 0.95f),
                        Color(0xFF020817).copy(alpha = 0.9f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                )

                val topLight = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (enabled) 0.82f else 0.16f),
                        Color.White.copy(alpha = if (enabled) 0.28f else 0.04f),
                        Color.Transparent
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width * 0.9f, size.height * 0.65f)
                )

                val bottomShade = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = if (enabled) 0.48f else 0.12f)
                    ),
                    start = Offset(0f, size.height * 0.45f),
                    end = Offset(size.width, size.height)
                )

                val glassEdge = Brush.sweepGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        Color(0xFF55E9FF).copy(alpha = 0.9f),
                        Color(0xFF9A7CFF).copy(alpha = 0.85f),
                        Color.White.copy(alpha = 0.95f)
                    ),
                    center = center
                )

                val innerEdge = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.85f),
                        Color.White.copy(alpha = 0.08f),
                        Color.Black.copy(alpha = 0.55f)
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                )

                val reflection = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.62f),
                        Color.White.copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    start = Offset(8.dp.toPx(), 2.dp.toPx()),
                    end = Offset(size.width * 0.75f, size.height * 0.35f)
                )

                onDrawBehind {
                    if (enabled) {
                        // Deep 3D body underneath the glass face.
                        drawRoundRect(
                            brush = darkSide,
                            topLeft = Offset(0f, 7.dp.toPx()),
                            size = size,
                            cornerRadius = corner
                        )
                    }

                    // Main face.
                    drawRoundRect(
                        brush = baseBrush,
                        cornerRadius = corner
                    )

                    if (enabled) {
                        // Light and dark depth.
                        drawRoundRect(
                            brush = topLight,
                            cornerRadius = corner
                        )

                        drawRoundRect(
                            brush = bottomShade,
                            cornerRadius = corner
                        )

                        // Bright outer glass rim.
                        drawRoundRect(
                            brush = glassEdge,
                            cornerRadius = corner,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 3.dp.toPx()
                            )
                        )

                        // Inner bevel.
                        drawRoundRect(
                            brush = innerEdge,
                            topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(
                                size.width - 4.dp.toPx(),
                                size.height - 4.dp.toPx()
                            ),
                            cornerRadius = CornerRadius(23.dp.toPx()),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 1.5.dp.toPx()
                            )
                        )

                        // Glossy reflection.
                        drawRoundRect(
                            brush = reflection,
                            topLeft = Offset(5.dp.toPx(), 4.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(
                                size.width - 10.dp.toPx(),
                                size.height * 0.38f
                            ),
                            cornerRadius = CornerRadius(21.dp.toPx())
                        )
                    }
                }
            }
            .border(
    width = if (enabled) 1.8.dp else 0.dp,
    color = Color.Transparent,
    shape = shape
)
            .padding(16.dp),
        content = content
    )
}
@Composable
private fun HistoryStyledCard(
    style: HistoryCardStyle,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(
        style.cornerRadius.dp
    )

    Column(
        modifier = Modifier
            .fillMaxWidth(
                (style.widthPercent / 100f).coerceIn(0.1f, 1f)
            )
            .then(
                if (style.heightDp > 0f) {
                    Modifier.heightIn(min = style.heightDp.dp)
                } else {
                    Modifier
                }
            )
            .shadow(8.dp, shape)
            .drawWithCache {
    val colors = style.gradientColors
        .take(5)
        .ifEmpty { listOf(style.backgroundColor) }
        .map { Color(it) }

    val angle = Math.toRadians(
        style.gradientAngle.toDouble()
    )

    val center = Offset(
        size.width / 2f,
        size.height / 2f
    )

    val dx = kotlin.math.cos(angle).toFloat() *
        size.width / 2f

    val dy = kotlin.math.sin(angle).toFloat() *
        size.height / 2f

    val brush = Brush.linearGradient(
        colors = colors,
        start = Offset(center.x - dx, center.y - dy),
        end = Offset(center.x + dx, center.y + dy)
    )

    onDrawBehind {
        drawRoundRect(
            brush = brush,
            cornerRadius = CornerRadius(
                style.cornerRadius.dp.toPx()
            )
        )
    }
            }
            
            .border(
                style.borderWidth.dp,
                Color(style.borderColor),
                shape
            )
            .padding(
                horizontal = style.horizontalPadding.dp,
                vertical = style.verticalPadding.dp
            ),
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

private fun savePiecePhoto(
    context: android.content.Context,
    bitmap: Bitmap?,
    uri: Uri?
): String {
    return try {
        val file = File(
            context.filesDir,
            "piece_${System.currentTimeMillis()}.jpg"
        )

        if (bitmap != null) {
            FileOutputStream(file).use { output ->
                bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    92,
                    output
                )
            }
        } else if (uri != null) {
            val input = context.contentResolver.openInputStream(uri)
                ?: return ""

            input.use { source ->
                FileOutputStream(file).use { output ->
                    source.copyTo(output)
                }
            }
        } else {
            return ""
        }

        file.absolutePath
    } catch (_: Exception) {
        ""
    }
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
                o.getString("time"),
                o.optString("photoPath", "")
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
                .put("photoPath", it.photoPath)
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
            val savedColors = item.optJSONArray("colors")

            val colorList = savedColors?.let { array ->
                (0 until array.length())
                    .map { array.optLong(it) }
                    .take(5)
            }?.takeIf { it.isNotEmpty() }
                ?: listOf(
                    item.optLong("color1", 0xFF4D8CA6L),
                    item.optLong("color2", 0xFF334270L)
                )

            ScreenDesign(
                enabled = item.optBoolean("enabled", true),
                color1 = item.optLong("color1", 0x664D8CA6L),
                color2 = item.optLong("color2", 0x44334270L),
                colors = colorList,
                transparency = item.optDouble(
                    "transparency", 0.65
                ).toFloat(),
                depth = item.optDouble(
                    "depth", 12.0
                ).toFloat(),
                glow = item.optDouble(
    "glow", 0.5
).toFloat(),
gradientAngle = item.optDouble(
    "gradientAngle", 45.0
).toFloat(),
gradientType = item.optString(
    "gradientType", "linear"
),
shading = item.optDouble(
    "shading", 0.35
).toFloat()
,
colorPositions = item.optJSONArray("colorPositions")?.let { array ->
    (0 until array.length())
        .map { array.optDouble(it).toFloat() }
        .take(5)
}?.takeIf { it.size == 5 }
    ?: listOf(0f, 0.25f, 0.5f, 0.75f, 1f)
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
        val colorArray = JSONArray()

        design.colors.take(5).forEach { color ->
            colorArray.put(color)
        }
val positionArray = JSONArray()

design.colorPositions.take(5).forEach { position ->
    positionArray.put(position.toDouble())
}
        item.put("enabled", design.enabled)
        item.put("color1", design.color1)
        item.put("color2", design.color2)
        item.put("colors", colorArray)
        item.put("transparency", design.transparency)
        item.put("depth", design.depth)
        item.put("glow", design.glow)
        item.put("gradientAngle", design.gradientAngle)
item.put("gradientType", design.gradientType)
item.put("shading", design.shading)
item.put("colorPositions", positionArray)
        json.put(screen, item)
    }

    prefs.edit()
        .putString("screen_designs", json.toString())
        .apply()
}

private fun loadHistoryCardStyle(
    prefs: android.content.SharedPreferences,
    key: String
): HistoryCardStyle {
    val item = JSONObject(
        prefs.getString(key, "{}") ?: "{}"
    )

    return HistoryCardStyle(
        backgroundColor = item.optLong("backgroundColor", 0xFF263B59L),
        textColor = item.optLong("textColor", 0xFFFFFFFFL),
        secondaryTextColor = item.optLong("secondaryTextColor", 0xFFCFD8E8L),
        accentColor = item.optLong("accentColor", 0xFF65E6D2L),
        fontSize = item.optDouble("fontSize", 16.0).toFloat(),
        fontStyle = item.optString("fontStyle", "bold"),
        cornerRadius = item.optDouble("cornerRadius", 20.0).toFloat(),
        widthPercent = item.optDouble("widthPercent", 100.0).toFloat(),
        heightDp = item.optDouble("heightDp", 0.0).toFloat(),
        horizontalPadding = item.optDouble("horizontalPadding", 16.0).toFloat(),
        verticalPadding = item.optDouble("verticalPadding", 16.0).toFloat(),
        borderColor = item.optLong("borderColor", 0xFF65E6D2L),
borderWidth = item.optDouble("borderWidth", 1.0).toFloat(),
gradientColors = item.optJSONArray("gradientColors")?.let { array ->
    (0 until array.length()).map { array.optLong(it) }.take(5)
}?.takeIf { it.isNotEmpty() }
    ?: listOf(
        0xFF263B59L,
        0xFF315A80L,
        0xFF4169A1L,
        0xFF6756A5L,
        0xFF65E6D2L
    ),
gradientAngle = item.optDouble("gradientAngle", 45.0).toFloat()
    )
}

private fun saveHistoryCardStyle(
    prefs: android.content.SharedPreferences,
    key: String,
    style: HistoryCardStyle
) {
    val item = JSONObject()

    item.put("backgroundColor", style.backgroundColor)
    item.put("textColor", style.textColor)
    item.put("secondaryTextColor", style.secondaryTextColor)
    item.put("accentColor", style.accentColor)
    item.put("fontSize", style.fontSize)
    item.put("fontStyle", style.fontStyle)
    item.put("cornerRadius", style.cornerRadius)
    item.put("widthPercent", style.widthPercent)
    item.put("heightDp", style.heightDp)
    item.put("horizontalPadding", style.horizontalPadding)
    item.put("verticalPadding", style.verticalPadding)
    item.put("borderColor", style.borderColor)
    item.put("borderWidth", style.borderWidth)
    val gradientArray = JSONArray()
style.gradientColors.take(5).forEach { color ->
    gradientArray.put(color)
}

item.put("gradientColors", gradientArray)
item.put("gradientAngle", style.gradientAngle)
    prefs.edit().putString(key, item.toString()).apply()
}

