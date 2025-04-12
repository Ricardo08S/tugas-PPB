package com.moneyconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.moneyconverter.ui.theme.MoneyConverterTheme
import java.text.DecimalFormat
import kotlin.math.max

class NumberFormatVisualTransformation(
    private val numberFormat: DecimalFormat = DecimalFormat("#,###")
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }
        val digitsOnly = originalText.filter { it.isDigit() }
        if (digitsOnly.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }
        if (digitsOnly == "0") {
            return TransformedText(AnnotatedString("0"), OffsetMapping.Identity)
        }

        val formattedText = try {
            numberFormat.format(digitsOnly.toLong())
        } catch (_: NumberFormatException) {
            return TransformedText(text, OffsetMapping.Identity)
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val originalSub = digitsOnly.take(offset)
                val formattedSub = try {
                    if (originalSub.isEmpty()) "" else numberFormat.format(originalSub.toLong())
                } catch (_: NumberFormatException) { "" }
                val separatorCount = formattedSub.count { it == ',' }
                return offset + separatorCount
            }
            override fun transformedToOriginal(offset: Int): Int {
                val formattedSub = formattedText.take(offset)
                val separatorCount = formattedSub.count { it == ',' }
                return max(0, offset - separatorCount)
            }
        }
        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoneyConverterTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CurrencyConverterScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

val exchangeRates = mapOf(
    "IDR" to 1.0, "USD" to 16000.0, "JPY" to 105.0, "EUR" to 17400.0, "GBP" to 20100.0,
    "AUD" to 10600.0, "SGD" to 11800.0, "CNY" to 2225.0, "SAR" to 4250.0, "MYR" to 3375.0
).toSortedMap()
val currencies = exchangeRates.keys.toList()

val currencyNames = mapOf(
    "IDR" to "Indonesian Rupiah", "USD" to "United States Dollar", "JPY" to "Japanese Yen",
    "EUR" to "Euro", "GBP" to "British Pound Sterling", "AUD" to "Australian Dollar",
    "SGD" to "Singapore Dollar", "CNY" to "Chinese Yuan", "SAR" to "Saudi Riyal",
    "MYR" to "Malaysian Ringgit"
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConverterScreen(modifier: Modifier = Modifier) {

    var amountInput by remember { mutableStateOf("") }
    var selectedFromCurrency by remember { mutableStateOf(currencies.firstOrNull { it == "IDR" } ?: currencies.firstOrNull() ?: "") }
    var selectedToCurrency by remember { mutableStateOf(currencies.firstOrNull { it == "USD" } ?: currencies.getOrNull(1) ?: "") }
    var conversionResultText by remember { mutableStateOf<String?>(null) }
    var detailedConversionText by remember { mutableStateOf<String?>(null)}
    var isAmountError by remember { mutableStateOf(false) }
    var fromDropdownExpanded by remember { mutableStateOf(false) }
    var toDropdownExpanded by remember { mutableStateOf(false) }
    val numberFormatTransformation = remember { NumberFormatVisualTransformation() }

    fun performConversion() {
        isAmountError = false
        detailedConversionText = null
        val amount = amountInput.toDoubleOrNull()

        if (amount == null) {
            if (amountInput.isNotBlank()) {
                isAmountError = true
            }
            conversionResultText = null
            detailedConversionText = null
            return
        }
        if (amount < 0) {
            isAmountError = true
            conversionResultText = null
            detailedConversionText = null
            return
        }


        if (selectedFromCurrency.isBlank() || selectedToCurrency.isBlank()) {
            conversionResultText = "Select currencies"
            return
        }

        val rateFrom = exchangeRates[selectedFromCurrency]
        val rateTo = exchangeRates[selectedToCurrency]

        if (rateFrom != null && rateTo != null && rateFrom != 0.0) {
            val result = amount * (rateFrom / rateTo)
            val decimalFormatResult = DecimalFormat("#,##0.00####")
            conversionResultText = "${decimalFormatResult.format(result)} $selectedToCurrency"

            val oneUnitResult = 1.0 * (rateFrom / rateTo)
            val decimalFormatDetail = DecimalFormat("#,##0.##")
            detailedConversionText = "1 $selectedFromCurrency = ${decimalFormatDetail.format(oneUnitResult)} $selectedToCurrency"

        } else {
            conversionResultText = "Error: Invalid rate"
            detailedConversionText = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE0F7FA))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Currency Converter",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp),
            color = Color.Black
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { newValue ->
                        val digitsOnly = newValue.filter { it.isDigit() }
                        if (digitsOnly.length <= 15) {
                            if (digitsOnly.startsWith("0") && digitsOnly.length > 1) {
                                amountInput = ""
                                isAmountError = false
                                conversionResultText = null
                                return@OutlinedTextField
                            }
                            amountInput = digitsOnly
                            isAmountError = false
                            conversionResultText = null
                        }
                    },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = isAmountError,
                    supportingText = {
                        if (isAmountError) {
                            Text("Please enter a valid number", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    visualTransformation = numberFormatTransformation
                )

                ExposedDropdownMenuBox(
                    expanded = fromDropdownExpanded,
                    onExpandedChange = { fromDropdownExpanded = !fromDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedFromCurrency,
                        onValueChange = {},
                        label = { Text("From Currency") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = fromDropdownExpanded,
                        onDismissRequest = { fromDropdownExpanded = false }
                    ) {
                        currencies.forEach { currencyCode ->
                            val fullName = currencyNames[currencyCode] ?: currencyCode
                            DropdownMenuItem(
                                text = { Text("$currencyCode - $fullName") },
                                onClick = {
                                    selectedFromCurrency = currencyCode
                                    fromDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                IconButton(
                    onClick = {
                        val temp = selectedFromCurrency
                        selectedFromCurrency = selectedToCurrency
                        selectedToCurrency = temp
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(Icons.Filled.SwapVert, contentDescription = "Swap Currencies")
                }

                ExposedDropdownMenuBox(
                    expanded = toDropdownExpanded,
                    onExpandedChange = { toDropdownExpanded = !toDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedToCurrency,
                        onValueChange = {},
                        label = { Text("To Currency") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = toDropdownExpanded,
                        onDismissRequest = { toDropdownExpanded = false }
                    ) {
                        currencies.forEach { currencyCode ->
                            val fullName = currencyNames[currencyCode] ?: currencyCode
                            DropdownMenuItem(
                                text = { Text("$currencyCode - $fullName") },
                                onClick = {
                                    selectedToCurrency = currencyCode
                                    toDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = { performConversion() },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .padding(top = 24.dp),
            enabled = amountInput.isNotBlank()
        ) {
            Text("Convert")
        }


        if (conversionResultText != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = conversionResultText!!,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )
                    if (detailedConversionText != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = detailedConversionText!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CurrencyConverterScreenFinalPreview() {
    MoneyConverterTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            CurrencyConverterScreen()
        }
    }
}