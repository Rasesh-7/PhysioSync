package com.example.physiosync.analysis

import com.example.physiosync.core.config.ExerciseConfig
import com.example.physiosync.core.model.LandmarkType
import com.example.physiosync.core.model.Point2D
import com.example.physiosync.core.model.PoseFrame
import kotlin.math.acos
import kotlin.math.sqrt

/**
 * Result of joint angle calculation.
 */
data class JointAngleResult(
    val angleDegrees: Float,
    val isLeftLeg: Boolean,
    val averageConfidence: Float
)

/**
 * Calculates joint angles from 3 body keypoints (Hip, Knee, Ankle) using 2D vector geometry.
 */
object KneeAngleCalculator {

    fun calculateKneeAngle(
        poseFrame: PoseFrame?,
        config: ExerciseConfig = ExerciseConfig()
    ): JointAngleResult? {
        if (poseFrame == null || poseFrame.landmarks.isEmpty()) return null

        val minConfidence = config.minKeypointConfidence

        // Evaluate Left Leg keypoints
        val leftHip = poseFrame.getLandmark(LandmarkType.LEFT_HIP)
        val leftKnee = poseFrame.getLandmark(LandmarkType.LEFT_KNEE)
        val leftAnkle = poseFrame.getLandmark(LandmarkType.LEFT_ANKLE)

        val leftValid = leftHip != null && leftKnee != null && leftAnkle != null &&
                leftHip.confidence >= minConfidence &&
                leftKnee.confidence >= minConfidence &&
                leftAnkle.confidence >= minConfidence

        val leftAvgConf = if (leftValid) {
            (leftHip!!.confidence + leftKnee!!.confidence + leftAnkle!!.confidence) / 3f
        } else 0f

        // Evaluate Right Leg keypoints
        val rightHip = poseFrame.getLandmark(LandmarkType.RIGHT_HIP)
        val rightKnee = poseFrame.getLandmark(LandmarkType.RIGHT_KNEE)
        val rightAnkle = poseFrame.getLandmark(LandmarkType.RIGHT_ANKLE)

        val rightValid = rightHip != null && rightKnee != null && rightAnkle != null &&
                rightHip.confidence >= minConfidence &&
                rightKnee.confidence >= minConfidence &&
                rightAnkle.confidence >= minConfidence

        val rightAvgConf = if (rightValid) {
            (rightHip!!.confidence + rightKnee!!.confidence + rightAnkle!!.confidence) / 3f
        } else 0f

        if (!leftValid && !rightValid) return null

        // Select side with higher confidence
        val isLeft = leftAvgConf >= rightAvgConf

        val (hipPt, kneePt, anklePt) = if (isLeft) {
            Triple(leftHip!!.point2D, leftKnee!!.point2D, leftAnkle!!.point2D)
        } else {
            Triple(rightHip!!.point2D, rightKnee!!.point2D, rightAnkle!!.point2D)
        }

        val angle = computeAngleBetweenPoints(hipPt, kneePt, anklePt)
        val selectedConf = if (isLeft) leftAvgConf else rightAvgConf

        return JointAngleResult(
            angleDegrees = angle,
            isLeftLeg = isLeft,
            averageConfidence = selectedConf
        )
    }

    /**
     * Computes the interior angle in degrees at vertex point B (Knee) formed by BA (Hip) and BC (Ankle).
     */
    fun computeAngleBetweenPoints(a: Point2D, b: Point2D, c: Point2D): Float {
        // Vector BA (Knee to Hip)
        val baX = (a.x - b.x).toDouble()
        val baY = (a.y - b.y).toDouble()

        // Vector BC (Knee to Ankle)
        val bcX = (c.x - b.x).toDouble()
        val bcY = (c.y - b.y).toDouble()

        val dotProduct = baX * bcX + baY * bcY
        val magBA = sqrt(baX * baX + baY * baY)
        val magBC = sqrt(bcX * bcX + bcY * bcY)

        if (magBA == 0.0 || magBC == 0.0) return 0f

        val cosTheta = (dotProduct / (magBA * magBC)).coerceIn(-1.0, 1.0)
        val angleRad = acos(cosTheta)

        return (angleRad * (180.0 / Math.PI)).toFloat()
    }
}
