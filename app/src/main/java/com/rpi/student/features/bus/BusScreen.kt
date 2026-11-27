package com.rpi.student.features.bus

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
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rpi.student.core.ui.SectionCard
import com.rpi.student.core.ui.SubScaffold

private val ROUTES = listOf("Rangpur City", "Mithapukur", "Pirgacha", "Badarganj")

/**
 * Bus Routine (blueprint §6) — route community (BUS group), admin-controlled schedule।
 */
@Composable
fun BusScreen(onBack: () -> Unit) {
    var route by remember { mutableStateOf(ROUTES.first()) }
    var shift by remember { mutableStateOf("Morning") }

    SubScaffold(title = "Bus Routine", onBack = onBack) { _ ->
        Column(
            Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionCard(title = "রুট ও শিফট") {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    ROUTES.forEach { r -> FilterChip(selected = route == r, onClick = { route = r }, label = { Text(r) }) }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = shift == "Morning", onClick = { shift = "Morning" }, label = { Text("Morning") })
                    FilterChip(selected = shift == "Second", onClick = { shift = "Second" }, label = { Text("Second") })
                }
            }

            SectionCard(title = "$route — $shift") {
                val stops = listOf(
                    Triple("College Gate", "08:15", 0),
                    Triple("Zero Point", "08:05", 1),
                    Triple("Jahaj Company Mor", "07:50", 2),
                    Triple("Station Road", "07:35", 3),
                ).sortedBy { it.third }
                stops.forEach { (name, time, _) ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(time, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Text("  $name", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("📣 Admin Notice", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "আগামীকাল Mithapukur রুটে রোড ম্যানেজমেন্ট — ২০ মিনিট আগে স্টপে থাকো।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                "BUS গ্রুপ এই রুটের সব স্টুডেন্টের auto-join community — admin ছাড়া কেউ স্কেডিউল বদলাতে পারবে না।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
