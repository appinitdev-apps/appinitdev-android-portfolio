
@Composable
fun AdmobBanner() {
    if (BuildConfig.DEBUG) return

    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                // ID de banner de prueba: ca-app-pub-3940256099942544/6300978111
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-8325653088416363/2056231085"
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}