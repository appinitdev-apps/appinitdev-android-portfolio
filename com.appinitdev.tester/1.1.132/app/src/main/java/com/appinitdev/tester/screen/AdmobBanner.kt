@Composable
fun AdmobBanner() {
    if (BuildConfig.DEBUG) return
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-8325653088416363/2056231085"
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}