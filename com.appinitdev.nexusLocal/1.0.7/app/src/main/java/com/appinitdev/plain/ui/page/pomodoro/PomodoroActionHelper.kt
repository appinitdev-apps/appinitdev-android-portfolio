internal fun sendPomodoroAction(action: String, vm: PomodoroViewModel) {
    sendEvent(
        WebSocketEvent(
            EventType.POMODORO_ACTION, JsonHelper.jsonEncode(
                PomodoroActionData(
                    action, vm.timeLeft.intValue,
                    vm.settings.value.getTotalSeconds(vm.currentState.value),
                    vm.completedCount.intValue, vm.currentRound.value, vm.currentState.value
                )
            )
        )
    )
}
