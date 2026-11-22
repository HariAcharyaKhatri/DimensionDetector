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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dimensiondetector.ar.ArCoreAvailabilityManager
import com.dimensiondetector.ar.ArCoreStatus
import com.dimensiondetector.core.DeviceDiagnostics
import com.dimensiondetector.ui.components.BadgeType
import com.dimensiondetector.ui.components.StatusBadge
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

enum class MeasurementUnit(val label: String, val detail: String) {
    METRIC("Metric (cm / m)", "Centimeters & Meters"),
    IMPERIAL("Imperial (in / ft)", "Inches & Feet")
}

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val arManager = remember { ArCoreAvailabilityManager(context) }
    val arStatus by arManager.status.collectAsState()
    val deviceInfo = remember { DeviceDiagnostics.collectDeviceInfo(context) }
    val appPreferences = remember { com.dimensiondetector.core.AppPreferences(context) }

    val selectedUnit by appPreferences.measurementUnit.collectAsState()
    val hapticFeedbackEnabled by appPreferences.hapticFeedback.collectAsState()
    val visualGridEnabled by appPreferences.visualGrid.collectAsState()

    LaunchedEffect(Unit) {
        arManager.checkAvailability()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate900)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(Slate800, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Settings",
                    color = White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Preferences & Diagnostics",
                    color = Slate400,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Units
        SettingsSectionHeader(title = "MEASUREMENT UNITS")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                MeasurementUnit.entries.forEachIndexed { index, unit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { appPreferences.setMeasurementUnit(unit) }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = unit.label,
                                color = if (selectedUnit == unit) Cyan400 else White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = unit.detail,
                                color = Slate400,
                                fontSize = 12.sp
                            )
                        }
                        if (selectedUnit == unit) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Cyan400,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (index < MeasurementUnit.entries.size - 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Slate700)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Scanning Assistance
        SettingsSectionHeader(title = "SCANNING ASSISTANCE")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Haptic Vibration",
                            color = White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Tactile click when points snap to physical surfaces",
                            color = Slate400,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = hapticFeedbackEnabled,
                        onCheckedChange = { appPreferences.setHapticFeedback(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Slate900,
                            checkedTrackColor = Cyan400,
                            uncheckedThumbColor = Slate400,
                            uncheckedTrackColor = Slate700
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Slate700)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Surface Grid Visualization",
                            color = White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Render subtle dot matrix on detected planes",
                            color = Slate400,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = visualGridEnabled,
                        onCheckedChange = { appPreferences.setVisualGrid(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Slate900,
                            checkedTrackColor = Cyan400,
                            uncheckedThumbColor = Slate400,
                            uncheckedTrackColor = Slate700
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Hardware & Diagnostics Link
        SettingsSectionHeader(title = "HARDWARE & AR CALIBRATION")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onNavigateToDiagnostics() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Cyan400.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = Cyan400,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Device & ARCore Diagnostics",
                                color = White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${deviceInfo.manufacturer.uppercase()} ${deviceInfo.model}",
                                color = Slate400,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Open Diagnostics",
                        tint = Slate400,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ARCore Status",
                        color = Slate400,
                        fontSize = 12.sp
                    )
                    val statusText = when (arStatus) {
                        is ArCoreStatus.SupportedInstalled -> "Ready & Installed"
                        is ArCoreStatus.SupportedNotInstalled -> "Requires Services"
                        is ArCoreStatus.SupportedApkTooOld -> "Update Required"
                        is ArCoreStatus.Unsupported -> "Uncertified"
                        is ArCoreStatus.Error -> "Error"
                        is ArCoreStatus.Checking -> "Checking..."
                    }
                    val statusBadge = when (arStatus) {
                        is ArCoreStatus.SupportedInstalled -> BadgeType.SUCCESS
                        is ArCoreStatus.SupportedNotInstalled, is ArCoreStatus.SupportedApkTooOld -> BadgeType.WARNING
                        is ArCoreStatus.Unsupported, is ArCoreStatus.Error -> BadgeType.ERROR
                        is ArCoreStatus.Checking -> BadgeType.INFO
                    }
                    StatusBadge(text = statusText, type = statusBadge)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // App Information Footer
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "DimensionDetector • Prototype 2.0",
                color = Slate400,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Spatial AI Core • ARCore Visual Inertial Odometry",
                color = Slate600,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = Slate400,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}
