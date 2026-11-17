# Device Compatibility — Samsung Galaxy A15 5G

## Official compatibility check

Checked against Google's ARCore supported devices list on 17 November 2026.

- Device family: Samsung Galaxy A15 5G
- ARCore: listed as supported
- Depth API: the device-list entry explicitly says **Supports Depth API**
- Official source: https://developers.google.com/ar/devices

## Important distinction

ARCore Depth API does not require a dedicated Time-of-Flight (ToF) sensor. Google's documentation explains that depth can be calculated from device motion and may be combined with hardware depth-sensor data when available.

Official guide: https://developers.google.com/ar/develop/java/depth/developer-guide

Do not infer that this phone has LiDAR or a dedicated ToF sensor from Depth API compatibility.

## Runtime checks required

Depth is disabled by default. The app must query support on the actual device and enable the mode only when supported:

```kotlin
val config = session.config
val depthSupported =
    session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)

if (depthSupported) {
    config.depthMode = Config.DepthMode.AUTOMATIC
}
session.configure(config)
```

The runtime check is authoritative for the installed device/software combination.

## Prototype 1 validation checklist

- [ ] Confirm exact model number in Android Settings > About phone
- [ ] Confirm Google Play Services for AR is installed and up to date
- [ ] Create and resume ARCore session successfully
- [ ] Confirm tracking state becomes TRACKING
- [ ] Confirm runtime Depth API support
- [ ] Enable AUTOMATIC depth mode
- [ ] Acquire depth images over multiple frames
- [ ] Check depth image dimensions, timestamps, and valid samples
- [ ] Move around a textured, non-reflective object and observe depth stability
- [ ] Record failures, lighting, distance, and surface conditions

## Limitations

The official compatibility listing confirms API support, not a guaranteed measurement accuracy. Depth quality and dimension error must be measured experimentally on the actual phone. A supported Depth API does not imply millimetre-level metrology.
