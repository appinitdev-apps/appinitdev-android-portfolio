
object AdManager {

    private var isInitialized = false

    private fun isDebug(context: Context): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    fun initialize(context: Context) {
        if (isDebug(context) || isInitialized) return

        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        val consentInformation = UserMessagingPlatform.getConsentInformation(context)
        consentInformation.requestConsentInfoUpdate(
            context as android.app.Activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    context
                ) { loadAndShowError ->
                    if (loadAndShowError != null) {
                        Log.w(
                            ContentValues.TAG,
                            "${loadAndShowError.errorCode}: ${loadAndShowError.message}"
                        )
                    }
                    if (consentInformation.canRequestAds()) {
                        initMobileAds(context)
                    }
                }
            },
            { requestConsentError ->
                Log.w(
                    ContentValues.TAG,
                    "${requestConsentError.errorCode}: ${requestConsentError.message}"
                )
            }
        )

        if (consentInformation.canRequestAds()) {
            initMobileAds(context)
        }
    }

    private fun initMobileAds(context: Context) {
        if (!isInitialized) {
            MobileAds.initialize(context)
            isInitialized = true
        }
    }
}

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val isDebug = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    if (isDebug) {
        
        return
    }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = ctx.getString(R.string.ad_unit_id_banner_default)
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
