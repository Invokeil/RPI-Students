package com.rpi.student.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rpi.student.core.theme.GlassSurface
import com.rpi.student.core.theme.isFullGlassSupported

/**
 * সাব-স্ক্রিনের স্ট্যান্ডার্ড শেল — glass top bar + back।
 * Glass শুধু chrome-এ (perf rule, blueprint §12)।
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerBottom(28.dp),
            ) {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    ),
                )
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            content(Modifier)
        }
    }
}

private fun RoundedCornerBottom(dp: androidx.compose.ui.unit.Dp) =
    androidx.compose.foundation.shape.RoundedCornerShape(bottomStart = dp, bottomEnd = dp)
