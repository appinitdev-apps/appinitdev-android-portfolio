
class UserActivity : BaseActivity(), AchievementAdapter.OnAchievementClickListener {
    private val TAG = "UserActivity"

    private var achievementsList = ArrayList<Achievement>()
    private lateinit var recyclerView: RecyclerView
    private var mAdapter: AchievementAdapter? = null

    private lateinit var btnSignOut: TextView
    private lateinit var tvUserInfo: TextView
    private lateinit var tvSyncStatus: TextView
    private lateinit var userImg: ImageView

    

    


    
   


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themePreference = ThemePreference(this)
        val themePrefValue = themePreference.getValue()
        if (themePrefValue == 100) {
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_NO -> setTheme(R.style.AppTheme)
                Configuration.UI_MODE_NIGHT_YES -> setTheme(R.style.AppThemeDark)
            }
        } else {
            setTheme(if (themePrefValue == 0) R.style.AppTheme else R.style.AppThemeDark)
        }
        setContentView(R.layout.activity_user)

        

        tvUserInfo = TextView(this).apply {
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(context.obtainStyledAttributes(intArrayOf(androidx.appcompat.R.attr.actionMenuTextColor)).let { ta -> val color = ta.getColor(0, currentTextColor); ta.recycle(); color })
        }
        tvSyncStatus = TextView(this).apply {
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(context.obtainStyledAttributes(intArrayOf(androidx.appcompat.R.attr.actionMenuTextColor)).let { ta -> val color = ta.getColor(0, currentTextColor); ta.recycle(); color })
        }

        
        try {
            val proBadge = findViewById<TextView>(R.id.pro_badge)
            val parent = proBadge.parent as? ViewGroup
            parent?.let {
                val index = it.indexOfChild(proBadge)
                val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                params.gravity = Gravity.CENTER_HORIZONTAL
                it.addView(tvUserInfo, index + 1, params)
                val params2 = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                params2.gravity = Gravity.CENTER_HORIZONTAL
                it.addView(tvSyncStatus, index + 2, params2)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to add dynamic UI elements", e)
        }

        btnSignOut = findViewById(R.id.signout_button)
        try {
            val proBadge = findViewById<TextView>(R.id.pro_badge)
            val parent = proBadge.parent as? ViewGroup
            parent?.addView(btnSignOut)
        } catch (_: Exception) {   }

        
        recyclerView = findViewById<RecyclerView>(R.id.recycler_view_achievements)
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        setupRecyclerView()

        findViewById<FrameLayout>(R.id.view_user).systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        
        setupBackButton()
        setupStats()
        rateSetup()
        shareSetup()

        
        val proPref = ProVersion(this).getValue()
        val proPlusPref = ProPlusVersion(this).getValue()
        if (proPref == 1) findViewById<TextView>(R.id.pro_badge).text = getString(R.string.non_pro)
        if (proPref == 100) findViewById<TextView>(R.id.pro_badge).text = getString(R.string.pro_user)
        if (proPlusPref == 100) findViewById<TextView>(R.id.pro_badge).text = getString(R.string.pro_plus_user)

        
        
        setUserTitleViews("User Page")

        
        val sharedPref = getSharedPreferences("UserActivityPrefs", Context.MODE_PRIVATE)
        val uriString = sharedPref.getString("user_img_uri", null)
        uriString?.let {
            try {
                val uri = Uri.parse(it)
                userImg.setImageURI(uri)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to set persisted user image uri", e)
            }
        }

        




        





    }



    private fun setUserTitleViews(name: String) {
      



    }


    private fun updateUiForSignOut() {
        tvUserInfo.text = getString(R.string.user_signed_out)
        tvSyncStatus.text = ""
        btnSignOut.visibility = View.GONE
        userImg.setImageResource(R.drawable.ic_account)
        setUserTitleViews("User Page")
    }

     







    private fun setupTitleController() {
        findViewById<FrameLayout>(R.id.common_title_user_color).visibility = View.INVISIBLE
        findViewById<TextView>(R.id.user_title).visibility = View.INVISIBLE
        findViewById<FrameLayout>(R.id.common_title_back_user).elevation = resources.getDimension(R.dimen.zero_elevation)
        findViewById<ScrollView>(R.id.user_scroll).viewTreeObserver.addOnScrollChangedListener {
            if (findViewById<ScrollView>(R.id.user_scroll).scrollY > 150) {
                findViewById<FrameLayout>(R.id.common_title_user_color).visibility = View.VISIBLE
                findViewById<TextView>(R.id.user_title).visibility = View.VISIBLE
                findViewById<FrameLayout>(R.id.common_title_back_user).elevation = resources.getDimension(R.dimen.one_elevation)
            } else {
                findViewById<FrameLayout>(R.id.common_title_user_color).visibility = View.INVISIBLE
                findViewById<TextView>(R.id.user_title).visibility = View.INVISIBLE
                findViewById<FrameLayout>(R.id.common_title_back_user).elevation = resources.getDimension(R.dimen.zero_elevation)
            }
        }

    }

    private fun setupBackButton() {
        findViewById<ImageButton>(R.id.back_btn).setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_achievements)
        achievementsList.clear()
        AchievementModel.getList(this, achievementsList)
        
        achievementsList.forEach { it.loadProgress(this) }
        achievementsList.sortByDescending { it.progress.toDouble() / it.maxProgress.toDouble() }
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        mAdapter = AchievementAdapter(achievementsList, this, this)
        recyclerView.adapter = mAdapter
        mAdapter?.notifyDataSetChanged()
    }

    private fun setupStats() {
        val statistics = ArrayList<Statistics>()
        StatisticsModel.getList(this, statistics)
        statistics.forEach { it.loadProgress(this) }
        if (statistics.size >= 3) {
            findViewById<TextView>(R.id.elements_stat).text = statistics[0].progress.toString()
            findViewById<TextView>(R.id.calculation_stat).text = statistics[1].progress.toString()
            findViewById<TextView>(R.id.search_stat).text = statistics[2].progress.toString()
        } else {
            
            findViewById<TextView>(R.id.elements_stat).text = "0"
            findViewById<TextView>(R.id.calculation_stat).text = "0"
            findViewById<TextView>(R.id.search_stat).text = "0"
        }
    }

    private fun rateSetup() {
        val manager = com.google.android.play.core.review.ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result

            } else {
                val exception = task.exception
                if (exception != null) {
                    Log.w(TAG, "Request review flow failed", exception)
                } else {
                    Log.w(TAG, "Request review flow failed with unknown error")
                }
            }
        }
    }

    private fun shareSetup() {

    }

    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        val params = findViewById<FrameLayout>(R.id.common_title_back_user).layoutParams as ViewGroup.LayoutParams
        params.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        findViewById<FrameLayout>(R.id.common_title_back_user).layoutParams = params


    }

    override fun achievementClickListener(item: Achievement, position: Int) {
        
    }
}