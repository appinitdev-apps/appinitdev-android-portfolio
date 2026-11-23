class OrderActivity : BaseActivity()  {
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
        setContentView(R.layout.activity_order_settings_page) 
        val dataSet = listOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5")
        val mAdapter = OrderAdapter(dataSet)
        val mList = findViewById<DragDropSwipeRecyclerView>(R.id.ord_recycler)
        mList.layoutManager = LinearLayoutManager(this)
        mList.adapter = mAdapter
        mList.dragListener = onItemDragListener
        findViewById<ConstraintLayout>(R.id.view_ord).systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        findViewById<ImageButton>(R.id.back_btn_ord).setOnClickListener {
            this.onBackPressed()
        }
    }
    private val onItemDragListener = object : OnItemDragListener<String> {
        override fun onItemDragged(previousPosition: Int, newPosition: Int, item: String) {
        }
        override fun onItemDropped(initialPosition: Int, finalPosition: Int, item: String) {
        }
    }
    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        findViewById<DragDropSwipeRecyclerView>(R.id.ord_recycler).setPadding(
            0,
            resources.getDimensionPixelSize(R.dimen.title_bar) + resources.getDimensionPixelSize(R.dimen.margin_space) + top,
            0,
            resources.getDimensionPixelSize(R.dimen.title_bar))
        val params2 = findViewById<FrameLayout>(R.id.common_title_back_ord).layoutParams as ViewGroup.LayoutParams
        params2.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        findViewById<FrameLayout>(R.id.common_title_back_ord).layoutParams = params2
    }
}
