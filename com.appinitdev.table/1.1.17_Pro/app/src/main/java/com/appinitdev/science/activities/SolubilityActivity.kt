class SolubilityActivity : BaseActivity() {
    private var backCallback: OnBackPressedCallback? = null
    private var onBackInvokedCb: android.window.OnBackInvokedCallback? = null
    private val uiHandler = Handler(Looper.getMainLooper())
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
        setContentView(R.layout.activity_solubility) 
        findViewById<FrameLayout>(R.id.view_sub).systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        backCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                val consumed = handleBackPress()
                if (!consumed) {
                    isEnabled = false
                    try {
                        onBackPressedDispatcher.onBackPressed()
                    } finally {
                        isEnabled = false
                    }
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, backCallback!!)
        setBackInterceptionEnabled(false)
        infoPanel()
        val mostUsedPreference = MostUsedPreference(this)
        val mostUsedPrefValue = mostUsedPreference.getValue()
        val targetLabel = "sol"
        val regex = Regex("($targetLabel)=(\\d\\.\\d)")
        val match = regex.find(mostUsedPrefValue)
        if (match != null) {
            val value = match.groups[2]!!.value.toDouble()
            val newValue = value + 1
            mostUsedPreference.setValue(mostUsedPrefValue.replace("$targetLabel=$value", "$targetLabel=$newValue"))
        }
        findViewById<ImageButton>(R.id.back_btn).setOnClickListener {
            this.onBackPressed()
        }
    }
    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        val paramsO = findViewById<FrameLayout>(R.id.boxm).layoutParams as ViewGroup.MarginLayoutParams
        paramsO.topMargin = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        findViewById<FrameLayout>(R.id.boxm).layoutParams = paramsO
        val params2 = findViewById<FrameLayout>(R.id.common_title_back_sul).layoutParams as ViewGroup.LayoutParams
        params2.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        findViewById<FrameLayout>(R.id.common_title_back_sul).layoutParams = params2
    }
    private fun infoPanel() {
        findViewById<ImageButton>(R.id.info_btn).setOnClickListener {
            Anim.fadeIn(findViewById<ConstraintLayout>(R.id.info_panel), 300)
            findViewById<TextView>(R.id.info_title).text = resources.getString(R.string.solubility_info_t)
            findViewById<TextView>(R.id.info_text).text = resources.getString(R.string.solubility_info_c)
            setBackInterceptionEnabled(true)
        }
        findViewById<FloatingActionButton>(R.id.info_back_btn).setOnClickListener {
            Anim.fadeOutAnim(findViewById<ConstraintLayout>(R.id.info_panel), 300)
            setBackInterceptionEnabled(anyOverlayOpen())
        }
        findViewById<TextView>(R.id.info_background).setOnClickListener {
            Anim.fadeOutAnim(findViewById<ConstraintLayout>(R.id.info_panel), 300)
            setBackInterceptionEnabled(anyOverlayOpen())
        }
    }
    override fun onBackPressed() {
        if (!handleBackPress()) {
            super.onBackPressed()
        }
    }
    private fun anyOverlayOpen(): Boolean {
        val infoVisible = findViewById<ConstraintLayout>(R.id.info_panel).visibility == View.VISIBLE
        return infoVisible
    }
    private fun handleBackPress(): Boolean {
        val infoPanel = findViewById<ConstraintLayout>(R.id.info_panel)
        if (infoPanel.visibility == View.VISIBLE) {
            Anim.fadeOutAnim(infoPanel, 300)
            setBackInterceptionEnabled(anyOverlayOpen())
            return true
        }
        return false
    }
    private fun setBackInterceptionEnabled(enabled: Boolean) {
        backCallback?.isEnabled = enabled
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (enabled) {
                if (onBackInvokedCb == null) {
                    onBackInvokedCb = android.window.OnBackInvokedCallback {
                        uiHandler.post {
                            try {
                                onBackPressedDispatcher.onBackPressed()
                            } catch (e: Exception) {
                                val consumed = handleBackPress()
                                if (!consumed) {
                                    finish()
                                }
                            }
                        }
                    }
                    try {
                        onBackInvokedDispatcher.registerOnBackInvokedCallback(
                            android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                            onBackInvokedCb!!
                        )
                    } catch (_: Exception) {
                    }
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