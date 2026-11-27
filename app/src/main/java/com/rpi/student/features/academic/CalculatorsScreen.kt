package com.rpi.student.features.academic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rpi.student.core.ui.SectionCard
import com.rpi.student.core.ui.SubScaffold
import com.rpi.student.core.ui.StatChip

// Reg-2022 grading scale (blueprint §4): A+ 80–100=4.00 … D 40–44=2.00, F<40=0.00
fun gradePoint(marks: Int): Double = when {
    marks >= 80 -> 4.00
    marks >= 75 -> 3.75
    marks >= 70 -> 3.50
    marks >= 65 -> 3.25
    marks >= 60 -> 3.00
    marks >= 55 -> 2.75
    marks >= 50 -> 2.50
    marks >= 45 -> 2.25
    marks >= 40 -> 2.00
    else -> 0.00
}

data class GpaRow(val name: String, val credit: Double, val marks: Int)

fun computeGpa(rows: List<GpaRow>): Double? {
    val totalCredit = rows.sumOf { it.credit }
    if (totalCredit == 0.0) return null
    return rows.sumOf { it.credit * gradePoint(it.marks) } / totalCredit
}

/** GPA Calculator — credit × grade point / Σcredit (Reg-2022)। */
@Composable
fun GpaCalculatorScreen(onBack: () -> Unit) {
    var rows by remember {
        mutableStateOf(
            listOf(
                GpaRow("Computer Application (2763)", 2.0, 72),
                GpaRow("Database (2764)", 3.0, 65),
                GpaRow("Web Dev (2765)", 3.0, 78),
            )
        )
    }
    SubScaffold(title = "GPA Calculator", onBack = onBack) { _ ->
        Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
            SectionCard(title = "Subject marks (Reg-2022 scale)") {
                rows.forEachIndexed { i, r ->
                    var text by remember(i) { mutableStateOf(r.marks.toString()) }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(r.name, style = MaterialTheme.typography.bodyMedium)
                            Text("Credit ${r.credit} · GP ${gradePoint(text.toIntOrNull() ?: 0)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedTextField(
                            value = text,
                            onValueChange = { v ->
                                text = v
                                val m = v.toIntOrNull()
                                if (m != null && m in 0..100) rows = rows.toMutableList().also { it[i] = r.copy(marks = m) }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.padding(start = 8.dp).width(80.dp),
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatChip("Σ Credit", rows.sumOf { it.credit }.toString(), Modifier.weight(1f))
                    Spacer(Modifier.padding(4.dp))
                    StatChip("GPA", "%.2f".format(computeGpa(rows) ?: 0.0), Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("নিয়ম: GPA = Σ(credit × GP) / Σcredit। যেকোনো subject-এ GP < 2.00 মানে সেই subject fail — referred/continuous fail টাইপ রেজাল্ট পেজে দেখাবে।", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** CGPA Calculator — ৮ সেমেস্টার weight: 5/5/10/10/20/20/20/10 (Reg-2022)। */
private val WEIGHTS = listOf(5, 5, 10, 10, 20, 20, 20, 10)

@Composable
fun CgpaCalculatorScreen(onBack: () -> Unit) {
    var gpas by remember { mutableStateOf(List(8) { "" }) }
    var target by remember { mutableStateOf("") }

    fun parse(i: Int): Double? = gpas[i].toDoubleOrNull()?.takeIf { it in 0.0..4.0 }

    SubScaffold(title = "CGPA Calculator", onBack = onBack) { _ ->
        Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
            SectionCard(title = "সেমেস্টার GPA দাও (0–4.00)") {
                for (i in 0 until 8 step 2) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        listOf(i, i + 1).forEach { idx ->
                            OutlinedTextField(
                                value = gpas[idx],
                                onValueChange = { v -> gpas = gpas.toMutableList().also { it[idx] = v } },
                                label = { Text("S${idx + 1} (w${WEIGHTS[idx]})") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                val done = (0 until 8).filter { parse(it) != null }
                val wd = done.sumOf { WEIGHTS[it] }
                val sd = done.sumOf { (parse(it) ?: 0.0) * WEIGHTS[it] }
                val wr = 100 - wd
                val cgpa = if (wd > 0) sd / wd else null
                val req = target.toDoubleOrNull()?.let { t -> if (wr > 0) (t * 100 - sd) / wr else null }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatChip("CGPA এখন পর্যন্ত", cgpa?.let { "%.2f".format(it) } ?: "—", Modifier.weight(1f))
                    Spacer(Modifier.padding(4.dp))
                    StatChip("Completed weight", "$wd/100", Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = target, onValueChange = { target = it }, label = { Text("Target CGPA (যেমন 3.75)") },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (req != null && req in 0.0..4.0) {
                    Spacer(Modifier.height(8.dp))
                    Card(Modifier.fillMaxWidth()) {
                        Text(
                            "🎯 Target %.2f পেতে হলে বাকি সেমেস্টারগুলোর average GPA লাগবে %.2f".format(
                                target.toDoubleOrNull() ?: 0.0, req
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("নিয়ম (Reg-2022): CGPA = Σ(GPAᵢ × weightᵢ) / Σweightᵢ — weight 5,5,10,10,20,20,20,10।", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
