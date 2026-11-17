# Dimension Detector

AI-assisted Android application for estimating real-world object dimensions (Length × Width × Height) using a phone camera and depth-aware 3D scanning.

> **Project status:** Foundation / planning. Device compatibility and measurement accuracy must be verified through testing; no professional-accuracy claim is made.

## Goal

Build an Android app that can detect an object, guide a user through a multi-view scan, reconstruct useful 3D geometry from available depth and camera-pose data, and estimate its dimensions with clear quality indicators.

## Measurement modes

### Single-photo mode
A normal RGB image alone cannot determine absolute real-world scale. This mode therefore needs an explicit scale constraint, such as a known-size reference, known camera-to-object distance, or supported depth data.

### Guided 3D scan mode
The primary workflow. The app guides the user to capture multiple viewpoints and combines available depth maps and camera poses to estimate the object's 3D dimensions.

## Initial technical direction

- Android, Kotlin, Jetpack Compose
- ARCore for tracking/depth, subject to exact-device capability verification
- On-device object detection and segmentation
- Depth back-projection, point-cloud processing, and bounding-box fitting
- Room for local measurement history

Library versions and ML model choices will be verified for compatibility and licensing before implementation.

## Accuracy principles

- Estimates are not professional measurements unless validated against reference instruments.
- Scan coverage, sensor confidence, and measurement error are distinct metrics.
- Device, lighting, distance, surface material, and object geometry affect results.
- The app should flag insufficient scans rather than display misleading precision.

## Roadmap

1. Camera/AR session and depth diagnostics.
2. Object selection, segmentation, and depth sampling.
3. Multi-view point-cloud accumulation and first dimension estimates.
4. Guided scanning, validation, and measurement history.
5. Later: 3D preview and export.

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) and [docs/ROADMAP.md](docs/ROADMAP.md).

## License

No open-source license has been selected yet. Public visibility does not automatically grant permission to reuse this code.
