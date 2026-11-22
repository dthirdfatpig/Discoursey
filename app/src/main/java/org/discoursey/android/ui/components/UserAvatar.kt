package org.discoursey.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.discoursey.android.data.api.models.User
import kotlin.math.abs

@Composable
fun UserAvatar(
    user: User?,
    baseUrl: String,
    size: Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    val avatarUrl = user?.getAvatarUrl(baseUrl, (size.value * 2).toInt())
    val name = user?.displayName ?: "?"
    val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    // Generate consistent avatar color based on username
    val colors = listOf(
        Color(0xFFE11D48), // Rose
        Color(0xFF7C3AED), // Violet
        Color(0xFF2563EB), // Blue
        Color(0xFF0D9488), // Teal
        Color(0xFFEA580C), // Orange
        Color(0xFF0284C7), // Sky
        Color(0xFF9333EA)  // Purple
    )
    val colorIndex = abs(name.hashCode()) % colors.size
    val fallbackColor = colors[colorIndex]

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(fallbackColor),
        contentAlignment = Alignment.Center
    ) {
        if (!avatarUrl.isNullOrBlank() && (avatarUrl.startsWith("http://") || avatarUrl.startsWith("https://"))) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = name,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = initial,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.42).sp
            )
        }
    }
}
