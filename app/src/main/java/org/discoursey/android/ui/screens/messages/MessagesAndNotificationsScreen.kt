package org.discoursey.android.ui.screens.messages

import android.os.Build
import android.text.Html
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.discoursey.android.data.api.models.NotificationItem
import org.discoursey.android.data.api.models.Topic
import org.discoursey.android.ui.components.TimeUtils
import org.discoursey.android.ui.components.UserAvatar
import org.discoursey.android.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesAndNotificationsScreen(
    viewModel: MainViewModel,
    onTopicClick: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 = Private Messages, 1 = Notifications

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = if (selectedTab == 0) "Private Messages" else "Notifications",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                if (selectedTab == 1) {
                    TextButton(onClick = { viewModel.markNotificationsAsRead() }) {
                        Text("Mark all read", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        // Two Primary Tabs: Private Messages vs Notifications
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = { Divider(color = MaterialTheme.colorScheme.outline) }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Mail, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Private Messages", fontWeight = FontWeight.SemiBold)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BadgedBox(
                            badge = {
                                if (uiState.unreadNotificationCount > 0) {
                                    Badge { Text(uiState.unreadNotificationCount.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Notifications", fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }

        // Tab Content
        if (selectedTab == 0) {
            PrivateMessagesList(
                messages = uiState.privateMessages,
                baseUrl = uiState.forumUrl,
                nameDisplayPreference = uiState.nameDisplayPreference,
                forumEnablesNames = uiState.forumEnablesNames,
                onMessageClick = onTopicClick
            )
        } else {
            NotificationsList(
                notifications = uiState.notifications,
                baseUrl = uiState.forumUrl,
                onNotificationClick = { notif ->
                    notif.topicId?.let { onTopicClick(it) }
                }
            )
        }
    }
}

@Composable
fun PrivateMessagesList(
    messages: List<Topic>,
    baseUrl: String,
    nameDisplayPreference: String,
    forumEnablesNames: Boolean,
    onMessageClick: (Long) -> Unit
) {
    if (messages.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No private messages in inbox.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp
                )
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(messages, key = { it.id }) { msg ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMessageClick(msg.id) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        user = msg.resolvedLastPosterUser,
                        baseUrl = baseUrl,
                        size = 48.dp
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val author = msg.resolvedLastPosterUser?.getDisplayName(
                                nameDisplayPreference,
                                forumEnablesNames
                            ) ?: "User"
                            Text(
                                text = author,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = TimeUtils.formatDiscourseTime(msg.lastPostedAt),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = msg.title,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val cleanSnippet = remember(msg.excerpt) {
                            msg.excerpt?.takeIf { it.isNotBlank() }?.let { raw ->
                                val spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    Html.fromHtml(raw, Html.FROM_HTML_MODE_COMPACT)
                                } else {
                                    @Suppress("DEPRECATION")
                                    Html.fromHtml(raw)
                                }
                                spanned.toString().trim().replace("\n", " ")
                            }
                        }
                        if (!cleanSnippet.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = cleanSnippet,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Divider(color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
fun NotificationsList(
    notifications: List<NotificationItem>,
    baseUrl: String,
    onNotificationClick: (NotificationItem) -> Unit
) {
    if (notifications.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No notifications right now.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp
                )
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(notifications, key = { it.id }) { notif ->
                val isUnread = !notif.read
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNotificationClick(notif) }
                        .background(
                            if (isUnread) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            else Color.Transparent
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Type Icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (isUnread) MaterialTheme.colorScheme.primary else Color(0xFF334155),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when (notif.notificationType) {
                            1 -> Icons.Default.AlternateEmail
                            2 -> Icons.Default.Reply
                            5 -> Icons.Default.Favorite
                            else -> Icons.Default.Notifications
                        }
                        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        val actorName = notif.data?.displayUsername ?: notif.data?.username ?: "Someone"
                        val topicTitle = notif.data?.topicTitle ?: "Discussion"
                        val actionDesc = when (notif.notificationType) {
                            1 -> "mentioned you in"
                            2 -> "replied to"
                            5 -> "liked your post in"
                            else -> "sent notification for"
                        }

                        Text(
                            text = "$actorName $actionDesc $topicTitle",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 14.sp,
                            fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = TimeUtils.formatDiscourseTime(notif.createdAt),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }

                    if (isUnread) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }
                }
                Divider(color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}
