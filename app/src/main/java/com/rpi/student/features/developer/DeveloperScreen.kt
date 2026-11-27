package com.rpi.student.features.developer

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rpi.student.BuildConfig
import com.rpi.student.core.ui.SectionCard
import com.rpi.student.core.ui.SubScaffold
import com.rpi.student.core.theme.BadgeGold
import com.rpi.student.update.UpdateManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Developer page — ডেভেলপার ডিটেইলস + redirect লিংক + OTA update।
 * GitHub repo-তে students দের Star দেওয়ার আহ্বান।
 */
private const val REPO_URL = "https://github.com/${BuildConfig.GITHUB_REPO}"
private const val FACEBOOK_URL = "https://www.facebook.com/hsr28h/"
private const val INSTAGRAM_URL = "https://www.instagram.com/himadrijr"
private const val EMAIL = "himadrient@proton.me"

@Composable
fun DeveloperScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ---------- OTA state ----------
    var checkState by remember { mutableStateOf<String?>(null) }
    var updateInfo by remember { mutableStateOf<UpdateManager.UpdateInfo?>(null) }
    var downloadPercent by remember { mutableIntStateOf(-1) }
    var downloadId by remember { mutableStateOf(0L) }
    var downloadFailed by remember { mutableStateOf<String?>(null) }

    fun open(url: String) = runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    // download poller
    LaunchedEffect(downloadId) {
        if (downloadId <= 0L) return@LaunchedEffect
        while (true) {
            when (val st = UpdateManager.queryState(context, downloadId)) {
                is UpdateManager.DownloadState.Progress -> downloadPercent = st.percent
                is UpdateManager.DownloadState.Done -> {
                    downloadPercent = 100
                    if (UpdateManager.canInstall(context)) {
                        UpdateManager.install(context, downloadId)
                    } else {
                        UpdateManager.requestInstallPermission(context)
                    }
                    break
                }
                is UpdateManager.DownloadState.Failed -> {
                    downloadFailed = st.message
                    downloadPercent = -1
                    break
                }
            }
            delay(1200)
        }
    }

    SubScaffold(title = "Developer", onBack = onBack) { _ ->
        Column(
            Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ---------- Developer identity ----------
            SectionCard(title = "") {
                Column(
                    Modifier.fillMaxWidth().padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier.size(72.dp).clip(CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        com.rpi.student.core.ui.RpiLoader(size = 72.dp, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Himadri Jr", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "Developer & Maintainer — ${BuildConfig.ORG_NAME}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Built with Kotlin + Jetpack Compose + Liquid Glass",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // ---------- Redirect links ----------
            SectionCard(title = "যোগাযোগ ও সোশ্যাল") {
                LinkRow(
                    icon = Icons.Filled.Public,
                    label = "GitHub — ${BuildConfig.GITHUB_REPO}",
                    sub = "Star দাও ⭐ — development সাপোর্ট করো",
                    onClick = { open(REPO_URL) },
                )
                LinkRow(
                    icon = Icons.Filled.Star,
                    label = "⭐ Star the repo",
                    sub = "নতুন update আর feature পেতে ওয়াচ করো",
                    onClick = { open("$REPO_URL/stargazers") },
                    tint = BadgeGold,
                )
                LinkRow(
                    icon = Icons.Filled.Facebook,
                    label = "Facebook",
                    sub = FACEBOOK_URL,
                    onClick = { open(FACEBOOK_URL) },
                )
                LinkRow(
                    icon = Icons.Filled.Code,
                    label = "Instagram",
                    sub = INSTAGRAM_URL,
                    onClick = { open(INSTAGRAM_URL) },
                )
                LinkRow(
                    icon = Icons.Filled.AlternateEmail,
                    label = "Email",
                    sub = EMAIL,
                    onClick = {
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:$EMAIL")
                                    putExtra(Intent.EXTRA_SUBJECT, "${BuildConfig.APP_NAME} — feedback")
                                }
                            )
                        }
                    },
                )
            }

            // ---------- OTA update ----------
            SectionCard(title = "App update") {
                Text(
                    "এখন চলছে v${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE})",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(8.dp))

                val info = updateInfo
                if (info == null) {
                    Button(
                        onClick = {
                            scope.launch {
                                checkState = null
                                downloadFailed = null
                                when (val r = UpdateManager.check()) {
                                    is UpdateManager.CheckResult.Available -> {
                                        updateInfo = r.info
                                        checkState = null
                                    }
                                    UpdateManager.CheckResult.UpToDate -> checkState = "✅ তুমি latest version-এ আছো"
                                    UpdateManager.CheckResult.NoRelease -> checkState = "এখনো কোনো release নেই — প্রথম push-এর পরে আসবে"
                                    is UpdateManager.CheckResult.Error -> checkState = "⚠ ${r.message}"
                                }
                            }
                        },
                        enabled = downloadPercent < 0,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Filled.SystemUpdate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.padding(horizontal = 4.dp))
                        Text("নতুন update চেক করো")
                    }
                    checkState?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Text(
                        "🚀 নতুন version v${info.versionName} এসেছে!",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(8.dp))
                    if (downloadPercent < 0) {
                        Button(
                            onClick = { downloadId = UpdateManager.startDownload(context, info) },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("ডাউনলোড শুরু করো (${info.versionName})") }
                    } else if (downloadPercent < 100) {
                        LinearProgressIndicator(
                            progress = { downloadPercent / 100f },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text("ডাউনলোড হচ্ছে... $downloadPercent%", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                downloadFailed?.let {
                    Spacer(Modifier.height(8.dp))
                    Text("⚠ $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }

            // ---------- OTA info ----------
            SectionCard(title = "Update কীভাবে কাজ করে?") {
                Text(
                    "প্রতিটা GitHub push → অটোমেটিক signed APK build → GitHub Release। অ্যাপ চালু হলে নতুন release থাকলে জানাবে; Developer page থেকে এক ট্যাপে download + install। Play Store-এ গেলে এই OTA Play-এর in-app update-এ পাল্টাবে।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                "${BuildConfig.APP_NAME} · ${BuildConfig.ORG_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun LinkRow(
    icon: ImageVector,
    label: String,
    sub: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(26.dp))
        Spacer(Modifier.padding(horizontal = 6.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("→", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
    }
}
