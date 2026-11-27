package com.rpi.student.features.academic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rpi.student.core.ui.SectionCard

/** Academic hub — সব academic module-এর প্রবেশদ্বার। */
@Composable
fun AcademicHubScreen(onNavigate: (String) -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Academic", style = MaterialTheme.typography.headlineMedium)
        Text("CST · 4th Semester · Morning · A", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        HubCard("📅 Class Routine", "আজকের ও সারা সপ্তাহের ক্লাস", "academic/routine", onNavigate)
        HubCard("📊 Results", "সেমিস্টার রেজাল্ট + মার্ক বিস্তারিত", "academic/results", onNavigate)
        HubCard("🧮 GPA Calculator", "Reg-2022 গ্রেডিং স্কেল", "academic/gpa", onNavigate)
        HubCard("🎓 CGPA Calculator", "৮ সেমেস্টার weight 5/5/10/10/20/20/20/10", "academic/cgpa", onNavigate)
        HubCard("📚 Subject Credit Browser", "বুকলিস্ট + credit + marks distribution", "academic/credits", onNavigate)
        HubCard("📝 Exam Routine", "কাউন্টডাউন + focus banner", "academic/exam", onNavigate)
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun HubCard(title: String, subtitle: String, route: String, onNavigate: (String) -> Unit) {
    Card(onClick = { onNavigate(route) }, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
