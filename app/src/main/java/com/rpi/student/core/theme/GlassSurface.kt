package com.rpi.student.core.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Pop surface (redesign) — solid, rounded, soft shadow. The name/API is kept from the
 * old Liquid Glass so every existing call site (top bar, tabs, composer) restyles automatically.
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
            .shadow(elevation = 8.dp, shape = shape, ambientColor = Color(0x261F1B3A), spotColor = Color(0x261F1B3A))
            .clip(shape)
            .background(fillOverride ?: g.fill),
        contentAlignment = contentAlignment,
        content = content,
    )
}
