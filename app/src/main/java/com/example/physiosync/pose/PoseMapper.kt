package com.example.physiosync.pose

import com.example.physiosync.core.model.BodyLandmark
import com.example.physiosync.core.model.LandmarkType
import com.example.physiosync.core.model.Point3D
import com.example.physiosync.core.model.PoseFrame
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * Maps ML Kit PoseLandmarks to PhysioSync pure Kotlin domain models.
 */
object PoseMapper {

    // ML Kit PoseLandmark integer constant mappings:
    // 11: LEFT_SHOULER, 12: RIGHT_SHOULER, 23: LEFT_HIP, 24: RIGHT_HIP
    // 25: LEFT_KNEE, 26: RIGHT_KNEE, 27: LEFT_ANKLE, 28: RIGHT_ANKLE
    // 7: LEFT_EAR, 8: RIGHT_EAR
    private val typeMapping: Map<Int, LandmarkType> = mapOf(
        PoseLandmark.LEFT_HIP to LandmarkType.LEFT_HIP,
        PoseLandmark.RIGHT_HIP to LandmarkType.RIGHT_HIP,
        PoseLandmark.LEFT_KNEE to LandmarkType.LEFT_KNEE,
        PoseLandmark.RIGHT_KNEE to LandmarkType.RIGHT_KNEE,
        PoseLandmark.LEFT_ANKLE to LandmarkType.LEFT_ANKLE,
        PoseLandmark.RIGHT_ANKLE to LandmarkType.RIGHT_ANKLE,
        11 to LandmarkType.LEFT_SHOULER,
        12 to LandmarkType.RIGHT_SHOULER,
        PoseLandmark.LEFT_EAR to LandmarkType.LEFT_EAR,
        PoseLandmark.RIGHT_EAR to LandmarkType.RIGHT_EAR
    )

    fun toDomainPoseFrame(pose: Pose, timestampMs: Long = System.currentTimeMillis()): PoseFrame {
        val landmarkMap = mutableMapOf<LandmarkType, BodyLandmark>()

        typeMapping.forEach { (mlKitType, domainType) ->
            val landmark = pose.getPoseLandmark(mlKitType)
            if (landmark != null) {
                val position3D = Point3D(
                    x = landmark.position3D.x,
                    y = landmark.position3D.y,
                    z = landmark.position3D.z
                )
                val bodyLandmark = BodyLandmark(
                    type = domainType,
                    position = position3D,
                    confidence = landmark.inFrameLikelihood
                )
                landmarkMap[domainType] = bodyLandmark
            }
        }

        return PoseFrame(landmarks = landmarkMap, timestampMs = timestampMs)
    }
}
