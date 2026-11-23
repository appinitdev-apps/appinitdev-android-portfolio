@Composable
fun FeedsPageEffects(feedsVM: FeedsViewModel) {
    LaunchedEffect(Channel.sharedFlow) {
        Channel.sharedFlow.collect { event ->
            when (event) {
                is PickFileResultEvent -> {
                    if (event.tag != PickFileTag.FEED) return@collect
                    val uri = event.uris.first()
                    InputStreamReader(contentResolver.openInputStream(uri)!!).use { reader ->
                        DialogHelper.showLoading()
                        withIO {
                            try {
                                FeedHelper.importAsync(reader)
                                feedsVM.loadAsync(withCount = true)
                                DialogHelper.hideLoading()
                            } catch (ex: Exception) {
                                DialogHelper.hideLoading()
                                DialogHelper.showMessage(ex.toString())
                            }
                        }
                    }
                }
                is ExportFileResultEvent -> {
                    if (event.type == ExportFileType.OPML) {
                        OutputStreamWriter(contentResolver.openOutputStream(event.uri)!!, Charsets.UTF_8).use { writer ->
                            withIO { FeedHelper.exportAsync(writer) }
                        }
                        val fileName = contentResolver.queryOpenableFileName(event.uri)
                        DialogHelper.showConfirmDialog("", LocaleHelper.getStringFAsync(Res.string.exported_to, "name", fileName))
                    }
                }
            }
        }
    }
}
