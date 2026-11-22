package org.discoursey.android.ui.screens.modern

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.discoursey.android.ui.theme.ModernBackground
import org.discoursey.android.ui.theme.ModernDivider
import org.discoursey.android.ui.theme.ModernNotificationRed

@Composable
fun ModernBottomBar(
    slots: List<String>,
    currentTab: String,
    onTabSelected: (String) -> Unit,
    unreadMessagesCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ModernBackground)
    ) {
        // Subtle top divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(ModernDivider)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            slots.take(5).forEach { slotKey ->
                val isSelected = currentTab == slotKey
                val icon = getSlotIcon(slotKey)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onTabSelected(slotKey) },
                    contentAlignment = Alignment.Center
                ) {
                    if (slotKey == "messages" && unreadMessagesCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = ModernNotificationRed,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = if (unreadMessagesCount > 99) "99+" else unreadMessagesCount.toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = slotKey,
                                tint = if (isSelected) Color.White else Color(0xFF8E8E93),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    } else if (slotKey == "menu") {
                        // Tiny dot indicator for menu
                        BadgedBox(
                            badge = {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(ModernNotificationRed, CircleShape)
                                )
                            }
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = slotKey,
                                tint = if (isSelected) Color.White else Color(0xFF8E8E93),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = slotKey,
                            tint = if (isSelected) Color.White else Color(0xFF8E8E93),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}

fun getSlotIcon(key: String): ImageVector {
    return when (key.lowercase()) {
        "home" -> Icons.Default.Home
        "search" -> Icons.Default.Search
        "messages" -> Icons.Default.ChatBubble
        "profile" -> Icons.Default.SentimentSatisfiedAlt
        "menu" -> Icons.Default.Menu
        "categories" -> Icons.Default.GridView
        "latest" -> Icons.Default.Whatshot
        "bookmarks" -> Icons.Default.Bookmark
        else -> Icons.Default.Apps
    }
}
