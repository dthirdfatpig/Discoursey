package org.discoursey.android.ui.screens.modern

import android.os.Build
import android.text.Html
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.discoursey.android.data.api.models.Topic
import org.discoursey.android.ui.components.TimeUtils
import org.discoursey.android.ui.components.UserAvatar
import org.discoursey.android.ui.theme.ModernBackground
import org.discoursey.android.ui.theme.ModernDivider

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ModernTopicListScreen(
    topics: List<Topic>,
    baseUrl: String,
    isRefreshing: Boolean,
    hasNewTopics: Boolean,
    nameDisplayPreference: String,
    forumEnablesNames: Boolean,
    onRefresh: () -> Unit,
    onTopicClick: (Long) -> Unit,
    onNewTopicClick: () -> Unit,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = onRefresh
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ModernBackground)
    ) {
        // Modern Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(ModernBackground)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = "Topics",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onNewTopicClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Topic",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Pull to refresh & stream container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ModernBackground)
            ) {
                items(topics, key = { it.id }) { topic ->
                    ModernTopicItem(
                        topic = topic,
                        baseUrl = baseUrl,
                        nameDisplayPreference = nameDisplayPreference,
                        forumEnablesNames = forumEnablesNames,
                        onClick = { onTopicClick(topic.id) }
                    )
                    // Thin divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(ModernDivider)
                    )
                }
            }

            // New Topics Alert Banner (Requirement 2)
            androidx.compose.animation.AnimatedVisibility(
                visible = hasNewTopics,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
            ) {
                Surface(
                    onClick = onRefresh,
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "New topics available • Tap to update",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = Color(0xFF1E293B),
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ModernTopicItem(
    topic: Topic,
    baseUrl: String,
    nameDisplayPreference: String = "AUTO",
    forumEnablesNames: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // Full Topic Title (No ellipsis, full title always visible)
        Text(
            text = topic.title,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Status Line: e.g. "49 posts, Topic pinned and closed"
        val statusText = buildString {
            val formattedPosts = if (topic.postsCount >= 1000) {
                String.format("%,d", topic.postsCount)
            } else {
                topic.postsCount.toString()
            }
            append("$formattedPosts posts")
            if (topic.pinned && topic.closed) {
                append(", Topic pinned and closed")
            } else if (topic.pinned) {
                append(", Topic pinned")
            } else if (topic.closed) {
                append(", Topic closed")
            }
        }

        Text(
            text = statusText,
            color = Color(0xFF71717A), // Muted zinc
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Last Poster Card (Avatar + User Name + Snippet + Timestamp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Avatar
            UserAvatar(
                user = topic.resolvedLastPosterUser,
                baseUrl = baseUrl,
                size = 46.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Resolved user name according to user preference & forum settings
                val primaryName = topic.resolvedLastPosterUser?.getDisplayName(
                    nameDisplayPreference,
                    forumEnablesNames
                ) ?: "Forum Member"

                Text(
                    text = primaryName,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Clean message snippet (clean HTML, omit entirely if null/empty)
                val cleanSnippet = remember(topic.excerpt) {
                    topic.excerpt?.takeIf { it.isNotBlank() }?.let { raw ->
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
                        color = Color(0xFFA1A1AA),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Relative Timestamp according to spec
                val formattedTime = remember(topic.lastPostedAt) {
                    TimeUtils.formatDiscourseTime(topic.lastPostedAt)
                }
                Text(
                    text = formattedTime.ifBlank { "Recently" },
                    color = Color(0xFF71717A),
                    fontSize = 11.sp
                )
            }
        }
    }
}
