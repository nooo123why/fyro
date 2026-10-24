package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.repository.VyroRepository
import com.example.data.util.VyroCoinsManager
import com.example.ui.theme.VyroAccentPink
import com.example.ui.theme.VyroElectricPurple
import com.example.ui.theme.VyroNeonCyan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    repository: VyroRepository,
    currentUsername: String,
    currentAvatarUrl: String,
    coinsManager: VyroCoinsManager? = null,
    onBack: () -> Unit = {},
    onPublishSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedMode by remember { mutableStateOf("Short") } // "Video", "Short", "Live"
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var mediaFileName by remember { mutableStateOf("My_Video.mp4") }
    var mediaFileSize by remember { mutableStateOf("24.5 MB") }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedVisibility by remember { mutableStateOf("Public") }
    var isVisibilityExpanded by remember { mutableStateOf(false) }

    var isPublishing by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }
    var uploadStep by remember { mutableIntStateOf(1) } // 1: Storage, 2: Database, 3: Feeds
    var publishError by remember { mutableStateOf<String?>(null) }

    // Media picker launcher
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val mime = context.contentResolver.getType(uri) ?: "video/mp4"
                    val ext = if (mime.contains("video")) "mp4" else "jpg"
                    val fileName = "vyro_${System.currentTimeMillis()}.$ext"
                    val cachedFile = File(context.cacheDir, fileName)
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        cachedFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    mediaFileName = fileName
                    val sizeMb = String.format("%.1f", cachedFile.length() / (1024.0 * 1024.0))
                    mediaFileSize = "$sizeMb MB • 1080p"
                    selectedMediaUri = Uri.fromFile(cachedFile)
                } catch (_: Exception) {
                    selectedMediaUri = uri
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Post", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("create_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D0F17))
            )
        },
        containerColor = Color(0xFF090A10)
    ) { padding ->
        if (isPublishing) {
            // Uploading Flow Screen (Exact Match to Showcase Reference #9)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Uploading... ${(uploadProgress * 100).toInt()}%",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Preview Thumbnail Box with progress overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF161926)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = selectedMediaUri ?: "https://picsum.photos/seed/vyro_preview/600/400",
                        contentDescription = "Video Thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${(uploadProgress * 100).toInt()}%",
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(mediaFileName, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 14.sp)
                Text(mediaFileSize, color = Color.Gray, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(20.dp))

                // Linear Progress bar
                LinearProgressIndicator(
                    progress = { uploadProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = VyroNeonCyan,
                    trackColor = Color.White.copy(alpha = 0.15f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Checklist indicators (Reference #9)
                UploadChecklistItem(title = "Uploading to Firebase Storage", isCompleted = uploadStep >= 1)
                Spacer(modifier = Modifier.height(10.dp))
                UploadChecklistItem(title = "Saving post to database", isCompleted = uploadStep >= 2)
                Spacer(modifier = Modifier.height(10.dp))
                UploadChecklistItem(title = "Updating feeds (real-time)", isCompleted = uploadStep >= 3)

                Spacer(modifier = Modifier.height(24.dp))

                Text("This may take a few seconds...", color = Color.Gray, fontSize = 12.sp)
            }
        } else {
            // Main Upload Setup Screen (Exact Match to Showcase Reference #4)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .testTag("create_screen_form")
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Segmented Pills: "Video", "Short", "Live" (Showcase reference #4)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF161926)
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Video", "Short", "Live").forEach { mode ->
                            val isSelected = selectedMode == mode
                            Button(
                                onClick = {
                                    selectedMode = mode
                                    if (mode == "Live") {
                                        Toast.makeText(context, "Live Streaming studio is in Creator Beta", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) VyroAccentPink else Color.Transparent,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(mode, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Upload Drop Zone (Showcase reference #4)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable {
                            mediaPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        }
                        .testTag("upload_drop_zone"),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF161926),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    if (selectedMediaUri != null) {
                        Box(contentAlignment = Alignment.Center) {
                            AsyncImage(
                                model = selectedMediaUri,
                                contentDescription = "Selected Video",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.75f),
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = VyroNeonCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Video Ready • Tap to change", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(VyroAccentPink.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = "Upload Video",
                                    tint = VyroAccentPink,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Tap to upload video", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("or drag and drop MP4 / WebM", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section: Add details
                Text("Add details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)

                Spacer(modifier = Modifier.height(12.dp))

                // Title
                Text("Title", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Enter a catchy title...", color = Color.Gray, fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("upload_title_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VyroNeonCyan,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedContainerColor = Color(0xFF161926),
                        unfocusedContainerColor = Color(0xFF161926),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Description
                Text("Description", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Tell viewers about your video...", color = Color.Gray, fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth().height(100.dp).testTag("upload_description_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VyroNeonCyan,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedContainerColor = Color(0xFF161926),
                        unfocusedContainerColor = Color(0xFF161926),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Visibility Dropdown
                Text("Visibility", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(6.dp))
                ExposedDropdownMenuBox(
                    expanded = isVisibilityExpanded,
                    onExpandedChange = { isVisibilityExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedVisibility,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isVisibilityExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VyroNeonCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedContainerColor = Color(0xFF161926),
                            unfocusedContainerColor = Color(0xFF161926),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = isVisibilityExpanded,
                        onDismissRequest = { isVisibilityExpanded = false },
                        modifier = Modifier.background(Color(0xFF161926))
                    ) {
                        listOf("Public", "Unlisted", "Private").forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item, color = Color.White) },
                                onClick = {
                                    selectedVisibility = item
                                    isVisibilityExpanded = false
                                }
                            )
                        }
                    }
                }

                if (publishError != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = publishError ?: "",
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Pink Upload Button (Showcase reference #4) — UNOBSTRUCTED & EASY TO TAP!
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            publishError = "Please enter a title for your video."
                            return@Button
                        }
                        publishError = null
                        isPublishing = true
                        uploadProgress = 0.1f
                        uploadStep = 1

                        coroutineScope.launch {
                            // Stage 1: Uploading media simulation
                            delay(400L)
                            uploadProgress = 0.45f
                            delay(400L)
                            uploadProgress = 0.72f
                            uploadStep = 2

                            // Stage 2: Firestore post creation
                            val format = if (selectedMode == "Video") "long" else "short"
                            val mediaUrl = selectedMediaUri?.toString()
                                ?: if (format == "short") "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
                                else "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"

                            val res = repository.createPost(
                                caption = "$title ${if (description.isNotBlank()) "— $description" else ""}",
                                mediaUrl = mediaUrl,
                                thumbnailUrl = mediaUrl,
                                audioTitle = "Original Sound - $currentUsername",
                                tags = listOf("vyro", selectedMode.lowercase(), "trending"),
                                authorUsername = currentUsername,
                                authorAvatarUrl = currentAvatarUrl,
                                videoFormat = format,
                                category = "Entertainment",
                                durationSeconds = if (format == "short") 20L else 180L
                            )

                            delay(300L)
                            uploadProgress = 1.0f
                            uploadStep = 3

                            // Award +50 Vyro Coins
                            coinsManager?.awardUploadReward()

                            delay(400L)
                            isPublishing = false
                            if (res.isSuccess) {
                                Toast.makeText(context, "🎉 Video published! Earned +50 Vyro Coins 🪙", Toast.LENGTH_LONG).show()
                                onPublishSuccess()
                            } else {
                                publishError = res.exceptionOrNull()?.localizedMessage ?: "Failed to upload video"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("upload_action_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VyroAccentPink, contentColor = Color.White)
                ) {
                    Text("Upload", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                // Generous bottom spacing
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun UploadChecklistItem(title: String, isCompleted: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(0.85f)) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (isCompleted) Color(0xFF10B981) else Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            color = if (isCompleted) Color.White else Color.Gray,
            fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
