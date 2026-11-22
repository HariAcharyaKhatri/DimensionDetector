package com.dimensiondetector.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dimensiondetector.ar.ArCoreAvailabilityManager
import com.dimensiondetector.ar.ArCoreInstallResult
import com.dimensiondetector.ar.ArCoreStatus
import com.dimensiondetector.ar.ArSessionManager
import com.dimensiondetector.ar.ArSessionState
import com.dimensiondetector.core.DeviceDiagnostics
import com.dimensiondetector.ui.components.BadgeType
import com.dimensiondetector.ui.components.InfoCard
import com.dimensiondetector.ui.components.InfoRow
import com.dimensiondetector.ui.components.StatusBadge
import com.dimensiondetector.ui.theme.Amber400
import com.dimensiondetector.ui.theme.Cyan400
import com.dimensiondetector.ui.theme.Emerald400
import com.dimensiondetector.ui.theme.Rose400
import com.dimensiondetector.ui.theme.Slate200
import com.dimensiondetector.ui.theme.Slate400
import com.dimensiondetector.ui.theme.Slate700
import com.dimensiondetector.ui.theme.Slate800
import com.dimensiondetector.ui.theme.Slate900
import com.dimensiondetector.ui.theme.White
import kotlinx.coroutines.launch

data class LiveSessionTestResult(
    val hasRun: Boolean = false,
    val success: Boolean = false,
    val isDepthSupported: Boolean = false,
    val message: String = ""
)

@Composable
fun DiagnosticsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    val deviceInfo = remember { DeviceDiagnostics.collectDeviceInfo(context) }
    val arManager = remember { ArCoreAvailabilityManager(context) }
    val arStatus by arManager.status.collectAsState()

    var isChecking by remember { mutableStateOf(false) }
    var sessionTestResult by remember { mutableStateOf(LiveSessionTestResult()) }
    var isTestingSession by remember { mutableStateOf(false) }

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
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(Slate800, RoundedCornerShape(10.dp))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Slate200
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "DEVICE DIAGNOSTICS",
                    color = Cyan400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Hardware & AR Verification",
                    color = White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Device Specs Card
        InfoCard(
            title = "Physical Device Specifications",
            icon = Icons.Default.PhoneAndroid
        ) {
            InfoRow(label = "Manufacturer", value = deviceInfo.manufacturer)
            InfoRow(label = "Model", value = deviceInfo.model)
            InfoRow(label = "Internal Device Name", value = deviceInfo.device)
            InfoRow(label = "Board / SoC", value = deviceInfo.board)
            InfoRow(label = "Hardware Platform", value = deviceInfo.hardware)
            InfoRow(label = "Android Version", value = "${deviceInfo.androidVersion} (API ${deviceInfo.apiLevel})")
            InfoRow(label = "Primary ABI", value = deviceInfo.supportedAbis.firstOrNull() ?: "Unknown")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Camera2 Subsystem Card
        InfoCard(
            title = "Camera2 Hardware Subsystem",
            icon = Icons.Default.CameraAlt,
            badge = {
                val hasFullOrBetter = deviceInfo.cameraInfo.hardwareLevel in listOf("FULL", "LEVEL_3")
                StatusBadge(
                    text = if (hasFullOrBetter) "Optimal" else "Limited",
                    type = if (hasFullOrBetter) BadgeType.SUCCESS else BadgeType.WARNING
                )
            }
        ) {
            InfoRow(label = "Rear Camera Available", value = if (deviceInfo.cameraInfo.hasBackCamera) "Yes" else "No")
            InfoRow(label = "Back Camera ID", value = deviceInfo.cameraInfo.backCameraId ?: "None")
            InfoRow(
                label = "Camera Hardware Level",
                value = deviceInfo.cameraInfo.hardwareLevel,
                valueColor = if (deviceInfo.cameraInfo.hardwareLevel in listOf("FULL", "LEVEL_3")) Emerald400 else Amber400
            )
            InfoRow(label = "Flash Unit Available", value = if (deviceInfo.cameraInfo.hasFlash) "Yes" else "No")
            InfoRow(
                label = "Camera2 RAW Depth Stream",
                value = if (deviceInfo.cameraInfo.isDepthCapable) "Supported" else "Not Exposed Directly",
                valueColor = if (deviceInfo.cameraInfo.isDepthCapable) Emerald400 else Slate400
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ARCore Official Certification & Availability
        InfoCard(
            title = "Google ARCore Certification Status",
            icon = Icons.Default.ViewInAr,
            badge = {
                when (arStatus) {
                    is ArCoreStatus.SupportedInstalled -> StatusBadge("Certified & Installed", BadgeType.SUCCESS)
                    is ArCoreStatus.SupportedNotInstalled -> StatusBadge("Install Needed", BadgeType.WARNING)
                    is ArCoreStatus.SupportedApkTooOld -> StatusBadge("Update Needed", BadgeType.WARNING)
                    is ArCoreStatus.Unsupported -> StatusBadge("Uncertified Hardware", BadgeType.ERROR)
                    is ArCoreStatus.Error -> StatusBadge("Query Error", BadgeType.ERROR)
                    is ArCoreStatus.Checking -> StatusBadge("Checking...", BadgeType.INFO)
                }
            }
        ) {
            val description = when (val s = arStatus) {
                is ArCoreStatus.SupportedInstalled ->
                    "This device has Google ARCore certification and Google Play Services for AR is installed."
                is ArCoreStatus.SupportedNotInstalled ->
                    "This device model is supported by Google ARCore, but Google Play Services for AR is not yet installed."
                is ArCoreStatus.SupportedApkTooOld ->
                    "Google Play Services for AR is installed but needs an update from Google Play Store."
                is ArCoreStatus.Unsupported ->
                    "Hardware limitation: ${s.reason} ARCore requires calibrated sensor profiles from Google. Uncertified devices (including POCO X6 5G) cannot create standard ARCore sessions."
                is ArCoreStatus.Error ->
                    s.message
                is ArCoreStatus.Checking ->
                    "Querying Google Play Services for AR availability..."
            }

            Text(
                text = description,
                color = Slate200,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isChecking = true
                            arManager.checkAvailability()
                            isChecking = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Re-check", fontSize = 13.sp)
                }

                if (arStatus is ArCoreStatus.SupportedNotInstalled && activity != null) {
                    Button(
                        onClick = {
                            val res = arManager.requestInstall(activity)
                            if (res is ArCoreInstallResult.Installed) {
                                scope.launch { arManager.checkAvailability() }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Cyan400, contentColor = Slate900)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Install AR", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live ARCore Session & Depth API Test
        InfoCard(
            title = "ARCore Session & Depth API Live Test",
            icon = Icons.Default.Security
        ) {
            Text(
                text = "Safely tests ARCore Session initialization and queries isDepthModeSupported(Config.DepthMode.AUTOMATIC) on this physical hardware without risking app crashes.",
                color = Slate400,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (sessionTestResult.hasRun) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (sessionTestResult.success) Emerald400.copy(alpha = 0.1f) else Rose400.copy(alpha = 0.1f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (sessionTestResult.success) "Session Created Successfully" else "Session Test Failed",
                                color = if (sessionTestResult.success) Emerald400 else Rose400,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            StatusBadge(
                                text = if (sessionTestResult.success) "ACTIVE" else "FAILED",
                                type = if (sessionTestResult.success) BadgeType.SUCCESS else BadgeType.ERROR
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoRow(
                            label = "Depth API Automatic",
                            value = if (sessionTestResult.isDepthSupported) "Supported" else "Unsupported",
                            valueColor = if (sessionTestResult.isDepthSupported) Emerald400 else Amber400
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = sessionTestResult.message,
                            color = Slate200,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (activity == null) return@Button
                    isTestingSession = true
                    scope.launch {
                        val sessionManager = ArSessionManager(context)
                        val result = sessionManager.createSession(activity)
                        if (result.isSuccess) {
                            val session = result.getOrNull()
                            val depth = sessionManager.diagnostics.value.isDepthSupported
                            sessionManager.destroySession()
                            sessionTestResult = LiveSessionTestResult(
                                hasRun = true,
                                success = true,
                                isDepthSupported = depth,
                                message = if (depth) {
                                    "ARCore initialized cleanly and Depth API is fully supported on this device."
                                } else {
                                    "ARCore initialized, but Depth API is not supported on this device. Mesh and depth scans will be disabled."
                                }
                            )
                        } else {
                            val exception = result.exceptionOrNull()
                            val errorMsg = sessionManager.diagnostics.value.errorMessage ?: exception?.message ?: "Unknown error"
                            sessionManager.destroySession()
                            sessionTestResult = LiveSessionTestResult(
                                hasRun = true,
                                success = false,
                                isDepthSupported = false,
                                message = "Test safely halted: $errorMsg"
                            )
                        }
                        isTestingSession = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isTestingSession,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Slate700,
                    contentColor = White
                )
            ) {
                if (isTestingSession) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Cyan400,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Testing Hardware...", fontSize = 13.sp)
                } else {
                    Icon(imageVector = Icons.Default.DeveloperBoard, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Run Live AR & Depth Test", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
