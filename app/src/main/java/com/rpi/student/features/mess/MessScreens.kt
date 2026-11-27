package com.rpi.student.features.mess

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

/**
 * Mess module (blueprint §16) — BD-style:
 * rate = (bazar + fixed cost) / total meals; bill = meals × rate − deposit;
 * off-meal deadline, bazar ledger, deposits, month-end lock।
 */
@Composable
fun MessScreen(onNavigate: (String) -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Mess", style = MaterialTheme.typography.headlineMedium)
        Text("নভেম্বর মাস · ১২ জন মেম্বার", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        SectionCard(title = "এই মাসের সারসংক্ষেপ") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatChip("Meal Rate", "৳54.20", Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                StatChip("আমার Meals", "86", Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                StatChip("বিল", "৳4,412", Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatChip("Bazar", "৳32,500", Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                StatChip("Deposit", "৳2,000", Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                StatChip("Total Meals", "598", Modifier.weight(1f))
            }
        }

        SectionCard(title = "আজকের Meal") {
            Text("Off-meal deadline: সকাল ৮টা / রাত ৯টা — পার হলে admin ছাড়া বদলাবে না", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row {
                Button(onClick = { onNavigate("mess/entry") }, modifier = Modifier.weight(1f)) { Text("Meal অন/অফ") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { onNavigate("mess/bazar") }, modifier = Modifier.weight(1f)) { Text("Bazar যোগ") }
            }
            Spacer(Modifier.height(8.dp))
            Row {
                Button(onClick = { onNavigate("mess/deposits") }, modifier = Modifier.weight(1f)) { Text("Deposit") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { onNavigate("mess/bill") }, modifier = Modifier.weight(1f)) { Text("মাসিক হিসাব") }
            }
        }

        SectionCard(title = "Holiday Mode") {
            Text("ছুটির সময় পুরো মেস বন্ধ — কেউ meal চার্জ পাবে না (admin চালু করবে)", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun MessEntryScreen(onBack: () -> Unit) {
    var on by remember { mutableStateOf(true) }
    SubScaffold(title = "Meal অন/অফ", onBack = onBack) { _ ->
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("সকাল (নাস্তা)", "দুপুর", "রাত").forEachIndexed { i, meal ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(meal, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                        var checked by remember { mutableStateOf(i != 2) }
                        Switch(checked = checked, onCheckedChange = { checked = it })
                    }
                }
            }
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("কাল সকালের জন্য অফ করো (deadline আজ রাত ৯টা)", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = !on, onCheckedChange = { on = !it })
                }
            }
        }
    }
}

@Composable
fun BazarScreen(onBack: () -> Unit) {
    var item by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    SubScaffold(title = "Bazar Ledger", onBack = onBack) { _ ->
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionCard(title = "নতুন Bazar") {
                OutlinedTextField(value = item, onValueChange = { item = it }, label = { Text("কী কিনলে (চাল/তেল/মাছ...)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("টাকা (৳)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                Button(onClick = { /* TODO: supabase mess_entries insert */ }, enabled = item.isNotBlank() && amount.isNotBlank()) {
                    Text("যোগ করো")
                }
            }
            SectionCard(title = "এই মাসের Bazar") {
                Text("১২/১১ — চাল ২৫ কেজি — ৳1,850 (Rakib)", style = MaterialTheme.typography.bodyMedium)
                Text("১৫/১১ — মাছ ৪ কেজি — ৳2,400 (Sumon)", style = MaterialTheme.typography.bodyMedium)
                Text("১৮/১১ — সবজি + মসলা — ৳920 (Nusrat)", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun DepositsScreen(onBack: () -> Unit) {
    SubScaffold(title = "Deposits", onBack = onBack) { _ ->
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionCard(title = "মাসের Deposit") {
                Text("Rakib — ৳2,000 (১১/১১)", style = MaterialTheme.typography.bodyMedium)
                Text("Nusrat — ৳2,000 (১২/১১)", style = MaterialTheme.typography.bodyMedium)
                Text("Sumon — ৳1,500 (১৪/১১)", style = MaterialTheme.typography.bodyMedium)
            }
            Text("Deposit বিল থেকে বাদ যাবে: bill = meals × rate − deposit।", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun MonthlyBillScreen(onBack: () -> Unit) {
    var locked by remember { mutableIntStateOf(1) }
    SubScaffold(title = "মাসিক হিসাব", onBack = onBack) { _ ->
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionCard(title = "নভেম্বর — Summary") {
                Text("তোমার meals: 86 × ৳54.20 = ৳4,661", style = MaterialTheme.typography.bodyLarge)
                Text("− deposit ৳2,000 → দিতে হবে ৳2,661", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                Text("মাস শেষে admin lock করলে summary + PDF export + পরের মাসে carry-forward হবে।", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Text(if (locked == 1) "🔒 মাস locked — হিসাব চূড়ান্ত" else "খোলা আছে", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
