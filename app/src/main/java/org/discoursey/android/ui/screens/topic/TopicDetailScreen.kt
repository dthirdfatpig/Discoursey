package org.discoursey.android.ui.screens.topic

import android.os.Build
import android.text.Html
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.discoursey.android.data.api.models.Poll
import org.discoursey.android.data.api.models.PollOption
import org.discoursey.android.data.api.models.Post
import org.discoursey.android.data.api.models.TopicDetailResponse
import org.discoursey.android.ui.components.MarkdownText
import org.discoursey.android.ui.components.TimeUtils
import org.discoursey.android.ui.components.UserAvatar
import org.discoursey.android.ui.viewmodel.MainViewModel

// Official default reactions enabled by the Discourse Reactions plugin (+1|laughing|open_mouth|clap|confetti_ball|hugs and core heart)
val DISCOURSE_REACTIONS_DEFAULT = listOf(
    "heart" to "❤️",
    "+1" to "👍",
    "laughing" to "😆",
    "open_mouth" to "😮",
    "clap" to "👏",
    "confetti_ball" to "🎊",
    "hugs" to "🫂"
)

fun reactionNameToEmoji(name: String): String {
    return when (name) {
        "heart" -> "❤️"
        "+1" -> "👍"
        "laughing" -> "😆"
        "open_mouth" -> "😮"
        "clap" -> "👏"
        "confetti_ball" -> "🎊"
        "hugs" -> "🫂"
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

    DisposableEffect(topicId) {
        onDispose {
            viewModel.clearTopicDetail()
        }
    }

    val detail = uiState.currentTopicDetail
    val isShowingCurrentTopic = detail != null && detail.id == topicId

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isShowingCurrentTopic) detail!!.title else "Loading topic...",
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
        if (!isShowingCurrentTopic || uiState.isLoading) {
            // Clean loader without flashing the previous topic
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            val posts = detail!!.postStream?.posts ?: emptyList()
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Topic Title Banner (Full title without truncation + Topic Voting)
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = detail.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
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

                            // Topic Voting Feature (if enabled on forum/category)
                            if (detail.canVote == true || detail.voteCount != null) {
                                val isVoted = detail.userVoted == true
                                val voteCount = detail.voteCount ?: 0
                                Surface(
                                    onClick = { viewModel.toggleTopicVote(topicId) },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isVoted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isVoted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isVoted) Icons.Default.Check else Icons.Default.ArrowUpward,
                                            contentDescription = "Vote",
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isVoted) "Voted ($voteCount)" else "Vote ($voteCount)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
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
                        onVotePoll = { pollName, optionId ->
                            viewModel.votePoll(post.id, pollName, optionId)
                        },
                        onReplyClick = { onReplyClick(topicId) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
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
    onVotePoll: (String, String) -> Unit,
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

            val formattedTime = TimeUtils.formatDiscourseTime(post.createdAt)
            Text(
                text = formattedTime.ifBlank { "Recently" },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Cooked Markdown Body (without raw filenames or raw poll markup)
        MarkdownText(
            htmlContent = post.cooked,
            baseUrl = baseUrl
        )

        // Native Interactive Discourse Polls Rendering
        if (post.polls.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            post.polls.forEach { poll ->
                PollCard(
                    poll = poll,
                    userVotedOptions = post.pollsVotes[poll.name] ?: emptyList(),
                    onOptionClick = { optionId ->
                        onVotePoll(poll.name, optionId)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Reactions Bar (Chips showing reactions)
        if (post.reactions.isNotEmpty() || post.likeCount > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Core Like / Heart chip (when no reactions plugin or heart like)
                val isHeartLiked = post.isLiked || post.currentUserReaction?.id == "heart"
                val heartReaction = post.reactions.firstOrNull { it.id == "heart" }
                val displayHeartCount = heartReaction?.count ?: post.likeCount

                if (displayHeartCount > 0 || isHeartLiked) {
                    Surface(
                        onClick = { onToggleReaction("heart") },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isHeartLiked) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isHeartLiked) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                 else null
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("❤️", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = maxOf(1, displayHeartCount).toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isHeartLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Custom Reactions from discourse-reactions plugin (excluding heart which is rendered above)
                post.reactions.filter { it.id != "heart" }.forEach { reaction ->
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
                                color = if (isMyReaction) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Default Discourse Reactions Picker Popup Row
        AnimatedVisibility(visible = showReactionPicker) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 6.dp,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DISCOURSE_REACTIONS_DEFAULT.forEach { (name, emoji) ->
                        val isSelected = post.currentUserReaction?.id == name || (name == "heart" && post.isLiked && post.currentUserReaction == null)
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.Transparent)
                                .clickable {
                                    onToggleReaction(name)
                                    showReactionPicker = false
                                }
                                .padding(8.dp)
                        ) {
                            Text(text = emoji, fontSize = 20.sp)
                        }
                    }
                }
            }
        }

        // Post Action Bar (Reaction trigger, Heart like, Reply)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reaction Picker Trigger
            IconButton(
                onClick = { showReactionPicker = !showReactionPicker },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddReaction,
                    contentDescription = "Reactions",
                    tint = if (showReactionPicker) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Like / Heart Toggle
            val isHeartLiked = post.isLiked || post.currentUserReaction?.id == "heart"
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleReaction("heart") }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isHeartLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isHeartLiked) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                if (post.likeCount > 0) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.likeCount.toString(),
                        color = if (isHeartLiked) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
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
                    contentDescription = "Reply",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@Composable
fun PollCard(
    poll: Poll,
    userVotedOptions: List<String>,
    onOptionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalVotes = poll.options.sumOf { it.votes }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = poll.title ?: "Poll",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (poll.status == "open") Color(0xFF10B981).copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (poll.status == "open") "Open" else "Closed",
                        color = if (poll.status == "open") Color(0xFF10B981) else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Poll Options with vote counts, percentages, and progress bars
            poll.options.forEach { option ->
                val isSelected = userVotedOptions.contains(option.id)
                val percentage = if (totalVotes > 0) (option.votes.toFloat() / totalVotes) else 0f
                val percentString = (percentage * 100).toInt()

                val cleanOptionLabel = remember(option.html) {
                    val spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Html.fromHtml(option.html, Html.FROM_HTML_MODE_COMPACT)
                    } else {
                        @Suppress("DEPRECATION")
                        Html.fromHtml(option.html)
                    }
                    spanned.toString().trim()
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = poll.status == "open") {
                            onOptionClick(option.id)
                        }
                        .padding(vertical = 6.dp, horizontal = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (poll.type == "multiple") {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { onOptionClick(option.id) },
                                    enabled = poll.status == "open",
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onOptionClick(option.id) },
                                    enabled = poll.status == "open",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = cleanOptionLabel,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "${option.votes} ($percentString%)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val animatedProgress by animateFloatAsState(targetValue = percentage, label = "poll_progress")
                    LinearProgressIndicator(
                        progress = animatedProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF64748B),
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${poll.voters} total voters • $totalVotes total votes",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}
