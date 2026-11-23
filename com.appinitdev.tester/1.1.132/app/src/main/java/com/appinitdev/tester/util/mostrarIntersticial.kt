
fun mostrarIntersticial(context: Context) {
    val adRequest = AdRequest.Builder().build()
    // Reemplaza con tu ID de anuncio real o el de prueba: ca-app-pub-3940256099942544/1033173712
    InterstitialAd.load(context, "ca-app-pub-8325653088416363/7383885437", adRequest, object : InterstitialAdLoadCallback() {
        override fun onAdLoaded(interstitialAd: InterstitialAd) {
            interstitialAd.show(context as Activity)
        }
        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
            // Manejar error si es necesario
        }
    })
}