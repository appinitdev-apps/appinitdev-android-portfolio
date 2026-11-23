
class FlipToFocusManager(
    context: Context,
    private val timerManager: TimerManager
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var isFaceDown = false
    private var isEnabled = false

    fun startListening() {
        if (!isEnabled && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
            isEnabled = true
        }
    }

    fun stopListening() {
        if (isEnabled) {
            sensorManager.unregisterListener(this)
            isEnabled = false
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val zAxis = event.values[2] 

        
        if (zAxis < -8.5f && !isFaceDown) {
            isFaceDown = true
            onDeviceFlippedFaceDown()
        }
        
        else if (zAxis > 8.0f && isFaceDown) {
            isFaceDown = false
            onDeviceFlippedFaceUp()
        }
    }

    private fun onDeviceFlippedFaceDown() {
        val timerData = timerManager.timerData.value
        if (timerData.state.isReset || timerData.state.isPaused) {
            timerManager.start()
        }
    }

    private fun onDeviceFlippedFaceUp() {
        val timerData = timerManager.timerData.value
        if (timerData.state.isRunning) {
            timerManager.toggle() 
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
