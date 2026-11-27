package com.rpi.student.features.academic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rpi.student.core.ui.StatChip
import com.rpi.student.core.ui.SubScaffold

private val SEMS = (1..8).toList()

/** Results — সেমিস্টার রেজাল্ট + subject-wise marks (theory/practical/continuous)। */
@Composable
fun ResultsScreen(onBack: () -> Unit) {
    var sem by remember { mutableIntStateOf(4) }
    SubScaffold(title = "Results", onBack = onBack) { _ ->
        Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                SEMS.take(4).forEach { s -> FilterChip(selected = sem == s, onClick = { sem = s }, label = { Text("$s") }) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                SEMS.drop(4).forEach { s -> FilterChip(selected = sem == s, onClick = { sem = s }, label = { Text("$s") }) }
            }
            Spacer(Modifier.height(12.dp))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("${sem}th Semester Result", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Row {
                        StatChip("GPA", "3.72", Modifier.weight(1f))
                        Spacer(Modifier.padding(4.dp))
                        StatChip("Failed", "0", Modifier.weight(1f))
                        Spacer(Modifier.padding(4.dp))
                        StatChip("Rescrutiny", "—", Modifier.weight(1f))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            // TODO: Supabase `results` table + btebresultszone seed — demo rows নিচে
            ResultRow("2763 Computer Application", "Theory 72/90 · Continuous 18/10* · Practical —/—", "A")
            ResultRow("2764 Database Management", "Theory 65/90 · Continuous 8/10 · Practical 42/50", "A−")
            ResultRow("2765 Web Development", "Theory — · Practical 45/50 · Continuous 9/10", "A")
            Text(
                "* continuous fail / referred / expelled / withheld — fail হলে আলাদা রঙে দেখাবে",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun ResultRow(subject: String, marks: String, grade: String) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text(subject, style = MaterialTheme.typography.titleMedium)
            Text(marks, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Grade: $grade", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}
