fun SchemaBuilder.addPomodoroSchema() {
    query("pomodoroSettings") {
        resolver { ->
            PomodoroSettingsPreference.getValueAsync().toModel()
        }
    }
    query("pomodoroToday") {
        resolver { ->
            val dao = AppDatabase.instance.pomodoroItemDao()
            val today = TimeHelper.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
            val vm = MainActivity.instance.get()!!.pomodoroVM
            PomodoroToday(
                date = today,
                completedCount = vm.completedCount.intValue,
                currentRound = vm.currentRound.intValue,
                timeLeft = vm.timeLeft.intValue,
                totalTime = vm.settings.value.getTotalSeconds(vm.currentState.value),
                isRunning = vm.isRunning.value,
                isPause = vm.isPaused.value,
                state = vm.currentState.value
            )
        }
    }
    mutation("startPomodoro") {
        resolver { timeLeft: Int ->
            sendEvent(HPomodoroStartEvent(timeLeft))
            true
        }
    }
    mutation("pausePomodoro") {
        resolver { ->
            sendEvent(HPomodoroPauseEvent())
            true
        }
    }
    mutation("stopPomodoro") {
        resolver { ->
            sendEvent(HPomodoroStopEvent())
            true
        }
    }
}
