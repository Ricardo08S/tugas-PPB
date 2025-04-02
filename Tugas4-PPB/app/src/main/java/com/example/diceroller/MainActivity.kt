package com.example.diceroller

/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFB3E5FC)
                ) {
                    DiceRollerApp()
                }
            }
        }
    }
}

@Composable
fun DiceRollerApp() {
    DiceWithButtonAndImage(modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center)
    )
}

@Composable
fun DiceWithButtonAndImage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val mediaPlayer = remember { MediaPlayer.create(context, R.raw.dice_sound) }
    val scope = rememberCoroutineScope()
    val rotation = remember { Animatable(0f) }
    var isRolling by remember { mutableStateOf(false) }

    var diceValues by remember { mutableStateOf(listOf(1, 1, 1)) }
    val diceImages = listOf(
        R.drawable.dice_1, R.drawable.dice_2, R.drawable.dice_3,
        R.drawable.dice_4, R.drawable.dice_5, R.drawable.dice_6
    )

    var totalValue by remember { mutableIntStateOf(3) }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = diceImages[diceValues[0] - 1]),
                contentDescription = "Top Dice ${diceValues[0]}",
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer(rotationZ = rotation.value)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                diceValues.drop(1).forEach { value ->
                    Image(
                        painter = painterResource(id = diceImages[value - 1]),
                        contentDescription = "Bottom Dice $value",
                        modifier = Modifier
                            .size(200.dp)
                            .graphicsLayer(rotationZ = rotation.value)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Total: $totalValue", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                mediaPlayer?.let {
                    it.seekTo(0)
                    it.start()
                }
                isRolling = true
                scope.launch {
                    rotation.snapTo(0f)
                    rotation.animateTo(
                        targetValue = 1080f,
                        animationSpec = tween(durationMillis = 1000)
                    )
                    diceValues = List(3) { (1..6).random() }
                    totalValue = diceValues.sum()
                    isRolling = false
                }
            },
            enabled = !isRolling
        ) {
            Text(text = if (isRolling) "Randomizing..." else "Throw", fontSize = 24.sp)
        }
    }
}
