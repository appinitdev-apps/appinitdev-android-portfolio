
class SettingsActivity : BaseActivity() {

    data class LanguageOption(val englishName: String, val nativeName: String, val locale: Locale) {
        fun getDisplayName(): String =
            if (englishName == nativeName) englishName else "$englishName ($nativeName)"
    }

    
    private var backCallback: OnBackPressedCallback? = null

    
    private var onBackInvokedCb: android.window.OnBackInvokedCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themePreference = ThemePreference(this)
        val themePrefValue = themePreference.getValue()

        if (themePrefValue == 100) {
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_NO -> { setTheme(R.style.AppTheme) }
                Configuration.UI_MODE_NIGHT_YES -> { setTheme(R.style.AppThemeDark) }
            }
        }
        if (themePrefValue == 0) { setTheme(R.style.AppTheme) }
        if (themePrefValue == 1) { setTheme(R.style.AppThemeDark) }
        setContentView(R.layout.activity_settings)

        if (themePrefValue == 100) {
            findViewById<TextView>(R.id.system_default_btn).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_radio_checked, 0, 0, 0)
            findViewById<TextView>(R.id.light_btn).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_radio_unchecked, 0, 0, 0)
            findViewById<TextView>(R.id.dark_btn).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_radio_unchecked, 0, 0, 0)
        }
        if (themePrefValue == 0) {
            findViewById<TextView>(R.id.system_default_btn).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_radio_unchecked, 0, 0, 0)
            findViewById<TextView>(R.id.light_btn).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_radio_checked, 0, 0, 0)
            findViewById<TextView>(R.id.dark_btn).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_radio_unchecked, 0, 0, 0)
        }
        if (themePrefValue == 1) {
            findViewById<TextView>(R.id.system_default_btn).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_radio_unchecked, 0, 0, 0)
            findViewById<TextView>(R.id.light_btn).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_radio_unchecked, 0, 0, 0)
            findViewById<TextView>(R.id.dark_btn).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_radio_checked, 0, 0, 0)
        }

        openPages()
        themeSettings()
        initializeCache()
        cacheSettings()
        initOfflineSwitches()
        initNavSwitches()
        initAnalyticsSwitch()

        findViewById<RelativeLayout>(R.id.offline_settings).setOnClickListener {
            findViewById<SwitchCompat>(R.id.offline_internet_switch).toggle()
        }
        findViewById<RelativeLayout>(R.id.nav_bar_settings).setOnClickListener {
            findViewById<SwitchCompat>(R.id.nav_bar_switch).toggle()
        }
        findViewById<RelativeLayout>(R.id.analytics_settings).setOnClickListener {
            findViewById<SwitchCompat>(R.id.analytics_switch).toggle()
        }

        findViewById<ConstraintLayout>(R.id.view).systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        
         




































        findViewById<ImageButton>(R.id.back_btn_set).setOnClickListener {
            this.onBackPressed()
        }

        findViewById<RelativeLayout>(R.id.unit_settings).setOnClickListener {
            val intent = Intent(this, UnitActivity::class.java)
            startActivity(intent)
        }


        
        
        backCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                
                val consumed = handleBackPress()
                if (consumed) {
                    
                    backCallback?.isEnabled = anyOverlayOpen()
                    return
                }

                
                
                backCallback?.isEnabled = false
                try {
                    if (isTaskRoot) {
                        moveTaskToBack(true)
                    } else {
                        finish()
                    }
                } catch (e: Exception) {
                    super@SettingsActivity.onBackPressed()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, backCallback!!)

        
        findViewById<RelativeLayout>(R.id.language_setting).setOnClickListener {
            showLanguageSelectionDialog()
        }
        updateLanguageContentText()
    }

    
    private fun anyOverlayOpen(): Boolean {
        val themePanel = findViewById<FrameLayout?>(R.id.theme_panel)
        return themePanel?.visibility == View.VISIBLE
    }

    
    private fun setBackInterceptionEnabled(enabled: Boolean) {
        backCallback?.isEnabled = enabled

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (enabled) {
                if (onBackInvokedCb == null) {
                    onBackInvokedCb = android.window.OnBackInvokedCallback {
                        
                        handleBackPress()
                        
                        if (!anyOverlayOpen()) {
                            
                            onBackInvokedDispatcher.unregisterOnBackInvokedCallback(onBackInvokedCb!!)
                            onBackInvokedCb = null
                            
                            backCallback?.isEnabled = false
                        }
                    }
                    onBackInvokedDispatcher.registerOnBackInvokedCallback(
                        OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                        onBackInvokedCb!!
                    )
                }
            } else {
                if (onBackInvokedCb != null) {
                    try {
                        onBackInvokedDispatcher.unregisterOnBackInvokedCallback(onBackInvokedCb!!)
                    } catch (_: Exception) {
                        
                    }
                    onBackInvokedCb = null
                }
            }
        }
    }

    

    private fun getSupportedLanguages(): List<LanguageOption> = listOf(
        LanguageOption("English", Locale("en").getDisplayLanguage(Locale("en")), Locale("en")),
        LanguageOption("Swedish", Locale("sv", "SE").getDisplayLanguage(Locale("sv", "SE")), Locale("sv", "SE")),
        LanguageOption("Spanish (Spain)", Locale("es", "ES").getDisplayLanguage(Locale("es", "ES")), Locale("es", "ES")),
        LanguageOption("Spanish (Argentina)", Locale("es", "AR").getDisplayLanguage(Locale("es", "AR")), Locale("es", "AR")),
        LanguageOption("Spanish (Mexico)", Locale("es", "MX").getDisplayLanguage(Locale("es", "MX")), Locale("es", "MX")),
        LanguageOption("Italian", Locale("it", "IT").getDisplayLanguage(Locale("it", "IT")), Locale("it", "IT")),
        LanguageOption("French", Locale("fr").getDisplayLanguage(Locale("fr")), Locale("fr")),
        LanguageOption("German", Locale("de").getDisplayLanguage(Locale("de")), Locale("de")),
        LanguageOption("Portuguese (Brazil)", Locale("pt", "BR").getDisplayLanguage(Locale("pt", "BR")), Locale("pt", "BR")),
        LanguageOption("Chinese (Simplified)", Locale("zh", "CN").getDisplayLanguage(Locale("zh", "CN")), Locale("zh", "CN")),
        LanguageOption("Afrikaans", Locale("af").getDisplayLanguage(Locale("af")), Locale("af")),
        LanguageOption("Hindi", Locale("hi").getDisplayLanguage(Locale("hi")), Locale("hi")),
        LanguageOption("Filipino", Locale.forLanguageTag("fil").getDisplayLanguage(Locale.forLanguageTag("fil")), Locale.forLanguageTag("fil")),
        LanguageOption("Urdu (Pakistan)", Locale("ur", "PK").getDisplayLanguage(Locale("ur", "PK")), Locale("ur", "PK")),
        LanguageOption("Urdu (India)", Locale("ur", "IN").getDisplayLanguage(Locale("ur", "IN")), Locale("ur", "IN"))
    )
    private fun getSystemLocale(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales.get(0)
        } else {
            resources.configuration.locale
        }
    }

    private fun getSystemLanguageOption(): LanguageOption {
        val systemLocale = getSystemLocale()
        return getSupportedLanguages().find {
            it.locale.language == systemLocale.language && (it.locale.country.isEmpty() || it.locale.country == systemLocale.country)
        } ?: getSupportedLanguages()[0]
    }

    private fun setupLanguageListener() {
        findViewById<RelativeLayout>(R.id.language_setting).setOnClickListener {
            showLanguageSelectionDialog()
        }
    }

    private fun showLanguageSelectionDialog() {
        val languages = getSupportedLanguages()
        val systemLangOption = getSystemLanguageOption()
        val languageNames = mutableListOf<String>()
        languageNames.add(getString(R.string.system_default))
        languageNames.addAll(languages.map { it.getDisplayName() })

        val currentPref = getCurrentLanguagePreference()
        var checkedItem = 0
        if (currentPref != null) {
            checkedItem = languages.indexOfFirst {
                it.locale.language == currentPref.language && it.locale.country == currentPref.country
            }
            if (checkedItem != -1) checkedItem += 1 else checkedItem = 0
        }

        
        val adapter = ArrayAdapter<String>(this, R.layout.dialog_language_item, R.id.lang_name, languageNames)

        
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.language_title))
            .setSingleChoiceItems(adapter, checkedItem) { dialogInterface, which ->
                when (which) {
                    0 -> setLanguagePreference(null) 
                    else -> setLanguagePreference(languages[which - 1].locale)
                }
                updateLanguageContentText()
                dialogInterface.dismiss()
                Handler().postDelayed({
                    LocaleUtil.restartForLocaleChange(this)
                }, 150)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_popup_bg)

        dialog.show()
    }
    private fun getColorFromAttr(attr: Int): Int {
        val typedValue = android.util.TypedValue()
        theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    private fun updateLanguageContentText() {
        val langOption = when (val pref = getCurrentLanguagePreference()) {
            null -> getSystemLanguageOption()
            else -> getSupportedLanguages().find {
                it.locale.language == pref.language && it.locale.country == pref.country
            } ?: getSystemLanguageOption()
        }
        findViewById<TextView>(R.id.language_content).text = langOption.getDisplayName()
    }

    private fun getCurrentLanguagePreference(): Locale? {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val lang = prefs.getString("app_language", null)
        val country = prefs.getString("app_country", "")
        return if (lang.isNullOrBlank()) null
        else Locale(lang, country ?: "")
    }

    private fun setLanguagePreference(locale: Locale?) {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        if (locale == null) {
            prefs.edit().remove("app_language").remove("app_country").apply()
        } else {
            prefs.edit()
                .putString("app_language", locale.language)
                .putString("app_country", locale.country ?: "")
                .apply()
        }
        
        com.appinitdev.science.utils.ElementDataLoader.clearCache()
    }

    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        val params = findViewById<FrameLayout>(R.id.common_title_back_set).layoutParams as ViewGroup.LayoutParams
        params.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        findViewById<FrameLayout>(R.id.common_title_back_set).layoutParams = params

        val titleParam = findViewById<FrameLayout>(R.id.title_box_settings).layoutParams as ViewGroup.MarginLayoutParams
        titleParam.rightMargin = right
        titleParam.leftMargin = left
        findViewById<FrameLayout>(R.id.title_box_settings).layoutParams = titleParam

        val params2 = findViewById<TextView>(R.id.element_title_downstate).layoutParams as ViewGroup.MarginLayoutParams
        params2.topMargin = top
        findViewById<TextView>(R.id.element_title_downstate).layoutParams = params2

        findViewById<LinearLayout>(R.id.personalization_box).setPadding(left, 0, right, 0)
        findViewById<LinearLayout>(R.id.advanced_box).setPadding(left, 0, right, 0)
    }

    
    private fun handleBackPress(): Boolean {
        val themePanel = findViewById<FrameLayout>(R.id.theme_panel)

        return if (themePanel.visibility == View.VISIBLE) {
            Utils.fadeOutAnim(themePanel, 300) 
            
            setBackInterceptionEnabled(false)
            true 
        } else {
            false 
        }
    }

    
    override fun onBackPressed() {
        if (!handleBackPress()) {
            super.onBackPressed()
        }
    }

    private fun initOfflineSwitches() {
        val offlinePreferences = offlinePreference(this)
        val offlinePrefValue = offlinePreferences.getValue()
        findViewById<SwitchCompat>(R.id.offline_internet_switch).isChecked = offlinePrefValue == 1

        findViewById<SwitchCompat>(R.id.offline_internet_switch).setOnCheckedChangeListener { _, _ ->
            if (findViewById<SwitchCompat>(R.id.offline_internet_switch).isChecked) { offlinePreferences.setValue(1) }
            else { offlinePreferences.setValue(0) }
        }
    }

    private fun initNavSwitches() {
        val navPreferences = hideNavPreference(this)
        val navPrefValue = navPreferences.getValue()
        findViewById<SwitchCompat>(R.id.nav_bar_switch).isChecked = navPrefValue == 1

        findViewById<SwitchCompat>(R.id.nav_bar_switch).setOnCheckedChangeListener { _, _ ->
            if (findViewById<SwitchCompat>(R.id.nav_bar_switch).isChecked) { navPreferences.setValue(1) }
            else { navPreferences.setValue(0) }
        }
    }

    private fun initAnalyticsSwitch() {
        val analyticsPreferences = AnalyticsPreference(this)
        val analyticsPrefValue = analyticsPreferences.getValue()
        findViewById<SwitchCompat>(R.id.analytics_switch).isChecked = analyticsPrefValue

        findViewById<SwitchCompat>(R.id.analytics_switch).setOnCheckedChangeListener { _, isChecked ->
            analyticsPreferences.setValue(isChecked)
            AnalyticsHelper.updateAnalyticsCollection(this)
            
            
            if (isChecked) {
                AnalyticsHelper.logEvent(this, "analytics_enabled", null)
            }
        }
    }

    private fun openPages() {
        findViewById<RelativeLayout>(R.id.favorite_settings).setOnClickListener {
            val intent = Intent(this, FavoritePageActivity::class.java)
            startActivity(intent)
        }
        findViewById<RelativeLayout>(R.id.order_settings).setOnClickListener {
            val intent = Intent(this, OrderActivity::class.java)
            startActivity(intent)
        }
        findViewById<RelativeLayout>(R.id.experimental_settings).setOnClickListener {
            val intent = Intent(this, ExperimentalActivity::class.java)
            startActivity(intent)
        }
    }

    private fun cacheSettings() {
        findViewById<LinearLayout>(R.id.cache_lay).setOnClickListener {
            this.cacheDir.deleteRecursively()
            initializeCache()
        }
    }

    private fun initializeCache() {
        try {
            var size: Long = 0
            size += getDirSize(this.cacheDir)
            size += getDirSize(this.externalCacheDir)
            (findViewById<View>(R.id.clear_cache_content) as TextView).text = readableFileSize(size)
        }
        catch(e: IOException) {
            ToastUtil.showToast(this, "Error Code: 11002")
        }
    }

    private fun getDirSize(dir: File?): Long {
        var size: Long = 0
        if (dir != null) {
            for (file in dir.listFiles()!!) {
                if (file != null && file.isDirectory) {
                    size += getDirSize(file)
                } else if (file != null && file.isFile) {
                    size += file.length()
                }
            }
        }
        return size
    }

    private fun readableFileSize(size: Long): String? {
        if (size <= 0) return "0 Bytes"
        val units = arrayOf("Bytes", "kB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())).toString() + " " + units[digitGroups]
    }

    private fun themeSettings() {
        val themePanel = findViewById<FrameLayout>(R.id.theme_panel)

        findViewById<TextView>(R.id.system_default_btn).setOnClickListener {
            val themePreference = ThemePreference(this)
            themePreference.setValue(100)
            val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            when (currentNightMode) {
                Configuration.UI_MODE_NIGHT_NO -> {
                    setTheme(R.style.AppTheme)
                } 
                Configuration.UI_MODE_NIGHT_YES -> {
                    setTheme(R.style.AppThemeDark)
                } 
            }
            Utils.fadeOutAnim(themePanel, 300)
            
            setBackInterceptionEnabled(false)

            val delayChange = Handler()
            delayChange.postDelayed({
                finish()
                overridePendingTransition(0, 0)
                startActivity(intent)
                SettingsActivity().finish()
                System.exit(0)
                overridePendingTransition(0, 0)
            }, 302)
        }
        findViewById<TextView>(R.id.light_btn).setOnClickListener {
            val themePreference = ThemePreference(this)
            themePreference.setValue(0)
            setTheme(R.style.AppTheme)
            Utils.fadeOutAnim(themePanel, 300)
            setBackInterceptionEnabled(false)

            val delayChange = Handler()
            delayChange.postDelayed({
                finish()
                overridePendingTransition(0, 0)
                startActivity(intent)
                SettingsActivity().finish()
                System.exit(0)
                overridePendingTransition(0, 0)
            }, 302)
        }
        findViewById<TextView>(R.id.dark_btn).setOnClickListener {
            val themePreference = ThemePreference(this)
            themePreference.setValue(1)
            setTheme(R.style.AppThemeDark)
            Utils.fadeOutAnim(themePanel, 300)
            setBackInterceptionEnabled(false)

            val delayChange = Handler()
            delayChange.postDelayed({
                finish()
                overridePendingTransition(0, 0)
                startActivity(intent)
                SettingsActivity().finish()
                System.exit(0)
                overridePendingTransition(0, 0)
            }, 302)
        }
        findViewById<RelativeLayout>(R.id.themes_settings).setOnClickListener {
            Utils.fadeInAnim(themePanel, 300)
            
            setBackInterceptionEnabled(true)
        }
        findViewById<TextView>(R.id.theme_background).setOnClickListener {
            Utils.fadeOutAnim(themePanel, 300)
            
            setBackInterceptionEnabled(false)
        }
        findViewById<TextView>(R.id.cancel_btn).setOnClickListener {
            Utils.fadeOutAnim(themePanel, 300)
            setBackInterceptionEnabled(false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        backCallback?.remove()
        backCallback = null
        if (onBackInvokedCb != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                onBackInvokedDispatcher.unregisterOnBackInvokedCallback(onBackInvokedCb!!)
            } catch (_: Exception) { }
            onBackInvokedCb = null
        }
    }
}