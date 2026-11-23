
object TabUtil {
    fun openCustomTab(url: String, pkgManager: PackageManager, context: Context) {
        val PACKAGE_NAME = "com.android.chrome"
        val customTabBuilder = CustomTabsIntent.Builder()

        customTabBuilder.setToolbarColor(ContextCompat.getColor(context, com.appinitdev.science.R.color.colorLightPrimary))
        customTabBuilder.setSecondaryToolbarColor(ContextCompat.getColor(context, com.appinitdev.science.R.color.colorLightPrimary))
        customTabBuilder.setShowTitle(true)

        val CustomTab = customTabBuilder.build()
        val intent = CustomTab.intent
        intent.data = Uri.parse(url)

        val packageManager = pkgManager
        val resolveInfoList = packageManager.queryIntentActivities(CustomTab.intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resolveInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            if (TextUtils.equals(packageName, PACKAGE_NAME))
                CustomTab.intent.setPackage(PACKAGE_NAME)
        }
        try {
            CustomTab.intent.data?.let { it1 -> CustomTab.launchUrl(context, it1) }
        }
        catch(e: IOException) {
            ToastUtil.showToast(context, "Error Code: 11001")
        }
    }
}