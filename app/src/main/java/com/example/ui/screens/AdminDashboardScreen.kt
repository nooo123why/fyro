package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReportItem
import com.example.data.repository.VyroRepository
import com.example.ui.theme.VyroNeonCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    repository: VyroRepository,
    onBack: () -> Unit
) {
    var reports by remember {
        mutableStateOf(
            listOf(
                ReportItem(
                    id = "rep_1",
                    reporterId = "user_456",
                    targetType = "post",
                    targetId = "sample_1",
                    reason = "Copyright infringement / Uncredited audio",
                    description = "Music in the video matches protected track",
                    status = "pending"
                ),
                ReportItem(
                    id = "rep_2",
                    reporterId = "user_789",
                    targetType = "comment",
                    targetId = "comment_1",
                    reason = "Spam / Promotion link",
                    description = "Repeated bot comments",
                    status = "pending"
                )
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Moderation Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .testTag("admin_dashboard_screen")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(VyroNeonCyan.copy(alpha = 0.15f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = VyroNeonCyan)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Authorized Moderator Console • Zero-Tolerance Safety Active",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = VyroNeonCyan
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Pending Content Reports (${reports.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (reports.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("All reports have been reviewed. Queue is clear!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(reports) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Target: ${item.targetType.uppercase()}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VyroNeonCyan
                                    )
                                    Text(
                                        text = item.status.uppercase(),
                                        fontSize = 11.sp,
                                        color = Color(0xFFF59E0B),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = item.reason,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (item.description.isNotBlank()) {
                                    Text(
                                        text = item.description,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            reports = reports.filter { it.id != item.id }
                                        }
                                    ) {
                                        Text("Dismiss")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            reports = reports.filter { it.id != item.id }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                                    ) {
                                        Text("Remove Content")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
