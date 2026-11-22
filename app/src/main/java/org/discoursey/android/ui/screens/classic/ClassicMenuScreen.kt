package org.discoursey.android.ui.screens.classic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.discoursey.android.data.api.models.UserProfile
import org.discoursey.android.ui.theme.ClassicAccentPink
import org.discoursey.android.ui.theme.ClassicBackground
import org.discoursey.android.ui.theme.ClassicDivider
import org.discoursey.android.ui.theme.ClassicHeader

@Composable
fun ClassicMenuScreen(
    userProfile: UserProfile?,
    currentTab: String,
    onTabSelect: (String) -> Unit,
    onNavigate: (String) -> Unit,
    onRefresh: () -> Unit,
    onToggleNightMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTopTab by remember { mutableStateOf(0) } // 0 = Profile, 1 = Messages, 2 = News/Topics

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ClassicBackground)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(ClassicHeader)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Discoursey",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
            }
            IconButton(onClick = { onNavigate("search") }) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
            }
            IconButton(onClick = { onNavigate("settings") }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
            }
        }

        // Sub-tabs: PROFILE, MESSAGES, TOPICS (matching screenshot 3)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(ClassicHeader)
        ) {
            val tabs = listOf("PROFILE", "MESSAGES", "TOPICS")
            tabs.forEachIndexed { index, tabName ->
                val isSelected = selectedTopTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            selectedTopTab = index
                            if (index == 1) onNavigate("messages")
                            if (index == 2) onNavigate("topics")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Text(
                            text = tabName,
                            color = if (isSelected) Color.White else Color(0xFF9E9E9E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .height(3.dp)
                                    .background(ClassicAccentPink)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(3.dp))
                        }
                    }
                }
            }
        }

        // Main Profile Header (matching screenshot 3)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = userProfile?.name ?: userProfile?.username ?: "Discourse User",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            // Night mode toggle icon
            IconButton(onClick = onToggleNightMode) {
                Icon(
                    imageVector = Icons.Default.Brightness4,
                    contentDescription = "Toggle Night Mode",
                    tint = Color(0xFFBDBDBD),
                    modifier = Modifier.size(22.dp)
                )
            }

            // Connection/device status icon
            Icon(
                imageVector = Icons.Default.Smartphone,
                contentDescription = "Mobile Connected",
                tint = Color(0xFFBDBDBD),
                modifier = Modifier.size(22.dp)
            )
        }

        Divider(color = ClassicDivider, thickness = 1.dp)

        // Menu Options matching Discourse capabilities
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            ClassicMenuItem(
                icon = Icons.Default.Bookmark,
                label = "Bookmarks",
                onClick = { onNavigate("bookmarks") }
            )

            ClassicMenuItem(
                icon = Icons.Default.Chat,
                label = "Replies",
                onClick = { onNavigate("replies") }
            )

            ClassicMenuItem(
                icon = Icons.Default.Forum,
                label = "Topics",
                onClick = { onNavigate("topics") }
            )

            // Integrated with Discourse Image Album plugin!
            ClassicMenuItem(
                icon = Icons.Default.PhotoLibrary,
                label = "Photos",
                onClick = { onNavigate("photos") }
            )

            ClassicMenuItem(
                icon = Icons.Default.Notifications,
                label = "Notifications",
                onClick = { onNavigate("notifications") }
            )

            ClassicMenuItem(
                icon = Icons.Default.AccountCircle,
                label = "Full Profile",
                onClick = { onNavigate("full_profile") }
            )

            ClassicMenuItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                onClick = { onNavigate("settings") }
            )
        }
    }
}

@Composable
fun ClassicMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFFE0E0E0),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(22.dp))
        Text(
            text = label,
            color = Color(0xFFEEEEEE),
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
