
 













object StreakManager {
    private const val PREFS = "streak_prefs"
    private const val KEY_LAST_PLAY = "last_play_date" 
    private const val KEY_STREAK = "current_streak"
    private const val KEY_BEST = "best_streak"
    private const val KEY_REMINDER_SCHEDULED = "reminder_scheduled"
    private const val REMINDER_ACTION = "com.jlindemann.science.STREAK_REMINDER"

    
    private val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

     



    fun recordPlay(ctx: Context): Int {
        val p = prefs(ctx)
        val todayS = todayString()

        val lastS = p.getString(KEY_LAST_PLAY, null)
        var streak = p.getInt(KEY_STREAK, 0)
        var best = p.getInt(KEY_BEST, 0)

        if (lastS == todayS) {
            
            return streak
        }

        if (lastS != null) {
            try {
                val lastDate = formatter.parse(lastS)
                val lastCal = Calendar.getInstance().apply { time = lastDate!! }
                val todayCal = Calendar.getInstance()
                
                lastCal.add(Calendar.DAY_OF_YEAR, 1)
                val consecutive = lastCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                        lastCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)

                if (consecutive) {
                    
                    streak = streak + 1
                } else {
                    
                    streak = 1
                }
            } catch (e: Exception) {
                
                streak = 1
            }
        } else {
            
            streak = 1
        }

        if (streak > best) best = streak

        p.edit()
            .putString(KEY_LAST_PLAY, todayS)
            .putInt(KEY_STREAK, streak)
            .putInt(KEY_BEST, best)
            .apply()

        
        if (streak >= 3) {
            scheduleReminder(ctx)
        } else {
            cancelReminder(ctx)
        }

        return streak
    }

     



    fun getCurrentStreak(ctx: Context): Int {
        val p = prefs(ctx)
        val streak = p.getInt(KEY_STREAK, 0)
        
        
        if (streak <= 0) return 0
        
        
        val lastPlayS = p.getString(KEY_LAST_PLAY, null) ?: return 0
        
        try {
            val lastDate = formatter.parse(lastPlayS)
            val lastCal = Calendar.getInstance().apply { time = lastDate!! }
            val todayCal = Calendar.getInstance()
            
            val daysDiff = calculateDaysDifference(lastCal, todayCal)
            
            
            if (daysDiff > 1) {
                
                p.edit()
                    .putInt(KEY_STREAK, 0)
                    .remove(KEY_LAST_PLAY)
                    .apply()
                cancelReminder(ctx)
                return 0
            }
            
            return streak
        } catch (e: Exception) {
            
            p.edit()
                .putInt(KEY_STREAK, 0)
                .remove(KEY_LAST_PLAY)
                .apply()
            cancelReminder(ctx)
            return 0
        }
    }

     


    fun getBestStreak(ctx: Context): Int {
        return prefs(ctx).getInt(KEY_BEST, 0)
    }

     


    fun getLastPlayDate(ctx: Context): String? {
        return prefs(ctx).getString(KEY_LAST_PLAY, null)
    }

     


    fun resetStreak(ctx: Context) {
        val p = prefs(ctx)
        p.edit().remove(KEY_STREAK).remove(KEY_LAST_PLAY).apply()
        cancelReminder(ctx)
    }

    fun setCurrentStreak(ctx: Context, streakValue: Int) {
        try {
            val p = prefs(ctx)
            val current = p.getInt(KEY_STREAK, 0)
            val best = p.getInt(KEY_BEST, 0)
            var newBest = best
            var newStreak = streakValue.coerceAtLeast(0)

            
            if (newStreak > 0) {
                val lastPlayS = p.getString(KEY_LAST_PLAY, null)
                if (lastPlayS != null) {
                    try {
                        val lastDate = formatter.parse(lastPlayS)
                        val lastCal = Calendar.getInstance().apply { time = lastDate!! }
                        val todayCal = Calendar.getInstance()
                        
                        
                        val daysDiff = calculateDaysDifference(lastCal, todayCal)
                        
                        
                        if (daysDiff > 1) {
                            newStreak = 0
                        }
                    } catch (e: Exception) {
                        
                        newStreak = 0
                    }
                } else {
                    
                    newStreak = 0
                }
            }

            if (newStreak > newBest) {
                newBest = newStreak
            }

            p.edit()
                .putInt(KEY_STREAK, newStreak)
                .putInt(KEY_BEST, newBest)
                .apply()

            
            if (newStreak >= 3) {
                scheduleReminder(ctx)
            } else {
                cancelReminder(ctx)
            }
        } catch (t: Throwable) {
            
            t.printStackTrace()
        }
    }


    fun setCurrentStreakWithDate(ctx: Context, streakValue: Int, lastPlayDate: String?) {
        try {
            val p = prefs(ctx)
            val best = p.getInt(KEY_BEST, 0)
            var newBest = best
            var newStreak = streakValue.coerceAtLeast(0)

            
            if (newStreak > 0 && lastPlayDate != null) {
                try {
                    val lastDate = formatter.parse(lastPlayDate)
                    val lastCal = Calendar.getInstance().apply { time = lastDate!! }
                    val todayCal = Calendar.getInstance()
                    
                    val daysDiff = calculateDaysDifference(lastCal, todayCal)
                    
                    
                    if (daysDiff > 1) {
                        newStreak = 0
                    }
                } catch (e: Exception) {
                    
                    newStreak = 0
                }
            } else if (newStreak > 0 && lastPlayDate == null) {
                
                newStreak = 0
            }

            if (newStreak > newBest) {
                newBest = newStreak
            }

            val editor = p.edit()
                .putInt(KEY_STREAK, newStreak)
                .putInt(KEY_BEST, newBest)
            
            if (newStreak > 0 && lastPlayDate != null) {
                editor.putString(KEY_LAST_PLAY, lastPlayDate)
            } else {
                editor.remove(KEY_LAST_PLAY)
            }
            
            editor.apply()

            
            if (newStreak >= 3) {
                scheduleReminder(ctx)
            } else {
                cancelReminder(ctx)
            }
        } catch (t: Throwable) {
            
            t.printStackTrace()
        }
    }

     



    private fun calculateDaysDifference(from: Calendar, to: Calendar): Int {
        val fromYear = from.get(Calendar.YEAR)
        val fromDay = from.get(Calendar.DAY_OF_YEAR)
        val toYear = to.get(Calendar.YEAR)
        val toDay = to.get(Calendar.DAY_OF_YEAR)
        
        if (fromYear == toYear) {
            return toDay - fromDay
        } else {
            
            var days = from.getActualMaximum(Calendar.DAY_OF_YEAR) - fromDay
            for (year in (fromYear + 1) until toYear) {
                val cal = Calendar.getInstance()
                cal.set(Calendar.YEAR, year)
                days += cal.getActualMaximum(Calendar.DAY_OF_YEAR)
            }
            days += toDay
            return days
        }
    }

    private fun todayString(): String {
        val cal = Calendar.getInstance()
        return formatter.format(cal.time)
    }

    private fun reminderPendingIntent(ctx: Context): PendingIntent {
        val intent = Intent(ctx.applicationContext, StreakReminderReceiver::class.java).apply {
            action = REMINDER_ACTION
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(
            ctx.applicationContext,
            0,
            intent,
            flags
        )
    }

     



    fun scheduleReminder(ctx: Context) {
        try {
            val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val triggerAt = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)
            val pi = reminderPendingIntent(ctx)
            
            AlarmManagerCompat.setExactAndAllowWhileIdle(am, AlarmManager.RTC_WAKEUP, triggerAt, pi)
            prefs(ctx).edit().putBoolean(KEY_REMINDER_SCHEDULED, true).apply()
        } catch (t: Throwable) {
            
            t.printStackTrace()
        }
    }

     


    fun cancelReminder(ctx: Context) {
        try {
            val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pi = reminderPendingIntent(ctx)
            am.cancel(pi)
            prefs(ctx).edit().putBoolean(KEY_REMINDER_SCHEDULED, false).apply()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

     


    fun isReminderScheduled(ctx: Context): Boolean {
        return prefs(ctx).getBoolean(KEY_REMINDER_SCHEDULED, false)
    }
}