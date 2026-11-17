# Development Roadmap

This roadmap is provisional. Each stage should produce a working APK and documented test results before the next stage is considered complete.

## Foundation
- [x] Create public GitHub repository
- [x] Add project overview
- [ ] Confirm exact phone model and Android version
- [ ] Verify SDK, JDK, Gradle, and Android build-tool compatibility
- [ ] Verify ARCore support for the exact device variant

## Prototype 1 — camera and depth diagnostics
- [ ] Create minimal Kotlin Android app
- [ ] Initialize and safely close AR session
- [ ] Display camera preview
- [ ] Check tracking state and depth availability
- [ ] Visualize depth data where supported
- [ ] Display confidence/quality diagnostics where available
- [ ] Build and install APK on the physical phone
- [ ] Record test conditions and limitations

**Acceptance:** app launches reliably, manages lifecycle correctly, and demonstrates depth availability—or clearly reports that it is unavailable—on the target device.

## Prototype 2 — object selection and depth sampling
- [ ] Add object detection
- [ ] Add segmentation or user-assisted object selection
- [ ] Sample depth from valid object pixels
- [ ] Show approximate point/region distance
- [ ] Reject invalid or low-confidence samples

## Prototype 3 — first 3D dimensions
- [ ] Back-project depth into camera-space points
- [ ] Transform points using matching camera poses
- [ ] Accumulate multiple views
- [ ] Filter outliers and background points
- [ ] Fit a bounding box
- [ ] Display L × W × H with quality warnings

## Prototype 4 — guided scan and validation
- [ ] Add scan-path guidance and coverage feedback
- [ ] Add scan completion checks
- [ ] Validate against reference measurements
- [ ] Measure repeatability across trials
- [ ] Document supported object types and failure cases

## Later enhancements
- [ ] Measurement history
- [ ] 3D preview
- [ ] CSV/PDF measurement report
- [ ] PLY/OBJ/GLB export, subject to implementation and validation
- [ ] Additional device compatibility testing

## Release gate
Do not call the app accurate or production-ready until it has a documented test protocol, ground-truth comparisons, repeatability results, device/environment limitations, and clear uncertainty/failure-state behavior.
