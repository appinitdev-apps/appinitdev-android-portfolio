data class SeekbarState(
        val position: Float,
        val max: Float,
        val min: Float,
        val stepSize: Float = 0f,
        val default: Float = 0f,
        val leftLabel: Boolean = false,
        val rightLabel: Boolean = false,
        val leftLabelProvider: ((progress: Float, min: Float, max: Float) -> String)? = null,
        val rightLabelProvider: ((progress: Float, min: Float, max: Float) -> String)? = null,
)
