package com.rpi.student.features.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rpi.student.core.ui.EmptyState
import com.rpi.student.core.ui.RpiLoader
import com.rpi.student.core.ui.SubScaffold
import com.rpi.student.core.theme.GlassSurface
import com.rpi.student.data.remote.Appwrite
import com.rpi.student.data.remote.AuthRepo
import com.rpi.student.data.remote.CloudRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Media Share feature flag (blueprint §7 lock):
 * v1 chat = text-only। বাটন UI-তে থাকবে, চালু হলে Cloudflare R2 presigned upload + pointer message।
 */
object ChatFlags {
    const val MEDIA_SHARE_ENABLED = false
}

data class UiGroup(val id: String, val name: String, val kind: String, val nonLeaveable: Boolean, val members: Int, val last: String)

private val defaultGroups = listOf(
    UiGroup("g-all", "All in One", "ALL", true, 4200, "পলিটেকনিক ভর্তি বিজ্ঞপ্তি এসেছে..."),
    UiGroup("g-family", "Family — Technology", "FAMILY", true, 640, "4th sem রুটিন পেয়ে গেছেন সবাই?"),
    UiGroup("g-own", "নিজের ক্লাস গ্রুপ", "OWN", true, 48, "আজ ল্যাব ফাইনাল কে কে প্রস্তুত?"),
    UiGroup("g-bus", "BUS — রুট গ্রুপ", "BUS", true, 85, "কাল বাস ২০ মিনিট দেরিতে..."),
)

/** Group list — ৪টা auto-join, non-leaveable default group (blueprint §7)। Appwrite groups → fallback default। */
@Composable
fun GroupListScreen(onOpenGroup: (String) -> Unit) {
    var groups by remember { mutableStateOf(defaultGroups) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        CloudRepo.groups().fold(
            onSuccess = { remote ->
                if (remote.isNotEmpty()) {
                    groups = remote.map { UiGroup(it.id, it.name, it.kind, it.nonLeaveable, it.memberCount, it.lastMessage) }
                }
                loading = false
            },
            onFailure = { loading = false },
        )
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Chat", style = MaterialTheme.typography.headlineMedium)
        Text(
            if (loading) "গ্রুপ লোড হচ্ছে..." else "তোমার ৪টা default group — ছেড়ে যাওয়া যাবে না",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        androidx.compose.foundation.layout.Spacer(Modifier.padding(vertical = 6.dp))
        if (loading) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                RpiLoader(size = 64.dp, color = MaterialTheme.colorScheme.primary)
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(groups) { g ->
                Card(onClick = { onOpenGroup(g.id) }, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(g.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            if (g.nonLeaveable) Icon(Icons.Filled.Lock, contentDescription = "non-leaveable", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp))
                        }
                        Text("${g.members} জন · ${g.last}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

data class UiMessage(val id: String, val sender: String, val body: String, val mine: Boolean, val time: String)

private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

/** Chat room — hot window (24h) UI; v1 text-only। Appwrite hot_messages + polling। */
@Composable
fun ChatRoomScreen(groupId: String, onBack: () -> Unit) {
    val groupName = defaultGroups.firstOrNull { it.id == groupId }?.name ?: "Group"
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    var myUserId by remember { mutableStateOf("") }
    var myName by remember { mutableStateOf("আমি") }
    var loading by remember { mutableStateOf(true) }
    val messages = remember { mutableStateListOf<UiMessage>() }
    val listState = rememberLazyListState()
    var lastBody by remember { mutableStateOf<String?>(null) }

    // my identity (session না থাকলে demo নাম)
    LaunchedEffect(Unit) {
        AuthRepo.currentUserOrNull()?.let { (uid, email) ->
            myUserId = uid
            AuthRepo.findMyProfile(uid)?.let { if (it.name.isNotBlank()) myName = it.name }
                ?: run { myName = email.substringBefore("@") }
        }
    }

    // load + poll hot window
    LaunchedEffect(groupId) {
        while (true) {
            if (Appwrite.isReady()) {
                CloudRepo.messages(groupId, myUserId).fold(
                    onSuccess = { list ->
                        messages.clear()
                        list.forEach { m ->
                            messages.add(
                                UiMessage(
                                    id = m.id,
                                    sender = m.userName,
                                    body = m.body,
                                    mine = m.userId == myUserId,
                                    time = timeFmt.format(Date(m.createdAt)),
                                )
                            )
                        }
                        loading = false
                    },
                    onFailure = { e ->
                        loading = false
                        if (lastBody == null) {
                            messages.clear()
                            messages.add(UiMessage("d1", "Demo", "Appwrite connect হয়নি — demo message। লগইন করে আবার খোলো।", false, "নেট"))
                            snackbar.showSnackbar("Chat load fail: ${e.message ?: "network"}")
                        }
                    },
                )
                lastBody = messages.lastOrNull()?.body
            } else {
                loading = false
            }
            delay(6000)
        }
    }

    // auto-scroll to bottom
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    SubScaffold(title = groupName, onBack = onBack) { _ ->
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                if (loading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        RpiLoader(size = 72.dp, color = MaterialTheme.colorScheme.primary)
                    }
                } else if (messages.isEmpty()) {
                    EmptyState("প্রথম মেসেজ দাও — ২৪ ঘণ্টার মেসেজ hot window-তে থাকবে, পরে auto-seal হবে")
                }
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(messages) { m ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = if (m.mine) Arrangement.End else Arrangement.Start) {
                            Box(
                                Modifier
                                    .background(
                                        if (m.mine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(16.dp),
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Column {
                                    if (!m.mine) Text(m.sender, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    Text(m.body, style = MaterialTheme.typography.bodyMedium, color = if (m.mine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                                    Text(m.time, style = MaterialTheme.typography.labelSmall, color = (if (m.mine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }
                SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
            }
            // Glass composer — chrome-only glass (perf rule)
            GlassSurface(Modifier.fillMaxWidth().padding(8.dp).imePadding(), shape = RoundedCornerShape(24.dp)) {
                Row(Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        scope.launch {
                            if (ChatFlags.MEDIA_SHARE_ENABLED) {
                                // TODO: CF R2 presigned direct upload → pointer message
                            } else {
                                snackbar.showSnackbar("Media Share শীঘ্রই আসছে — Cloudflare R2 দিয়ে (v1 এ টেক্সট only)")
                            }
                        }
                    }) { Icon(Icons.Filled.AttachFile, contentDescription = "Attach") }
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = { Text("মেসেজ লেখো...") },
                        modifier = Modifier.weight(1f),
                        maxLines = 3,
                    )
                    IconButton(onClick = {
                        val text = input.trim()
                        if (text.isNotBlank()) {
                            input = ""
                            messages.add(UiMessage("local-${System.currentTimeMillis()}", myName, text, true, "এখন"))
                            scope.launch {
                                if (myUserId.isBlank()) {
                                    snackbar.showSnackbar("মেসেজ পাঠাতে লগইন করতে হবে")
                                } else {
                                    CloudRepo.sendMessage(groupId, myUserId, myName, text).fold(
                                        onSuccess = { /* poller দিয়ে আসবে */ },
                                        onFailure = { snackbar.showSnackbar("পাঠানো যায়নি: ${it.message ?: "network"}") },
                                    )
                                }
                            }
                        }
                    }) { Icon(Icons.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary) }
                }
            }
        }
    }
}
