package com.rpi.student.features.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rpi.student.core.ui.SectionCard
import com.rpi.student.core.ui.SubScaffold
import com.rpi.student.core.theme.BadgeBlue
import com.rpi.student.core.theme.BadgeGold
import kotlinx.coroutines.launch

/**
 * Profile — badge display (none → blue verified → gold, server-only set),
 * ব্যক্তিগত তথ্য (admin ছাড়া বদলানো যাবে না — change request + OTP)।
 */
@Composable
fun ProfileScreen(onNavigate: (String) -> Unit, onLogout: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionCard(title = "") {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Text("R", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp).clip(CircleShape))
                    Text("তোমার নাম (Demo)", style = MaterialTheme.typography.titleMedium)
                    AssistChip(
                        onClick = {},
                        label = { Text("✔ Verified — Blue Badge") },
                        colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                            containerColor = BadgeBlue.copy(alpha = 0.12f),
                        ),
                    )
                }
            }
        }

        SectionCard(title = "একাডেমিক পরিচয়") {
            ProfileRow("টেকনোলজি", "CST (Computer Science & Technology)")
            ProfileRow("রোল / রেজিস্ট্রেশন", "5422101 / 2022101542")
            ProfileRow("সেশন", "2022-23")
            ProfileRow("সেমিস্টার", "৪র্থ (4/8)")
            ProfileRow("শিফট / সেকশন", "Morning · A")
            ProfileRow("বাস রুট", "Mithapukur")
        }

        SectionCard(title = "Gold Badge (admin)") {
            Text("Gold badge পাবে যদি: মেস ম্যানেজার, ক্লাস রুটিন আপডেট দেওয়া, নোটিস বোর্ড সাহায্য — admin set করবে।", style = MaterialTheme.typography.bodyMedium)
            AssistChip(
                onClick = {},
                label = { Text("🏅 Eligible — admin approval বাকি") },
                colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                    containerColor = BadgeGold.copy(alpha = 0.15f),
                ),
            )
        }

        SectionCard(title = "ব্যক্তিগত তথ্য বদলাতে চাও?") {
            Text("রোল/রেজিস্ট্রেশন/টেকনোলজি/সেমিস্টার বদলাতে change request → OTP confirm → admin approve লাগবে।", style = MaterialTheme.typography.bodyMedium)
            AssistChip(onClick = { onNavigate("profile/edit-request") }, label = { Text("Change Request দাও") })
        }

        SectionCard(title = "অ্যাপ") {
            AssistChip(onClick = { onNavigate("profile/settings") }, label = { Text("⚙ Settings") })
            Spacer(Modifier.padding(vertical = 4.dp))
            AssistChip(onClick = { onNavigate("profile/developer") }, label = { Text("👨‍💻 Developer — লিংক ও update") })
            Spacer(Modifier.padding(vertical = 4.dp))
            AssistChip(onClick = { onNavigate("profile/about") }, label = { Text("ℹ About") })
            Spacer(Modifier.padding(vertical = 4.dp))
            AssistChip(
                onClick = {
                    kotlinx.coroutines.MainScope().launch {
                        com.rpi.student.data.remote.AuthRepo.logout()
                        onLogout()
                    }
                },
                label = { Text("🚪 লগআউট") },
            )
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    SubScaffold(title = "Settings", onBack = onBack) { _ ->
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(onClick = { onNavigate("profile/edit-request") }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("ব্যক্তিগত তথ্য পরিবর্তন", style = MaterialTheme.typography.titleMedium)
                    Text("OTP-verified change request", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("থিম", style = MaterialTheme.typography.titleMedium)
                    Text("Liquid Glass — system dark/light follow করছে (API 31+ full effect)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("নোটিফিকেশন", style = MaterialTheme.typography.titleMedium)
                    Text("Exam countdown, chat, mess deadline — সব চালু", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Media Share (Chat)", style = MaterialTheme.typography.titleMedium)
                    Text("Feature flag: OFF — চালু হলে Cloudflare R2-তে upload হবে (90-day TTL)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun EditRequestScreen(onBack: () -> Unit) {
    SubScaffold(title = "Change Request", onBack = onBack) { _ ->
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionCard(title = "কী বদলাতে চাও?") {
                Text("• রোল / রেজিস্ট্রেশন ভুল হলে\n• সেমিস্টার প্রমোশন হলে\n• বাস রুট / মেস মেম্বারশিপ বদলালে", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.padding(6.dp))
                Text("ফ্লো: request দাও → তোমার ফোনে OTP push (email OTP backup) → কনফার্ম → admin approve → ডেটা বদলাবে (server-side, RLS-protected)।", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    SubScaffold(title = "About", onBack = onBack) { _ ->
        Column(Modifier.padding(16.dp)) {
            SectionCard(title = com.rpi.student.BuildConfig.APP_NAME) {
                Text(com.rpi.student.BuildConfig.ORG_NAME, style = MaterialTheme.typography.bodyLarge)
                Text("v${com.rpi.student.BuildConfig.VERSION_NAME} · Kotlin + Jetpack Compose + Liquid Glass", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Backend: Appwrite · OTA update: GitHub Releases · Email OTP: Appwrite + Resend SMTP", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Developer: Himadri Jr — GitHub: ${com.rpi.student.BuildConfig.GITHUB_REPO}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
