class IntroductionActivity : AppCompatActivity(), BillingManager.Listener {
    companion object {
        private const val PRO_PAGE_INDEX = 3
    }
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var nextButton: Button
    private lateinit var skipButton: Button
    private lateinit var backButton: ImageButton
    private lateinit var introductionAdapter: IntroductionPagerAdapter
    private lateinit var billingManager: BillingManager
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
        setContentView(R.layout.activity_introduction)
        viewPager = findViewById(R.id.intro_viewpager)
        tabLayout = findViewById(R.id.intro_tab_layout)
        nextButton = findViewById(R.id.intro_next_button)
        skipButton = findViewById(R.id.intro_skip_button)
        backButton = findViewById(R.id.intro_back_button)
        introductionAdapter = IntroductionPagerAdapter(this)
        viewPager.adapter = introductionAdapter
        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()
        nextButton.setOnClickListener {
            if (viewPager.currentItem < introductionAdapter.itemCount - 1) {
                viewPager.currentItem += 1
            } else {
                completeIntroduction()
            }
        }
        skipButton.setOnClickListener { completeIntroduction() }
        backButton.setOnClickListener {
            if (viewPager.currentItem > 0) viewPager.currentItem -= 1
        }
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateButtonStates(position, introductionAdapter.itemCount)
            }
        })
        updateButtonStates(0, introductionAdapter.itemCount)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewPager.currentItem > 0) viewPager.currentItem -= 1 else finish()
            }
        })
        billingManager = BillingManager(this, lifecycleScope, this)
        billingManager.initialize()
    }
    override fun onDestroy() {
        super.onDestroy()
        billingManager.endConnection()
    }
    fun getProductDetail(productId: String) = billingManager.getProductDetail(productId)
    fun isOwnsProVersion() = billingManager.isOwnsProVersion()
    fun isOwnsProPlusVersion() = billingManager.isOwnsProPlusVersion()
    fun getFormattedPrice(productDetails: com.android.billingclient.api.ProductDetails?) : String {
        val formattedPrice = productDetails?.oneTimePurchaseOfferDetails?.formattedPrice
        return formattedPrice ?: getString(R.string.price_not_available)
    }
    fun purchaseProduct(productDetails: com.android.billingclient.api.ProductDetails) {
        billingManager.launchPurchase(productDetails)
    }
    override fun onProductsUpdated() {
        runOnUiThread { viewPager.adapter?.notifyItemChanged(PRO_PAGE_INDEX) }
    }
    override fun onPurchasesUpdated() {
        checkAndSetPreferences()
        runOnUiThread { viewPager.adapter?.notifyItemChanged(PRO_PAGE_INDEX) }
    }
    override fun onPurchaseCompleted(productId: String) {
        val proPref = ProVersion(this)
        val proPlusPref = ProPlusVersion(this)
        when (productId) {
            BillingManager.PRO_VERSION_ID -> {
                proPref.setValue(100)
                proPlusPref.setValue(1)
            }
            BillingManager.PRO_PLUS_VERSION_ID, BillingManager.PRO_PLUS_UPGRADE_ID -> {
                proPref.setValue(100)
                proPlusPref.setValue(100)
            }
        }
        runOnUiThread {
            viewPager.adapter?.notifyItemChanged(PRO_PAGE_INDEX)
        }
    }
    override fun onError(message: String) {
    }
    private fun checkAndSetPreferences() {
        val proPref = ProVersion(this)
        val proPlusPref = ProPlusVersion(this)
        val ownsPro = billingManager.isOwnsProVersion()
        val ownsProPlus = billingManager.isOwnsProPlusVersion()
        when {
            ownsPro && !ownsProPlus -> {
                proPref.setValue(100)
                proPlusPref.setValue(1)
            }
            ownsProPlus -> {
                proPref.setValue(100)
                proPlusPref.setValue(100)
            }
            else -> {
                proPref.setValue(1)
                proPlusPref.setValue(1)
            }
        }
    }
    private fun updateButtonStates(position: Int, totalPages: Int) {
        backButton.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
        if (position == totalPages - 1) {
            nextButton.text = getString(R.string.intro_finish)
            skipButton.visibility = View.GONE
        } else {
            nextButton.text = getString(R.string.intro_next)
            skipButton.visibility = View.VISIBLE
        }
    }
    private fun completeIntroduction() {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("introduction_shown", true).apply()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}