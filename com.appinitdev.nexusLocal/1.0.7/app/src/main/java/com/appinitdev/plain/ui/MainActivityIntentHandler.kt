
internal fun MainActivity.handleIntent(intent: Intent) {
    if (intent.getBooleanExtra("start_web_service", false)) {
        coIO {
            WebPreference.putAsync(true)
            sendEvent(StartHttpServerEvent())
        }
    }

    if (intent.getBooleanExtra("navigate_to_web_settings", false)) {
        val nav = navControllerState.value
        val alreadyThere = nav?.currentBackStackEntry?.destination?.hasRoute(Routing.WebSettings::class) == true
        if (!alreadyThere) nav?.navigate(Routing.WebSettings)
    }

    if (intent.action == Intent.ACTION_VIEW) {
        val uri = intent.data ?: return
        val mimeType = contentResolver.getType(uri)
        if (mimeType != null) {
            if (mimeType.startsWith("text/")) navControllerState.value?.navigateTextFile(uri.toString())
            else if (mimeType == "application/pdf") navControllerState.value?.navigatePdf(uri)
            else DialogHelper.showErrorMessage(LocaleHelper.getString(Res.string.not_supported_error))
        } else {
            DialogHelper.showErrorMessage(LocaleHelper.getString(Res.string.not_supported_error))
        }
    } else if (intent.action == Intent.ACTION_SEND) {
        if (intent.type?.startsWith("text/") == true) {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return
            pendingFileUris = null
            pendingForwardText = sharedText
            showForwardTargetOptions(peerVM)
            return
        }
        val uri = intent.parcelable(Intent.EXTRA_STREAM) as? Uri ?: return
        pendingFileUris = setOf(uri)
        pendingForwardText = null
        showForwardTargetOptions(peerVM)
    } else if (intent.action == Intent.ACTION_SEND_MULTIPLE) {
        val uris = intent.parcelableArrayList<Uri>(Intent.EXTRA_STREAM)
        if (uris != null) {
            pendingFileUris = uris.toSet()
            pendingForwardText = null
            showForwardTargetOptions(peerVM)
        }
    } else if (intent.action == Constants.ACTION_PLAY_MEDIA) {
        val path = intent.getStringExtra(Constants.EXTRA_MEDIA_PATH) ?: return
        navControllerState.value?.navigate(Routing.PlayMedia(path))
    }
}

private fun MainActivity.showForwardTargetOptions(peerVM: PeerViewModel) {
    coMain {
        DialogHelper.showLoading()
        withIO { peerVM.loadPeers() }
        DialogHelper.hideLoading()
        showForwardTargetDialog = true
    }
}
