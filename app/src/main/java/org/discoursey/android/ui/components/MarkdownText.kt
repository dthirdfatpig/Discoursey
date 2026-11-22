package org.discoursey.android.ui.components

import android.os.Build
import android.text.Html
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage

@Composable
fun MarkdownText(
    htmlContent: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    baseUrl: String = ""
) {
    // Check for Discourse Video Stream BBCode tag: [video-stream id="..."]
    val hasVideoStream = htmlContent.contains("[video-stream")

    // Extract image URLs from HTML <img> tags or Discourse upload markup
    val imageUrls = remember(htmlContent) {
        val list = mutableListOf<String>()
        val imgTagRegex = Regex("""<img[^>]+src=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        imgTagRegex.findAll(htmlContent).forEach { match ->
            var src = match.groupValues[1]
            if (src.startsWith("//")) {
                src = "https:$src"
            } else if (src.startsWith("/") && baseUrl.isNotBlank()) {
                val cleanBase = baseUrl.trimEnd('/')
                src = "$cleanBase$src"
            }
            if (!src.contains("emoji", ignoreCase = true) && !src.contains("/images/emoji/")) {
                list.add(src)
            }
        }
        list
    }

    Column(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                TextView(context).apply {
                    setTextColor(textColor.toArgb())
                    textSize = 15f
                    setLineSpacing(6f, 1.15f)
                    linksClickable = true
                }
            },
            update = { textView ->
                val cleanedHtml = htmlContent
                    .replace(Regex("\\[video-stream[^\\]]*\\]"), "")
                    // Remove <img> tags from text view since they are rendered natively with Coil below
                    .replace(Regex("""<img[^>]+>"""), "")
                val spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(cleanedHtml, Html.FROM_HTML_MODE_COMPACT)
                } else {
                    @Suppress("DEPRECATION")
                    Html.fromHtml(cleanedHtml)
                }
                textView.text = spanned
            }
        )

        // Render images attached in post
        if (imageUrls.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            imageUrls.forEach { imgUrl ->
                AsyncImage(
                    model = imgUrl,
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (hasVideoStream) {
            Spacer(modifier = Modifier.height(10.dp))
            VideoStreamPlayerBadge()
        }
    }
}

@Composable
fun VideoStreamPlayerBadge(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(Color(0xFF0F172A), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.PlayCircle,
                contentDescription = "Video Stream",
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(52.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Adaptive Stream Ready (Discourse Video Stream)",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )
        }
    }
}
