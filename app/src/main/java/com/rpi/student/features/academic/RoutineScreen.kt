package com.rpi.student.features.academic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rpi.student.core.ui.EmptyState
import com.rpi.student.core.ui.SubScaffold

private val DAYS = listOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu")

/** Class Routine — day tabs + class slots (offline Room cache, seed আসার পর লাইভ)। */
@Composable
fun RoutineScreen(onBack: () -> Unit) {
    var day by remember { mutableStateOf(0) }
    SubScaffold(title = "Class Routine", onBack = onBack) { _ ->
        Column(Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                DAYS.forEachIndexed { i, d ->
                    FilterChip(selected = day == i, onClick = { day = i }, label = { Text(d) })
                }
            }
            Spacer(Modifier.height(12.dp))
            // TODO: routineDao.byDay(tech, sem, section, shift, day) collectAsState
            val demo = if (day % 2 == 0) demoSlots else emptyList()
            if (demo.isEmpty()) {
                EmptyState("এই দিনে ক্লাস নেই — আরাম করো 😌")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(demo) { slot ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp)) {
                                Text("${slot.first} — ${slot.second}", style = MaterialTheme.typography.titleMedium)
                                Text(slot.third, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

private val demoSlots = listOf(
    Triple("09:00–10:30", "2763 Computer Application", "Theory · Room 402 · AKS"),
    Triple("10:30–12:00", "2764 Database Management", "Theory · Room 404 · MJH"),
    Triple("12:45–14:15", "2765 Web Development", "Practical · Lab 3 · RI"),
)
