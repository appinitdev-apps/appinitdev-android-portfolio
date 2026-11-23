
/** BottomSheet inside note for adding files, recording audio. */
class AddBottomSheet(callbacks: AddActions, @ColorInt color: Int?) :
    ActionBottomSheet(createActions(callbacks), color) {

    companion object {
        const val TAG = "AddBottomSheet"

        fun createActions(callbacks: AddActions) =
            listOf(
                Action(R.string.take_photo, R.drawable.add_images) { _ ->
                    callbacks.takePhoto()
                    true
                },
                Action(R.string.create_drawing, R.drawable.edit) { _ ->
                    callbacks.createDrawing()
                    true
                },
                Action(R.string.add_images, R.drawable.add_images) { _ ->
                    callbacks.addImages()
                    true
                },
                Action(R.string.attach_file, R.drawable.text_file) { _ ->
                    callbacks.attachFiles()
                    true
                },
                Action(R.string.speech_to_text, R.drawable.record_audio) { _ ->
                    callbacks.speechToText()
                    true
                },
            ) +
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    listOf(
                        Action(R.string.record_audio, R.drawable.record_audio) { _ ->
                            callbacks.recordAudio()
                            true
                        }
                    )
                else listOf()
    }
}

interface AddActions {
    fun takePhoto()

    fun createDrawing()

    fun addImages()

    fun attachFiles()

    fun recordAudio()

    fun speechToText()
}
