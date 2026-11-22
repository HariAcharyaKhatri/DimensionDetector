package com.dimensiondetector.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dimensiondetector.ar.ArCoreAvailabilityManager
import com.dimensiondetector.ar.ArCoreStatus
import com.dimensiondetector.ui.components.BadgeType
import com.dimensiondetector.ui.components.StatusBadge
import com.dimensiondetector.ui.theme.Cyan400
import com.dimensiondetector.ui.theme.Cyan500
import com.dimensiondetector.ui.theme.Emerald400
import com.dimensiondetector.ui.theme.Emerald500
import com.dimensiondetector.ui.theme.Indigo400
import com.dimensiondetector.ui.theme.Indigo500
import com.dimensiondetector.ui.theme.Slate200
import com.dimensiondetector.ui.theme.Slate400
import com.dimensiondetector.ui.theme.Slate600
import com.dimensiondetector.ui.theme.Slate700
import com.dimensiondetector.ui.theme.Slate800
import com.dimensiondetector.ui.theme.Slate900
import com.dimensiondetector.ui.theme.Violet400
import com.dimensiondetector.ui.theme.Violet500
import com.dimensiondetector.ui.theme.White

@Composable
fun HomeScreen(
    onNavigateToScan: (mode: String) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val arManager = remember { ArCoreAvailabilityManager(context) }
    val arStatus by arManager.status.collectAsState()

    LaunchedEffect(Unit) {
        arManager.checkAvailability()
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate900)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // App Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "DimensionDetector",
                        color = White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Cyan400.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "AI 3D",
                            color = Cyan400,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Spatial AI Measurement Studio",
                    color = Slate400,
                    fontSize = 12.sp
                )
            }

            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .size(42.dp)
                    .background(Slate800, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Slate200
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System Readiness Status Pill
        val (statusText, badgeType) = when (arStatus) {
            is ArCoreStatus.SupportedInstalled -> Pair("Spatial Tracking Ready", BadgeType.SUCCESS)
            is ArCoreStatus.SupportedNotInstalled -> Pair("AR Services Install Required", BadgeType.WARNING)
            is ArCoreStatus.SupportedApkTooOld -> Pair("AR Services Update Needed", BadgeType.WARNING)
            is ArCoreStatus.Unsupported -> Pair("Hardware Compatibility Notice", BadgeType.WARNING)
            is ArCoreStatus.Error -> Pair("Sensor Check Error", BadgeType.ERROR)
            is ArCoreStatus.Checking -> Pair("Calibrating Sensors...", BadgeType.INFO)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Slate800.copy(alpha = 0.6f))
                .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (badgeType == BadgeType.SUCCESS) Emerald400 else Cyan400)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AR ENGINE STATUS",
                    color = Slate400,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            StatusBadge(text = statusText, type = badgeType)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section Title: Modes
        Text(
            text = "MEASUREMENT MODES",
            color = Slate400,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )

        // Mode Card 1: Object Dimensions (L x W x H)
        MeasurementModeCard(
            title = "Object Dimensions",
            badge = "Instant L × W × H",
            description = "Point at any box, package, or furniture on a table or floor for real-time dimensional bounding.",
            icon = Icons.Default.ViewInAr,
            gradientColors = listOf(Color(0xFF0F766E), Color(0xFF0D9488)),
            accentColor = Cyan400,
            onClick = { onNavigateToScan("object") }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Mode Card 2: Guided 3D Spatial Scan
        MeasurementModeCard(
            title = "Guided 3D Scan",
            badge = "Multi-Angle Point Cloud",
            description = "Orbit 360° around an object to accumulate spatial point clouds and compute oriented bounding cages.",
            icon = Icons.AutoMirrored.Filled.RotateRight,
            gradientColors = listOf(Color(0xFF4C1D95), Color(0xFF6D28D9)),
            accentColor = Violet400,
            onClick = { onNavigateToScan("scan3d") }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Mode Card 3: Room & Space Tool
        MeasurementModeCard(
            title = "Room & Space Tool",
            badge = "Distance & Area",
            description = "Measure wall lengths, floor areas, door clearances, and ceiling heights with point-to-point snapping.",
            icon = Icons.Default.SquareFoot,
            gradientColors = listOf(Color(0xFF064E3B), Color(0xFF047857)),
            accentColor = Emerald400,
            onClick = { onNavigateToScan("room") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Best Practice Tips
        Text(
            text = "SCANNING ACCURACY TIPS",
            color = Slate400,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                AccuracyTipRow(
                    number = "1",
                    title = "Adequate Lighting & Texture",
                    description = "Objects on textured surfaces (wood, fabric, carpet) stabilize tracking faster than glossy, featureless glass."
                )
                Spacer(modifier = Modifier.height(12.dp))
                AccuracyTipRow(
                    number = "2",
                    title = "Smooth Orbital Motion",
                    description = "Move slowly around the target. Rapid jerks or fast pans can degrade optical feature tracking."
                )
                Spacer(modifier = Modifier.height(12.dp))
                AccuracyTipRow(
                    number = "3",
                    title = "Genuine Physical Scale",
                    description = "All dimensions are triangulated from tracked camera trajectories and plane anchors without synthetic approximations."
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MeasurementModeCard(
    title: String,
    badge: String,
    description: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.linearGradient(gradientColors)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = title,
                        color = White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badge,
                        color = accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = description,
                color = Slate400,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Launch Mode",
                    color = accentColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun AccuracyTipRow(
    number: String,
    title: String,
    description: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Slate700),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = Cyan400,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                color = Slate200,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = Slate400,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}
