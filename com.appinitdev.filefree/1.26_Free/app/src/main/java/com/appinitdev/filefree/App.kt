

class App : AppinitdevApp() {
    override val isAppLockFeatureAvailable = true

    override fun onCreate() {
        super.onCreate()
        Reprint.initialize(this)
        try {
            MobileAds.initialize(this) {}
            InterstitialAdManager.loadAd(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
