package com.rpi.student.core.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Liquid Glass surface — app-wide glass container.
 *
 * v1 slice: self-contained implementation (translucent fill + specular border +
 * soft shadow). API 31+ এ পূর্ণ refraction/blur Kyant0 backdrop দিয়ে পরের
 * slice-এ এই ফাইলের ভেতরেই বসবে — বাকি app code বদলাবে না।
 * Glass শুধু chrome-এ (top bar, bottom tabs, FAB, composer) — blueprint §12 perf rule।
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(LocalRpiColors.current.glass.cornerRadius.dp),
    fillOverride: Color? = null,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit,
) {
    val g = LocalRpiColors.current.glass
    Box(
        modifier = modifier
            .shadow(elevation = 6.dp, shape = shape, ambientColor = Color(0x22000000), spotColor = Color(0x22000000))
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        g.highlight.copy(alpha = if (isFullGlassSupported) 0.25f else 0.12f),
                        (fillOverride ?: g.fill),
                    )
                )
            )
            .border(width = 1.dp, brush = Brush.verticalGradient(listOf(g.border, g.highlight)), shape = shape),
        contentAlignment = contentAlignment,
        content = content,
    )
}
