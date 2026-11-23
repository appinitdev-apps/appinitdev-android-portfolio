
fun mostrarIntersticial(context: Context) {
    val adRequest = AdRequest.Builder().build()
    
    InterstitialAd.load(context, "ca-app-pub-8325653088416363/7383885437", adRequest, object : InterstitialAdLoadCallback() {
        override fun onAdLoaded(interstitialAd: InterstitialAd) {
            interstitialAd.show(context as Activity)
        }
        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
            
        }
    })
}