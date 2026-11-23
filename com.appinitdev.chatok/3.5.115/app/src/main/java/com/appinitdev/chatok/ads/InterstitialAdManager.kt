
object InterstitialAdManager {
    private var interstitialAd: InterstitialAd? = null

    fun loadAd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, "ca-app-pub-8325653088416363/8146905130", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                interstitialAd = null
            }
        })
    }

    fun showAd(activity: Activity) {
        interstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadAd(activity) 
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    interstitialAd = null
                    loadAd(activity) 
                }
            }
            ad.show(activity)
        } ?: loadAd(activity) 
    }
}
