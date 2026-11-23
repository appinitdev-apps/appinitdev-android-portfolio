
data class BreakExercise(
    val title: String,
    val instruction: String,
    val durationSeconds: Int,
    val iconResName: String
)

object BreakExercises {

    val EXERCISES = listOf(
        BreakExercise(
            title = "20-20-20 Eye Rest",
            instruction = "Look at an object at least 20 feet (6 meters) away for 20 seconds to reduce eye strain.",
            durationSeconds = 20,
            iconResName = "ic_eye"
        ),
        BreakExercise(
            title = "Shoulder & Neck Stretch",
            instruction = "Roll your shoulders backward 5 times, then gently tilt your head side to side.",
            durationSeconds = 30,
            iconResName = "ic_stretch"
        ),
        BreakExercise(
            title = "Deep Diaphragmatic Breathing",
            instruction = "Inhale slowly for 4 seconds, hold for 4 seconds, exhale for 6 seconds. Repeat 3 times.",
            durationSeconds = 45,
            iconResName = "ic_breath"
        ),
        BreakExercise(
            title = "Hand & Wrist Release",
            instruction = "Extend your arms forward, flex your wrist up and down gently for 15 seconds each.",
            durationSeconds = 30,
            iconResName = "ic_hand"
        )
    )

    fun getRandomExercise(): BreakExercise {
        return EXERCISES.random()
    }
}
