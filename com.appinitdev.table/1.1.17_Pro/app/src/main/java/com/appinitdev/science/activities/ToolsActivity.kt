class ToolsActivity : BaseActivity() {
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
        setContentView(R.layout.activity_tools)
        findViewById<FrameLayout>(R.id.view_tools).systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        toolListeners()
        mostUsedBar()
        updateProPlusBadge()
        findViewById<ImageButton>(R.id.back_btn).setOnClickListener {
            this.onBackPressed()
        }
    }
    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
            val params = findViewById<FrameLayout>(R.id.common_title_back_tab).layoutParams as ViewGroup.LayoutParams
            params.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
            findViewById<FrameLayout>(R.id.common_title_back_tab).layoutParams = params
            val params2 = findViewById<TextView>(R.id.tools_title_downstate).layoutParams as ViewGroup.MarginLayoutParams
            params2.topMargin = top
            findViewById<TextView>(R.id.tools_title_downstate).layoutParams = params2
    }
    private fun updateProPlusBadge() {
        val proPlusPref = ProPlusVersion(this)
        val proPlusPrefValue = proPlusPref.getValue()
        val isBeforeDeadline = ProPlusTimeUtil.isBeforeJanuary2026()
        if (proPlusPrefValue != 100 && isBeforeDeadline) {
            findViewById<TextView>(R.id.pro_plus_gas_text).visibility = View.VISIBLE
        } else {
            findViewById<TextView>(R.id.pro_plus_gas_text).visibility = View.GONE
        }
    }
    private fun mostUsedBar() {
        val mostUsedToolPreference = MostUsedToolPreference(this)
        val proPlusPref = ProPlusVersion(this)
        val proPlusPrefValue = proPlusPref.getValue()
        val isBeforeDeadline = ProPlusTimeUtil.isBeforeJanuary2026()
        val regex = Regex("(\\w{3})=(\\d\\.\\d)") 
        val matches = regex.findAll(mostUsedToolPreference.getValue())
            .map { it.groups[1]!!.value to it.groups[2]!!.value.toDouble() }
            .toList()
        val sortedValues = matches.sortedByDescending { it.second }
        val textView1: TextView = findViewById(R.id.mostT_1)
        val textView2: TextView = findViewById(R.id.mostT_2)
        val textView3: TextView = findViewById(R.id.mostT_3)
        val textViewList = listOf(textView1, textView2, textView3)
        sortedValues.forEachIndexed { index, pair ->
            if (index < textViewList.size) {
                when (pair.first) {
                    "cal" -> textViewList[index].text = getString(R.string.cal)
                    "uni" -> textViewList[index].text = getString(R.string.uni)
                    "fla" -> textViewList[index].text = getString(R.string.fla)
                    "gas" -> textViewList[index].text = getString(R.string.gas)
                }
                textViewList[index].setOnClickListener {
                    if (pair.first == "gas" && proPlusPrefValue != 100 && isBeforeDeadline) {
                        val intent = Intent(this, ProActivity::class.java)
                        startActivity(intent)
                    } else {
                        val activity = when (pair.first) {
                            "cal" -> CalculatorActivity::class.java
                            "uni" -> UnitConversionActivity::class.java
                            "fla" -> FlashCardActivity::class.java
                            "gas" -> IdealGasCalculatorActivity::class.java
                            else -> null
                        }
                        activity?.let {
                            val intent = Intent(this, it)
                            startActivity(intent)
                        }
                    }
                }
            }
        }
    }
    private fun toolListeners() {
        val proPlusPref = ProPlusVersion(this)
        val proPlusPrefValue = proPlusPref.getValue()
        val isBeforeDeadline = ProPlusTimeUtil.isBeforeJanuary2026()
        findViewById<FrameLayout>(R.id.tool_calculator).setOnClickListener {
            val intent = Intent(this, CalculatorActivity::class.java)
            startActivity(intent)
        }
        findViewById<TextView>(R.id.calculator_btn).setOnClickListener {
            val intent = Intent(this, CalculatorActivity::class.java)
            startActivity(intent)
        }
        findViewById<FrameLayout>(R.id.tool_unit_converter).setOnClickListener {
            val intent = Intent(this, UnitConversionActivity::class.java)
            startActivity(intent)
        }
        findViewById<TextView>(R.id.unit_converter_btn).setOnClickListener {
            val intent = Intent(this, UnitConversionActivity::class.java)
            startActivity(intent)
        }
        findViewById<FrameLayout>(R.id.flashcards).setOnClickListener {
            val intent = Intent(this, FlashCardActivity::class.java)
            startActivity(intent)
        }
        findViewById<TextView>(R.id.flashcards_btn).setOnClickListener {
            val intent = Intent(this, FlashCardActivity::class.java)
            startActivity(intent)
        }
        findViewById<FrameLayout>(R.id.tool_ideal_gas_calculator).setOnClickListener {
            if (proPlusPrefValue != 100 && isBeforeDeadline) {
                val intent = Intent(this, ProActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, IdealGasCalculatorActivity::class.java)
                startActivity(intent)
            }
        }
        findViewById<TextView>(R.id.ideal_gas_calculator_btn).setOnClickListener {
            if (proPlusPrefValue != 100 && isBeforeDeadline) {
                val intent = Intent(this, ProActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, IdealGasCalculatorActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
