class ElementInfoActivity : InfoExtension() {
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
        val ElementSendAndLoadPreference = ElementSendAndLoad(this)
        var ElementSendAndLoadValue = ElementSendAndLoadPreference.getValue()
        setContentView(R.layout.activity_element_info)
        Utils.fadeInAnim(findViewById<ScrollView>(R.id.scr_view), 300)
        readJson()
        findViewById<CardView>(R.id.shell).visibility = View.GONE
        findViewById<CardView>(R.id.detail_emission).visibility = View.GONE
        detailViews()
        offlineCheck()
        nextPrev()
        favoriteBarSetup()
        elementAnim(findViewById<FrameLayout>(R.id.overview_inc), findViewById<FrameLayout>(R.id.properties_inc))
        findViewById<ConstraintLayout>(R.id.view).systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        findViewById<ImageButton>(R.id.back_btn).setOnClickListener { super.onBackPressed() }
        findViewById<FloatingActionButton>(R.id.edit_fav_btn).setOnClickListener {
            val intent = Intent(this, FavoritePageActivity::class.java)
            startActivity(intent)
        }
        findViewById<TextView>(R.id.get_pro_btn).setOnClickListener {
            val intent = Intent(this, ProActivity::class.java)
            startActivity(intent)
        }
        findViewById<TextView>(R.id.get_pro_hardness_btn).setOnClickListener {
            val intent = Intent(this, ProActivity::class.java)
            startActivity(intent)
        }
        findViewById<TextView>(R.id.get_pro_hazard_btn).setOnClickListener {
            val intent = Intent(this, ProActivity::class.java)
            startActivity(intent)
        }
        findViewById<TextView>(R.id.notes_sync_status).setOnClickListener {
            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
        }
        val proPref = ProVersion(this)
        var proPrefValue = proPref.getValue()
        if (proPrefValue==100) {
            proChanges()
        }
        else {
            findViewById<LinearLayout>(R.id.more_properties).visibility = View.VISIBLE 
            findViewById<LinearLayout>(R.id.hardness_properties).visibility = View.VISIBLE 
        }
        backCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                val consumed = handleBackPress()
                if (!consumed) {
                    backCallback?.isEnabled = false
                    try {
                        if (isTaskRoot) {
                            moveTaskToBack(true)
                        } else {
                            finish()
                        }
                    } catch (e: Exception) {
                        super@ElementInfoActivity.onBackPressed()
                    }
                } else {
                    backCallback?.isEnabled = anyOverlayOpen()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, backCallback!!)
    }
    private fun proChanges() {
        findViewById<FrameLayout>(R.id.pro_box).visibility = View.GONE
        findViewById<LinearLayout>(R.id.more_properties).visibility = View.VISIBLE
        findViewById<FrameLayout>(R.id.pro_hardness_box).visibility = View.GONE
        findViewById<LinearLayout>(R.id.hardness_properties).visibility = View.VISIBLE
        findViewById<FrameLayout>(R.id.pro_hazard_box).visibility = View.GONE
        findViewById<LinearLayout>(R.id.hazard_properties).visibility = View.VISIBLE
    }
    override fun onBackPressed() {
        if (!handleBackPress()) {
            super.onBackPressed()
        }
    }
    private fun anyOverlayOpen(): Boolean {
        val shell = findViewById<CardView?>(R.id.shell)
        val shellBg = findViewById<RealtimeBlurView?>(R.id.shell_background)
        val detail = findViewById<CardView?>(R.id.detail_emission)
        val detailBg = findViewById<RealtimeBlurView?>(R.id.detail_emission_background)
        return (shell?.visibility == View.VISIBLE) ||
                (shellBg?.visibility == View.VISIBLE) ||
                (detail?.visibility == View.VISIBLE) ||
                (detailBg?.visibility == View.VISIBLE)
    }
    private fun setBackInterceptionEnabled(enabled: Boolean) {
        backCallback?.isEnabled = enabled
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (enabled) {
                if (onBackInvokedCb == null) {
                    onBackInvokedCb = android.window.OnBackInvokedCallback {
                        val consumed = handleBackPress()
                        if (!anyOverlayOpen()) {
                            try {
                                onBackInvokedDispatcher.unregisterOnBackInvokedCallback(onBackInvokedCb!!)
                            } catch (_: Exception) { }
                            onBackInvokedCb = null
                            backCallback?.isEnabled = false
                            if (!consumed) {
                                try {
                                    if (isTaskRoot) moveTaskToBack(true) else finish()
                                } catch (_: Exception) {   }
                            }
                        }
                    }
                    onBackInvokedDispatcher.registerOnBackInvokedCallback(
                        android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                        onBackInvokedCb!!
                    )
                }
            } else {
                if (onBackInvokedCb != null) {
                    try {
                        onBackInvokedDispatcher.unregisterOnBackInvokedCallback(onBackInvokedCb!!)
                    } catch (_: Exception) { }
                    onBackInvokedCb = null
                }
            }
        }
    }
    private fun handleBackPress(): Boolean {
        val shell = findViewById<CardView>(R.id.shell)
        val shellBg = findViewById<RealtimeBlurView>(R.id.shell_background)
        val detail = findViewById<CardView>(R.id.detail_emission)
        val detailBg = findViewById<RealtimeBlurView>(R.id.detail_emission_background)
        return if (shellBg.visibility == View.VISIBLE || shell.visibility == View.VISIBLE) {
            Utils.fadeOutAnim(shell, 300)
            Utils.fadeOutAnim(shellBg, 300)
            setBackInterceptionEnabled(anyOverlayOpen())
            true
        } else if (detail.visibility == View.VISIBLE || detailBg.visibility == View.VISIBLE) {
            Utils.fadeOutAnim(detail, 300)
            Utils.fadeOutAnim(detailBg, 300)
            setBackInterceptionEnabled(anyOverlayOpen())
            true
        } else {
            false
        }
    }
    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        val params = findViewById<FrameLayout>(R.id.frame).layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        findViewById<FrameLayout>(R.id.frame).layoutParams = params
        val paramsO = findViewById<Space>(R.id.offline_space).layoutParams as ViewGroup.MarginLayoutParams
        paramsO.topMargin += top
        findViewById<Space>(R.id.offline_space).layoutParams = paramsO
        val params2 = findViewById<FrameLayout>(R.id.common_title_back).layoutParams as ViewGroup.LayoutParams
        params2.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        findViewById<FrameLayout>(R.id.common_title_back).layoutParams = params2
    }
    private fun offlineCheck() {
        val offlinePreferences = offlinePreference(this)
        val offlinePrefValue = offlinePreferences.getValue()
        if (offlinePrefValue == 1) {
            findViewById<FrameLayout>(R.id.frame).visibility = View.GONE
            findViewById<Space>(R.id.offline_space).visibility = View.VISIBLE
            findViewById<ImageView>(R.id.sp_img).visibility = View.GONE
            findViewById<TextView>(R.id.sp_offline).visibility = View.VISIBLE
            findViewById<TextView>(R.id.sp_offline).text = getString(R.string.go_online_for_emission)
        }
        else {
            findViewById<FrameLayout>(R.id.frame).visibility = View.VISIBLE
            findViewById<Space>(R.id.offline_space).visibility = View.GONE
            findViewById<ImageView>(R.id.sp_img).visibility = View.VISIBLE
            findViewById<TextView>(R.id.sp_offline).visibility = View.GONE
        }
    }
    override fun onResume() {
        super.onResume()
        favoriteBarSetup()
    }
    private fun detailViews() {
        findViewById<CardView>(R.id.electron_view).setOnClickListener {
            Utils.fadeInAnim(findViewById<CardView>(R.id.shell), 300)
            Utils.fadeInAnim(findViewById<RealtimeBlurView>(R.id.shell_background), 300)
            setBackInterceptionEnabled(true)
        }
        findViewById<FloatingActionButton>(R.id.close_shell_btn).setOnClickListener {
            Utils.fadeOutAnim(findViewById<CardView>(R.id.shell), 300)
            Utils.fadeOutAnim(findViewById<RealtimeBlurView>(R.id.shell_background), 300)
            setBackInterceptionEnabled(anyOverlayOpen())
        }
        findViewById<RealtimeBlurView>(R.id.shell_background).setOnClickListener {
            Utils.fadeOutAnim(findViewById<CardView>(R.id.shell), 300)
            Utils.fadeOutAnim(findViewById<RealtimeBlurView>(R.id.shell_background), 300)
            setBackInterceptionEnabled(anyOverlayOpen())
        }
        findViewById<ImageView>(R.id.sp_img).setOnClickListener {
            Utils.fadeInAnim(findViewById<CardView>(R.id.detail_emission), 300)
            Utils.fadeInAnim(findViewById<RealtimeBlurView>(R.id.detail_emission_background), 300)
            setBackInterceptionEnabled(true)
        }
        findViewById<FloatingActionButton>(R.id.close_emission_btn).setOnClickListener {
            Utils.fadeOutAnim(findViewById<CardView>(R.id.detail_emission), 300)
            Utils.fadeOutAnim(findViewById<RealtimeBlurView>(R.id.detail_emission_background), 300)
            setBackInterceptionEnabled(anyOverlayOpen())
        }
        findViewById<RealtimeBlurView>(R.id.detail_emission_background).setOnClickListener {
            Utils.fadeOutAnim(findViewById<CardView>(R.id.detail_emission), 300)
            Utils.fadeOutAnim(findViewById<RealtimeBlurView>(R.id.detail_emission_background), 300)
            setBackInterceptionEnabled(anyOverlayOpen())
        }
    }
    private fun elementAnim(view: View, view2: View) {
        view.alpha = 0.0f
        view.animate().setDuration(150)
        view.animate().alpha(1.0f)
        val delay = Handler()
        delay.postDelayed({
            view2.alpha = 0.0f
            view2.animate().setDuration(150)
            view2.animate().alpha(1.0f)
        }, 150)
    }
    private fun nextPrev() {
        findViewById<ImageButton>(R.id.next_btn).setOnClickListener {
            try {
                val ElementSendAndLoadPreference = ElementSendAndLoad(this)
                val ElementSendAndLoadValue = ElementSendAndLoadPreference.getValue()
                val jsonObject = ElementDataLoader.loadElementData(this, ElementSendAndLoadValue ?: "hydrogen")
                if (jsonObject != null) {
                    val currentNumb = jsonObject.optString("element_atomic_number", "---")
                    val elements = ArrayList<Element>()
                    ElementModel.getList(elements, this)
                    val item = elements[currentNumb.toInt()]
                    val elementSendAndLoad = ElementSendAndLoad(this)
                    elementSendAndLoad.setValue(item.elementKey)
                    readJson()
                }
            }
            catch (e: IOException) {}
        }
        findViewById<ImageButton>(R.id.previous_btn).setOnClickListener {
            try {
                val ElementSendAndLoadPreference = ElementSendAndLoad(this)
                val ElementSendAndLoadValue = ElementSendAndLoadPreference.getValue()
                val jsonObject = ElementDataLoader.loadElementData(this, ElementSendAndLoadValue ?: "hydrogen")
                if (jsonObject != null) {
                    val currentNumb = jsonObject.optString("element_atomic_number", "---")
                    val elements = ArrayList<Element>()
                    ElementModel.getList(elements, this)
                    val item = elements[currentNumb.toInt()-2]
                    val elementSendAndLoad = ElementSendAndLoad(this)
                    elementSendAndLoad.setValue(item.elementKey)
                    readJson()
                }
            }
            catch (e: IOException) {}
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