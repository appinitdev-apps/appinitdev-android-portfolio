
@Composable
fun AdBanner(modifier: Modifier = Modifier, isVip: Boolean = false) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            val adsRemoved = PreferencesManager(context).isAdsRemoved() == true
            if (!BuildConfig.DEBUG && !adsRemoved && !isVip) {
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = "ca-app-pub-8325653088416363/8429278726"
                    loadAd(AdRequest.Builder().build())
                }
            } else {
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = "ca-app-pub-8325653088416363/8429278726"
                    visibility = View.GONE
                }
            }
        },
        update = { adView ->
            if (BuildConfig.DEBUG || isVip || PreferencesManager(adView.context).isAdsRemoved() == true) {
                adView.visibility = View.GONE
            } else {
                adView.visibility = View.VISIBLE
            }
        }
    )
}