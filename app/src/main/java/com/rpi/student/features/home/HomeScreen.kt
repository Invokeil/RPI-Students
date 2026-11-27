package com.rpi.student.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rpi.student.core.ui.FocusBanner
import com.rpi.student.core.ui.SectionCard
import com.rpi.student.core.ui.StatChip
import com.rpi.student.update.UpdateManager

/**
 * Home dashboard — আজকের ক্লাস, exam countdown (focus nudge), quick actions, notices,
 * OTA update banner (নতুন GitHub release থাকলে)।
 */
@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    var updateAvailable by remember { mutableStateOf(false) }

    // OTA silent check — নতুন release থাকলে banner (Developer page থেকে download)
    LaunchedEffect(Unit) {
        runCatching {
            updateAvailable = UpdateManager.check() is UpdateManager.CheckResult.Available
        }
    }

    Column(
        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("স্বাগতম 👋", style = MaterialTheme.typography.headlineMedium)
        Text("Rangpur Polytechnic Institute", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        if (updateAvailable) {
            Card(onClick = { onNavigate("profile/developer") }, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "🚀 নতুন update এসেছে! Developer page থেকে নামাও (Profile → Developer)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(14.dp),
                )
            }
        }

        // Exam ≤7 দিন হলে এই banner দেখাবে (v1 auto-behavior — blueprint §13)
        FocusBanner(daysLeft = 5, examName = "4th Semester Final")

        SectionCard(title = "আজকের ক্লাস", trailing = {
            Text("Routine →", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(4.dp).let { it })
        }) {
            // TODO: Room routineDao.byDay(আজ) — seed data আসার পর লাইভ হবে
            Text("১ম ক্লাস · 09:00 · Computer Application (2763) · Room 402", style = MaterialTheme.typography.bodyMedium)
            Text("২য় ক্লাস · 10:30 · Database (2764) · Lab 3", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Text("শিফট: Morning · Section A · 4th Semester", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        SectionCard(title = "Quick Actions") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickAction("GPA", "academic/gpa", onNavigate, Modifier.weight(1f))
                QuickAction("CGPA", "academic/cgpa", onNavigate, Modifier.weight(1f))
                QuickAction("Credits", "academic/credits", onNavigate, Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickAction("Bus", "bus", onNavigate, Modifier.weight(1f))
                QuickAction("Pomodoro", "focus/pomodoro", onNavigate, Modifier.weight(1f))
                QuickAction("Fitness", "focus/fitness", onNavigate, Modifier.weight(1f))
            }
        }

        SectionCard(title = "Daily Discipline") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatChip("Pomodoro আজ", "3", Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                StatChip("স্ট্রিক", "7 দিন", Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                StatChip("পড়া মিনিট", "135", Modifier.weight(1f))
            }
        }

        SectionCard(title = "নোটিস") {
            Text("• 4th sem final রুটিন প্রকাশিত — Academic → Exam Routine", style = MaterialTheme.typography.bodyMedium)
            Text("• বাস রুটিন আপডেট (admin) — Bus ট্যাব", style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun QuickAction(label: String, route: String, onNavigate: (String) -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = { onNavigate(route) }, modifier = modifier) {
        Column(Modifier.padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}
