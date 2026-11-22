package com.dimensiondetector.ar

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.NotYetAvailableException
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ArSessionDiagnostics(
    val sessionState: ArSessionState = ArSessionState.Uninitialized,
    val isDepthSupported: Boolean = false,
    val isDepthActive: Boolean = false,
    val isDepthImageAcquired: Boolean = false,
    val depthFrameCount: Long = 0,
    val depthStatus: String = "Evaluating",
    val trackingState: TrackingState = TrackingState.STOPPED,
    val frameCount: Long = 0,
    val fps: Double = 0.0,
    val errorMessage: String? = null
)

sealed interface ArSessionState {
    data object Uninitialized : ArSessionState
    data object Initializing : ArSessionState
    data class Active(val depthEnabled: Boolean) : ArSessionState
    data object Paused : ArSessionState
    data class Failed(val reason: String, val isHardwareIncompatible: Boolean = false) : ArSessionState
}

class ArSessionManager(private val context: Context) {

    private val tag = "ArSessionManager"
    var session: Session? = null
        private set

    private val _diagnostics = MutableStateFlow(ArSessionDiagnostics())
    val diagnostics: StateFlow<ArSessionDiagnostics> = _diagnostics.asStateFlow()

    private var lastFpsCalculationTime = 0L
    private var frameCounterInWindow = 0
    private var totalFrames = 0L

    fun createSession(activity: Activity): Result<Session> {
        _diagnostics.value = _diagnostics.value.copy(sessionState = ArSessionState.Initializing, errorMessage = null)
        try {
            val newSession = Session(activity)
            val config = Config(newSession)

            // Configure autofocus for reliable scanning
            config.focusMode = Config.FocusMode.AUTO

            // Check Depth API support separately via official isDepthModeSupported query
            val depthSupported = newSession.isDepthModeSupported(Config.DepthMode.AUTOMATIC)
            Log.i(tag, "ARCore Depth API supported on hardware (isDepthModeSupported): $depthSupported")

            if (depthSupported) {
                config.depthMode = Config.DepthMode.AUTOMATIC
                Log.i(tag, "Enabling AUTOMATIC depth mode")
            } else {
                config.depthMode = Config.DepthMode.DISABLED
                Log.i(tag, "Depth API unsupported by device profile; depth mode DISABLED")
            }

            newSession.configure(config)
            session = newSession

            _diagnostics.value = _diagnostics.value.copy(
                sessionState = ArSessionState.Active(depthEnabled = depthSupported),
                isDepthSupported = depthSupported,
                isDepthActive = depthSupported,
                depthStatus = if (depthSupported) "Ready to acquire" else "Unsupported by device profile"
            )
            return Result.success(newSession)

        } catch (e: UnavailableDeviceNotCompatibleException) {
            val errorMsg = "Device hardware is not certified for ARCore. Missing camera/IMU calibration."
            Log.e(tag, errorMsg, e)
            _diagnostics.value = _diagnostics.value.copy(
                sessionState = ArSessionState.Failed(errorMsg, isHardwareIncompatible = true),
                errorMessage = errorMsg
            )
            return Result.failure(e)

        } catch (e: UnavailableArcoreNotInstalledException) {
            val errorMsg = "Google Play Services for AR is not installed."
            Log.e(tag, errorMsg, e)
            _diagnostics.value = _diagnostics.value.copy(
                sessionState = ArSessionState.Failed(errorMsg),
                errorMessage = errorMsg
            )
            return Result.failure(e)

        } catch (e: UnavailableApkTooOldException) {
            val errorMsg = "Google Play Services for AR is outdated. Update required."
            Log.e(tag, errorMsg, e)
            _diagnostics.value = _diagnostics.value.copy(
                sessionState = ArSessionState.Failed(errorMsg),
                errorMessage = errorMsg
            )
            return Result.failure(e)

        } catch (e: UnavailableSdkTooOldException) {
            val errorMsg = "Application ARCore SDK is outdated. Update the app."
            Log.e(tag, errorMsg, e)
            _diagnostics.value = _diagnostics.value.copy(
                sessionState = ArSessionState.Failed(errorMsg),
                errorMessage = errorMsg
            )
            return Result.failure(e)

        } catch (e: UnavailableUserDeclinedInstallationException) {
            val errorMsg = "User declined installation of Google Play Services for AR."
            Log.e(tag, errorMsg, e)
            _diagnostics.value = _diagnostics.value.copy(
                sessionState = ArSessionState.Failed(errorMsg),
                errorMessage = errorMsg
            )
            return Result.failure(e)

        } catch (e: Exception) {
            val errorMsg = "Failed to create ARCore session: ${e.message}"
            Log.e(tag, errorMsg, e)
            _diagnostics.value = _diagnostics.value.copy(
                sessionState = ArSessionState.Failed(errorMsg),
                errorMessage = errorMsg
            )
            return Result.failure(e)
        }
    }

    fun resumeSession(): Boolean {
        val s = session ?: return false
        return try {
            s.resume()
            _diagnostics.value = _diagnostics.value.copy(
                sessionState = ArSessionState.Active(depthEnabled = _diagnostics.value.isDepthActive),
                errorMessage = null
            )
            true
        } catch (e: CameraNotAvailableException) {
            val errorMsg = "Camera is currently unavailable (used by another app or system)."
            Log.e(tag, errorMsg, e)
            _diagnostics.value = _diagnostics.value.copy(
                sessionState = ArSessionState.Failed(errorMsg),
                errorMessage = errorMsg
            )
            false
        } catch (e: Exception) {
            val errorMsg = "Failed to resume ARCore session: ${e.message}"
            Log.e(tag, errorMsg, e)
            _diagnostics.value = _diagnostics.value.copy(
                sessionState = ArSessionState.Failed(errorMsg),
                errorMessage = errorMsg
            )
            false
        }
    }

    fun pauseSession() {
        try {
            session?.pause()
            if (_diagnostics.value.sessionState is ArSessionState.Active) {
                _diagnostics.value = _diagnostics.value.copy(sessionState = ArSessionState.Paused)
            }
        } catch (e: Exception) {
            Log.w(tag, "Exception during session pause: ${e.message}")
        }
    }

    fun destroySession() {
        try {
            session?.close()
            session = null
            _diagnostics.value = ArSessionDiagnostics()
        } catch (e: Exception) {
            Log.w(tag, "Exception during session close: ${e.message}")
        }
    }

    fun reportError(message: String) {
        _diagnostics.value = _diagnostics.value.copy(errorMessage = message)
    }

    fun updateMetrics(trackingState: TrackingState) {
        totalFrames++
        frameCounterInWindow++
        val now = System.currentTimeMillis()
        if (lastFpsCalculationTime == 0L) {
            lastFpsCalculationTime = now
        } else {
            val elapsed = now - lastFpsCalculationTime
            if (elapsed >= 1000) {
                val currentFps = (frameCounterInWindow * 1000.0) / elapsed
                frameCounterInWindow = 0
                lastFpsCalculationTime = now
                _diagnostics.value = _diagnostics.value.copy(
                    frameCount = totalFrames,
                    fps = String.format("%.1f", currentFps).toDoubleOrNull() ?: currentFps,
                    trackingState = trackingState
                )
                return
            }
        }
        _diagnostics.value = _diagnostics.value.copy(
            frameCount = totalFrames,
            trackingState = trackingState
        )
    }

    /**
     * Inspects actual runtime depth frame availability separately from isDepthModeSupported.
     */
    fun checkFrameDepth(frame: Frame) {
        if (!_diagnostics.value.isDepthActive) {
            return
        }

        try {
            val depthImage = frame.acquireDepthImage16Bits()
            val width = depthImage.width
            val height = depthImage.height
            depthImage.close()

            val newCount = _diagnostics.value.depthFrameCount + 1
            _diagnostics.value = _diagnostics.value.copy(
                isDepthImageAcquired = true,
                depthFrameCount = newCount,
                depthStatus = "${width}x${height} 16-bit depth stream active"
            )
        } catch (e: NotYetAvailableException) {
            // Normal during camera initialization and multi-view parallax accumulation
            if (!_diagnostics.value.isDepthImageAcquired) {
                _diagnostics.value = _diagnostics.value.copy(
                    depthStatus = "Accumulating stereo parallax"
                )
            }
        } catch (e: Exception) {
            if (!_diagnostics.value.isDepthImageAcquired) {
                _diagnostics.value = _diagnostics.value.copy(
                    depthStatus = "Runtime query failed: ${e.message ?: e.javaClass.simpleName}"
                )
            }
        }
    }
}
