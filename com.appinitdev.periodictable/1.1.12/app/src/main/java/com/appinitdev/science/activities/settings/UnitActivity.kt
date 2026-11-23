

class UnitActivity : BaseActivity()  {
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

        setContentView(R.layout.activity_unit) 
        findViewById<FrameLayout>(R.id.view_unit).systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        
         




















tempUnits()
        findViewById<ImageButton>(R.id.back_btn_unit).setOnClickListener {
            this.onBackPressed()
        }
    }

    private fun tempUnits() {
        val tempPreference = TemperatureUnits(this)
        val tempPrefValue = tempPreference.getValue()
        if (tempPrefValue == "kelvin") {
            findViewById<Button>(R.id.kelvin_btn).background = ContextCompat.getDrawable(this, R.drawable.chip_active)
            findViewById<Button>(R.id.celsius_btn).background = ContextCompat.getDrawable(this, R.drawable.chip_outline)
            findViewById<Button>(R.id.fahrenheit_btn).background = ContextCompat.getDrawable(this, R.drawable.chip_outline)
        }
        if (tempPrefValue == "celsius") {
            findViewById<Button>(R.id.kelvin_btn).background = ContextCompat.getDrawable(this, R.drawable.chip_outline)
            findViewById<Button>(R.id.celsius_btn).background = ContextCompat.getDrawable(this, R.drawable.chip_active)
            findViewById<Button>(R.id.fahrenheit_btn).background = ContextCompat.getDrawable(this, R.drawable.chip_outline)
        }
        if (tempPrefValue == "fahrenheit") {
            findViewById<Button>(R.id.kelvin_btn).background = ContextCompat.getDrawable(this, R.drawable.chip_outline)
            findViewById<Button>(R.id.celsius_btn).background = ContextCompat.getDrawable(this, R.drawable.chip_outline)
            findViewById<Button>(R.id.fahrenheit_btn).background = ContextCompat.getDrawable(this, R.drawable.chip_active)
        }
        findViewById<Button>(R.id.kelvin_btn).setOnClickListener {
            tempPreference.setValue("kelvin")
            findViewById<Button>(R.id.kelvin_btn).background = ContextCompat.getDrawable(this, R.drawable.chip_active)
            findViewById<Button>(R.id.celsius_btn).background = ContextCompat.getDrawable(this, R.drawable.chip_outline)
            findViewById<Button>(R.id.fahrenheit_btn).background = ContextCompat.getDrawable(this, R.drawable.chip_outline)
        }
        findViewById<Button>(R.id.celsius_btn).setOnClickListener {
            tempPreference.setValue("celsius")
            findViewById<Button>(R.id.kelvin_btn).background = ContextCompat.getDrawable(this, R.drawable.chip_outline)
            findViewById<Button>(R.id.celsius_btn).background = ContextCompat.getDrawable(this, R.drawable.chip_active)
            findViewById<Button>(R.id.fahrenheit_btn).background = ContextCompat.getDrawable(this, R.drawable.chip_outline)
        }
        findViewById<Button>(R.id.fahrenheit_btn).setOnClickListener {
            tempPreference.setValue("fahrenheit")
            findViewById<Button>(R.id.kelvin_btn).background = ContextCompat.getDrawable(this, R.drawable.chip_outline)
            findViewById<Button>(R.id.celsius_btn).background = ContextCompat.getDrawable(this, R.drawable.chip_outline)
            findViewById<Button>(R.id.fahrenheit_btn).background = ContextCompat.getDrawable(this, R.drawable.chip_active)
        }
    }

    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        val paramsTitle = findViewById<FrameLayout>(R.id.common_title_back_unit).layoutParams as ViewGroup.LayoutParams
        paramsTitle.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        findViewById<FrameLayout>(R.id.common_title_back_unit).layoutParams = paramsTitle

        val paramsLin = findViewById<TextView>(R.id.unit_title_downstate).layoutParams as ViewGroup.MarginLayoutParams
        paramsLin.topMargin = top
        findViewById<TextView>(R.id.unit_title_downstate).layoutParams = paramsLin
    }
}



