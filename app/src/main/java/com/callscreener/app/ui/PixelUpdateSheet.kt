package com.callscreener.app.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.callscreener.app.ota.DownloadStatus
import com.callscreener.app.ota.OtaUpdateManager
import com.callscreener.app.ota.UpdateInfo
import com.callscreener.app.root.RootPrivilegeManager
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

@Composable
fun PixelExpressiveUpdateDialog(
    updateInfo: UpdateInfo,
    downloadStatus: DownloadStatus,
    monetColors: ColorScheme,
    onDismiss: () -> Unit,
    onStartDownload: () -> Unit,
    onInstallStandard: (File) -> Unit,
    onInstallRoot: (File) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isRootActive = remember { RootPrivilegeManager.isRootAvailable() }

    Dialog(
        onDismissRequest = {
            if (downloadStatus !is DownloadStatus.Downloading && downloadStatus !is DownloadStatus.Installing) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = downloadStatus !is DownloadStatus.Downloading && downloadStatus !is DownloadStatus.Installing,
            dismissOnClickOutside = downloadStatus !is DownloadStatus.Downloading && downloadStatus !is DownloadStatus.Installing
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(horizontal = 20.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(32.dp))
                    .background(monetColors.surface)
                    .border(1.dp, monetColors.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(32.dp))
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(monetColors.primaryContainer.copy(alpha = 0.5f))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = monetColors.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "SYSTEM UPDATE",
                                color = monetColors.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Resonix ${updateInfo.versionName}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = monetColors.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (updateInfo.fileSize.isNotBlank()) {
                            UpdateMetaChip(text = updateInfo.fileSize, monetColors = monetColors)
                        }
                        if (updateInfo.releaseDate.isNotBlank()) {
                            UpdateMetaChip(text = updateInfo.releaseDate, monetColors = monetColors)
                        }
                        UpdateMetaChip(
                            text = "Build ${updateInfo.versionCode}",
                            monetColors = monetColors,
                            isAccent = true
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(monetColors.surfaceVariant.copy(alpha = 0.4f))
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "WHAT'S NEW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = monetColors.primary,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = updateInfo.changelog,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = monetColors.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    when (downloadStatus) {
                        is DownloadStatus.Downloading -> {
                            val animProgress by animateFloatAsState(
                                targetValue = if (downloadStatus.progress > 0) downloadStatus.progress else 0.5f,
                                animationSpec = spring(stiffness = Spring.StiffnessLow),
                                label = "progress"
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Downloading update...",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = monetColors.onSurface
                                    )
                                    val percentStr = if (downloadStatus.progress > 0) {
                                        "${(downloadStatus.progress * 100).toInt()}%"
                                    } else {
                                        "Streaming"
                                    }
                                    Text(
                                        text = percentStr,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = monetColors.primary
                                    )
                                }

                                LinearProgressIndicator(
                                    progress = { animProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = monetColors.primary,
                                    trackColor = monetColors.surfaceVariant
                                )

                                if (downloadStatus.totalBytes > 0) {
                                    val downloadedMB = downloadStatus.downloadedBytes.toFloat() / (1024 * 1024)
                                    val totalMB = downloadStatus.totalBytes.toFloat() / (1024 * 1024)
                                    Text(
                                        text = String.format(Locale.US, "%.1f MB / %.1f MB", downloadedMB, totalMB),
                                        fontSize = 11.sp,
                                        color = monetColors.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        is DownloadStatus.Downloaded -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Ready to Install",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF81C995),
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )

                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onInstallStandard(downloadStatus.apkFile)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = monetColors.primary)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Rounded.InstallMobile, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Text("Install Update", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }

                                if (isRootActive) {
                                    OutlinedButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onInstallRoot(downloadStatus.apkFile)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp),
                                        shape = RoundedCornerShape(18.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFB74D))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Rounded.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Text("1-Tap Root Silent Install", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }

                        is DownloadStatus.Installing -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.5.dp,
                                    color = monetColors.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Applying update...",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = monetColors.onSurface
                                )
                            }
                        }

                        is DownloadStatus.Error -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = downloadStatus.message,
                                    fontSize = 12.sp,
                                    color = monetColors.error
                                )
                                Button(
                                    onClick = onStartDownload,
                                    colors = ButtonDefaults.buttonColors(containerColor = monetColors.primary),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("Retry Download", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        DownloadStatus.Idle -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TextButton(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(18.dp)
                                ) {
                                    Text("Later", color = monetColors.onSurfaceVariant, fontSize = 14.sp)
                                }

                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onStartDownload()
                                    },
                                    modifier = Modifier
                                        .weight(1.4f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = monetColors.primary)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Text("Update Now", fontWeight = FontWeight.Bold, fontSize = 14.sp)
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

@Composable
private fun UpdateMetaChip(
    text: String,
    monetColors: ColorScheme,
    isAccent: Boolean = false
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isAccent) monetColors.primary.copy(alpha = 0.18f)
                else monetColors.surfaceVariant.copy(alpha = 0.5f)
            )
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.5.sp,
            fontWeight = if (isAccent) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isAccent) monetColors.primary else monetColors.onSurfaceVariant
        )
    }
}
