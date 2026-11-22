package com.dimensiondetector.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.dimensiondetector.ar.ArCoreAvailabilityManager
import com.dimensiondetector.ar.ArCoreStatus
import com.dimensiondetector.core.DeviceDiagnostics
import com.dimensiondetector.ui.components.BadgeType
import com.dimensiondetector.ui.components.InfoCard
import com.dimensiondetector.ui.components.InfoRow
import com.dimensiondetector.ui.components.StatusBadge
import com.dimensiondetector.ui.theme.Amber400
import com.dimensiondetector.ui.theme.Cyan400
import com.dimensiondetector.ui.theme.Cyan500
import com.dimensiondetector.ui.theme.Emerald400
import com.dimensiondetector.ui.theme.Rose400
import com.dimensiondetector.ui.theme.Slate200
import com.dimensiondetector.ui.theme.Slate400
import com.dimensiondetector.ui.theme.Slate600
import com.dimensiondetector.ui.theme.Slate700
import com.dimensiondetector.ui.theme.Slate800
import com.dimensiondetector.ui.theme.Slate900
import com.dimensiondetector.ui.theme.White

@Composable
fun HomeScreen(
    onNavigateToScan: () -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val arManager = remember { ArCoreAvailabilityManager(context) }
    val arStatus by arManager.status.collectAsState()

    val deviceInfo = remember { DeviceDiagnostics.collectDeviceInfo(context) }
    val hasCameraPermission = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(Unit) {
        arManager.checkAvailability()
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate900)
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        // App Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DIMENSION DETECTOR",
                    color = Cyan400,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "3D Scanner & Depth",
                    color = White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            StatusBadge(
                text = "Prototype 1.0",
                type = BadgeType.INFO
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Device Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Slate800, Slate700)
                    )
                )
                .border(1.dp, Slate600, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Cyan400.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = Cyan400,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "${deviceInfo.manufacturer.uppercase()} ${deviceInfo.model}",
                        color = White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Android ${deviceInfo.androidVersion} (API ${deviceInfo.apiLevel}) • Board: ${deviceInfo.board}",
                        color = Slate400,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Readiness Matrix
        InfoCard(
            title = "Hardware & AR Readiness",
            icon = Icons.Default.Sensors,
            badge = {
                val badgeType = when (arStatus) {
                    is ArCoreStatus.SupportedInstalled -> BadgeType.SUCCESS
                    is ArCoreStatus.SupportedNotInstalled, is ArCoreStatus.SupportedApkTooOld -> BadgeType.WARNING
                    is ArCoreStatus.Unsupported, is ArCoreStatus.Error -> BadgeType.ERROR
                    is ArCoreStatus.Checking -> BadgeType.INFO
                }
                val badgeText = when (arStatus) {
                    is ArCoreStatus.SupportedInstalled -> "AR Ready"
                    is ArCoreStatus.SupportedNotInstalled -> "Install AR Services"
                    is ArCoreStatus.SupportedApkTooOld -> "Update AR Services"
                    is ArCoreStatus.Unsupported -> "Uncertified Hardware"
                    is ArCoreStatus.Error -> "Check Error"
                    is ArCoreStatus.Checking -> "Checking..."
                }
                StatusBadge(text = badgeText, type = badgeType)
            }
        ) {
            InfoRow(
                label = "Camera Hardware Level",
                value = deviceInfo.cameraInfo.hardwareLevel,
                valueColor = if (deviceInfo.cameraInfo.hasBackCamera) Emerald400 else Rose400
            )
            InfoRow(
                label = "Camera Permission",
                value = if (hasCameraPermission) "Granted" else "Pending Prompt",
                valueColor = if (hasCameraPermission) Emerald400 else Amber400
            )
            InfoRow(
                label = "ARCore Certification",
                value = when (arStatus) {
                    is ArCoreStatus.SupportedInstalled -> "Certified & Installed"
                    is ArCoreStatus.SupportedNotInstalled -> "Supported (Requires Install)"
                    is ArCoreStatus.SupportedApkTooOld -> "Supported (Update Needed)"
                    is ArCoreStatus.Unsupported -> "Uncertified / Incompatible"
                    is ArCoreStatus.Error -> "Query Failed"
                    is ArCoreStatus.Checking -> "Querying Google..."
                },
                valueColor = when (arStatus) {
                    is ArCoreStatus.SupportedInstalled -> Emerald400
                    is ArCoreStatus.SupportedNotInstalled, is ArCoreStatus.SupportedApkTooOld -> Amber400
                    is ArCoreStatus.Unsupported, is ArCoreStatus.Error -> Rose400
                    is ArCoreStatus.Checking -> Cyan400
                }
            )
            InfoRow(
                label = "Depth Sensor / API",
                value = if (deviceInfo.cameraInfo.isDepthCapable) "Hardware Depth Output" else "Software / AR Mode Dependent",
                valueColor = Slate200
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons
        Button(
            onClick = onNavigateToScan,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Cyan500,
                contentColor = Slate900
            )
        ) {
            Icon(
                imageVector = Icons.Default.ViewInAr,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Launch AR Scanner & Preview",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onNavigateToDiagnostics,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Cyan400
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.horizontalGradient(listOf(Slate600, Slate700))
            )
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Device & Sensor Diagnostics",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Prototype Roadmap & Validation Principles
        InfoCard(
            title = "Development Roadmap",
            icon = Icons.Default.Info
        ) {
            RoadmapItem(
                title = "Stage 1 — Camera & Depth Diagnostics",
                status = "COMPLETED",
                isDone = true
            )
            RoadmapItem(
                title = "Stage 2 — Object Selection & Depth Sampling",
                status = "PENDING",
                isDone = false
            )
            RoadmapItem(
                title = "Stage 3 — Multi-View Point Cloud & Dimensions",
                status = "PENDING",
                isDone = false
            )
            RoadmapItem(
                title = "Stage 4 — Guided Scan & Reference Validation",
                status = "PENDING",
                isDone = false
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quality & Device Disclaimer
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Slate800.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Amber400,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Accuracy Note: Uncertified devices (like POCO X6) report hardware limitations safely without crashing. No fake dimensions or estimated scales are displayed unless verified against active physical depth data.",
                    color = Slate400,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun RoadmapItem(
    title: String,
    status: String,
    isDone: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isDone) Emerald400 else Slate600,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            color = if (isDone) Slate200 else Slate400,
            fontSize = 13.sp,
            fontWeight = if (isDone) FontWeight.Medium else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        StatusBadge(
            text = status,
            type = if (isDone) BadgeType.SUCCESS else BadgeType.NEUTRAL
        )
    }
}
