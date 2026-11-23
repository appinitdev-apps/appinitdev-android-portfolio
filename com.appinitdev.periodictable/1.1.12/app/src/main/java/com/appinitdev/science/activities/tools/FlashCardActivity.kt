class FlashCardActivity : BaseActivity() {
    private lateinit var toggles: List<ToggleButton>
    private lateinit var infoText: TextView
    private var resultDialog: ResultDialogFragment? = null
    private var lastLevel: Int = -1
    private var backCallback: OnBackPressedCallback? = null
    private var onBackInvokedCb: android.window.OnBackInvokedCallback? = null
    private var livesPopupWindow: PopupWindow? = null
    private var livesPopupView: View? = null
    private val uiHandler = Handler(Looper.getMainLooper())
    private var cachedLives: Int = -1
    private var livesPolling = false
    private val LIVES_POLL_INTERVAL_MS = 1000L 
    private var tvEnableSyncLogin: TextView? = null
    private var tvSyncStatus: TextView? = null
    private var lastResultsHandledAt: Long = 0L
    private val RESULTS_DEBOUNCE_MS = 2000L
    private val oneTapLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            try {
            } catch (t: Throwable) {
            }
        }
    }
    private val livesPollRunnable = object : Runnable {
        override fun run() {
            val refilled = LivesManager.refillLivesIfNeeded(this@FlashCardActivity)
            val currentLives = LivesManager.getLives(this@FlashCardActivity)
            val livesChanged = currentLives != cachedLives
            if (livesChanged || refilled) {
                cachedLives = currentLives
                updateLivesCount()
                updateCategoryBoxes()
            }
            updateLivesInfo()
            if (livesPopupWindow?.isShowing == true && livesPopupView != null) {
                updateLivesPopupView(livesPopupView!!)
            }
            if (livesPolling) {
                uiHandler.postDelayed(this, LIVES_POLL_INTERVAL_MS)
            }
        }
    }
    private data class CategorySpec(val key: String, val labelRes: Int, val isPro: Boolean = false)
    private data class LevelBoxSpec(val range: IntRange, val categories: List<CategorySpec>)
    private val createdCategoryRows = mutableListOf<Pair<View, String>>() 
    private val REWARD_LEVELS = listOf(10, 15, 20)
    private val levelBoxesSpec = listOf(
        LevelBoxSpec(0..4, listOf(
            CategorySpec("element_symbols", R.string.element_symbols),
            CategorySpec("element_names", R.string.element_names),
            CategorySpec("element_classifications", R.string.element_groups),
            CategorySpec("discovered_by", R.string.discovered_by, isPro = true),
            CategorySpec("discovery_year", R.string.discovery_year, isPro = true)
        )),
        LevelBoxSpec(5..9, listOf(
            CategorySpec("appearance", R.string.appearance),
            CategorySpec("atomic_number", R.string.atomic_number),
            CategorySpec("electrical_type", R.string.electrical_type, isPro = true),
            CategorySpec("radioactive", R.string.radioactive, isPro = true)
        )),
        LevelBoxSpec(10..14, listOf(
            CategorySpec("atomic_mass", R.string.atomic_mass),
            CategorySpec("density", R.string.density),
            CategorySpec("electronegativity", R.string.electronegativity, isPro = true),
            CategorySpec("block", R.string.block, isPro = true)
        )),
        LevelBoxSpec(15..19, listOf(
            CategorySpec("magnetic_type", R.string.magnetic_type),
            CategorySpec("phase_stp", R.string.phase_stp),
            CategorySpec("crystal_structure", R.string.crystal_structure, isPro = true),
            CategorySpec("superconducting_point", R.string.superconducting_point, isPro = true)
        )),
        LevelBoxSpec(20..24, listOf(
            CategorySpec("neutron_cross_sectional", R.string.neutron_cross_sectional),
            CategorySpec("specific_heat_capacity", R.string.specific_heat_capacity),
            CategorySpec("mohs_hardness", R.string.mohs_hardness, isPro = true),
            CategorySpec("vickers_hardness", R.string.vickers_hardness, isPro = true),
            CategorySpec("brinell_hardness", R.string.brinell_hardness, isPro = true)
        )),
        LevelBoxSpec(25..29, listOf(
            CategorySpec("element_boiling_celsius", R.string.boiling_point_celsius),
            CategorySpec("element_boiling_fahrenheit", R.string.boiling_point_fahrenheit),
            CategorySpec("element_boiling_kelvin", R.string.boiling_point_kelvin),
            CategorySpec("element_melting_celsius", R.string.melting_point_celsius, isPro = true),
            CategorySpec("element_melting_fahrenheit", R.string.melting_point_fahrenheit, isPro = true),
            CategorySpec("element_melting_kelvin", R.string.melting_point_kelvin, isPro = true)
        )),
        LevelBoxSpec(30..34, listOf(
            CategorySpec("earth_crust", R.string.abundance_earth_crust),
            CategorySpec("earth_soils", R.string.abundance_earth_soils, isPro = true)
        ))
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themePreference = com.appinitdev.science.preferences.ThemePreference(this)
        val themePrefValue = themePreference.getValue()
        if (themePrefValue == 100) {
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_NO -> setTheme(R.style.AppTheme)
                Configuration.UI_MODE_NIGHT_YES -> setTheme(R.style.AppThemeDark)
            }
        }
        if (themePrefValue == 0) setTheme(R.style.AppTheme)
        if (themePrefValue == 1) setTheme(R.style.AppThemeDark)
        setContentView(R.layout.activity_flashcards)
        findViewById<FrameLayout>(R.id.view_flash).systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        tvSyncStatus = findViewById<TextView?>(R.id.sync_status)
        tvEnableSyncLogin?.setOnClickListener {
            var started = false
            try {
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            if (!started) {
            }
        }
        findViewById<TextView>(R.id.get_pro_plus_btn).setOnClickListener {
            startActivity(Intent(this, ProActivity::class.java))
        }
        val isProOrProPlus = (ProVersion(this).getValue() == 100) || (ProPlusVersion(this).getValue() == 100)
        if (isProOrProPlus) {
            tvSyncStatus?.visibility = View.VISIBLE
            tvSyncStatus?.text = "" 
        } else {
            tvSyncStatus?.visibility = View.GONE
        }
        findViewById<ImageButton>(R.id.back_btn_fla).setOnClickListener {
            onBackPressed()
        }
        val achievementsBtn = findViewById<ImageButton>(R.id.achievements_btn)
        achievementsBtn.setOnClickListener {
            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
        }
        try {
            val toolsLayout = findViewById<LinearLayout>(R.id.tools_layout)
            val livesTextView = findViewById<TextView>(R.id.tv_lives_count)
            val streakView = TextView(this).apply {
                id = resources.getIdentifier("streak_count_text", "id", packageName).takeIf { it != 0 }
                    ?: View.generateViewId()
                val sizePx = (46 * resources.displayMetrics.density).toInt()
                val padH = resources.getDimensionPixelSize(R.dimen.padding_small)
                val padV = resources.getDimensionPixelSize(R.dimen.padding_small)
                setPadding(padH, padV, padH, padV)
                setTextColor(context.obtainStyledAttributes(intArrayOf(R.attr.colorOnPrimary)).let { ta -> val color = ta.getColor(0, currentTextColor); ta.recycle(); color })
                textSize = 14f
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                setBackgroundResource(R.drawable.sunny)
                visibility = View.GONE
                layoutParams = LinearLayout.LayoutParams(sizePx, sizePx)
                setOnClickListener {
                    startActivity(Intent(this@FlashCardActivity, UserActivity::class.java))
                }
            }
            val insertIndex = if (toolsLayout.indexOfChild(livesTextView) >= 0) toolsLayout.indexOfChild(livesTextView) else toolsLayout.childCount
            toolsLayout.addView(streakView, insertIndex)
        } catch (_: Exception) {
        }
        val mostUsedPreference = MostUsedToolPreference(this)
        val mostUsedPrefValue = mostUsedPreference.getValue()
        val targetLabel = "fla"
        val regex = Regex("($targetLabel)=(\\d\\.\\d)")
        val match = regex.find(mostUsedPrefValue)
        if (match != null) {
            val value = match.groups[2]!!.value.toDouble()
            val newValue = value + 1
            mostUsedPreference.setValue(mostUsedPrefValue.replace("$targetLabel=$value", "$targetLabel=$newValue"))
        }
        infoText = findViewById(R.id.tv_lives_info)
        setupDifficultyToggles()
        buildLevelBoxes()
        findViewById<ImageButton>(R.id.shuffle_btn).setOnClickListener {
            launchRandomUnlockedGame()
        }
        setProFabVisibilityGoneIfProValue100()
        val livesCountView = findViewById<TextView>(R.id.tv_lives_count)
        livesCountView.setOnClickListener {
            showLivesInfoPopup(livesCountView)
        }
        backCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                val consumed = handleBackPress()
                backCallback?.isEnabled = anyOverlayOpen()
            }
        }
        onBackPressedDispatcher.addCallback(this, backCallback!!)
        supportFragmentManager.registerFragmentLifecycleCallbacks(object : androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentDestroyed(fm: androidx.fragment.app.FragmentManager, f: androidx.fragment.app.Fragment) {
                super.onFragmentDestroyed(fm, f)
                if (f is ResultDialogFragment) {
                    setBackInterceptionEnabled(anyOverlayOpen())
                }
            }
        }, true)
    }
    private fun buildLevelBoxes() {
        val container = findViewById<LinearLayout>(R.id.boxes_container)
        container.removeAllViews()
        createdCategoryRows.clear()
        val inflater = LayoutInflater.from(this)
        val isProUser = checkProPlusStatus()
        for (boxSpec in levelBoxesSpec) {
            val boxView = inflater.inflate(R.layout.level_box, container, false)
            val titleView = boxView.findViewById<TextView>(R.id.level_box_title)
            val categoriesContainer = boxView.findViewById<LinearLayout>(R.id.level_categories_container)
            val rewardsContainer = boxView.findViewById<LinearLayout>(R.id.level_rewards_container)
            val rewardsButtons = boxView.findViewById<LinearLayout>(R.id.level_rewards_buttons)
            titleView.text = getString(R.string.level_range_format, boxSpec.range.first)
            boxView.tag = boxSpec.range
            for (cat in boxSpec.categories) {
                val row = inflater.inflate(R.layout.category_button_row, categoriesContainer, false)
                val label = row.findViewById<TextView>(R.id.category_label)
                val proBadge = row.findViewById<TextView>(R.id.category_pro_badge)
                val icon = row.findViewById<ImageView>(R.id.category_icon)
                label.setText(cat.labelRes)
                proBadge.visibility = if (cat.isPro) View.VISIBLE else View.GONE
                row.tag = cat
                row.setOnClickListener {
                    val isPro = checkProPlusStatus()
                    if (cat.isPro && !isPro) {
                        startActivity(Intent(this, ProActivity::class.java))
                        return@setOnClickListener
                    }
                    if (!row.isEnabled) return@setOnClickListener
                    val intent = Intent(this, LearningGamesActivity::class.java)
                    intent.putExtra("difficulty", getSelectedDifficulty())
                    intent.putExtra("category", cat.key)
                    startActivity(intent)
                }
                categoriesContainer.addView(row)
                createdCategoryRows.add(row to cat.key)
            }
            val rewardLevelsInBox = REWARD_LEVELS.filter { it in boxSpec.range }
            rewardsButtons.removeAllViews()
            if (rewardLevelsInBox.isNotEmpty()) {
                rewardsContainer.visibility = View.VISIBLE
                for (rl in rewardLevelsInBox) {
                    val rewardView = inflater.inflate(R.layout.reward_button, rewardsButtons, false)
                    rewardView.tag = rl
                    val text = rewardView.findViewById<TextView>(R.id.reward_button_text)
                    val checkIcon = rewardView.findViewById<ImageView>(R.id.reward_check_icon)
                    text.text = getString(R.string.pro_plus_rewards, getString(R.string.reward_5pct_title))
                    val claimed = isRewardClaimed(rl)
                    if (claimed) {
                        checkIcon.setImageResource(R.drawable.ic_check)
                        checkIcon.visibility = View.VISIBLE
                        rewardView.alpha = 1f
                    } else {
                        checkIcon.visibility = View.GONE
                        rewardView.alpha = 0.6f
                    }
                    rewardView.isClickable = false
                    rewardView.isFocusable = true 
                    if (rl == 10 && !isProUser) {
                        rewardView.isEnabled = false
                        rewardView.alpha = 0.35f
                    } else {
                        rewardView.isEnabled = true
                    }
                    rewardsButtons.addView(rewardView)
                }
            } else {
                rewardsContainer.visibility = View.GONE
            }
            container.addView(boxView)
        }
        updateCategoryBoxes()
    }
    private fun isRewardClaimed(level: Int): Boolean {
        val prefs = getSharedPreferences("rewards_prefs", MODE_PRIVATE)
        return prefs.getBoolean("claimed_reward_$level", false)
    }
    private fun setRewardClaimed(level: Int) {
        val prefs = getSharedPreferences("rewards_prefs", MODE_PRIVATE)
        prefs.edit().putBoolean("claimed_reward_$level", true).apply()
    }
    private fun applyRewardIfNeeded(level: Int) {
        if (level == 10 && !checkProPlusStatus()) return
        if (isRewardClaimed(level)) return
        val userLevel = XpManager.getLevel(XpManager.getXp(this))
        if (userLevel < level) return
        XpManager.addXpBonusMultiplier(this, 0.05f)
        setRewardClaimed(level)
        Toast.makeText(this, "5% XP bonus unlocked at level $level!", Toast.LENGTH_SHORT).show()
        updateXpAndLevelStats()
    }
    private fun setProFabVisibilityGoneIfProValue100() {
        val proPreference = ProVersion(this)
        val valueP = proPreference.getValue()
        if (valueP == 100) {
            findViewById<TextView>(R.id.sync_desc_title).visibility = View.GONE
            findViewById<TextView>(R.id.sync_desc).visibility = View.GONE
        } else {
            findViewById<TextView>(R.id.sync_desc_title).visibility = View.VISIBLE
            findViewById<TextView>(R.id.sync_desc).visibility = View.VISIBLE
        }
        val proPlusPref = ProPlusVersion(this)
        val value = proPlusPref.getValue()
        if (value == 100) {
            findViewById<FrameLayout>(R.id.pro_box).visibility = View.GONE
        } else {
            findViewById<FrameLayout>(R.id.pro_box).visibility = View.VISIBLE
        }
    }
    override fun onResume() {
        super.onResume()
        LivesManager.refillLivesIfNeeded(this)
        cachedLives = LivesManager.getLives(this)
        updateLivesCount()
        updateLivesInfo()
        updateCategoryBoxes()
        updateXpAndLevelStats()
        setProFabVisibilityGoneIfProValue100()
        refreshStreakDisplay()
        val gameFinished = intent.getBooleanExtra("game_finished", false)
        val results = intent.getParcelableArrayListExtra<GameResultItem>("game_results")
        val totalQuestions = intent.getIntExtra("total_questions", results?.size ?: 0)
        val difficulty = intent.getStringExtra("difficulty") ?: "easy"
        if (results != null && results.isNotEmpty()) {
            showGameResultsPopup(results, gameFinished, totalQuestions, difficulty)
            intent.removeExtra("game_finished")
            intent.removeExtra("game_results")
            intent.removeExtra("total_questions")
            intent.removeExtra("difficulty")
        }
        showLevelUpPopupIfNeeded()
        startLivesPolling()
    }
    private fun refreshStreakDisplay() {
        try {
            val toolsLayout = findViewById<LinearLayout>(R.id.tools_layout)
            val streakViewId = resources.getIdentifier("streak_count_text", "id", packageName)
            val streakView = if (streakViewId != 0) findViewById<TextView?>(streakViewId) else null
            if (streakView == null) return
            val streak = StreakManager.getCurrentStreak(this)
            if (streak <= 0) {
                streakView.visibility = View.GONE
            } else {
                streakView.visibility = View.VISIBLE
                streakView.text = streak.toString()
            }
        } catch (_: Exception) {}
    }
    override fun onPause() {
        super.onPause()
        stopLivesPolling()
    }
    private fun startLivesPolling() {
        if (livesPolling) return
        livesPolling = true
        uiHandler.post(livesPollRunnable)
    }
    private fun stopLivesPolling() {
        livesPolling = false
        uiHandler.removeCallbacks(livesPollRunnable)
    }
    private fun showLevelUpPopupIfNeeded() {
        val xp = XpManager.getXp(this)
        val currentLevel = XpManager.getLevel(xp)
        if (lastLevel != -1 && currentLevel > lastLevel) {
            AlertDialog.Builder(this)
                .setTitle("Level Up!")
                .setMessage("Congratulations, you've reached level $currentLevel!")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .setCancelable(true)
                .show()
        }
        lastLevel = currentLevel
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val gameFinished = intent.getBooleanExtra("game_finished", false)
        val results = intent.getParcelableArrayListExtra<GameResultItem>("game_results")
        val totalQuestions = intent.getIntExtra("total_questions", results?.size ?: 0)
        val difficulty = intent.getStringExtra("difficulty") ?: "easy"
        if (results != null && results.isNotEmpty()) {
            showGameResultsPopup(results, gameFinished, totalQuestions, difficulty)
        }
    }
    private fun setupDifficultyToggles() {
        toggles = listOf(
            findViewById(R.id.toggle_easy),
            findViewById(R.id.toggle_medium),
            findViewById(R.id.toggle_hard)
        )
        toggles.forEach { toggle ->
            toggle.setOnCheckedChangeListener { btn, isChecked ->
                if (isChecked) {
                    toggles.filter { it != btn }.forEach { it.isChecked = false }
                }
            }
        }
        toggles[0].isChecked = true
    }
    private fun getSelectedDifficulty(): String {
        return when {
            toggles[0].isChecked -> "easy"
            toggles[1].isChecked -> "medium"
            toggles[2].isChecked -> "hard"
            else -> "easy"
        }
    }
    private fun updateCategoryBoxes() {
        val xp = XpManager.getXp(this)
        val userLevel = XpManager.getLevel(xp)
        val isProUser = checkProPlusStatus()
        val lives = LivesManager.getLives(this)
        val isEnabled = lives > 0
        val container = findViewById<LinearLayout>(R.id.boxes_container)
        for (i in 0 until container.childCount) {
            val box = container.getChildAt(i)
            val levelRange = box.tag as? IntRange ?: continue
            val title = box.findViewById<TextView>(R.id.level_box_title)
            val categoriesContainer = box.findViewById<LinearLayout>(R.id.level_categories_container)
            val rewardsContainer = box.findViewById<LinearLayout>(R.id.level_rewards_container)
            val rewardsButtons = box.findViewById<LinearLayout>(R.id.level_rewards_buttons)
            val unlocked = userLevel >= levelRange.last
            val current = userLevel in levelRange
            val boxIsUnlocked = unlocked || current
            box.alpha = if (boxIsUnlocked) 1f else 0.5f
            title.setLockDrawable(boxIsUnlocked)
            for (j in 0 until categoriesContainer.childCount) {
                val child = categoriesContainer.getChildAt(j)
                val cat = child.tag as? CategorySpec
                if (cat != null) {
                    val childIsEnabled = boxIsUnlocked && isEnabled && (!cat.isPro || isProUser)
                    child.isEnabled = childIsEnabled
                    child.alpha = if (childIsEnabled) 1f else 0.5f
                }
            }
            if (rewardsButtons != null && rewardsButtons.childCount > 0) {
                for (k in 0 until rewardsButtons.childCount) {
                    val child = rewardsButtons.getChildAt(k)
                    val rl = child.tag as? Int ?: continue
                    if (!isRewardClaimed(rl) && userLevel >= rl) {
                        applyRewardIfNeeded(rl)
                    }
                    val claimed = isRewardClaimed(rl)
                    val checkIcon = child.findViewById<ImageView?>(R.id.reward_check_icon)
                    val textView = child.findViewById<TextView?>(R.id.reward_button_text)
                    textView?.text = getString(R.string.reward_5pct_title)
                    if (claimed) {
                        checkIcon?.setImageResource(R.drawable.ic_check)
                        checkIcon?.visibility = View.VISIBLE
                        child.alpha = 1f
                    } else {
                        checkIcon?.visibility = View.GONE
                        child.alpha = 0.5f
                    }
                    child.isEnabled = false
                }
                rewardsContainer.visibility = if (rewardsButtons.childCount > 0) View.VISIBLE else View.GONE
            }
        }
    }
    private fun checkProPlusStatus(): Boolean {
        val proPlusPref = ProPlusVersion(this)
        val proPlusPrefValue = proPlusPref.getValue()
        return proPlusPrefValue == 100
    }
    private fun updateLivesInfo() {
        val lives = LivesManager.getLives(this)
        if (lives == 0) {
            infoText.visibility = View.VISIBLE
            val millis = LivesManager.getMillisToRefill(this)
            val hours = TimeUnit.MILLISECONDS.toHours(millis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
            if (hours > 0) {
                infoText.text = getString(R.string.out_of_lives_hours, hours, minutes, seconds)
            } else {
                infoText.text = getString(R.string.out_of_lives_minutes, minutes, seconds)
            }
        } else {
            infoText.visibility = View.GONE
        }
    }
    private fun updateXpAndLevelStats() {
        val xp = XpManager.getXp(this)
        val level = XpManager.getLevel(xp)
        val minXp = XpManager.getXpForLevel(level)
        val maxXp = XpManager.getXpForLevel(level + 1)
        val xpInLevel = xp - minXp
        val xpRequired = maxXp - minXp
        val completed = getCompletedQuizzes()
        findViewById<TextView>(R.id.completed_quizzes_stat).text = completed.toString()
        val multiplier = XpManager.getXpBonusMultiplier(this)
        val multiplierPct = ((multiplier - 1.0f) * 100).toInt()
        findViewById<TextView>(R.id.total_xp_stat).text = getString(R.string.total_xp_stat, xp)
        findViewById<TextView>(R.id.xp_bonus).text = getString(R.string.xp_bonus, multiplierPct)
        findViewById<TextView>(R.id.level_stat).text = level.toString()
        findViewById<ProgressBar>(R.id.xp_progress).apply {
            max = xpRequired
            progress = xpInLevel
        }
        findViewById<TextView>(R.id.progress_text).text = getString(R.string.progress_xp, xpInLevel, xpRequired)
    }
    override fun onBackPressed() {
        if (!handleBackPress()) {
            super.onBackPressed()
        }
    }
    private fun showGameResultsPopup(
        results: List<GameResultItem>,
        gameFinished: Boolean,
        totalQuestions: Int,
        difficulty: String = "easy"
    ) {
        val now = System.currentTimeMillis()
        if (now - lastResultsHandledAt < RESULTS_DEBOUNCE_MS) {
            return
        }
        lastResultsHandledAt = now
        if (resultDialog?.isVisible == true) return
        resultDialog = ResultDialogFragment.newInstance(results, gameFinished, totalQuestions, difficulty)
        resultDialog?.show(supportFragmentManager, "GameResultsPopup")
        updateXpAndLevelStats()
        if (gameFinished) {
            incrementCompletedQuizzes()
        }
        setBackInterceptionEnabled(true)
    }
    private fun getCompletedQuizzes(): Int {
        val prefs = getSharedPreferences("game_stats", MODE_PRIVATE)
        return prefs.getInt("completed_quizzes", 0)
    }
    private fun incrementCompletedQuizzes() {
        val prefs = getSharedPreferences("game_stats", MODE_PRIVATE)
        val current = prefs.getInt("completed_quizzes", 0)
        prefs.edit().putInt("completed_quizzes", current + 1).apply()
    }
    fun View.animateVisibility(
        setVisible: Boolean,
        duration: Long = 200,
        visibleAlpha: Float = 1.0f
    ) {
        if (setVisible) {
            alpha = 0f
            visibility = View.VISIBLE
            animate()
                .alpha(visibleAlpha)
                .setDuration(duration)
                .setListener(null)
        } else {
            animate()
                .alpha(0f)
                .setDuration(duration)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        visibility = View.INVISIBLE
                    }
                })
        }
    }
    private fun getAllUnlockedCategoryButtons(): List<Pair<View, String>> {
        val isPro = checkProPlusStatus()
        return createdCategoryRows.filter { (view, key) ->
            val catSpec = view.tag as? CategorySpec
            if (catSpec == null) false
            else view.isEnabled && (!catSpec.isPro || isPro)
        }.map { it.first to it.second }
    }
    private fun launchRandomUnlockedGame() {
        val unlocked = getAllUnlockedCategoryButtons()
        if (unlocked.isEmpty()) {
            Toast.makeText(this, "No unlocked games available!", Toast.LENGTH_SHORT).show()
            return
        }
        val (btn, category) = unlocked.random()
        val catSpec = btn.tag as? CategorySpec
        val isProCategory = catSpec?.isPro == true
        if (isProCategory && !checkProPlusStatus()) {
            startActivity(Intent(this, ProActivity::class.java))
            return
        }
        val intent = Intent(this, LearningGamesActivity::class.java)
        intent.putExtra("difficulty", getSelectedDifficulty())
        intent.putExtra("category", category)
        startActivity(intent)
    }
    override fun onApplySystemInsets(top: Int, bottom: Int, left: Int, right: Int) {
        val params = findViewById<FrameLayout>(R.id.common_title_back_fla).layoutParams as ViewGroup.LayoutParams
        params.height = top + resources.getDimensionPixelSize(R.dimen.title_bar)
        findViewById<FrameLayout>(R.id.common_title_back_fla).layoutParams = params
        val params2 = findViewById<TextView>(R.id.flashcard_title_downstate).layoutParams as ViewGroup.MarginLayoutParams
        params2.topMargin = top
        findViewById<TextView>(R.id.flashcard_title_downstate).layoutParams = params2
    }
    private fun showLivesInfoPopup(anchor: View) {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.popup_lives_info, null)
        LivesManager.refillLivesIfNeeded(this)
        livesPopupView = popupView
        updateLivesPopupView(popupView)
        livesPopupWindow?.dismiss()
        val popupWindow = PopupWindow(
            popupView,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.elevation = 8f
        livesPopupWindow = popupWindow
        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, location[0], location[1] + anchor.height)
        setBackInterceptionEnabled(true)
        popupWindow.setOnDismissListener {
            setBackInterceptionEnabled(anyOverlayOpen())
            livesPopupWindow = null
            livesPopupView = null
        }
        Handler(Looper.getMainLooper()).postDelayed({
            if (popupWindow.isShowing) popupWindow.dismiss()
        }, 3000)
    }
    private fun updateLivesPopupView(popupView: View) {
        val lives = LivesManager.getLives(this)
        val millis = LivesManager.getMillisToRefill(this)
        val maxLives = LivesManager.getMaxLives(this)
        val refillAmount = LivesManager.getRefillAmount(this)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        val livesText = popupView.findViewById<TextView>(R.id.lives_info_text)
        if (lives >= maxLives) {
            livesText.text = getString(R.string.you_have_full_lives)
        } else {
            livesText.text = getString(R.string.next_life_in, minutes, seconds, refillAmount, if (refillAmount > 1) "s" else "")
        }
    }
    private fun anyOverlayOpen(): Boolean {
        val dialogVisible = resultDialog?.isVisible == true
        val popupVisible = livesPopupWindow?.isShowing == true
        return dialogVisible || popupVisible
    }
    private fun setBackInterceptionEnabled(enabled: Boolean) {
        backCallback?.isEnabled = enabled
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (enabled) {
                if (onBackInvokedCb == null) {
                    onBackInvokedCb = android.window.OnBackInvokedCallback {
                        handleBackPress()
                        if (!anyOverlayOpen()) {
                            try {
                                onBackInvokedDispatcher.unregisterOnBackInvokedCallback(onBackInvokedCb!!)
                            } catch (_: Exception) { }
                            onBackInvokedCb = null
                            backCallback?.isEnabled = false
                        }
                    }
                    onBackInvokedDispatcher.registerOnBackInvokedCallback(
                        android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                        onBackInvokedCb!!
                    )
                }
            } else {
                if (onBackInvokedCb != null) {
                    try {
                        onBackInvokedDispatcher.unregisterOnBackInvokedCallback(onBackInvokedCb!!)
                    } catch (_: Exception) { }
                    onBackInvokedCb = null
                }
            }
        }
    }
    private fun handleBackPress(): Boolean {
        if (resultDialog?.isVisible == true) {
            resultDialog?.dismiss()
            setBackInterceptionEnabled(anyOverlayOpen())
            return true
        }
        if (livesPopupWindow?.isShowing == true) {
            livesPopupWindow?.dismiss()
            setBackInterceptionEnabled(anyOverlayOpen())
            return true
        }
        return false
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
        livesPopupWindow?.dismiss()
        livesPopupWindow = null
        stopLivesPolling()
    }
    private fun TextView.setLockDrawable(unlocked: Boolean) {
        val drawable = if (unlocked) R.drawable.ic_lock_open else R.drawable.ic_lock
        setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0)
    }
}