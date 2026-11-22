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
    val hasVideoStream = htmlContent.contains("[video-stream", ignoreCase = true)

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
        list.distinct()
    }

    // Clean HTML for the TextView by stripping out image metadata, filenames, poll wrappers, and video tags
    val cleanedHtml = remember(htmlContent) {
        htmlContent
            // Strip lightbox metadata that displays file names & image dimensions
            .replace(Regex("""<div class=["']meta["'][\s\S]*?</div>""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""<span class=["']filename["'][\s\S]*?</span>""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""<span class=["']informations["'][\s\S]*?</span>""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""<svg[\s\S]*?</svg>""", RegexOption.IGNORE_CASE), "")
            // Strip polls from raw text since they are rendered as native interactive Compose poll widgets
            .replace(Regex("""<div class=["']poll["'][\s\S]*?</div>""", RegexOption.IGNORE_CASE), "")
            // Strip video stream markup from text view
            .replace(Regex("""\[video-stream[^\]]*\][\s\S]*?\[/video-stream\]""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\[video-stream[^\]]*\]""", RegexOption.IGNORE_CASE), "")
            // Remove <img> tags since they are rendered natively with Coil below
            .replace(Regex("""<img[^>]+>""", RegexOption.IGNORE_CASE), "")
            .trim()
    }

    Column(modifier = modifier) {
        if (cleanedHtml.isNotBlank()) {
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
                    val spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Html.fromHtml(cleanedHtml, Html.FROM_HTML_MODE_COMPACT)
                    } else {
                        @Suppress("DEPRECATION")
                        Html.fromHtml(cleanedHtml)
                    }
                    textView.text = spanned
                }
            )
        }

        // Render images attached in post without file names
        if (imageUrls.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            imageUrls.forEach { imgUrl ->
                AsyncImage(
                    model = imgUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp)
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
