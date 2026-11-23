class ElectrodeActivity : BaseActivity() {
    private var seriesList = ArrayList<Series>()
    var mAdapter = ElectrodeAdapter(seriesList, this, this)
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
        setContentView(R.layout.activity_electrode) 
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
        recyclerView()
        clickSearch()
        val mostUsedPreference = MostUsedPreference(this)
        val mostUsedPrefValue = mostUsedPreference.getValue()
        val targetLabel = "ele"
        val regex = Regex("($targetLabel)=(\\d\\.\\d)")
        val match = regex.find(mostUsedPrefValue)
        if (match != null) {
            val value = match.groups[2]!!.value.toDouble()
            val newValue = value + 1
            mostUsedPreference.setValue(mostUsedPrefValue.replace("$targetLabel=$value", "$targetLabel=$newValue"))
        }
        findViewById<FrameLayout>(R.id.view_ele).systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        findViewById<ImageButton>(R.id.back_btn).setOnClickListener {
            this.onBackPressed()
        }
    }
    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        findViewById<RecyclerView>(R.id.e_view).setPadding(0, resources.getDimensionPixelSize(R.dimen.title_bar) + resources.getDimensionPixelSize(R.dimen.margin_space) + top, 0, resources.getDimensionPixelSize(R.dimen.title_bar))
        val params2 = findViewById<FrameLayout>(R.id.common_title_back_elo).layoutParams as ViewGroup.LayoutParams
        params2.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        findViewById<FrameLayout>(R.id.common_title_back_elo).layoutParams = params2
        val searchEmptyImgPrm = findViewById<LinearLayout>(R.id.empty_search_box_ele).layoutParams as ViewGroup.MarginLayoutParams
        searchEmptyImgPrm.topMargin = top + (resources.getDimensionPixelSize(R.dimen.title_bar))
        findViewById<LinearLayout>(R.id.empty_search_box_ele).layoutParams = searchEmptyImgPrm
    }
    private fun recyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.e_view)
        val series = ArrayList<Series>()
        SeriesModel.getList(series, this)
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val adapter = ElectrodeAdapter(series, this, this)
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
        findViewById<EditText>(R.id.edit_ele).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {}
            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ){}
            override fun afterTextChanged(s: Editable) {
                filter(s.toString(), series, recyclerView)
            }
        })
    }
    private fun filter(text: String, list: ArrayList<Series>, recyclerView: RecyclerView) {
        val filteredList: ArrayList<Series> = ArrayList()
        for (item in list) {
            if (item.name.lowercase(Locale.ROOT).contains(text.lowercase(Locale.ROOT))) {
                filteredList.add(item)
            }
        }
        val handler = android.os.Handler()
        handler.postDelayed({
            if (recyclerView.adapter!!.itemCount == 0) {
                Anim.fadeIn(findViewById<LinearLayout>(R.id.empty_search_box_ele), 300)
            }
            else {
                findViewById<LinearLayout>(R.id.empty_search_box_ele).visibility = View.GONE
            }
        }, 10)
        mAdapter.filterList(filteredList)
        mAdapter.notifyDataSetChanged()
        recyclerView.adapter = ElectrodeAdapter(filteredList, this, this)
    }
    private fun clickSearch() {
        findViewById<ImageButton>(R.id.search_btn).setOnClickListener {
            Utils.fadeInAnim(findViewById<FrameLayout>(R.id.search_bar_ele), 150)
            Utils.fadeOutAnim(findViewById<FrameLayout>(R.id.title_box), 1)
            findViewById<EditText>(R.id.edit_ele).requestFocus()
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(findViewById<EditText>(R.id.edit_ele), InputMethodManager.SHOW_IMPLICIT)
            setBackInterceptionEnabled(true)
        }
        findViewById<ImageButton>(R.id.close_ele_search).setOnClickListener {
            Utils.fadeOutAnim(findViewById<FrameLayout>(R.id.search_bar_ele), 1)
            val delayClose = Handler()
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
    private fun anyOverlayOpen(): Boolean {
        val searchBarVisible = findViewById<FrameLayout>(R.id.search_bar_ele).visibility == View.VISIBLE
        return searchBarVisible
    }
    private fun handleBackPress(): Boolean {
        val searchBar = findViewById<FrameLayout>(R.id.search_bar_ele)
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