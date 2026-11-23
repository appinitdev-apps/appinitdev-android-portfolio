
class EmissionActivity : BaseActivity(), EmissionAdapter.OnEmissionClickListener {
    private var emiList = ArrayList<Element>()
    var mAdapter = EmissionAdapter(emiList, this, this)

    
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
        setContentView(R.layout.activity_emission) 

        
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

        
        val mostUsedPreference = MostUsedPreference(this)
        val mostUsedPrefValue = mostUsedPreference.getValue()
        val targetLabel = "emi"
        val regex = Regex("($targetLabel)=(\\d\\.\\d)")
        val match = regex.find(mostUsedPrefValue)
        if (match != null) {
            val value = match.groups[2]!!.value.toDouble()
            val newValue = value + 1
            mostUsedPreference.setValue(mostUsedPrefValue.replace("$targetLabel=$value", "$targetLabel=$newValue"))
        }

        recyclerView()
        clickSearch()

        
        findViewById<SlidingUpPanelLayout>(R.id.sliding_layout_e).addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View?, slideOffset: Float) { }
            override fun onPanelStateChanged(panel: View?, previousState: SlidingUpPanelLayout.PanelState, newState: SlidingUpPanelLayout.PanelState) {
                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    Utils.fadeOutAnim(findViewById<TextView>(R.id.background_emi), 300)
                    Utils.fadeOutAnim(findViewById<FrameLayout>(R.id.emission_detail), 300)
                }
                
                setBackInterceptionEnabled(anyOverlayOpen())
            }
        })

        findViewById<TextView>(R.id.background_emi).setOnClickListener{
            if (findViewById<FrameLayout>(R.id.emission_detail).visibility == View.VISIBLE) {
                Utils.fadeOutAnim(findViewById<FrameLayout>(R.id.emission_detail), 300)
                Utils.fadeOutAnim(findViewById<TextView>(R.id.background_emi), 300)
            }
            else {
                Utils.fadeOutAnim(findViewById<SlidingUpPanelLayout>(R.id.sliding_layout_e), 300)
                Utils.fadeOutAnim(findViewById<TextView>(R.id.background_emi), 300)
            }
            
            setBackInterceptionEnabled(anyOverlayOpen())
        }

        findViewById<FrameLayout>(R.id.view_emi).systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        findViewById<ImageButton>(R.id.back_btn_emi).setOnClickListener { this.onBackPressed() }
    }

    override fun emiClickListener(item: Element, position: Int) {
        try {
            val jsonObject = ElementDataLoader.loadElementData(this, item.elementKey)
            if (jsonObject != null) {
                val url = jsonObject.optString("short", "---")
                val hUrl = "https://www.jlindemann.se/atomic/emission_lines/"
                val extg = ".gif"
                val fURL = hUrl + url + extg
                findViewById<TextView>(R.id.emi_title).text = item.elementKey
                try {
                    Picasso.get().load(fURL).into(findViewById<ImageView>(R.id.emi_img_detail))
                    Utils.fadeInAnimBack(findViewById<TextView>(R.id.background_emi), 300)
                }
                catch(e: ConnectException) {
                    
                }
            }
        }
        catch (e: IOException) { }
        Utils.fadeInAnim(findViewById<FrameLayout>(R.id.emission_detail), 300)
        findViewById<SlidingUpPanelLayout>(R.id.sliding_layout_e).panelState = SlidingUpPanelLayout.PanelState.EXPANDED

        
        setBackInterceptionEnabled(true)
    }

    private fun recyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.emi_view)
        val emiList = ArrayList<Element>()
        ElementModel.getList(emiList, this)
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val adapter = EmissionAdapter(emiList, this, this)
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()

        findViewById<EditText>(R.id.edit_emi).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int){}
            override fun afterTextChanged(s: Editable) { filter(s.toString(), emiList, recyclerView) }
        })
    }

    private fun filter(text: String, list: ArrayList<Element>, recyclerView: RecyclerView) {
        val filteredList: ArrayList<Element> = ArrayList()
        for (item in list) { if (item.element.lowercase(Locale.ROOT).contains(text.lowercase(Locale.ROOT))) { filteredList.add(item) } }
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            if (recyclerView.adapter!!.itemCount == 0) {
                Anim.fadeIn(findViewById<LinearLayout>(R.id.empty_search_box_emi), 300)
            }
            else {
                findViewById<LinearLayout>(R.id.empty_search_box_emi).visibility = View.GONE
            }
        }, 10)
        mAdapter.filterList(filteredList)
        mAdapter.notifyDataSetChanged()
        recyclerView.adapter = EmissionAdapter(filteredList, this, this)
    }

    private fun clickSearch() {
        findViewById<ImageButton>(R.id.search_btn_emi).setOnClickListener {
            Utils.fadeInAnim(findViewById<FrameLayout>(R.id.search_bar_emi), 150)
            Utils.fadeOutAnim(findViewById<FrameLayout>(R.id.title_box), 1)
            findViewById<EditText>(R.id.edit_emi).requestFocus()
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(findViewById<EditText>(R.id.edit_emi), InputMethodManager.SHOW_IMPLICIT)

            
            setBackInterceptionEnabled(true)
        }
        findViewById<ImageButton>(R.id.close_ele_search_emi).setOnClickListener {
            Utils.fadeOutAnim(findViewById<FrameLayout>(R.id.search_bar_emi), 1)
            val delayClose = Handler(Looper.getMainLooper())
            delayClose.postDelayed({
                Utils.fadeInAnim(findViewById<FrameLayout>(R.id.title_box), 150)
            }, 151)

            val view = this.currentFocus
            if (view != null) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }

            
            setBackInterceptionEnabled(anyOverlayOpen())
        }
    }

    override fun onBackPressed() {
        
        if (!handleBackPress()) {
            super.onBackPressed()
        }
    }

    
    private fun anyOverlayOpen(): Boolean {
        val panel = findViewById<SlidingUpPanelLayout>(R.id.sliding_layout_e)
        val panelExpanded = panel.panelState == SlidingUpPanelLayout.PanelState.EXPANDED
        val backgroundVisible = findViewById<TextView>(R.id.background_emi).visibility == View.VISIBLE
        val detailVisible = findViewById<FrameLayout>(R.id.emission_detail).visibility == View.VISIBLE
        val searchBarVisible = findViewById<FrameLayout>(R.id.search_bar_emi).visibility == View.VISIBLE
        return panelExpanded || backgroundVisible || detailVisible || searchBarVisible
    }

    
    private fun handleBackPress(): Boolean {
        val panel = findViewById<SlidingUpPanelLayout>(R.id.sliding_layout_e)
        val background = findViewById<TextView>(R.id.background_emi)
        val detail = findViewById<FrameLayout>(R.id.emission_detail)
        val searchBar = findViewById<FrameLayout>(R.id.search_bar_emi)

        
        if (detail.visibility == View.VISIBLE) {
            Utils.fadeOutAnim(detail, 300)
            Utils.fadeOutAnim(background, 300)
            panel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            setBackInterceptionEnabled(anyOverlayOpen())
            return true
        }

        
        if (panel.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            panel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            Utils.fadeOutAnim(background, 300)
            Utils.fadeOutAnim(detail, 300)
            setBackInterceptionEnabled(anyOverlayOpen())
            return true
        }

        
        if (searchBar.visibility == View.VISIBLE) {
            Utils.fadeOutAnim(searchBar, 1)
            Handler(Looper.getMainLooper()).postDelayed({
                Utils.fadeInAnim(findViewById<FrameLayout>(R.id.title_box), 150)
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

    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        findViewById<RecyclerView>(R.id.emi_view).setPadding(0, resources.getDimensionPixelSize(R.dimen.title_bar) + resources.getDimensionPixelSize(R.dimen.margin_space) + top, 0, resources.getDimensionPixelSize(R.dimen.title_bar))

        val params2 = findViewById<FrameLayout>(R.id.common_title_back_emi).layoutParams as ViewGroup.LayoutParams
        params2.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        findViewById<FrameLayout>(R.id.common_title_back_emi).layoutParams = params2

        val searchEmptyImgPrm = findViewById<LinearLayout>(R.id.empty_search_box_emi).layoutParams as ViewGroup.MarginLayoutParams
        searchEmptyImgPrm.topMargin = top + (resources.getDimensionPixelSize(R.dimen.title_bar))
        findViewById<LinearLayout>(R.id.empty_search_box_emi).layoutParams = searchEmptyImgPrm

        val params3 = findViewById<SlidingUpPanelLayout>(R.id.sliding_layout_e).layoutParams as ViewGroup.MarginLayoutParams
        params3.topMargin = top + resources.getDimensionPixelSize(R.dimen.panel_margin)
        findViewById<SlidingUpPanelLayout>(R.id.sliding_layout_e).layoutParams = params3
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