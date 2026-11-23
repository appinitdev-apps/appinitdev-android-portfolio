class App : AppinitdevApp() {
    override val isAppLockFeatureAvailable = true
    override fun onCreate() {
        super.onCreate()
        Reprint.initialize(this)
        Picasso.setSingletonInstance(Picasso.Builder(this).downloader(object : Downloader {
            override fun load(request: Request) = Response.Builder().build()
            override fun shutdown() {}
        }).build())
    }
}