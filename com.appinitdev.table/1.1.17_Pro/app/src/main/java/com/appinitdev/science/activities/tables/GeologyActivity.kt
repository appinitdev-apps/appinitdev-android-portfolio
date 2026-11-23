
class GeologyActivity : BaseActivity(), GeologyAdapter.OnGeologyClickListener {
    private var geologyList = ArrayList<Geology>()
    var mAdapter = GeologyAdapter(geologyList, this, this)

    
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
        setContentView(R.layout.activity_geology) 

        
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

        val recyclerView = findViewById<RecyclerView>(R.id.geo_view)
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val item = ArrayList<Geology>()
        GeologyModel.getList(item)

        recyclerView()
        clickSearch()
        chipListeners(item, recyclerView)
        findViewById<Button>(R.id.clear_btn).visibility = View.GONE

        findViewById<FrameLayout>(R.id.view_geo).systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        
        findViewById<TextView>(R.id.detail_background_geo).setOnClickListener {
            Utils.fadeOutAnim(findViewById<FrameLayout>(R.id.geo_details), 300)
            
            setBackInterceptionEnabled(anyOverlayOpen())
        }

        findViewById<ImageButton>(R.id.back_btn_geo).setOnClickListener {
            this.onBackPressed()
        }
    }

    override fun geologyClickListener(item: Geology, position: Int) {
        
        findViewById<TextView>(R.id.geo_detail_title).text = item.name
        findViewById<TextView>(R.id.geo_type).text = getString(R.string.type_label) + item.type
        findViewById<TextView>(R.id.geo_group).text = getString(R.string.group_label) + item.group
        findViewById<TextView>(R.id.geo_color).text = getString(R.string.color_label) + item.color
        findViewById<TextView>(R.id.geo_strike).text = getString(R.string.streak_label) + item.streak
        findViewById<TextView>(R.id.geo_cristal).text = getString(R.string.cristal_structure_label) + item.cristal
        findViewById<TextView>(R.id.geo_hardness).text = getString(R.string.hardness_label) + item.hardness
        findViewById<TextView>(R.id.geo_density).text = getString(R.string.density_label) + item.density
        findViewById<TextView>(R.id.geo_magnetism).text = getString(R.string.magnetism_label) + item.magnetism
        findViewById<TextView>(R.id.geo_hydrochloride).text = item.hydrochloride

        
        Utils.fadeInAnim(findViewById<FrameLayout>(R.id.geo_details), 300)
        setBackInterceptionEnabled(true)
    }

    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        findViewById<RecyclerView>(R.id.geo_view).setPadding(0, resources.getDimensionPixelSize(R.dimen.title_bar_ph) + top, 0, resources.getDimensionPixelSize(R.dimen.title_bar_ph))
        val params2 = findViewById<FrameLayout>(R.id.common_title_back_geo).layoutParams as ViewGroup.LayoutParams
        params2.height = top + resources.getDimensionPixelSize(R.dimen.title_bar_ph)
        findViewById<FrameLayout>(R.id.common_title_back_geo).layoutParams = params2

        val searchEmptyImgPrm = findViewById<LinearLayout>(R.id.empty_search_box_geo).layoutParams as ViewGroup.MarginLayoutParams
        searchEmptyImgPrm.topMargin = top + (resources.getDimensionPixelSize(R.dimen.title_bar))
        findViewById<LinearLayout>(R.id.empty_search_box_geo).layoutParams = searchEmptyImgPrm
    }

    private fun recyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.geo_view)
        val geology = ArrayList<Geology>()

        GeologyModel.getList(geology)
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val adapter = GeologyAdapter(geology, this, this)
        recyclerView.adapter = adapter

        adapter.notifyDataSetChanged()

        
        val mostUsedPreference = MostUsedPreference(this)
        val mostUsedPrefValue = mostUsedPreference.getValue()
        val targetLabel = "geo"
        val regex = Regex("($targetLabel)=(\\d\\.\\d)")
        val match = regex.find(mostUsedPrefValue)
        if (match != null) {
            val value = match.groups[2]!!.value.toDouble()
            val newValue = value + 1
            mostUsedPreference.setValue(mostUsedPrefValue.replace("$targetLabel=$value", "$targetLabel=$newValue"))
        }

        findViewById<EditText>(R.id.edit_geo).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int){}
            override fun afterTextChanged(s: Editable) {
                filter(s.toString(), geology, recyclerView)
            }
        })
    }

    
    private fun filter(text: String, list: ArrayList<Geology>, recyclerView: RecyclerView) {
        val filteredList: ArrayList<Geology> = ArrayList()
        for (item in list) {
            val geoPreference = GeologyPreference(this)
            val geoPrefValue = geoPreference.getValue()
            if (item.name.lowercase(Locale.ROOT).contains(text.lowercase(Locale.ROOT))) {
                if (item.type.lowercase(Locale.ROOT).contains(geoPrefValue.lowercase(Locale.ROOT))) {
                    filteredList.add(item)
                }
            }
        }
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            if (recyclerView.adapter!!.itemCount == 0) {
                Anim.fadeIn(findViewById<LinearLayout>(R.id.empty_search_box_geo), 300)
            }
            else {
                findViewById<LinearLayout>(R.id.empty_search_box_geo).visibility = View.GONE
            }
        }, 10)
        mAdapter.filterList(filteredList)
        mAdapter.notifyDataSetChanged()
        recyclerView.adapter = GeologyAdapter(filteredList, this, this)
    }

    private fun clickSearch() {
        findViewById<ImageButton>(R.id.search_btn_geo).setOnClickListener {
            Utils.fadeInAnim(findViewById<FrameLayout>(R.id.search_bar_geo), 150)
            Utils.fadeOutAnim(findViewById<FrameLayout>(R.id.title_box_geo), 1)

            findViewById<EditText>(R.id.edit_geo).requestFocus()
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(findViewById<EditText>(R.id.edit_geo), InputMethodManager.SHOW_IMPLICIT)

            
            setBackInterceptionEnabled(true)
        }
        findViewById<ImageButton>(R.id.close_geo_search).setOnClickListener {
            Utils.fadeOutAnim(findViewById<FrameLayout>(R.id.search_bar_geo), 1)

            val delayClose = Handler(Looper.getMainLooper())
            delayClose.postDelayed({
                Utils.fadeInAnim(findViewById<FrameLayout>(R.id.title_box_geo), 150)
            }, 151)

            val view = this.currentFocus
            if (view != null) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }

            
            setBackInterceptionEnabled(anyOverlayOpen())
        }
    }

    private fun chipListeners(list: ArrayList<Geology>, recyclerView: RecyclerView) {
        findViewById<Button>(R.id.rocks_btn).setOnClickListener {
            updateButtonColor("rocks_btn")
            val geoPreference = GeologyPreference(this)
            geoPreference.setValue("rock")
            findViewById<EditText>(R.id.edit_geo).setText("test")
            findViewById<EditText>(R.id.edit_geo).setText("")
        }
        findViewById<Button>(R.id.soils_btn).setOnClickListener {
            updateButtonColor("soils_btn")
            val geoPreference = GeologyPreference(this)
            geoPreference.setValue("soil")
            findViewById<EditText>(R.id.edit_geo).setText("test")
            findViewById<EditText>(R.id.edit_geo).setText("")
        }
        findViewById<Button>(R.id.minerals_btn).setOnClickListener {
            updateButtonColor("minerals_btn")
            val geoPreference = GeologyPreference(this)
            geoPreference.setValue("mineral")
            findViewById<EditText>(R.id.edit_geo).setText("test")
            findViewById<EditText>(R.id.edit_geo).setText("")
        }
    }

    private fun updateButtonColor(btn: String) {
        findViewById<Button>(R.id.rocks_btn).background = getDrawable(R.drawable.chip)
        findViewById<Button>(R.id.soils_btn).background = getDrawable(R.drawable.chip)
        findViewById<Button>(R.id.minerals_btn).background = getDrawable(R.drawable.chip)

        val delay = Handler(Looper.getMainLooper())
        delay.postDelayed({
            val resIDB = resources.getIdentifier(btn, "id", packageName)
            val button = findViewById<Button>(resIDB)
            button.background = getDrawable(R.drawable.chip_active)
        }, 200)

        findViewById<Button>(R.id.clear_btn).visibility = View.VISIBLE
        findViewById<Button>(R.id.clear_btn).setOnClickListener {
            val resIDB = resources.getIdentifier(btn, "id", packageName)
            val button = findViewById<Button>(resIDB)
            val geoPreference = GeologyPreference(this)
            button.background = getDrawable(R.drawable.chip)
            geoPreference.setValue("")
            findViewById<EditText>(R.id.edit_geo).setText("test")
            findViewById<EditText>(R.id.edit_geo).setText("")
            findViewById<Button>(R.id.clear_btn).visibility = View.GONE
        }
    }

    
    private fun anyOverlayOpen(): Boolean {
        val detailsVisible = findViewById<FrameLayout>(R.id.geo_details).visibility == View.VISIBLE
        val backgroundVisible = findViewById<TextView>(R.id.detail_background_geo).visibility == View.VISIBLE
        val searchBarVisible = findViewById<FrameLayout>(R.id.search_bar_geo).visibility == View.VISIBLE
        return detailsVisible || backgroundVisible || searchBarVisible
    }

    
    private fun handleBackPress(): Boolean {
        val details = findViewById<FrameLayout>(R.id.geo_details)
        val background = findViewById<TextView>(R.id.detail_background_geo)
        val searchBar = findViewById<FrameLayout>(R.id.search_bar_geo)

        
        if (details.visibility == View.VISIBLE || background.visibility == View.VISIBLE) {
            Utils.fadeOutAnim(details, 300)
            Utils.fadeOutAnim(background, 300)
            setBackInterceptionEnabled(anyOverlayOpen())
            return true
        }

        
        if (searchBar.visibility == View.VISIBLE) {
            Utils.fadeOutAnim(searchBar, 1)
            Handler(Looper.getMainLooper()).postDelayed({
                Utils.fadeInAnim(findViewById<FrameLayout>(R.id.title_box_geo), 150)
            }, 151)

            val view = this.currentFocus
            if (view != null) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            setBackInterceptionEnabled(anyOverlayOpen())
            return true
        }

        return false
    }

    override fun onBackPressed() {
        
        if (!handleBackPress()) {
            super.onBackPressed()
        }
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