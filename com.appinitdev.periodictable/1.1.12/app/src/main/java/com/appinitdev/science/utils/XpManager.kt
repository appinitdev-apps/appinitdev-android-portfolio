
object XpManager {
    private const val XP_KEY = "user_xp"
    private const val PREFS = "xp_prefs"
    private const val LEVELS = 100

    
    private const val REWARDS_PREFS = "rewards_prefs"
    private const val XP_BONUS_KEY = "xp_bonus_multiplier"
    private const val DEFAULT_XP_MULTIPLIER = 1.0f

    
    private val xpTable = intArrayOf(
        0,       
        90,      
        180,     
        270,     
        360,     
        495,     
        630,     
        765,     
        900,     
        1050,    
        1275,    
        1500,    
        1725,    
        1950,    
        2175,    
        2550,    
        2925,    
        3300,    
        3675,    
        4050,    
        4575     
    )

    
    private fun xpToReachLevel(n: Int): Int {
        if (n <= 1) return 0
        if (n - 1 < xpTable.size) return xpTable[n - 1]
        
        
        val base = xpTable.last()
        val extraLevels = n - xpTable.size
        return (base + (600 * (1.02.pow(extraLevels) - 1) / 0.02)).roundToInt()
    }

    fun getLevel(xp: Int): Int {
        var level = 1
        while (level < LEVELS && xpToReachLevel(level + 1) <= xp) {
            level++
        }
        return level
    }

    fun getXpForLevel(level: Int): Int {
        return xpToReachLevel(level)
    }

    fun getXp(context: Context): Int {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(XP_KEY, 0)
    }

     




    fun setXp(context: Context, xp: Int) {
        val safe = if (xp < 0) 0 else xp
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putInt(XP_KEY, safe).apply()
    }

     






    fun addXp(context: Context, amount: Int) {
        if (amount <= 0) return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val oldXp = prefs.getInt(XP_KEY, 0)
        val newXp = oldXp + amount
        prefs.edit().putInt(XP_KEY, newXp).apply()
    }

     





    fun addGameXp(context: Context, amount: Int) {
        if (amount <= 0) return
        val multiplier = getXpBonusMultiplier(context).coerceAtLeast(DEFAULT_XP_MULTIPLIER)
        val finalAmount = (amount * multiplier).roundToInt()

        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val oldXp = prefs.getInt(XP_KEY, 0)
        val newXp = oldXp + finalAmount
        prefs.edit().putInt(XP_KEY, newXp).apply()
    }

    fun getCurrentLevel(context: Context): Int {
        return getLevel(getXp(context))
    }

    fun getXpProgressInLevel(context: Context): Pair<Int, Int> {
        val xp = getXp(context)
        val level = getLevel(xp)
        val minXp = getXpForLevel(level)
        val maxXp = getXpForLevel(level + 1)
        return Pair(xp - minXp, maxXp - minXp)
    }

    

     



    fun getXpBonusMultiplier(context: Context): Float {
        return context.getSharedPreferences(REWARDS_PREFS, Context.MODE_PRIVATE)
            .getFloat(XP_BONUS_KEY, DEFAULT_XP_MULTIPLIER)
    }

     



    fun addXpBonusMultiplier(context: Context, delta: Float) {
        if (delta == 0f) return
        val prefs = context.getSharedPreferences(REWARDS_PREFS, Context.MODE_PRIVATE)
        val curr = prefs.getFloat(XP_BONUS_KEY, DEFAULT_XP_MULTIPLIER)
        prefs.edit().putFloat(XP_BONUS_KEY, curr + delta).apply()
    }

     


    fun setXpBonusMultiplier(context: Context, multiplier: Float) {
        val prefs = context.getSharedPreferences(REWARDS_PREFS, Context.MODE_PRIVATE)
        prefs.edit().putFloat(XP_BONUS_KEY, multiplier.coerceAtLeast(DEFAULT_XP_MULTIPLIER)).apply()
    }
}