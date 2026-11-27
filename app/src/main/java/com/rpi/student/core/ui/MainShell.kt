package com.rpi.student.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.rpi.student.core.navigation.Routes
import com.rpi.student.core.theme.GlassSurface

private data class Tab(val route: String, val label: String, val icon: ImageVector, val color: Color, val onColor: Color = Color.White)

private val tabs = listOf(
    Tab(Routes.HOME, "Home", Icons.Filled.Home, Color(0xFF6C4DFF)),
    Tab(Routes.ACADEMIC, "Academic", Icons.Filled.CalendarMonth, Color(0xFFFF6B6B)),
    Tab(Routes.CHAT_LIST, "Chat", Icons.Filled.Chat, Color(0xFF2EC4A6)),
    Tab(Routes.MESS, "Mess", Icons.Filled.RestaurantMenu, Color(0xFFFFB300), Color(0xFF1F1B3A)),
    Tab(Routes.PROFILE, "Profile", Icons.Filled.Person, Color(0xFF3DA5FF)),
)

/** Main tabs-এর শেল — glass bottom bar (Liquid Glass chrome) + content। */
@Composable
fun MainShell(
    currentRoute: String,
    onTab: (String) -> Unit,
    content: @Composable () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            GlassSurface(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(36.dp),
            ) {
                NavigationBar(containerColor = Color.Transparent, tonalElevation = 0.dp, windowInsets = WindowInsets(0, 0, 0, 0), modifier = Modifier.height(68.dp)) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = { onTab(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = tab.color,
                                selectedIconColor = tab.onColor,
                                selectedTextColor = tab.color,
                                unselectedIconColor = tab.color,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            content()
        }
    }
}
