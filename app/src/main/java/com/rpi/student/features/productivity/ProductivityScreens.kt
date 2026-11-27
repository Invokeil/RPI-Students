package com.rpi.student.features.productivity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rpi.student.core.ui.SectionCard
import com.rpi.student.core.ui.StatChip
import com.rpi.student.core.ui.SubScaffold
import kotlinx.coroutines.delay

// Poly-style presets (blueprint §15)
private val PRESETS = listOf(
    "Theory 25/5" to (25 to 5),
    "Math 40/8" to (40 to 8),
    "Drawing/Lab 50/10" to (50 to 10),
    "Exam sprint 45/10" to (45 to 10),
)

/** Pomodoro — subject-linked, poly presets, session count → Daily Discipline card। */
@Composable
fun PomodoroScreen(onBack: () -> Unit) {
    var presetIdx by remember { mutableIntStateOf(0) }
    var subject by remember { mutableStateOf("2764 Database") }
    var running by remember { mutableStateOf(false) }
    var phase by remember { mutableStateOf("focus") }   // focus | break
    var secondsLeft by remember { mutableIntStateOf(PRESETS[0].second.first * 60) }
    var doneToday by remember { mutableIntStateOf(3) }

    LaunchedEffect(running, phase, presetIdx) {
        if (!running) return@LaunchedEffect
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft -= 1
        }
        running = false
        if (phase == "focus") {
            doneToday += 1
            phase = "break"
            secondsLeft = PRESETS[presetIdx].second.second * 60
        } else {
            phase = "focus"
            secondsLeft = PRESETS[presetIdx].second.first * 60
        }
        // TODO: pomoDao.insert(PomoSessionEntity(...)) — Daily Discipline-এ যাবে
    }

    SubScaffold(title = "Pomodoro", onBack = onBack) { _ ->
        Column(
            Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionCard(title = "Preset বেছে নাও") {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    PRESETS.take(2).forEachIndexed { i, (label, _) ->
                        FilterChip(selected = presetIdx == i, onClick = {
                            presetIdx = i; running = false; phase = "focus"; secondsLeft = PRESETS[i].second.first * 60
                        }, label = { Text(label) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                    PRESETS.drop(2).forEachIndexed { j, (label, _) ->
                        val i = j + 2
                        FilterChip(selected = presetIdx == i, onClick = {
                            presetIdx = i; running = false; phase = "focus"; secondsLeft = PRESETS[i].second.first * 60
                        }, label = { Text(label) })
                    }
                }
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (phase == "focus") "🎯 ফোকাস — $subject" else "☕ ব্রেক", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "%02d:%02d".format(secondsLeft / 60, secondsLeft % 60),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row {
                        Button(onClick = { running = !running }) { Text(if (running) "থামাও" else "চালাও") }
                        Spacer(Modifier.padding(6.dp))
                        OutlinedButton(onClick = {
                            running = false; phase = "focus"; secondsLeft = PRESETS[presetIdx].second.first * 60
                        }) { Text("রিসেট") }
                    }
                }
            }

            SectionCard(title = "Daily Discipline") {
                Row {
                    StatChip("আজকের সেশন", "$doneToday", Modifier.weight(1f))
                    Spacer(Modifier.padding(6.dp))
                    StatChip("মোট মিনিট", "${doneToday * PRESETS[presetIdx].second.first}", Modifier.weight(1f))
                    Spacer(Modifier.padding(6.dp))
                    StatChip("স্ট্রিক", "7 দিন 🔥", Modifier.weight(1f))
                }
            }
        }
    }
}

/** Fitness — স্টেপ কাউন্ট + স্ট্রিক (Sensor.TYPE_STEP_COUNTER — permission-free)। */
@Composable
fun FitnessScreen(onBack: () -> Unit) {
    SubScaffold(title = "Fitness", onBack = onBack) { _ ->
        Column(
            Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionCard(title = "আজকের স্টেপ") {
                Text("6,842", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                Text("টার্গেট ৮,০০০ — ৮৫%", style = MaterialTheme.typography.bodyMedium)
                Text("TODO: SensorManager TYPE_STEP_COUNTER লাইভ হবে — v1.1 slice", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            SectionCard(title = "স্ট্রিক") {
                Text("🔥 ৭ দিন ধরে পড়া + হাঁটা চালু", style = MaterialTheme.typography.bodyLarge)
                Text("Pomodoro + স্টেপ মিলে Daily Discipline card তৈরি করে।", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
