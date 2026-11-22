package com.dimensiondetector.ar

import android.app.Activity
import android.content.Context
import com.google.ar.core.ArCoreApk
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

sealed interface ArCoreStatus {
    data object Checking : ArCoreStatus
    data object SupportedInstalled : ArCoreStatus
    data object SupportedNotInstalled : ArCoreStatus
    data object SupportedApkTooOld : ArCoreStatus
    data class Unsupported(val reason: String) : ArCoreStatus
    data class Error(val message: String) : ArCoreStatus
}

sealed interface ArCoreInstallResult {
    data object Installed : ArCoreInstallResult
    data object InstallationRequested : ArCoreInstallResult
    data object UserDeclined : ArCoreInstallResult
    data class Failed(val error: String) : ArCoreInstallResult
}

class ArCoreAvailabilityManager(private val context: Context) {

    private val _status = MutableStateFlow<ArCoreStatus>(ArCoreStatus.Checking)
    val status: StateFlow<ArCoreStatus> = _status.asStateFlow()

    suspend fun checkAvailability(): ArCoreStatus = withContext(Dispatchers.IO) {
        _status.value = ArCoreStatus.Checking
        try {
            val availability = ArCoreApk.getInstance().checkAvailability(context)
            val result = when {
                availability.isSupported -> {
                    when (availability) {
                        ArCoreApk.Availability.SUPPORTED_INSTALLED -> ArCoreStatus.SupportedInstalled
                        ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED -> ArCoreStatus.SupportedNotInstalled
                        ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD -> ArCoreStatus.SupportedApkTooOld
                        else -> ArCoreStatus.SupportedInstalled
                    }
                }
                availability == ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE -> {
                    ArCoreStatus.Unsupported(
                        "Device is not certified for Google Play Services for AR. Sensor and camera calibration data is missing."
                    )
                }
                availability.isTransient -> {
                    // Retry once if transient
                    Thread.sleep(300)
                    val retry = ArCoreApk.getInstance().checkAvailability(context)
                    if (retry.isSupported) {
                        ArCoreStatus.SupportedInstalled
                    } else if (retry == ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE) {
                        ArCoreStatus.Unsupported("Hardware is not ARCore-certified.")
                    } else {
                        ArCoreStatus.Unsupported("ARCore availability check timed out or unknown state ($retry).")
                    }
                }
                else -> {
                    ArCoreStatus.Unsupported("Hardware is not capable of running ARCore ($availability).")
                }
            }
            _status.value = result
            result
        } catch (e: Exception) {
            val error = ArCoreStatus.Error("Error checking ARCore availability: ${e.message}")
            _status.value = error
            error
        }
    }

    fun requestInstall(activity: Activity): ArCoreInstallResult {
        return try {
            val installStatus = ArCoreApk.getInstance().requestInstall(activity, true)
            when (installStatus) {
                ArCoreApk.InstallStatus.INSTALLED -> ArCoreInstallResult.Installed
                ArCoreApk.InstallStatus.INSTALL_REQUESTED -> ArCoreInstallResult.InstallationRequested
            }
        } catch (e: UnavailableDeviceNotCompatibleException) {
            ArCoreInstallResult.Failed("Device is not compatible with Google Play Services for AR.")
        } catch (e: UnavailableUserDeclinedInstallationException) {
            ArCoreInstallResult.UserDeclined
        } catch (e: Exception) {
            ArCoreInstallResult.Failed("Installation failed: ${e.message}")
        }
    }
}
