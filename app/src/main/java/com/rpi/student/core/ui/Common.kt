package com.rpi.student.core.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rpi.student.core.theme.LocalRpiColors
import kotlin.math.min

// ---------- Shared chrome pieces ----------

@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            trailing?.invoke()
        }
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Text(
        message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth().padding(vertical = 24.dp),
    )
}

/**
 * Exam focus banner — exam-routine অনুযায়ী app behavior change-এর v1 রূপ
 * (blueprint §13: v1 = banner/notification level; Phase 2 = deep focus mode)।
 */
@Composable
fun FocusBanner(daysLeft: Int, examName: String, modifier: Modifier = Modifier) {
    val c = LocalRpiColors.current
    val urgent = daysLeft <= 1
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(if (urgent) c.alert else c.accent, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(if (urgent) "\uD83D\uDCCD" else "\u23F3", color = Color.White) // 📍 / ⏳
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                if (daysLeft <= 0) "আজ $examName — ফোকাস মোড!" else "$examName — $daysLeft দিন বাকি",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
            Text("Best of luck, ফোকাস রাখো", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
        }
    }
}

// ---------- RpiLoader — logo-loader.svg এর হুবহু Compose রূপ ----------
// ৩টা nested U-arc, dashed stroke দৌড়ায় (2.8s loop, staggered 180ms),
// traditional spinner এর বদলে সব লোডিং স্ক্রিনে এটাই দেখাবে (user lock)।

private fun uPath(topY: Float, radius: Float): Path = Path().apply {
    val cx = 128f
    moveTo(cx - radius, topY)
    lineTo(cx - radius, 128f)
    arcTo(
        rect = androidx.compose.ui.geometry.Rect(
            left = cx - radius, top = 128f - radius, right = cx + radius, bottom = 128f + radius
        ),
        startAngleDegrees = 180f, sweepAngleDegrees = 180f, forceMoveTo = false,
    )
    lineTo(cx + radius, topY)
}

@Composable
fun RpiLoader(size: Dp = 96.dp, color: Color = Color(0xFF000000)) {
    val transition = rememberInfiniteTransition(label = "rpi_loader")
    // SVG keyframes: 0%→1.1, 40/60%→0, 100%→-1.1 (pathLength=1), cubic-bezier(.65,0,.35,1)≈FastOutSlowIn
    val phases = listOf(0, 180, 360).map { offset ->
        transition.animateFloat(
            initialValue = 1.1f,
            targetValue = -1.1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 2800
                    1.1f at 0
                    0f at 1120 using androidx.compose.animation.core.FastOutSlowInEasing
                    0f at 1680
                    -1.1f at 2800 using androidx.compose.animation.core.FastOutSlowInEasing
                },
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(offset),
            ),
            label = "p$offset",
        )
    }
    val stroke = 20f
    Canvas(Modifier.size(size)) {
        val scale = min(this.size.width, this.size.height) / 256f
        val radii = listOf(84f, 56f, 28f)
        val tops = listOf(40f, 72f, 104f)
        for (i in 0..2) {
            val path = uPath(tops[i], radii[i])
            val pm = PathMeasure()
            pm.setPath(path, false)
            val len = pm.length
            val dash = len / 4f           // dasharray:1 3 (pathLength=1)
            val phase = phases[i].value * len
            drawPath(
                path = path,
                color = color,
                style = Stroke(
                    width = stroke * scale,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash * 3f), phase),
                ),
            )
        }
    }
}
