
object LivesManager {
    private const val PREFS_NAME = "lives_prefs"
    private const val LIVES_KEY = "lives_count"
    private const val LAST_REFILL_KEY = "last_refill_time"

    
    private const val NOTIF_PREFS = "lives_notifications_prefs"
    private const val PREF_KEY_WAS_UNDER_10 = "was_under_10"

    private const val DEFAULT_MAX_LIVES = 30
    private const val PRO_MAX_LIVES = 60
    private const val REFILL_AMOUNT = 5
    private const val REFILL_INTERVAL_MS = 10 * 60 * 1000L 

    
    private const val WORK_UNIQUE_NAME = "lives_refill_worker"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun getNotifPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(NOTIF_PREFS, Context.MODE_PRIVATE)

     


    fun getMaxLives(context: Context): Int {
        val proPlus = ProPlusVersion(context)
        val pro = ProVersion(context)
        val proPlusValue = proPlus.getValue()
        val proValue = pro.getValue()
        return when {
            proPlusValue == 100 -> Int.MAX_VALUE 
            proValue == 100 -> PRO_MAX_LIVES
            else -> DEFAULT_MAX_LIVES
        }
    }

    fun getLives(context: Context): Int {
        val maxLives = getMaxLives(context)
        val lives = getPrefs(context).getInt(LIVES_KEY, maxLives)
        return if (maxLives == Int.MAX_VALUE) Int.MAX_VALUE else lives.coerceAtMost(maxLives)
    }

    fun setLives(context: Context, lives: Int) {
        val maxLives = getMaxLives(context)
        val value = if (maxLives == Int.MAX_VALUE) Int.MAX_VALUE else lives.coerceIn(0, maxLives)
        getPrefs(context).edit().putInt(LIVES_KEY, value).apply()
        
        if (value >= maxLives && maxLives != Int.MAX_VALUE) {
            getPrefs(context).edit().putLong(LAST_REFILL_KEY, System.currentTimeMillis()).apply()
            
            if (wasUnderThreshold(context) && maxLives != Int.MAX_VALUE) {
                
                NotificationHelper.sendLivesRefilledNotification(context)
                clearUnderThresholdFlag(context)
            }
        }
    }

    fun loseLife(context: Context): Boolean {
        val prefs = getPrefs(context)
        val maxLives = getMaxLives(context)
        val lives = getLives(context)
        if (maxLives == Int.MAX_VALUE) return true 
        return if (lives > 0) {
            val newLives = lives - 1
            prefs.edit().putInt(LIVES_KEY, newLives).apply()
            
            if (prefs.getLong(LAST_REFILL_KEY, 0L) == 0L) {
                prefs.edit().putLong(LAST_REFILL_KEY, System.currentTimeMillis()).apply()
            }
            
            if (newLives < 10) {
                markUnderThreshold(context)
            }
            
            ensureRefillWorkerScheduled(context)
            true
        } else {
            false
        }
    }

    fun loseLives(context: Context, count: Int): Boolean {
        val prefs = getPrefs(context)
        val maxLives = getMaxLives(context)
        val lives = getLives(context)
        if (maxLives == Int.MAX_VALUE) return true 
        if (lives <= 0) return false
        val newLives = (lives - count).coerceAtLeast(0)
        prefs.edit().putInt(LIVES_KEY, newLives).apply()
        
        if (prefs.getLong(LAST_REFILL_KEY, 0L) == 0L) {
            prefs.edit().putLong(LAST_REFILL_KEY, System.currentTimeMillis()).apply()
        }
        if (newLives < 10) markUnderThreshold(context)
        
        ensureRefillWorkerScheduled(context)
        return true
    }

     











    fun refillLivesIfNeeded(context: Context): Boolean {
        val prefs = getPrefs(context)
        val maxLives = getMaxLives(context)
        if (maxLives == Int.MAX_VALUE) return false 

        val livesBefore = getLives(context)
        var lives = livesBefore
        var lastRefill = prefs.getLong(LAST_REFILL_KEY, 0L)
        val now = System.currentTimeMillis()

        
        if (lastRefill == 0L && lives < maxLives) {
            if (lives == 0) {
                
                
                lastRefill = now - REFILL_INTERVAL_MS
                prefs.edit().putLong(LAST_REFILL_KEY, lastRefill).apply()
            } else {
                
                lastRefill = now
                prefs.edit().putLong(LAST_REFILL_KEY, lastRefill).apply()
                
                return false
            }
        }

        
        if (lives >= maxLives) {
            prefs.edit().putLong(LAST_REFILL_KEY, now).apply()
            
            if (wasUnderThreshold(context)) {
                
                NotificationHelper.sendLivesRefilledNotification(context)
                clearUnderThresholdFlag(context)
            }
            return false
        }

        val intervalsPassed = ((now - lastRefill) / REFILL_INTERVAL_MS).toInt()
        if (intervalsPassed > 0) {
            val refillLives = intervalsPassed * REFILL_AMOUNT
            val newLives = (lives + refillLives).coerceAtMost(maxLives)
            
            val advancedRefillTime = lastRefill + intervalsPassed * REFILL_INTERVAL_MS
            prefs.edit()
                .putInt(LIVES_KEY, newLives)
                .putLong(LAST_REFILL_KEY, advancedRefillTime)
                .apply()

            
            if (newLives >= maxLives && wasUnderThreshold(context)) {
                NotificationHelper.sendLivesRefilledNotification(context)
                clearUnderThresholdFlag(context)
            }

            
            ensureRefillWorkerScheduled(context)
            return newLives > lives
        }
        return false
    }

    fun getMillisToRefill(context: Context): Long {
        val prefs = getPrefs(context)
        val maxLives = getMaxLives(context)
        val lives = getLives(context)
        if (maxLives == Int.MAX_VALUE || lives >= maxLives) return 0
        var lastRefill = prefs.getLong(LAST_REFILL_KEY, 0L)
        val now = System.currentTimeMillis()
        
        if (lastRefill == 0L) {
            
            return REFILL_INTERVAL_MS
        }
        val timeSinceLast = now - lastRefill
        val timeLeft = REFILL_INTERVAL_MS - (timeSinceLast % REFILL_INTERVAL_MS)
        return if (timeLeft > 0) timeLeft else 0
    }

    fun getRefillAmount(context: Context): Int {
        return REFILL_AMOUNT
    }

    fun getRefillIntervalMs(context: Context): Long {
        return REFILL_INTERVAL_MS
    }

    

     



    private fun markUnderThreshold(context: Context) {
        val p = getNotifPrefs(context)
        p.edit().putBoolean(PREF_KEY_WAS_UNDER_10, true).apply()
    }

    private fun clearUnderThresholdFlag(context: Context) {
        val p = getNotifPrefs(context)
        p.edit().putBoolean(PREF_KEY_WAS_UNDER_10, false).apply()
    }

    fun wasUnderThreshold(context: Context): Boolean {
        val p = getNotifPrefs(context)
        return p.getBoolean(PREF_KEY_WAS_UNDER_10, false)
    }

     





    fun ensureRefillWorkerScheduled(context: Context) {
        try {
            val wm = WorkManager.getInstance(context.applicationContext)
            
            val request = PeriodicWorkRequestBuilder<LivesRefillWorker>(15, TimeUnit.MINUTES)
                .setInitialDelay(5, TimeUnit.MINUTES)
                .build()
            wm.enqueueUniquePeriodicWork(WORK_UNIQUE_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}