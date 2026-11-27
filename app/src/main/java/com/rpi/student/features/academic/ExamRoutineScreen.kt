package com.rpi.student.features.academic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rpi.student.core.ui.FocusBanner
import com.rpi.student.core.ui.SectionCard
import com.rpi.student.core.ui.SubScaffold

/**
 * Exam Routine — কাউন্টডাউন + focus banner (v1 auto-behavior: exam ≤7 দিন হলে
 * app জুড়ে banner/nudge; exam-day তে morning banner — blueprint §13)।
 */
@Composable
fun ExamRoutineScreen(onBack: () -> Unit) {
    SubScaffold(title = "Exam Routine", onBack = onBack) { _ ->
        Column(
            Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FocusBanner(daysLeft = 5, examName = "4th Semester Final")
            SectionCard(title = "4th Semester Final (Reg-2022)") {
                ExamRow("2763 Computer Application", "১৫ ডিসেম্বর · সকাল ৯:০০ · Theory")
                ExamRow("2764 Database Management", "১৮ ডিসেম্বর · সকাল ৯:০০ · Theory + MCQ")
                ExamRow("2765 Web Development", "২১ ডিসেম্বর · সকাল ৯:০০ · Theory")
                ExamRow("2765S Web Development Practical", "২৪ ডিসেম্বর · ল্যাব ৩ · Practical")
            }
            SectionCard(title = "Focus টিপস (Pomodoro preset)") {
                Text("• Exam sprint: 45/10 preset — Pomodoro পেজে চালু করো", style = MaterialTheme.typography.bodyMedium)
                Text("• রাতে মোবাইল কমাও — Phase 2 focus mode আসলে auto-block হবে", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ExamRow(subject: String, whenWhere: String) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text(subject, style = MaterialTheme.typography.titleMedium)
            Text(whenWhere, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
