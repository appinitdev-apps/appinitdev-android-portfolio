class TimerServiceStarter(
    private val context: Context,
) : EventListener {
    override fun onEvent(event: Event) {
        when (event) {
            is Event.Start, is Event.Pause, is Event.AddOneMinute, is Event.UpdateActiveLabel -> startService()
            is Event.Reset -> startService(Action.Reset)
            is Event.Finished -> startServiceWithFinished(event.autostartNextSession)
            else -> {}
        }
    }
    private fun startService(action: Action = Action.StartOrUpdate) {
        context.startService(
            TimerService.createIntentWithAction(
                context,
                action,
            ),
        )
    }
    private fun startServiceWithFinished(autoStart: Boolean) {
        context.startService(
            TimerService.createFinishEvent(
                context,
                autoStart,
            ),
        )
    }
}
