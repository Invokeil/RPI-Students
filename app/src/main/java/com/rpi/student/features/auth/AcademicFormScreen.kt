package com.rpi.student.features.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rpi.student.core.ui.RpiLoader
import com.rpi.student.core.ui.SectionCard
import com.rpi.student.data.remote.AuthRepo
import kotlinx.coroutines.launch

/**
 * ধাপ ২ — mandatory academic registration (blueprint §3):
 * name, roll, registration, session, technology, semester 1–8, shift, section, bus route, mess।
 * জমা দিলে OTP ইমেইল যাবে (Appwrite email token) → verify → profile doc (status=pending)
 * → admin verify → blue badge + 4-group auto-join।
 */
private val TECHNOLOGIES = listOf("Civil", "CST", "Electrical", "Electromedical", "Electronics", "Mechanical", "Power")

@Composable
fun AcademicFormScreen(onSubmit: (String) -> Unit, onSkipToPending: () -> Unit) {
    var fullName by remember { mutableStateOf("") }
    var roll by remember { mutableStateOf("") }
    var reg by remember { mutableStateOf("") }
    var session by remember { mutableStateOf("") }
    var tech by remember { mutableStateOf("") }
    var semester by remember { mutableStateOf(1) }
    var shift by remember { mutableStateOf("Morning") }
    var section by remember { mutableStateOf("A") }
    var busRoute by remember { mutableStateOf("") }
    var messMember by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState())) {
        Text("একাডেমিক তথ্য", style = MaterialTheme.typography.headlineMedium)
        Text("এগুলো ছাড়া academic module লক থাকবে", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))

        SectionCard(title = "মূল তথ্য") {
            OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("তোমার নাম") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = roll, onValueChange = { roll = it }, label = { Text("রোল") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = reg, onValueChange = { reg = it }, label = { Text("রেজিস্ট্রেশন নম্বর") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = session, onValueChange = { session = it }, label = { Text("সেশন (যেমন 2022-23)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(12.dp))

        SectionCard(title = "টেকনোলজি") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                TECHNOLOGIES.take(4).forEach { t ->
                    FilterChip(selected = tech == t, onClick = { tech = t }, label = { Text(t) })
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                TECHNOLOGIES.drop(4).forEach { t ->
                    FilterChip(selected = tech == t, onClick = { tech = t }, label = { Text(t) })
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        SectionCard(title = "সেমিস্টার (১–৮)") {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                (1..8).forEach { s ->
                    FilterChip(selected = semester == s, onClick = { semester = s }, label = { Text("$s") })
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        SectionCard(title = "শিফট ও সেকশন") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = shift == "Morning", onClick = { shift = "Morning" }, label = { Text("Morning") })
                FilterChip(selected = shift == "Second", onClick = { shift = "Second" }, label = { Text("Second") })
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = section == "A", onClick = { section = "A" }, label = { Text("Section A") })
                FilterChip(selected = section == "B", onClick = { section = "B" }, label = { Text("Section B") })
            }
        }
        Spacer(Modifier.height(12.dp))

        SectionCard(title = "ঐচ্ছিক") {
            OutlinedTextField(value = busRoute, onValueChange = { busRoute = it }, label = { Text("বাস রুট (না থাকলে খালি)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("মেস মেম্বার", modifier = Modifier.weight(1f))
                Switch(checked = messMember, onCheckedChange = { messMember = it })
            }
        }
        Spacer(Modifier.height(20.dp))

        val valid = fullName.isNotBlank() && roll.isNotBlank() && reg.isNotBlank() && session.isNotBlank() && tech.isNotBlank()
        Button(
            onClick = {
                // ফর্ম ডেটা SignupState-এ → OTP ইমেইল পাঠাও → OTP screen-এ যাও
                SignupState.fullName = fullName.trim()
                SignupState.roll = roll.trim()
                SignupState.registration = reg.trim()
                SignupState.session = session.trim()
                SignupState.technology = tech
                SignupState.semester = semester
                SignupState.shift = shift
                SignupState.section = section
                SignupState.busRoute = busRoute.trim()
                SignupState.messMember = messMember

                busy = true
                error = null
                scope.launch {
                    AuthRepo.sendOtp(SignupState.userId, SignupState.email).fold(
                        onSuccess = {
                            busy = false
                            onSubmit(SignupState.email)
                        },
                        onFailure = {
                            busy = false
                            error = "OTP পাঠানো যায়নি: ${AuthRepo.friendlyError(it)} — নিচের fallback ব্যবহার করো"
                        },
                    )
                }
            },
            enabled = valid && !busy,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) { if (busy) RpiLoader(size = 22.dp, color = MaterialTheme.colorScheme.onPrimary) else Text("জমা দাও — OTP যাবে ইমেইলে") }

        Spacer(Modifier.height(10.dp))
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
        }
        OutlinedButton(
            onClick = {
                // SMTP কনফিগার না থাকলে — password session দিয়ে সরাসরি ঢোকা (profile তবু সেভ হবে)
                SignupState.fullName = fullName.trim()
                SignupState.roll = roll.trim()
                SignupState.registration = reg.trim()
                SignupState.session = session.trim()
                SignupState.technology = tech
                SignupState.semester = semester
                SignupState.shift = shift
                SignupState.section = section
                SignupState.busRoute = busRoute.trim()
                SignupState.messMember = messMember
                onSkipToPending()
            },
            enabled = valid,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("OTP পাইনি? (SMTP সেট না থাকলে) সরাসরি এগিয়ে যাও") }

        Spacer(Modifier.height(12.dp))
        Text(
            "জমা দেওয়ার পর admin verification শেষ না হওয়া পর্যন্ত academic module লক থাকবে। ব্যক্তিগত তথ্য পরে বদলাতে চাইলে OTP approval লাগবে।",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun OtpScreen(email: String, onVerified: () -> Unit, onFallback: () -> Unit) {
    var code by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        SectionCard(title = "ইমেইল OTP") {
            Text("$email-এ ৬-ডিজিটের কোড গেছে (১০ মিনিট ভ্যালিড)", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = code, onValueChange = { if (it.length <= 6) code = it.filter { c -> c.isDigit() } },
                label = { Text("৬-ডিজিটের কোড") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    busy = true
                    error = null
                    scope.launch {
                        AuthRepo.verifyOtp(SignupState.userId, code).fold(
                            onSuccess = {
                                // সেশন হয়ে গেছে — profile doc সেভ করো (pending status)
                                AuthRepo.saveProfile(SignupState.userId, SignupState.toUserEntity())
                                SignupState.clear()
                                busy = false
                                onVerified()
                            },
                            onFailure = {
                                busy = false
                                error = "কোড ভুল/মেয়াদ শেষ: ${AuthRepo.friendlyError(it)}"
                            },
                        )
                    }
                },
                enabled = code.length == 6 && !busy,
                modifier = Modifier.fillMaxWidth(),
            ) { if (busy) RpiLoader(size = 20.dp, color = MaterialTheme.colorScheme.onPrimary) else Text("ভেরিফাই করো") }
            Spacer(Modifier.height(10.dp))
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
            }
            androidx.compose.material3.OutlinedButton(
                onClick = {
                    scope.launch {
                        AuthRepo.login(SignupState.email, SignupState.password).fold(
                            onSuccess = {
                                AuthRepo.saveProfile(SignupState.userId, SignupState.toUserEntity())
                                SignupState.clear()
                                onFallback()
                            },
                            onFailure = { onFallback() },
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("OTP আসেনি? সরাসরি এগিয়ে যাও") }
        }
    }
}

@Composable
fun VerificationPendingScreen(onContinueDemo: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        SectionCard(title = "ভেরিফিকেশন পেন্ডিং") {
            Text("তোমার একাডেমিক তথ্য admin চেক করছে।", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Text("✅ Verify হলে: blue badge + ৪টা group-এ auto-join + সব module আনলক", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Text("❌ রিজেক্ট হলে: ঠিক করে আবার জমা দিতে পারবে", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onContinueDemo, modifier = Modifier.fillMaxWidth()) { Text("Demo — ভেরিফাইড হিসেবে ঢুকো") }
        }
    }
}
