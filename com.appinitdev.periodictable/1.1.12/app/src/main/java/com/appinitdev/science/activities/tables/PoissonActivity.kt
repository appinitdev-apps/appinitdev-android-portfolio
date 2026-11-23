
class PoissonActivity : BaseActivity(), PoissonAdapter.OnPoissonClickListener {
    private var poissonList = ArrayList<Poisson>()
    var mAdapter = PoissonAdapter(poissonList, this, this)

    
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
        setContentView(R.layout.activity_poisson) 

        
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

        val recyclerView = findViewById<RecyclerView>(R.id.poi_view)
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val itempoi = ArrayList<Poisson>()
        PoissonModel.getList(itempoi)

        
        val mostUsedPreference = MostUsedPreference(this)
        val mostUsedPrefValue = mostUsedPreference.getValue()
        val targetLabel = "poi"
        val regex = Regex("($targetLabel)=(\\d\\.\\d)")
        val match = regex.find(mostUsedPrefValue)
        if (match != null) {
            val value = match.groups[2]!!.value.toDouble()
            val newValue = value + 1
            mostUsedPreference.setValue(mostUsedPrefValue.replace("$targetLabel=$value", "$targetLabel=$newValue"))
        }

        recyclerView()
        clickSearch()
        chipListeners(itempoi, recyclerView)
        findViewById<Button>(R.id.clear_btn).visibility = View.GONE

        
        findViewById<FrameLayout>(R.id.poi_det_inc_background).setOnClickListener {
            hideInfoPanel()
            setBackInterceptionEnabled(anyOverlayOpen())
        }
        findViewById<ImageButton>(R.id.close_detail_poisson_btn).setOnClickListener {
            hideInfoPanel()
            setBackInterceptionEnabled(anyOverlayOpen())
        }

        findViewById<FrameLayout>(R.id.view_poi).systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        findViewById<ImageButton>(R.id.back_btn_poi).setOnClickListener {
            this.onBackPressed()
        }
    }

    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        findViewById<RecyclerView>(R.id.poi_view).setPadding(0, resources.getDimensionPixelSize(R.dimen.title_bar_ph) + top, 0, resources.getDimensionPixelSize(R.dimen.title_bar_ph))
        val params2 = findViewById<FrameLayout>(R.id.common_title_back_poi).layoutParams as ViewGroup.LayoutParams
        params2.height = top + resources.getDimensionPixelSize(R.dimen.title_bar_ph)
        findViewById<FrameLayout>(R.id.common_title_back_poi).layoutParams = params2

        val searchEmptyImgPrm = findViewById<LinearLayout>(R.id.empty_search_box_poi).layoutParams as ViewGroup.MarginLayoutParams
        searchEmptyImgPrm.topMargin = top + (resources.getDimensionPixelSize(R.dimen.title_bar))
        findViewById<LinearLayout>(R.id.empty_search_box_poi).layoutParams = searchEmptyImgPrm
    }

    private fun recyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.poi_view)
        val poisson = ArrayList<Poisson>()

        PoissonModel.getList(poisson)
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val adapter = PoissonAdapter(poisson, this, this)
        recyclerView.adapter = adapter

        adapter.notifyDataSetChanged()

        findViewById<EditText>(R.id.edit_poi).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int){}
            override fun afterTextChanged(s: Editable) {
                filter(s.toString(), poisson, recyclerView)
            }
        })
    }

    
    override fun poissonClickListener(item: Poisson, position: Int) {
        showInfoPanel(item.name, item.start, item.end, item.type)
    }

    
    private fun showInfoPanel(title: String, start: Double, end: Double, type: String) {
        Anim.fadeIn(findViewById<ConstraintLayout>(R.id.poi_det_inc), 150)
        findViewById<FrameLayout>(R.id.poi_det_inc_background).visibility = View.VISIBLE

        
        setBackInterceptionEnabled(true)

        findViewById<ProgressBar>(R.id.pb_poisson_detail).progress = (start*100*2).toInt() 
        findViewById<ProgressBar>(R.id.pb_poisson_detail).secondaryProgress = (end*100*2).toInt() 
        findViewById<TextView>(R.id.detail_poisson_title).text = title
    }

    
    private fun hideInfoPanel() {
        Anim.fadeOutAnim(findViewById<ConstraintLayout>(R.id.poi_det_inc), 150)
        findViewById<FrameLayout>(R.id.poi_det_inc_background).visibility = View.GONE

        
        setBackInterceptionEnabled(anyOverlayOpen())
    }

    
    private fun filter(text: String, list: ArrayList<Poisson>, recyclerView: RecyclerView) {
        val filteredList: ArrayList<Poisson> = ArrayList()
        for (item in list) {
            val poissonPreference = PoissonPreferences(this)
            val poissonPrefValue = poissonPreference.getValue()
            if (item.name.lowercase(Locale.ROOT).contains(text.lowercase(Locale.ROOT))) {
                if (item.type.lowercase(Locale.ROOT).contains(poissonPrefValue.lowercase(Locale.ROOT))) {
                    filteredList.add(item)
                }
            }
        }
        val handler = android.os.Handler()
        handler.postDelayed({
            if (recyclerView.adapter!!.itemCount == 0) {
                Anim.fadeIn(findViewById<LinearLayout>(R.id.empty_search_box_poi), 300)
            }
            else {
                findViewById<LinearLayout>(R.id.empty_search_box_poi).visibility = View.GONE
            }
        }, 10)
        mAdapter.filterList(filteredList)
        mAdapter.notifyDataSetChanged()
        recyclerView.adapter = PoissonAdapter(filteredList, this, this)
    }

    private fun clickSearch() {
        findViewById<ImageButton>(R.id.search_btn_poi).setOnClickListener {
            Utils.fadeInAnim(findViewById<FrameLayout>(R.id.search_bar_poi), 150)
            Utils.fadeOutAnim(findViewById<FrameLayout>(R.id.title_box_poi), 1)

            findViewById<EditText>(R.id.edit_poi).requestFocus()
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(findViewById<EditText>(R.id.edit_poi), InputMethodManager.SHOW_IMPLICIT)

            
            setBackInterceptionEnabled(true)
        }
        findViewById<ImageButton>(R.id.close_poi_search).setOnClickListener {
            Utils.fadeOutAnim(findViewById<FrameLayout>(R.id.search_bar_poi), 1)

            val delayClose = Handler()
            delayClose.postDelayed({
                Utils.fadeInAnim(findViewById<FrameLayout>(R.id.title_box_poi), 150)
            }, 151)

            val view = this.currentFocus
            if (view != null) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }

            
            setBackInterceptionEnabled(anyOverlayOpen())
        }
    }

    private fun chipListeners(list: ArrayList<Poisson>, recyclerView: RecyclerView) {
        findViewById<Button>(R.id.rocks_btn).setOnClickListener {
            updateButtonColor("rocks_btn")
            val poissonPreference = PoissonPreferences(this)
            poissonPreference.setValue("rock")
            findViewById<EditText>(R.id.edit_poi).setText("test")
            findViewById<EditText>(R.id.edit_poi).setText("")
        }
        findViewById<Button>(R.id.soils_btn).setOnClickListener {
            updateButtonColor("soils_btn")
            val poissonPreference = PoissonPreferences(this)
            poissonPreference.setValue("soil")
            findViewById<EditText>(R.id.edit_poi).setText("test")
            findViewById<EditText>(R.id.edit_poi).setText("")
        }
        findViewById<Button>(R.id.minerals_btn).setOnClickListener {
            updateButtonColor("minerals_btn")
            val poissonPreference = PoissonPreferences(this)
            poissonPreference.setValue("mineral")
            findViewById<EditText>(R.id.edit_poi).setText("test")
            findViewById<EditText>(R.id.edit_poi).setText("")
        }
    }

    private fun updateButtonColor(btn: String) {
        findViewById<Button>(R.id.rocks_btn).background = getDrawable(R.drawable.chip)
        findViewById<Button>(R.id.soils_btn).background = getDrawable(R.drawable.chip)
        findViewById<Button>(R.id.minerals_btn).background = getDrawable(R.drawable.chip)

        val delay = Handler()
        delay.postDelayed({
            val resIDB = resources.getIdentifier(btn, "id", packageName)
            val button = findViewById<Button>(resIDB)
            button.background = getDrawable(R.drawable.chip_active)
        }, 200)

        findViewById<Button>(R.id.clear_btn).visibility = View.VISIBLE
        findViewById<Button>(R.id.clear_btn).setOnClickListener {
            val resIDB = resources.getIdentifier(btn, "id", packageName)
            val button = findViewById<Button>(resIDB)
            val poissonPreference = PoissonPreferences(this)
            button.background = getDrawable(R.drawable.chip)
            poissonPreference.setValue("")
            findViewById<EditText>(R.id.edit_poi).setText("test")
            findViewById<EditText>(R.id.edit_poi).setText("")
            findViewById<Button>(R.id.clear_btn).visibility = View.GONE
        }
    }

    
    private fun anyOverlayOpen(): Boolean {
        val infoVisible = findViewById<ConstraintLayout>(R.id.poi_det_inc).visibility == View.VISIBLE
        val backgroundVisible = findViewById<FrameLayout>(R.id.poi_det_inc_background).visibility == View.VISIBLE
        val searchBarVisible = findViewById<FrameLayout>(R.id.search_bar_poi).visibility == View.VISIBLE
        return infoVisible || backgroundVisible || searchBarVisible
    }

    
    private fun handleBackPress(): Boolean {
        val infoPanel = findViewById<ConstraintLayout>(R.id.poi_det_inc)
        val background = findViewById<FrameLayout>(R.id.poi_det_inc_background)
        val searchBar = findViewById<FrameLayout>(R.id.search_bar_poi)

        if (infoPanel.visibility == View.VISIBLE || background.visibility == View.VISIBLE) {
            hideInfoPanel()
            setBackInterceptionEnabled(anyOverlayOpen())
            return true
        }

        if (searchBar.visibility == View.VISIBLE) {
            Utils.fadeOutAnim(searchBar, 1)
            Handler(Looper.getMainLooper()).postDelayed({
                Utils.fadeInAnim(findViewById<FrameLayout>(R.id.title_box_poi), 150)
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