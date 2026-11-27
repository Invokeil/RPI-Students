package com.rpi.student.features.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.rpi.student.BuildConfig
import com.rpi.student.core.ui.RpiLoader
import com.rpi.student.core.ui.SectionCard
import com.rpi.student.data.remote.AuthRepo
import kotlinx.coroutines.launch

// ================================================================
// Auth flow — Appwrite ব্যাকএন্ড (email/password session + email OTP token)
// ধাপ: Signup (account create) → Academic form → OTP → profile doc → pending
// ইমেইল OTP পাঠাতে Appwrite Console-এ SMTP (Resend) সেট করা থাকতে হবে;
// না থাকলে OTP screen-এ fallback আছে (password session)।
// ================================================================

@Composable
fun LoginScreen(onLogin: () -> Unit, onGoSignup: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(BuildConfig.APP_NAME, style = MaterialTheme.typography.headlineLarge)
        Text(BuildConfig.ORG_NAME, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))

        SectionCard(title = "লগইন করো") {
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("ইমেইল") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = pass, onValueChange = { pass = it },
                label = { Text("পাসওয়ার্ড") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    busy = true
                    error = null
                    scope.launch {
                        AuthRepo.login(email.trim(), pass).fold(
                            onSuccess = { busy = false; onLogin() },
                            onFailure = {
                                busy = false
                                error = AuthRepo.friendlyError(it)
                            },
                        )
                    }
                },
                enabled = email.isNotBlank() && pass.length >= 6 && !busy,
                modifier = Modifier.fillMaxWidth(),
            ) { if (busy) RpiLoader(size = 20.dp, color = MaterialTheme.colorScheme.onPrimary) else Text("লগইন") }
            Spacer(Modifier.height(8.dp))
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
            }
            OutlinedButton(onClick = onGoSignup, modifier = Modifier.fillMaxWidth()) {
                Text("নতুন অ্যাকাউন্ট খোলো")
            }
        }
    }
}

@Composable
fun SignupScreen(onNext: () -> Unit, onGoLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var pass2 by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("নতুন অ্যাকাউন্ট", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        SectionCard(title = "ধাপ ১ — ইমেইল দিয়ে শুরু") {
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("ইমেইল") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("পাসওয়ার্ড") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = pass2, onValueChange = { pass2 = it }, label = { Text("পাসওয়ার্ড আবার দাও") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    busy = true
                    error = null
                    scope.launch {
                        AuthRepo.signUp(email.trim(), pass, name = null).fold(
                            onSuccess = { userId ->
                                SignupState.userId = userId
                                SignupState.email = email.trim()
                                SignupState.password = pass
                                busy = false
                                onNext()
                            },
                            onFailure = {
                                busy = false
                                error = AuthRepo.friendlyError(it)
                            },
                        )
                    }
                },
                enabled = email.isNotBlank() && pass.length >= 6 && pass == pass2 && !busy,
                modifier = Modifier.fillMaxWidth(),
            ) { if (busy) RpiLoader(size = 20.dp, color = MaterialTheme.colorScheme.onPrimary) else Text("অ্যাকাউন্ট খোলো") }
            Spacer(Modifier.height(8.dp))
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
            }
            OutlinedButton(onClick = onGoLogin, modifier = Modifier.fillMaxWidth()) { Text("অ্যাকাউন্ট আছে? লগইন") }
        }
    }
}
