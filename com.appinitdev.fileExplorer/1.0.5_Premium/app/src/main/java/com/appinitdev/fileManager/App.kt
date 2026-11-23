

class App : AppinitdevApp() {
    override val isAppLockFeatureAvailable = true

    override fun onCreate() {
        super.onCreate()
        Reprint.initialize(this)
    }
}
