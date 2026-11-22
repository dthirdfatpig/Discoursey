package org.discoursey.android.ui.screens.composer

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.discoursey.android.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposerScreen(
    topicId: Long?, // null if creating a new topic
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onOpenPhotoAlbum: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf(uiState.categories.firstOrNull()?.id) }
    var isPosting by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadStatusMessage by remember { mutableStateOf<String?>(null) }

    // Activity result launcher for picking images
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploading = true
            uploadStatusMessage = "Uploading image to Discourse..."
            viewModel.uploadAttachment(
                context = context,
                uri = uri,
                isVideo = false
            ) { markdown, error ->
                isUploading = false
                if (markdown != null) {
                    content = if (content.isBlank()) markdown else "$content\n$markdown\n"
                    uploadStatusMessage = null
                    Toast.makeText(context, "Image uploaded and attached!", Toast.LENGTH_SHORT).show()
                } else {
                    uploadStatusMessage = null
                    Toast.makeText(context, error ?: "Image upload failed.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Activity result launcher for picking videos (handles stream plugin or direct upload)
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploading = true
            uploadStatusMessage = "Processing and uploading video..."
            viewModel.uploadAttachment(
                context = context,
                uri = uri,
                isVideo = true
            ) { markdown, error ->
                isUploading = false
                if (markdown != null) {
                    content = if (content.isBlank()) markdown else "$content\n$markdown\n"
                    uploadStatusMessage = null
                    Toast.makeText(context, "Video uploaded and attached!", Toast.LENGTH_SHORT).show()
                } else {
                    uploadStatusMessage = null
                    Toast.makeText(context, error ?: "Video upload failed.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (topicId == null) "New Topic" else "Reply to Topic",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (content.isBlank()) return@Button
                            isPosting = true
                            if (topicId == null) {
                                viewModel.createTopic(title, content, selectedCategoryId) { success ->
                                    isPosting = false
                                    if (success) onBack()
                                }
                            } else {
                                viewModel.replyToTopic(topicId, content) { success ->
                                    isPosting = false
                                    if (success) onBack()
                                }
                            }
                        },
                        enabled = !isPosting && !isUploading && content.isNotBlank() && (topicId != null || title.isNotBlank()),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isPosting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Post", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Uploading progress banner
            if (isUploading) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = uploadStatusMessage ?: "Uploading attachment...",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // New Topic Title & Category
            if (topicId == null) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Topic Title") },
                    placeholder = { Text("What is this discussion about?") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category selector chips
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryId == cat.id,
                            onClick = { selectedCategoryId = cat.id },
                            label = { Text(cat.name) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Formatting & Attachment Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Direct Image Upload
                ToolbarButton(Icons.Default.AddPhotoAlternate, "Upload Image") {
                    imagePickerLauncher.launch("image/*")
                }

                // Direct Video / Video Stream Upload
                ToolbarButton(Icons.Default.VideoCall, "Upload Video") {
                    videoPickerLauncher.launch("video/*")
                }

                // Discourse Image Album Integration
                ToolbarButton(Icons.Default.PhotoLibrary, "Photo Album") {
                    onOpenPhotoAlbum()
                }

                Spacer(modifier = Modifier.width(6.dp))

                ToolbarButton(Icons.Default.FormatBold, "Bold") { content += "**bold**" }
                ToolbarButton(Icons.Default.FormatItalic, "Italic") { content += "*italic*" }
                ToolbarButton(Icons.Default.FormatQuote, "Quote") { content += "\n> Quote\n" }
                ToolbarButton(Icons.Default.FormatListBulleted, "List") { content += "\n- Item\n" }
                ToolbarButton(Icons.Default.Link, "Link") { content += "[Link text](https://...)" }
                ToolbarButton(Icons.Default.Code, "Code") { content += " `code` " }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Message Composer Text Area
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = {
                    Text(
                        if (topicId == null) "Type your post content here... Markdown and attachments are supported."
                        else "Write your reply... (Markdown, images, videos, and albums supported)"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(10.dp)
            )
        }
    }
}

@Composable
fun ToolbarButton(
    icon: ImageVector,
    desc: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = desc,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
