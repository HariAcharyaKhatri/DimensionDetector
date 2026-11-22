package com.dimensiondetector.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.Display
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dimensiondetector.ar.ArCoreAvailabilityManager
import com.dimensiondetector.ar.ArCoreStatus
import com.dimensiondetector.ar.ArSessionManager
import com.dimensiondetector.ar.ArSessionState
import com.dimensiondetector.ar.BackgroundRenderer
import com.dimensiondetector.core.DeviceDiagnostics
import com.dimensiondetector.ui.components.BadgeType
import com.dimensiondetector.ui.components.InfoCard
import com.dimensiondetector.ui.components.StatusBadge
import com.dimensiondetector.ui.theme.Amber400
import com.dimensiondetector.ui.theme.Cyan400
import com.dimensiondetector.ui.theme.Cyan500
import com.dimensiondetector.ui.theme.Emerald400
import com.dimensiondetector.ui.theme.Rose400
import com.dimensiondetector.ui.theme.Slate200
import com.dimensiondetector.ui.theme.Slate400
import com.dimensiondetector.ui.theme.Slate700
import com.dimensiondetector.ui.theme.Slate800
import com.dimensiondetector.ui.theme.Slate900
import com.dimensiondetector.ui.theme.White
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

@Composable
fun ArScanScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    val arManager = remember { ArCoreAvailabilityManager(context) }
    val arStatus by arManager.status.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    val sessionManager = remember { ArSessionManager(context) }
    val sessionDiagnostics by sessionManager.diagnostics.collectAsState()

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
        arManager.checkAvailability()
    }

    // Lifecycle binding
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> sessionManager.resumeSession()
                Lifecycle.Event.ON_PAUSE -> sessionManager.pauseSession()
                Lifecycle.Event.ON_DESTROY -> sessionManager.destroySession()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            sessionManager.destroySession()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Slate900)
    ) {
        when {
            // Case 1: Missing Camera Permission
            !hasCameraPermission -> {
                PermissionRequiredView(
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    onNavigateBack = onNavigateBack
                )
            }

            // Case 2: Checking availability
            arStatus is ArCoreStatus.Checking -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Cyan400)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Querying ARCore hardware capabilities...",
                        color = Slate200,
                        fontSize = 14.sp
                    )
                }
            }

            // Case 3: Device is Unsupported / Uncertified (e.g. POCO X6 5G)
            arStatus is ArCoreStatus.Unsupported || sessionDiagnostics.sessionState is ArSessionState.Failed -> {
                val failureReason = (sessionDiagnostics.sessionState as? ArSessionState.Failed)?.reason
                    ?: (arStatus as? ArCoreStatus.Unsupported)?.reason
                    ?: "ARCore is not supported on this device."

                HardwareLimitationView(
                    reason = failureReason,
                    onNavigateBack = onNavigateBack,
                    onNavigateToDiagnostics = onNavigateToDiagnostics
                )
            }

            // Case 4: Supported but Google Play Services for AR not installed
            arStatus is ArCoreStatus.SupportedNotInstalled -> {
                ArServicesMissingView(
                    onInstall = {
                        activity?.let { arManager.requestInstall(it) }
                    },
                    onNavigateBack = onNavigateBack
                )
            }

            // Case 5: Supported and Installed -> Render live AR surface
            else -> {
                ArCameraSurface(
                    activity = activity,
                    sessionManager = sessionManager
                )

                // Floating HUD Overlay
                ArScanningHud(
                    diagnostics = sessionDiagnostics,
                    onNavigateBack = onNavigateBack
                )
            }
        }
    }
}

@Composable
private fun ArCameraSurface(
    activity: Activity?,
    sessionManager: ArSessionManager
) {
    AndroidView(
        factory = { ctx ->
            GLSurfaceView(ctx).apply {
                preserveEGLContextOnPause = true
                setEGLContextClientVersion(2)
                setEGLConfigChooser(8, 8, 8, 8, 16, 0)

                val backgroundRenderer = BackgroundRenderer()

                setRenderer(object : GLSurfaceView.Renderer {
                    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
                        backgroundRenderer.createOnGlThread()

                        if (activity != null && sessionManager.session == null) {
                            val result = sessionManager.createSession(activity)
                            result.getOrNull()?.let { session ->
                                session.setCameraTextureName(backgroundRenderer.textureId)
                                sessionManager.resumeSession()
                            }
                        } else {
                            sessionManager.session?.setCameraTextureName(backgroundRenderer.textureId)
                        }
                    }

                    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                        GLES20.glViewport(0, 0, width, height)
                        val display = (ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
                        sessionManager.session?.setDisplayGeometry(display.rotation, width, height)
                    }

                    override fun onDrawFrame(gl: GL10?) {
                        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
                        val session = sessionManager.session ?: return

                        val frame = try {
                            session.update()
                        } catch (e: com.google.ar.core.exceptions.SessionPausedException) {
                            return
                        } catch (e: com.google.ar.core.exceptions.NotYetAvailableException) {
                            return
                        } catch (e: com.google.ar.core.exceptions.CameraNotAvailableException) {
                            sessionManager.reportError("Camera is currently unavailable.")
                            return
                        } catch (e: Exception) {
                            Log.e("ArCameraSurface", "Exception during session.update()", e)
                            return
                        }

                        // Always update metrics once frame is obtained from camera HAL
                        val camera = frame.camera
                        sessionManager.updateMetrics(camera.trackingState)

                        // Render camera preview background
                        try {
                            backgroundRenderer.draw(frame)
                        } catch (e: Exception) {
                            Log.e("ArCameraSurface", "Exception during backgroundRenderer.draw()", e)
                        }

                        // Verify Depth API at runtime separately without guessing
                        sessionManager.checkFrameDepth(frame)
                    }
                })

                renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun ArScanningHud(
    diagnostics: com.dimensiondetector.ar.ArSessionDiagnostics,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top HUD Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(42.dp)
                    .background(Slate900.copy(alpha = 0.8f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = White
                )
            }

            // Tracking State Badge
            val trackingBadgeType = when (diagnostics.trackingState) {
                TrackingState.TRACKING -> BadgeType.SUCCESS
                TrackingState.PAUSED -> BadgeType.WARNING
                TrackingState.STOPPED -> BadgeType.ERROR
            }
            StatusBadge(
                text = "TRACKING: ${diagnostics.trackingState.name}",
                type = trackingBadgeType
            )

            // FPS Counter
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Slate900.copy(alpha = 0.8f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${diagnostics.fps} FPS",
                    color = Cyan400,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Bottom HUD Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Slate900.copy(alpha = 0.9f))
                .border(1.dp, Slate700, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SENSOR STATUS",
                        color = Slate400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    val depthBadgeType = when {
                        diagnostics.isDepthActive && diagnostics.isDepthImageAcquired -> BadgeType.SUCCESS
                        diagnostics.isDepthActive -> BadgeType.INFO
                        else -> BadgeType.WARNING
                    }
                    val depthText = when {
                        diagnostics.isDepthActive && diagnostics.isDepthImageAcquired -> "DEPTH: ACTIVE (${diagnostics.depthFrameCount})"
                        diagnostics.isDepthActive -> "DEPTH: ACCUMULATING"
                        else -> "DEPTH: DISABLED"
                    }
                    StatusBadge(text = depthText, type = depthBadgeType)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when {
                        diagnostics.isDepthActive && diagnostics.isDepthImageAcquired ->
                            "Physical 16-bit depth stream active (${diagnostics.depthStatus}). Multi-view sampling ready."
                        diagnostics.isDepthActive ->
                            "Depth API enabled in session (${diagnostics.depthStatus}). Move device to accumulate stereo parallax."
                        else ->
                            "Depth API query isDepthModeSupported() = false on current device profile. RGB pose tracking is active."
                    },
                    color = Slate200,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun HardwareLimitationView(
    reason: String,
    onNavigateBack: () -> Unit,
    onNavigateToDiagnostics: () -> Unit
) {
    val context = LocalContext.current
    val deviceInfo = remember { DeviceDiagnostics.collectDeviceInfo(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Amber400.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Amber400,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "ARCore Hardware Limitation",
            color = White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Device: ${deviceInfo.manufacturer.uppercase()} ${deviceInfo.model}",
            color = Cyan400,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = reason,
            color = Slate400,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Slate800)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Honest Prototype Verification:",
                    color = Slate200,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• DimensionDetector does not display synthetic or fake measurements.\n• Depth scanning requires Google ARCore sensor calibration tables which are not certified for this specific phone model.\n• You can review full camera and hardware specifications on the Diagnostics screen.",
                    color = Slate400,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onNavigateToDiagnostics,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Cyan500, contentColor = Slate900)
        ) {
            Text(text = "View Device Diagnostics", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Return to Home", color = Slate200)
        }
    }
}

@Composable
private fun PermissionRequiredView(
    onRequestPermission: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Cyan400.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = Cyan400,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Camera Permission Required",
            color = White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "DimensionDetector requires camera access to run the ARCore session, evaluate depth maps, and capture multi-viewpoint frames.",
            color = Slate400,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRequestPermission,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Cyan500, contentColor = Slate900)
        ) {
            Text(text = "Grant Camera Permission", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Cancel", color = Slate200)
        }
    }
}

@Composable
private fun ArServicesMissingView(
    onInstall: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Amber400.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Amber400,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Google Play Services for AR Needed",
            color = White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "This device model supports ARCore, but Google Play Services for AR is not installed. Tap below to install it from Google Play.",
            color = Slate400,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onInstall,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Cyan500, contentColor = Slate900)
        ) {
            Text(text = "Install from Google Play", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Cancel", color = Slate200)
        }
    }
}
