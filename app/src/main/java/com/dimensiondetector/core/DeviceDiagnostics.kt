package com.dimensiondetector.core

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build

data class DeviceHardwareInfo(
    val manufacturer: String,
    val model: String,
    val device: String,
    val board: String,
    val hardware: String,
    val androidVersion: String,
    val apiLevel: Int,
    val supportedAbis: List<String>,
    val cameraInfo: CameraHardwareInfo
)

data class CameraHardwareInfo(
    val hasBackCamera: Boolean,
    val backCameraId: String?,
    val hardwareLevel: String,
    val hasFlash: Boolean,
    val isDepthCapable: Boolean
)

object DeviceDiagnostics {

    fun collectDeviceInfo(context: Context): DeviceHardwareInfo {
        val cameraInfo = collectCameraInfo(context)
        return DeviceHardwareInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            device = Build.DEVICE,
            board = Build.BOARD,
            hardware = Build.HARDWARE,
            androidVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            supportedAbis = Build.SUPPORTED_ABIS.toList(),
            cameraInfo = cameraInfo
        )
    }

    private fun collectCameraInfo(context: Context): CameraHardwareInfo {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraIds = cameraManager.cameraIdList
            var backCameraId: String? = null
            var hardwareLevelStr = "Unknown"
            var hasFlash = false
            var isDepthCapable = false

            for (id in cameraIds) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                    backCameraId = id
                    val level = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                    hardwareLevelStr = when (level) {
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> "LEGACY"
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "LIMITED"
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "FULL"
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> "LEVEL_3"
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL -> "EXTERNAL"
                        else -> "UNKNOWN ($level)"
                    }
                    hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false

                    val capabilities = characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
                    if (capabilities != null) {
                        isDepthCapable = capabilities.contains(
                            CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT
                        )
                    }
                    break
                }
            }

            CameraHardwareInfo(
                hasBackCamera = backCameraId != null,
                backCameraId = backCameraId,
                hardwareLevel = hardwareLevelStr,
                hasFlash = hasFlash,
                isDepthCapable = isDepthCapable
            )
        } catch (e: Exception) {
            CameraHardwareInfo(
                hasBackCamera = false,
                backCameraId = null,
                hardwareLevel = "Error querying Camera2: ${e.message}",
                hasFlash = false,
                isDepthCapable = false
            )
        }
    }
}
