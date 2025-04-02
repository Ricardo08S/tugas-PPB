package com.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.calculator.ui.theme.CalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculatorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ConversionCalculatorScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionCalculatorScreen(modifier: Modifier = Modifier) {
    var inputValue by remember { mutableStateOf(TextFieldValue("")) }
    var resultDecimal by remember { mutableStateOf("") }
    var resultBinary by remember { mutableStateOf("") }
    var resultOctal by remember { mutableStateOf("") }
    var resultHex by remember { mutableStateOf("") }
    var selectedInputSystem by remember { mutableStateOf("Decimal") }
    var showDialog by remember { mutableStateOf(false) }
    val dropdownOptions = listOf("Decimal", "Binary", "Octal", "Hexadecimal")

    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedInputSystem)
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Select Input System") },
                text = {
                    Column {
                        dropdownOptions.forEach { option ->
                            Button(
                                onClick = {
                                    selectedInputSystem = option
                                    showDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(option)
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = inputValue,
            onValueChange = { newValue ->
                val filteredText = when (selectedInputSystem) {
                    "Decimal" -> newValue.text.filter { it.isDigit() }
                    "Binary" -> newValue.text.filter { it == '0' || it == '1' }
                    "Octal" -> newValue.text.filter { it in '0'..'7' }
                    "Hexadecimal" -> newValue.text.filter { it.isDigit() || it in 'A'..'F' || it in 'a'..'f' }.uppercase()
                    else -> newValue.text
                }
                inputValue = newValue.copy(text = filteredText, selection = TextRange(newValue.selection.start, newValue.selection.end))
            },
            label = { Text("Enter Number in $selectedInputSystem") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = when (selectedInputSystem) {
                    "Decimal" -> KeyboardType.Number
                    "Binary" -> KeyboardType.Text
                    "Octal" -> KeyboardType.Number
                    "Hexadecimal" -> KeyboardType.Text
                    else -> KeyboardType.Text
                },
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                try {
                    val input = inputValue.text.trim()
                    when (selectedInputSystem) {
                        "Decimal" -> {
                            val decimalValue = input.toInt()
                            resultDecimal = decimalValue.toString()
                            resultBinary = decimalValue.toString(2)
                            resultOctal = decimalValue.toString(8)
                            resultHex = decimalValue.toString(16).uppercase()
                        }
                        "Binary" -> {
                            val decimalValue = input.toInt(2)
                            resultDecimal = decimalValue.toString()
                            resultBinary = input
                            resultOctal = decimalValue.toString(8)
                            resultHex = decimalValue.toString(16).uppercase()
                        }
                        "Octal" -> {
                            val decimalValue = input.toInt(8)
                            resultDecimal = decimalValue.toString()
                            resultBinary = decimalValue.toString(2)
                            resultOctal = input
                            resultHex = decimalValue.toString(16).uppercase()
                        }
                        "Hexadecimal" -> {
                            val decimalValue = input.toInt(16)
                            resultDecimal = decimalValue.toString()
                            resultBinary = decimalValue.toString(2)
                            resultOctal = decimalValue.toString(8)
                            resultHex = input.uppercase()
                        }
                    }
                } catch (_: Exception) {
                    resultDecimal = "Invalid Input"
                    resultBinary = "Invalid Input"
                    resultOctal = "Invalid Input"
                    resultHex = "Invalid Input"
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Convert")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ResultCard(label = "Decimal", value = resultDecimal)
            ResultCard(label = "Binary", value = resultBinary)
            ResultCard(label = "Octal", value = resultOctal)
            ResultCard(label = "Hexadecimal", value = resultHex)
        }
    }
}

@Composable
fun ResultCard(label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Color.Black),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium, color = Color.Black)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalculatorPreview() {
    CalculatorTheme {
        ConversionCalculatorScreen()
    }
}