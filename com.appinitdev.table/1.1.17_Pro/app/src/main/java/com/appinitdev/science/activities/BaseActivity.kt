
abstract class BaseActivity : AppCompatActivity(), View.OnApplyWindowInsetsListener {
    companion object {
        private const val TAG = "BaseActivity"
    }

    private var systemUiConfigured = false

    // --- Locale wrapping for runtime language switching ---
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("app_language", null)
        val country = prefs.getString("app_country", null)

        if (lang.isNullOrBlank()) {
            super.attachBaseContext(newBase)
            return
        }

        // Si el usuario sí eligió un idioma, construimos el Locale y envolvemos el contexto
        val locale = if (country.isNullOrBlank()) Locale(lang) else Locale(lang, country)
        val context = LocaleUtil.wrap(newBase, locale)

        super.attachBaseContext(context)
//        val locale = if (lang.isNullOrBlank()) null
//        else if (country.isNullOrBlank()) Locale(lang)
//        else Locale(lang, country)
//        val context = LocaleUtil.wrap(newBase, locale)
//        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // initialize Firebase


        // Initialize Firebase Analytics
        AnalyticsHelper.initialize(this)


    }

    override fun onResume() {
        super.onResume()
        checkAchievements()
        
        // Log screen view for analytics
        val screenName = this.javaClass.simpleName.replace("Activity", "")
        AnalyticsHelper.logScreenView(this, screenName, this.javaClass.simpleName)
    }

    override fun onStart() {
        super.onStart()
        val content = findViewById<View>(android.R.id.content)
        content.setOnApplyWindowInsetsListener(this)

        if (!systemUiConfigured) {
            systemUiConfigured = true
        }
    }

    open fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) = Unit

    override fun onApplyWindowInsets(v: View, insets: WindowInsets): WindowInsets {
        Pasteur.info(TAG, "height: ${insets.systemWindowInsetBottom}")
        onApplySystemInsets(
            insets.systemWindowInsetTop,
            insets.systemWindowInsetBottom,
            insets.systemWindowInsetLeft,
            insets.systemWindowInsetRight
        )
        return insets.consumeSystemWindowInsets()
    }

    private fun checkAchievements() {
        val achievements = ArrayList<Achievement>()
        AchievementModel.getList(this, achievements)

        for (achievement in achievements) {
            if (achievement.isMaxProgressReached() && !achievement.isToastShown(this)) {
                Toast.makeText(this, "Achievement reached: ${achievement.title}", Toast.LENGTH_SHORT).show()
                achievement.markToastShown(this)
            }
        }
    }

    open fun updateLivesCount() {
        val lives = LivesManager.getLives(this)
        val maxLives = LivesManager.getMaxLives(this)
        val livesDisplay = if (maxLives == Int.MAX_VALUE) "∞" else lives.toString()

        // Try both possible IDs (XML may differ between screens)
        val livesCountView = findViewById<TextView?>(R.id.tv_lives_count)
        livesCountView?.text = livesDisplay

        val livesLabelView = findViewById<TextView?>(R.id.tv_lives)
        livesLabelView?.text = getString(R.string.lives_label, livesDisplay)
    }


}