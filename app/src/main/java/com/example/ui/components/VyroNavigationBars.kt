package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.BottomTab
import com.example.ui.theme.VyroElectricPurple
import com.example.ui.theme.VyroNeonCyan

@Composable
fun VyroBottomBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    unreadMessagesCount: Int = 0
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vyro_bottom_bar")
    ) {
        // Home (YouTube Long-form Feed)
        NavigationBarItem(
            selected = selectedTab == BottomTab.HOME,
            onClick = { onTabSelected(BottomTab.HOME) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == BottomTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home",
                    tint = if (selectedTab == BottomTab.HOME) VyroNeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            label = { Text("Home", fontSize = 11.sp, fontWeight = if (selectedTab == BottomTab.HOME) FontWeight.Bold else FontWeight.Normal) },
            modifier = Modifier.testTag("tab_home")
        )

        // Shorts (TikTok-style Fullscreen Feed)
        NavigationBarItem(
            selected = selectedTab == BottomTab.SHORTS,
            onClick = { onTabSelected(BottomTab.SHORTS) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == BottomTab.SHORTS) Icons.Filled.Bolt else Icons.Outlined.Bolt,
                    contentDescription = "Shorts",
                    tint = if (selectedTab == BottomTab.SHORTS) VyroNeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            label = { Text("Shorts", fontSize = 11.sp, fontWeight = if (selectedTab == BottomTab.SHORTS) FontWeight.Bold else FontWeight.Normal) },
            modifier = Modifier.testTag("tab_shorts")
        )

        // Create Button (Prominent Center Button)
        NavigationBarItem(
            selected = selectedTab == BottomTab.CREATE,
            onClick = { onTabSelected(BottomTab.CREATE) },
            icon = {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(listOf(VyroElectricPurple, VyroNeonCyan))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Video",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            },
            label = {},
            modifier = Modifier.testTag("tab_create")
        )

        // Subscriptions
        NavigationBarItem(
            selected = selectedTab == BottomTab.SUBSCRIPTIONS,
            onClick = { onTabSelected(BottomTab.SUBSCRIPTIONS) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == BottomTab.SUBSCRIPTIONS) Icons.Filled.Subscriptions else Icons.Outlined.Subscriptions,
                    contentDescription = "Subscriptions",
                    tint = if (selectedTab == BottomTab.SUBSCRIPTIONS) VyroNeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            label = { Text("Subscriptions", fontSize = 10.sp, fontWeight = if (selectedTab == BottomTab.SUBSCRIPTIONS) FontWeight.Bold else FontWeight.Normal) },
            modifier = Modifier.testTag("tab_subscriptions")
        )

        // Profile
        NavigationBarItem(
            selected = selectedTab == BottomTab.PROFILE,
            onClick = { onTabSelected(BottomTab.PROFILE) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == BottomTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Profile",
                    tint = if (selectedTab == BottomTab.PROFILE) VyroNeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            label = { Text("Profile", fontSize = 11.sp, fontWeight = if (selectedTab == BottomTab.PROFILE) FontWeight.Bold else FontWeight.Normal) },
            modifier = Modifier.testTag("tab_profile")
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VyroTopBar(
    title: String = "VYRO",
    isGuestMode: Boolean = false,
    guestRemainingSeconds: Long = 0L,
    onGuestUpgradeClick: () -> Unit = {},
    showNotificationAction: Boolean = true,
    showSettingsAction: Boolean = true,
    onNotificationsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    navigationIcon: (@Composable () -> Unit)? = null
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (isGuestMode) {
                    Spacer(modifier = Modifier.width(8.dp))
                    val mins = guestRemainingSeconds / 60
                    val secs = guestRemainingSeconds % 60
                    val formattedTime = String.format("%02d:%02d", mins, secs)
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onGuestUpgradeClick() }
                            .testTag("topbar_guest_pill"),
                        color = VyroNeonCyan.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VyroNeonCyan)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⏱️ $formattedTime",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = VyroNeonCyan
                            )
                        }
                    }
                }
            }
        },
        navigationIcon = { navigationIcon?.invoke() },
        actions = {
            if (showNotificationAction) {
                IconButton(
                    onClick = onNotificationsClick,
                    modifier = Modifier.testTag("action_notifications")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            if (showSettingsAction) {
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.testTag("action_settings")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}
