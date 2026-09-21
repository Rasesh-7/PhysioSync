package com.example.physiosync.core.model

/**
 * Deterministic form classification flags with explainable concrete rationale.
 */
enum class FormFlag(val description: String, val coachingTip: String) {
    GOOD(
        description = "Proper form executed.",
        coachingTip = "Good repetition! Keep up the form."
    ),
    REDUCED_ROM(
        description = "Reduced Range of Motion (ROM). Full extension target was not reached.",
        coachingTip = "Extend your knee further to reach full target extension."
    ),
    IRREGULAR_TEMPO(
        description = "Irregular movement tempo. Movement executed too fast or inconsistently.",
        coachingTip = "Slow down and control your movement tempo."
    ),
    LOW_CONFIDENCE(
        description = "Low keypoint confidence during rep detection.",
        coachingTip = "Please ensure full side-view visibility in camera frame."
    )
}
