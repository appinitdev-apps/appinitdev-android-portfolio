
object BackupRestoreManager {

    fun exportBackupJson(context: Context): String {
        val dbHelper = BmiDatabaseHelper(context)
        val root = JSONObject()

        // 1. Export History
        val historyArray = JSONArray()
        val historyRecords = dbHelper.getAllHistoryRecords()
        for (rec in historyRecords) {
            val obj = JSONObject().apply {
                put("id", rec.id)
                put("timestamp", rec.timestamp)
                put("categoryId", rec.categoryId)
                put("categoryName", rec.categoryName)
                put("weightKg", rec.weightKg)
                put("heightCm", rec.heightCm)
                put("bmiValue", rec.bmiValue)
                put("statusText", rec.statusText)
                put("gender", rec.gender.name)
            }
            historyArray.put(obj)
        }
        root.put("history", historyArray)

        // 2. Export Family Members
        val membersArray = JSONArray()
        val members = dbHelper.getAllFamilyMembers()
        for (m in members) {
            val obj = JSONObject().apply {
                put("id", m.id)
                put("name", m.name)
                put("birthTimestamp", m.birthTimestamp)
                put("gender", m.gender.name)
                put("relationship", m.relationship)
                put("avatarDrawableResId", m.avatarDrawableResId)
            }
            membersArray.put(obj)
        }
        root.put("members", membersArray)

        // 3. Export Growth Records
        val growthArray = JSONArray()
        for (m in members) {
            val records = dbHelper.getGrowthRecordsForMember(m.id)
            for (g in records) {
                val obj = JSONObject().apply {
                    put("id", g.id)
                    put("memberId", g.memberId)
                    put("timestamp", g.timestamp)
                    put("weightKg", g.weightKg)
                    put("heightCm", g.heightCm)
                    put("bmiValue", g.bmiValue)
                    put("statusText", g.statusText)
                }
                growthArray.put(obj)
            }
        }
        root.put("growth_records", growthArray)

        return root.toString(2)
    }

    fun importBackupJson(context: Context, jsonString: String): Boolean {
        return try {
            val dbHelper = BmiDatabaseHelper(context)
            val root = JSONObject(jsonString)

            if (root.has("history")) {
                val historyArray = root.getJSONArray("history")
                for (i in 0 until historyArray.length()) {
                    val obj = historyArray.getJSONObject(i)
                    dbHelper.insertHistoryRecord(
                        BmiRecord(
                            id = obj.optLong("id", System.currentTimeMillis()),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            categoryId = obj.optInt("categoryId", 5),
                            categoryName = obj.optString("categoryName", ""),
                            weightKg = obj.optDouble("weightKg", 0.0),
                            heightCm = obj.optDouble("heightCm", 0.0),
                            bmiValue = obj.optDouble("bmiValue", 0.0),
                            statusText = obj.optString("statusText", ""),
                            gender = if (obj.optString("gender", "FEMALE") == "MALE") Gender.MALE else Gender.FEMALE
                        )
                    )
                }
            }

            if (root.has("members")) {
                val membersArray = root.getJSONArray("members")
                for (i in 0 until membersArray.length()) {
                    val obj = membersArray.getJSONObject(i)
                    dbHelper.insertOrUpdateFamilyMember(
                        FamilyMember(
                            id = obj.optLong("id", System.currentTimeMillis()),
                            name = obj.optString("name", "Familiar"),
                            birthTimestamp = obj.optLong("birthTimestamp", System.currentTimeMillis()),
                            gender = if (obj.optString("gender", "FEMALE") == "MALE") Gender.MALE else Gender.FEMALE,
                            relationship = obj.optString("relationship", "Hijo/a"),
                            avatarDrawableResId = obj.optInt("avatarDrawableResId", R.drawable.uno)
                        )
                    )
                }
            }

            if (root.has("growth_records")) {
                val growthArray = root.getJSONArray("growth_records")
                for (i in 0 until growthArray.length()) {
                    val obj = growthArray.getJSONObject(i)
                    dbHelper.insertGrowthRecord(
                        GrowthRecord(
                            id = obj.optLong("id", System.currentTimeMillis()),
                            memberId = obj.optLong("memberId", 0L),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            weightKg = obj.optDouble("weightKg", 0.0),
                            heightCm = obj.optDouble("heightCm", 0.0),
                            bmiValue = obj.optDouble("bmiValue", 0.0),
                            statusText = obj.optString("statusText", "")
                        )
                    )
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
