class SaveAsActivity : SimpleActivity() {
    private val binding by viewBinding(ActivitySaveAsBinding::inflate)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        tryInitFileManager()
    }
    private fun tryInitFileManager() {
        handleStoragePermission { granted ->
            if (granted) {
                saveAsDialog()
            } else {
                toast(C.string.no_storage_permissions)
                finish()
            }
        }
    }
    private fun saveAsDialog() {
        if (intent.action == Intent.ACTION_SEND && intent.extras?.containsKey(Intent.EXTRA_STREAM) == true) {
            FilePickerDialog(this, pickFile = false, showHidden = config.shouldShowHidden(), showFAB = true, showFavoritesButton = true) {
                val destination = it
                handleSAFDialog(destination) {
                    toast(C.string.saving)
                    ensureBackgroundThread {
                        try {
                            if (!getDoesFilePathExist(destination)) {
                                if (needsStupidWritePermissions(destination)) {
                                    val document = getDocumentFile(destination)
                                    document!!.createDirectory(destination.getFilenameFromPath())
                                } else {
                                    File(destination).mkdirs()
                                }
                            }
                            val source = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)!!
                            val originalFilename = getFilenameFromContentUri(source)
                                ?: source.toString().getFilenameFromPath()
                            val filename = sanitizeFilename(originalFilename)
                            val mimeType = contentResolver.getType(source)
                                ?: intent.type?.takeIf { it != "*/*" }
                                ?: filename.getMimeType()
                            val inputStream = contentResolver.openInputStream(source)
                            val destinationPath = "$destination/$filename"
                            val outputStream = getFileOutputStreamSync(destinationPath, mimeType, null)!!
                            inputStream!!.copyTo(outputStream)
                            rescanPaths(arrayListOf(destinationPath))
                            toast(C.string.file_saved)
                            finish()
                        } catch (e: Exception) {
                            showErrorToast(e)
                            finish()
                        }
                    }
                }
            }
        } else {
            toast(C.string.unknown_error_occurred)
            finish()
        }
    }
    override fun onResume() {
        super.onResume()
        setupToolbar(binding.activitySaveAsToolbar, NavigationIcon.Arrow)
    }
    private fun sanitizeFilename(filename: String): String {
        return filename.replace("[/\\\\<>:\"|?*\u0000-\u001F]".toRegex(), "_")
            .takeIf { it.isNotBlank() } ?: "unnamed_file"
    }
}
