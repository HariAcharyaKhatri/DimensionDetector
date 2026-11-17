# Technical Architecture (Draft)

## Design principles
- Keep camera/depth acquisition separate from perception and measurement.
- Preserve timestamps, camera intrinsics, poses, and depth confidence with each accepted frame.
- Process on-device where practical.
- Reject or qualify poor scans instead of returning false precision.
- Hide device-specific capabilities behind interfaces.

## Planned processing pipeline
```text
Camera / AR session
  -> RGB + intrinsics + pose + depth (+ confidence)
  -> object detection / user selection
  -> object segmentation mask
  -> depth filtering and camera-space back-projection
  -> transform points into a shared world frame
  -> multi-view accumulation and registration
  -> outlier filtering and geometry fitting
  -> L × W × H + quality indicators
  -> measurement record / visualization / export
```

## Camera and depth
The AR scanning workflow should have a single owner for the camera. When ARCore owns the camera, do not start a competing CameraX session against that same camera. A separate photo workflow may use CameraX when the AR session is inactive.

For each accepted frame, retain the RGB/depth data needed by the pipeline, camera intrinsics, camera pose, timestamps, tracking state, and confidence/quality metadata where exposed by the API. Follow the API's documented units, coordinate conventions, and image alignment rules.

## Geometry
Back-project valid depth pixels using camera intrinsics to obtain camera-space points. Transform them using the corresponding camera pose into a common scan frame. Restrict points using the selected object's segmentation mask, then accumulate and filter multiple views.

Candidate dimension methods:
- **AABB:** world-axis-aligned bounding box; simple but orientation-dependent.
- **OBB:** oriented bounding box; often more appropriate for rotated box-like objects.
- **Primitive fitting:** specialized methods for cylinders, spheres, planes, and supported shapes.
- **Mesh-derived dimensions:** possible later for irregular objects, with higher compute and validation needs.

No single fitting method is correct for every object class.

## Perception
Detection locates candidate objects; segmentation estimates their pixel boundaries and is a separate capability. Segmentation is important for excluding background depth.

Model selection must consider Android runtime support, latency and memory on the target phone, model quality, model size, and licensing.

## Measurement quality
Keep these distinct:
- **Coverage:** how much of the object was observed.
- **Confidence:** consistency/quality of sensor and algorithm evidence.
- **Uncertainty/error:** expected deviation from true dimensions.

Validate against reference tools such as calibrated rulers, calipers, or laser distance meters over repeated trials. Do not present excessive decimal precision.

## Suggested package boundaries
- `camera/` — camera lifecycle and frame abstraction
- `ar/` — AR session, tracking, depth capability checks
- `perception/` — detection and segmentation interfaces
- `geometry/` — back-projection, transforms, point clouds
- `measurement/` — fitting, dimensions, uncertainty
- `data/` — Room entities, repositories, export
- `ui/` — Compose screens and scan guidance

## Open engineering questions
- Exact ARCore/depth support for the target Galaxy A15 5G variant.
- Runtime and model choice after on-device profiling.
- Segmentation approach for common object classes.
- Achievable accuracy and repeatability by object size, material, and distance.
- Preferred 3D export formats and their implementation cost.
