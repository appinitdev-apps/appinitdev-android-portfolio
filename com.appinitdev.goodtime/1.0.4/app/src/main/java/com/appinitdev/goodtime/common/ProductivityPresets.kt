data class ProductivityPreset(
    val id: String,
    val name: String,
    val description: String,
    val workDurationMins: Int,
    val breakDurationMins: Int,
    val longBreakDurationMins: Int,
    val sessionsBeforeLongBreak: Int,
    val isCountdown: Boolean = true
) {
    fun toTimerProfile(): TimerProfile {
        return TimerProfile(
            name = name,
            workDuration = workDurationMins,
            breakDuration = breakDurationMins,
            longBreakDuration = longBreakDurationMins,
            sessionsBeforeLongBreak = sessionsBeforeLongBreak,
            isBreakEnabled = breakDurationMins > 0,
            isLongBreakEnabled = longBreakDurationMins > 0,
            isCountdown = isCountdown
        )
    }
}
object ProductivityPresets {
    val CLASSIC_POMODORO = ProductivityPreset(
        id = "classic_pomodoro",
        name = "Classic Pomodoro",
        description = "25 minutes focus followed by a 5-minute break. Ideal for structured tasks.",
        workDurationMins = 25,
        breakDurationMins = 5,
        longBreakDurationMins = 15,
        sessionsBeforeLongBreak = 4
    )
    val RULE_52_17 = ProductivityPreset(
        id = "rule_52_17",
        name = "Rule 52/17",
        description = "52 minutes intense work followed by 17 minutes complete rest. Maximizes peak mental performance.",
        workDurationMins = 52,
        breakDurationMins = 17,
        longBreakDurationMins = 30,
        sessionsBeforeLongBreak = 3
    )
    val ULTRADIAN_RHYTHM = ProductivityPreset(
        id = "ultradian",
        name = "Ultradian Rhythm",
        description = "90 minutes deep focus aligned with natural body energy cycles, followed by 20 minutes rest.",
        workDurationMins = 90,
        breakDurationMins = 20,
        longBreakDurationMins = 30,
        sessionsBeforeLongBreak = 2
    )
    val RULE_50_10 = ProductivityPreset(
        id = "rule_50_10",
        name = "Rule 50/10",
        description = "50 minutes focus and 10 minutes break. Excellent balance for long workdays.",
        workDurationMins = 50,
        breakDurationMins = 10,
        longBreakDurationMins = 20,
        sessionsBeforeLongBreak = 3
    )
    val FLOW_STATE_STOPWATCH = ProductivityPreset(
        id = "flow_state",
        name = "Flow State (Stopwatch)",
        description = "Open-ended timer counting up. Best for deep creative work without countdown pressure.",
        workDurationMins = 0,
        breakDurationMins = 0,
        longBreakDurationMins = 0,
        sessionsBeforeLongBreak = 0,
        isCountdown = false
    )
    val ALL_PRESETS = listOf(
        CLASSIC_POMODORO,
        RULE_52_17,
        ULTRADIAN_RHYTHM,
        RULE_50_10,
        FLOW_STATE_STOPWATCH
    )
}
