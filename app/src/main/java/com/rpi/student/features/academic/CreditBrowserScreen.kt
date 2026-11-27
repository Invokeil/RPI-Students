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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rpi.student.core.ui.SectionCard
import com.rpi.student.core.ui.SubScaffold

private val TECHS = listOf("Civil", "CST", "Electrical", "Electromedical", "Electronics", "Mechanical", "Power")

/**
 * Subject Credit Browser — top priority module (blueprint §4)।
 * tech + regulation + semester বেছে subject-এর booklist context, credit,
 * theory/practical full marks ও availability দেখা যাবে (btebresultszone harvest seed)।
 */
@Composable
fun CreditBrowserScreen(onBack: () -> Unit) {
    var tech by remember { mutableStateOf("CST") }
    var reg by remember { mutableIntStateOf(2022) }
    var sem by remember { mutableIntStateOf(4) }

    SubScaffold(title = "Subject Credits", onBack = onBack) { _ ->
        Column(Modifier.padding(16.dp)) {
            SectionCard(title = "ফিল্টার") {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    TECHS.take(4).forEach { t -> FilterChip(selected = tech == t, onClick = { tech = t }, label = { Text(t) }) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                    TECHS.drop(4).forEach { t -> FilterChip(selected = tech == t, onClick = { tech = t }, label = { Text(t) }) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                    FilterChip(selected = reg == 2022, onClick = { reg = 2022 }, label = { Text("Reg 2022") })
                    FilterChip(selected = reg == 2016, onClick = { reg = 2016 }, label = { Text("Reg 2016") })
                    (1..8).take(4).forEach { s -> FilterChip(selected = sem == s, onClick = { sem = s }, label = { Text("S$s") }) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                    (5..8).forEach { s -> FilterChip(selected = sem == s, onClick = { sem = s }, label = { Text("S$s") }) }
                }
            }
            Spacer(Modifier.height(12.dp))
            // TODO: subjectDao.bySemester(tech, reg, sem) — seed importer চালু হলে লাইভ
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(demoSubjects) { s ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            Row {
                                Text(s.code, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.padding(horizontal = 8.dp))
                                Text("Credit ${s.credit}", style = MaterialTheme.typography.labelLarge)
                            }
                            Text(s.name, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                s.marksDetail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            Text(
                "Sources: btebresultszone booklist + GPA-calculator pages → seed importer → Appwrite subjects → Room cache।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

private data class DemoSubject(val code: String, val name: String, val credit: Double, val marksDetail: String)

private val demoSubjects = listOf(
    DemoSubject("2763", "Computer Application", 2.0, "Theory 90 ✓ · Practical — · Total 100"),
    DemoSubject("2764", "Database Management System", 3.0, "Theory 90 ✓ · Continuous 10 ✓ · Practical 50 ✓ · Total 150"),
    DemoSubject("2765", "Web Development", 3.0, "Theory 90 ✓ · Continuous 10 ✓ · Practical 50 ✓ · Total 150"),
    DemoSubject("2766", "Software Engineering", 2.0, "Theory 80 ✓ · Continuous 20 ✓ · Total 100"),
)
