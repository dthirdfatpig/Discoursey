package org.discoursey.android.ui.screens.classic

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.discoursey.android.data.api.models.Topic
import org.discoursey.android.ui.components.TimeUtils
import org.discoursey.android.ui.theme.ClassicBackground
import org.discoursey.android.ui.theme.ClassicDivider
import org.discoursey.android.ui.theme.ClassicHeader

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ClassicTopicListScreen(
    topics: List<Topic>,
    isRefreshing: Boolean,
    hasNewTopics: Boolean,
    nameDisplayPreference: String,
    forumEnablesNames: Boolean,
    onRefresh: () -> Unit,
    onTopicClick: (Long) -> Unit,
    onHomeClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onOverflowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var filterText by remember { mutableStateOf("") }
    val filteredTopics = remember(topics, filterText) {
        if (filterText.isBlank()) topics
        else topics.filter { it.title.contains(filterText, ignoreCase = true) }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = onRefresh
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ClassicBackground)
    ) {
        // Classic Top Bar (matching screenshot)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(ClassicHeader)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onHomeClick) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Topics",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRefreshClick) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = Color.White
                )
            }
            IconButton(onClick = onOverflowClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = Color.White
                )
            }
        }

        // "Type to filter" search bar (matching screenshot)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ClassicBackground)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            OutlinedTextField(
                value = filterText,
                onValueChange = { filterText = it },
                placeholder = {
                    Text(
                        text = "Type to filter",
                        color = Color(0xFF757575),
                        fontSize = 14.sp
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF616161),
                    unfocusedBorderColor = Color(0xFF424242),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(4.dp)
            )
        }

        // Classic Topics Stream with Pull-to-Refresh
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ClassicBackground)
            ) {
                items(filteredTopics, key = { it.id }) { topic ->
                    ClassicTopicItem(
                        topic = topic,
                        nameDisplayPreference = nameDisplayPreference,
                        forumEnablesNames = forumEnablesNames,
                        onClick = { onTopicClick(topic.id) }
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(ClassicDivider)
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
                    .padding(top = 8.dp)
            ) {
                Surface(
                    onClick = onRefresh,
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF0284C7),
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "New topics available • Tap to update",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = Color(0xFF262626),
                contentColor = Color.White
            )
        }
    }
}

@Composable
fun ClassicTopicItem(
    topic: Topic,
    nameDisplayPreference: String = "AUTO",
    forumEnablesNames: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp) // Tight padding matching screenshot
    ) {
        // Full topic title without truncation
        Text(
            text = topic.title,
            color = Color(0xFFE0E0E0),
            fontSize = 14.5.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 19.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        // "Last by [User], [Date/Time]"
        val posterName = topic.resolvedLastPosterUser?.getDisplayName(
            nameDisplayPreference,
            forumEnablesNames
        ) ?: "Forum Member"
        val formattedTime = TimeUtils.formatDiscourseTime(topic.lastPostedAt)
        val timeDisplay = if (formattedTime.isNotBlank()) formattedTime else "Recent"

        Text(
            text = "Last by $posterName, $timeDisplay",
            color = Color(0xFF9E9E9E),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Comment bubble + posts count: e.g. 💬 29
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.ChatBubble,
                contentDescription = null,
                tint = Color(0xFF9E9E9E),
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = topic.postsCount.toString(),
                color = Color(0xFFBDBDBD),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
