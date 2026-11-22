package org.discoursey.android.ui.screens.topic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AddReaction
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.discoursey.android.data.api.models.Post
import org.discoursey.android.data.api.models.TopicDetailResponse
import org.discoursey.android.ui.components.MarkdownText
import org.discoursey.android.ui.components.TimeUtils
import org.discoursey.android.ui.components.UserAvatar
import org.discoursey.android.ui.viewmodel.MainViewModel

// Standard Discourse Reactions
val DISCOURSE_EMOJIS = listOf(
    "heart" to "❤️",
    "+1" to "👍",
    "laughing" to "😂",
    "open_mouth" to "😮",
    "cry" to "😢",
    "rocket" to "🚀",
    "tada" to "🎉",
    "fire" to "🔥"
)

fun reactionNameToEmoji(name: String): String {
    return when (name) {
        "heart" -> "❤️"
        "+1" -> "👍"
        "laughing" -> "😂"
        "open_mouth" -> "😮"
        "cry" -> "😢"
        "rocket" -> "🚀"
        "tada" -> "🎉"
        "fire" -> "🔥"
        else -> "❤️"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDetailScreen(
    topicId: Long,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onReplyClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(topicId) {
        viewModel.loadTopicDetail(topicId)
    }

    val detail = uiState.currentTopicDetail

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = detail?.title ?: "Topic #$topicId",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 2
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadTopicDetail(topicId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onReplyClick(topicId) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Reply, contentDescription = "Reply")
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading && detail == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (detail != null) {
            val posts = detail.postStream?.posts ?: emptyList()
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Topic Title Banner (Full title always shown without truncation)
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = detail.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${detail.postsCount} posts",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Divider(color = MaterialTheme.colorScheme.outline)
                }

                items(posts, key = { it.id }) { post ->
                    PostCard(
                        post = post,
                        baseUrl = uiState.forumUrl,
                        nameDisplayPreference = uiState.nameDisplayPreference,
                        forumEnablesNames = uiState.forumEnablesNames,
                        onToggleReaction = { emojiName ->
                            viewModel.toggleReaction(post.id, emojiName)
                        },
                        onReplyClick = { onReplyClick(topicId) }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
fun PostCard(
    post: Post,
    baseUrl: String,
    nameDisplayPreference: String,
    forumEnablesNames: Boolean,
    onToggleReaction: (String) -> Unit,
    onReplyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showReactionPicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Author Info Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                user = org.discoursey.android.data.api.models.User(
                    id = post.userId,
                    username = post.username,
                    name = post.name,
                    avatarTemplate = post.avatarTemplate
                ),
                baseUrl = baseUrl,
                size = 40.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Primary display name according to settings
                val primaryName = post.getDisplayName(nameDisplayPreference, forumEnablesNames)
                val secondaryName = post.getSecondaryName(nameDisplayPreference, forumEnablesNames)

                Text(
                    text = primaryName,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                if (secondaryName != null) {
                    Text(
                        text = "$secondaryName • #${post.postNumber}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        text = "#${post.postNumber}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            // Timestamp according to spec
            val formattedTime = TimeUtils.formatDiscourseTime(post.createdAt)
            Text(
                text = formattedTime.ifBlank { "Recently" },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Cooked Markdown Body (with images and video stream rendering)
        MarkdownText(
            htmlContent = post.cooked,
            baseUrl = baseUrl
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Reactions Bar (Chips showing reactions)
        if (post.reactions.isNotEmpty() || post.likeCount > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // If standard likes exist without reactions plugin
                if (post.reactions.isEmpty() && post.likeCount > 0) {
                    Surface(
                        onClick = { onToggleReaction("heart") },
                        shape = RoundedCornerShape(14.dp),
                        color = if (post.isLiked) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (post.isLiked) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                 else null
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("❤️", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = post.likeCount.toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Custom Reactions from discourse-reactions plugin
                post.reactions.forEach { reaction ->
                    val isMyReaction = post.currentUserReaction?.id == reaction.id
                    val emojiChar = reactionNameToEmoji(reaction.id)
                    Surface(
                        onClick = { onToggleReaction(reaction.id) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isMyReaction) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isMyReaction) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                 else null
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(emojiChar, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = reaction.count.toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Emoji Reaction Picker Popup Row
        if (showReactionPicker) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 4.dp,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DISCOURSE_EMOJIS.forEach { (name, emoji) ->
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    onToggleReaction(name)
                                    showReactionPicker = false
                                }
                                .padding(6.dp)
                        ) {
                            Text(text = emoji, fontSize = 20.sp)
                        }
                    }
                }
            }
        }

        // Post Action Bar (Reaction button, Heart, Reply)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji Reaction Trigger Button
            IconButton(
                onClick = { showReactionPicker = !showReactionPicker },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddReaction,
                    contentDescription = "Add Reaction",
                    tint = if (showReactionPicker) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Standard Heart/Like Button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleReaction("heart") }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (post.isLiked) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                if (post.likeCount > 0) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.likeCount.toString(),
                        color = if (post.isLiked) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Reply Button
            IconButton(
                onClick = onReplyClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Reply,
                    contentDescription = "Reply to post",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}
