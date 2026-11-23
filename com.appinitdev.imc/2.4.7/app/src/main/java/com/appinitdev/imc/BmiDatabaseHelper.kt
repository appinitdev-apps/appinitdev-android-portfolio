data class ProfileMeasurement(
    val id: Long = System.currentTimeMillis(),
    val memberId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val metricType: String, 
    val valueNumeric: Double,
    val secondaryValue: Double = 0.0,
    val unitLabel: String,
    val statusLabel: String,
    val note: String = ""
)
class BmiDatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "bmi_app_database.db"
        private const val DATABASE_VERSION = 3
        private const val TABLE_HISTORY = "bmi_history"
        private const val COL_H_ID = "id"
        private const val COL_H_TIMESTAMP = "timestamp"
        private const val COL_H_CATEGORY_ID = "category_id"
        private const val COL_H_CATEGORY_NAME = "category_name"
        private const val COL_H_WEIGHT_KG = "weight_kg"
        private const val COL_H_HEIGHT_CM = "height_cm"
        private const val COL_H_BMI_VALUE = "bmi_value"
        private const val COL_H_STATUS_TEXT = "status_text"
        private const val COL_H_GENDER = "gender"
        private const val TABLE_MEMBERS = "family_members"
        private const val COL_M_ID = "id"
        private const val COL_M_NAME = "name"
        private const val COL_M_BIRTH_TIMESTAMP = "birth_timestamp"
        private const val COL_M_GENDER = "gender"
        private const val COL_M_RELATIONSHIP = "relationship"
        private const val COL_M_AVATAR_RES_ID = "avatar_res_id"
        private const val TABLE_GROWTH_RECORDS = "family_growth_records"
        private const val COL_G_ID = "id"
        private const val COL_G_MEMBER_ID = "member_id"
        private const val COL_G_TIMESTAMP = "timestamp"
        private const val COL_G_WEIGHT_KG = "weight_kg"
        private const val COL_G_HEIGHT_CM = "height_cm"
        private const val COL_G_BMI_VALUE = "bmi_value"
        private const val COL_G_STATUS_TEXT = "status_text"
        private const val TABLE_WATER = "daily_water_intake"
        private const val COL_W_ID = "id"
        private const val COL_W_DATE_STR = "date_str"
        private const val COL_W_CONSUMED_ML = "consumed_ml"
        private const val COL_W_TARGET_ML = "target_ml"
        private const val TABLE_MEASUREMENTS = "profile_measurements"
        private const val COL_PM_ID = "id"
        private const val COL_PM_MEMBER_ID = "member_id"
        private const val COL_PM_TIMESTAMP = "timestamp"
        private const val COL_PM_METRIC_TYPE = "metric_type"
        private const val COL_PM_VALUE_NUMERIC = "value_numeric"
        private const val COL_PM_SECONDARY_VALUE = "secondary_value"
        private const val COL_PM_UNIT_LABEL = "unit_label"
        private const val COL_PM_STATUS_LABEL = "status_label"
        private const val COL_PM_NOTE = "note"
    }
    override fun onCreate(db: SQLiteDatabase) {
        val createHistoryTable = """
            CREATE TABLE $TABLE_HISTORY (
                $COL_H_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_H_TIMESTAMP INTEGER NOT NULL,
                $COL_H_CATEGORY_ID INTEGER NOT NULL,
                $COL_H_CATEGORY_NAME TEXT NOT NULL,
                $COL_H_WEIGHT_KG REAL NOT NULL,
                $COL_H_HEIGHT_CM REAL NOT NULL,
                $COL_H_BMI_VALUE REAL NOT NULL,
                $COL_H_STATUS_TEXT TEXT NOT NULL,
                $COL_H_GENDER TEXT NOT NULL
            )
        """.trimIndent()
        val createMembersTable = """
            CREATE TABLE $TABLE_MEMBERS (
                $COL_M_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_M_NAME TEXT NOT NULL,
                $COL_M_BIRTH_TIMESTAMP INTEGER NOT NULL,
                $COL_M_GENDER TEXT NOT NULL,
                $COL_M_RELATIONSHIP TEXT NOT NULL,
                $COL_M_AVATAR_RES_ID INTEGER NOT NULL
            )
        """.trimIndent()
        val createGrowthTable = """
            CREATE TABLE $TABLE_GROWTH_RECORDS (
                $COL_G_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_G_MEMBER_ID INTEGER NOT NULL,
                $COL_G_TIMESTAMP INTEGER NOT NULL,
                $COL_G_WEIGHT_KG REAL NOT NULL,
                $COL_G_HEIGHT_CM REAL NOT NULL,
                $COL_G_BMI_VALUE REAL NOT NULL,
                $COL_G_STATUS_TEXT TEXT NOT NULL
            )
        """.trimIndent()
        val createWaterTable = """
            CREATE TABLE $TABLE_WATER (
                $COL_W_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_W_DATE_STR TEXT UNIQUE NOT NULL,
                $COL_W_CONSUMED_ML INTEGER NOT NULL,
                $COL_W_TARGET_ML INTEGER NOT NULL
            )
        """.trimIndent()
        val createMeasurementsTable = """
            CREATE TABLE $TABLE_MEASUREMENTS (
                $COL_PM_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PM_MEMBER_ID INTEGER NOT NULL,
                $COL_PM_TIMESTAMP INTEGER NOT NULL,
                $COL_PM_METRIC_TYPE TEXT NOT NULL,
                $COL_PM_VALUE_NUMERIC REAL NOT NULL,
                $COL_PM_SECONDARY_VALUE REAL DEFAULT 0.0,
                $COL_PM_UNIT_LABEL TEXT NOT NULL,
                $COL_PM_STATUS_LABEL TEXT NOT NULL,
                $COL_PM_NOTE TEXT DEFAULT ''
            )
        """.trimIndent()
        db.execSQL(createHistoryTable)
        db.execSQL(createMembersTable)
        db.execSQL(createGrowthTable)
        db.execSQL(createWaterTable)
        db.execSQL(createMeasurementsTable)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            val createWaterTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_WATER (
                    $COL_W_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COL_W_DATE_STR TEXT UNIQUE NOT NULL,
                    $COL_W_CONSUMED_ML INTEGER NOT NULL,
                    $COL_W_TARGET_ML INTEGER NOT NULL
                )
            """.trimIndent()
            db.execSQL(createWaterTable)
        }
        if (oldVersion < 3) {
            val createMeasurementsTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_MEASUREMENTS (
                    $COL_PM_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COL_PM_MEMBER_ID INTEGER NOT NULL,
                    $COL_PM_TIMESTAMP INTEGER NOT NULL,
                    $COL_PM_METRIC_TYPE TEXT NOT NULL,
                    $COL_PM_VALUE_NUMERIC REAL NOT NULL,
                    $COL_PM_SECONDARY_VALUE REAL DEFAULT 0.0,
                    $COL_PM_UNIT_LABEL TEXT NOT NULL,
                    $COL_PM_STATUS_LABEL TEXT NOT NULL,
                    $COL_PM_NOTE TEXT DEFAULT ''
                )
            """.trimIndent()
            db.execSQL(createMeasurementsTable)
        }
    }
    fun insertProfileMeasurement(measurement: ProfileMeasurement): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_PM_MEMBER_ID, measurement.memberId)
            put(COL_PM_TIMESTAMP, measurement.timestamp)
            put(COL_PM_METRIC_TYPE, measurement.metricType)
            put(COL_PM_VALUE_NUMERIC, measurement.valueNumeric)
            put(COL_PM_SECONDARY_VALUE, measurement.secondaryValue)
            put(COL_PM_UNIT_LABEL, measurement.unitLabel)
            put(COL_PM_STATUS_LABEL, measurement.statusLabel)
            put(COL_PM_NOTE, measurement.note)
        }
        return db.insert(TABLE_MEASUREMENTS, null, values)
    }
    fun getMeasurementsForMemberAndMetric(memberId: Long, metricType: String): List<ProfileMeasurement> {
        val list = mutableListOf<ProfileMeasurement>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_MEASUREMENTS,
            null,
            "$COL_PM_MEMBER_ID = ? AND $COL_PM_METRIC_TYPE = ?",
            arrayOf(memberId.toString(), metricType),
            null, null, "$COL_PM_TIMESTAMP ASC"
        )
        cursor.use {
            if (it.moveToFirst()) {
                val idIdx = it.getColumnIndexOrThrow(COL_PM_ID)
                val mIdIdx = it.getColumnIndexOrThrow(COL_PM_MEMBER_ID)
                val timeIdx = it.getColumnIndexOrThrow(COL_PM_TIMESTAMP)
                val typeIdx = it.getColumnIndexOrThrow(COL_PM_METRIC_TYPE)
                val valIdx = it.getColumnIndexOrThrow(COL_PM_VALUE_NUMERIC)
                val secValIdx = it.getColumnIndexOrThrow(COL_PM_SECONDARY_VALUE)
                val unitIdx = it.getColumnIndexOrThrow(COL_PM_UNIT_LABEL)
                val statusIdx = it.getColumnIndexOrThrow(COL_PM_STATUS_LABEL)
                val noteIdx = it.getColumnIndexOrThrow(COL_PM_NOTE)
                do {
                    list.add(
                        ProfileMeasurement(
                            id = it.getLong(idIdx),
                            memberId = it.getLong(mIdIdx),
                            timestamp = it.getLong(timeIdx),
                            metricType = it.getString(typeIdx),
                            valueNumeric = it.getDouble(valIdx),
                            secondaryValue = it.getDouble(secValIdx),
                            unitLabel = it.getString(unitIdx),
                            statusLabel = it.getString(statusIdx),
                            note = it.getString(noteIdx)
                        )
                    )
                } while (it.moveToNext())
            }
        }
        return list
    }
    fun getLatestMeasurementForMemberAndMetric(memberId: Long, metricType: String): ProfileMeasurement? {
        val list = getMeasurementsForMemberAndMetric(memberId, metricType)
        return list.lastOrNull()
    }
    fun getAllMeasurementsForMember(memberId: Long): List<ProfileMeasurement> {
        val list = mutableListOf<ProfileMeasurement>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_MEASUREMENTS,
            null,
            "$COL_PM_MEMBER_ID = ?",
            arrayOf(memberId.toString()),
            null, null, "$COL_PM_TIMESTAMP DESC"
        )
        cursor.use {
            if (it.moveToFirst()) {
                val idIdx = it.getColumnIndexOrThrow(COL_PM_ID)
                val mIdIdx = it.getColumnIndexOrThrow(COL_PM_MEMBER_ID)
                val timeIdx = it.getColumnIndexOrThrow(COL_PM_TIMESTAMP)
                val typeIdx = it.getColumnIndexOrThrow(COL_PM_METRIC_TYPE)
                val valIdx = it.getColumnIndexOrThrow(COL_PM_VALUE_NUMERIC)
                val secValIdx = it.getColumnIndexOrThrow(COL_PM_SECONDARY_VALUE)
                val unitIdx = it.getColumnIndexOrThrow(COL_PM_UNIT_LABEL)
                val statusIdx = it.getColumnIndexOrThrow(COL_PM_STATUS_LABEL)
                val noteIdx = it.getColumnIndexOrThrow(COL_PM_NOTE)
                do {
                    list.add(
                        ProfileMeasurement(
                            id = it.getLong(idIdx),
                            memberId = it.getLong(mIdIdx),
                            timestamp = it.getLong(timeIdx),
                            metricType = it.getString(typeIdx),
                            valueNumeric = it.getDouble(valIdx),
                            secondaryValue = it.getDouble(secValIdx),
                            unitLabel = it.getString(unitIdx),
                            statusLabel = it.getString(statusIdx),
                            note = it.getString(noteIdx)
                        )
                    )
                } while (it.moveToNext())
            }
        }
        return list
    }
    fun deleteProfileMeasurement(id: Long) {
        val db = writableDatabase
        db.delete(TABLE_MEASUREMENTS, "$COL_PM_ID = ?", arrayOf(id.toString()))
    }
    fun getWaterIntakeForDate(dateStr: String): Pair<Int, Int> {
        val db = readableDatabase
        val cursor = db.query(TABLE_WATER, arrayOf(COL_W_CONSUMED_ML, COL_W_TARGET_ML), "$COL_W_DATE_STR = ?", arrayOf(dateStr), null, null, null)
        cursor.use {
            if (it.moveToFirst()) {
                val consumed = it.getInt(it.getColumnIndexOrThrow(COL_W_CONSUMED_ML))
                val target = it.getInt(it.getColumnIndexOrThrow(COL_W_TARGET_ML))
                return Pair(consumed, target)
            }
        }
        return Pair(0, 2000)
    }
    fun addWaterIntake(dateStr: String, mlToAdd: Int, defaultTargetMl: Int = 2000): Int {
        val db = writableDatabase
        val current = getWaterIntakeForDate(dateStr)
        val newTotal = (current.first + mlToAdd).coerceAtLeast(0)
        val target = if (current.second > 0) current.second else defaultTargetMl
        val values = ContentValues().apply {
            put(COL_W_DATE_STR, dateStr)
            put(COL_W_CONSUMED_ML, newTotal)
            put(COL_W_TARGET_ML, target)
        }
        db.insertWithOnConflict(TABLE_WATER, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        return newTotal
    }
    fun resetWaterIntake(dateStr: String) {
        val db = writableDatabase
        db.delete(TABLE_WATER, "$COL_W_DATE_STR = ?", arrayOf(dateStr))
    }
    fun insertHistoryRecord(record: BmiRecord): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_H_TIMESTAMP, record.timestamp)
            put(COL_H_CATEGORY_ID, record.categoryId)
            put(COL_H_CATEGORY_NAME, record.categoryName)
            put(COL_H_WEIGHT_KG, record.weightKg)
            put(COL_H_HEIGHT_CM, record.heightCm)
            put(COL_H_BMI_VALUE, record.bmiValue)
            put(COL_H_STATUS_TEXT, record.statusText)
            put(COL_H_GENDER, record.gender.name)
        }
        return db.insert(TABLE_HISTORY, null, values)
    }
    fun getAllHistoryRecords(): List<BmiRecord> {
        val list = mutableListOf<BmiRecord>()
        val db = readableDatabase
        val cursor = db.query(TABLE_HISTORY, null, null, null, null, null, "$COL_H_TIMESTAMP DESC")
        cursor.use {
            if (it.moveToFirst()) {
                val idIdx = it.getColumnIndexOrThrow(COL_H_ID)
                val timeIdx = it.getColumnIndexOrThrow(COL_H_TIMESTAMP)
                val catIdIdx = it.getColumnIndexOrThrow(COL_H_CATEGORY_ID)
                val catNameIdx = it.getColumnIndexOrThrow(COL_H_CATEGORY_NAME)
                val weightIdx = it.getColumnIndexOrThrow(COL_H_WEIGHT_KG)
                val heightIdx = it.getColumnIndexOrThrow(COL_H_HEIGHT_CM)
                val bmiIdx = it.getColumnIndexOrThrow(COL_H_BMI_VALUE)
                val statusIdx = it.getColumnIndexOrThrow(COL_H_STATUS_TEXT)
                val genderIdx = it.getColumnIndexOrThrow(COL_H_GENDER)
                do {
                    list.add(
                        BmiRecord(
                            id = it.getLong(idIdx),
                            timestamp = it.getLong(timeIdx),
                            categoryId = it.getInt(catIdIdx),
                            categoryName = it.getString(catNameIdx),
                            weightKg = it.getDouble(weightIdx),
                            heightCm = it.getDouble(heightIdx),
                            bmiValue = it.getDouble(bmiIdx),
                            statusText = it.getString(statusIdx),
                            gender = if (it.getString(genderIdx) == "MALE") Gender.MALE else Gender.FEMALE
                        )
                    )
                } while (it.moveToNext())
            }
        }
        return list
    }
    fun deleteHistoryRecord(id: Long) {
        val db = writableDatabase
        db.delete(TABLE_HISTORY, "$COL_H_ID = ?", arrayOf(id.toString()))
    }
    fun clearAllHistory() {
        val db = writableDatabase
        db.delete(TABLE_HISTORY, null, null)
    }
    fun insertOrUpdateFamilyMember(member: FamilyMember): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_M_NAME, member.name)
            put(COL_M_BIRTH_TIMESTAMP, member.birthTimestamp)
            put(COL_M_GENDER, member.gender.name)
            put(COL_M_RELATIONSHIP, member.relationship)
            put(COL_M_AVATAR_RES_ID, member.avatarDrawableResId)
        }
        return if (member.id > 0 && existsMember(member.id)) {
            db.update(TABLE_MEMBERS, values, "$COL_M_ID = ?", arrayOf(member.id.toString()))
            member.id
        } else {
            db.insert(TABLE_MEMBERS, null, values)
        }
    }
    private fun existsMember(id: Long): Boolean {
        val db = readableDatabase
        val cursor = db.query(TABLE_MEMBERS, arrayOf(COL_M_ID), "$COL_M_ID = ?", arrayOf(id.toString()), null, null, null)
        cursor.use { return it.moveToFirst() }
    }
    fun getAllFamilyMembers(): List<FamilyMember> {
        val list = mutableListOf<FamilyMember>()
        val db = readableDatabase
        val cursor = db.query(TABLE_MEMBERS, null, null, null, null, null, "$COL_M_ID DESC")
        cursor.use {
            if (it.moveToFirst()) {
                val idIdx = it.getColumnIndexOrThrow(COL_M_ID)
                val nameIdx = it.getColumnIndexOrThrow(COL_M_NAME)
                val birthIdx = it.getColumnIndexOrThrow(COL_M_BIRTH_TIMESTAMP)
                val genderIdx = it.getColumnIndexOrThrow(COL_M_GENDER)
                val relIdx = it.getColumnIndexOrThrow(COL_M_RELATIONSHIP)
                val avatarIdx = it.getColumnIndexOrThrow(COL_M_AVATAR_RES_ID)
                do {
                    list.add(
                        FamilyMember(
                            id = it.getLong(idIdx),
                            name = it.getString(nameIdx),
                            birthTimestamp = it.getLong(birthIdx),
                            gender = if (it.getString(genderIdx) == "MALE") Gender.MALE else Gender.FEMALE,
                            relationship = it.getString(relIdx),
                            avatarDrawableResId = it.getInt(avatarIdx)
                        )
                    )
                } while (it.moveToNext())
            }
        }
        return list
    }
    fun deleteFamilyMember(memberId: Long) {
        val db = writableDatabase
        db.delete(TABLE_MEMBERS, "$COL_M_ID = ?", arrayOf(memberId.toString()))
        db.delete(TABLE_GROWTH_RECORDS, "$COL_G_MEMBER_ID = ?", arrayOf(memberId.toString()))
        db.delete(TABLE_MEASUREMENTS, "$COL_PM_MEMBER_ID = ?", arrayOf(memberId.toString()))
    }
    fun insertGrowthRecord(record: GrowthRecord): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_G_MEMBER_ID, record.memberId)
            put(COL_G_TIMESTAMP, record.timestamp)
            put(COL_G_WEIGHT_KG, record.weightKg)
            put(COL_G_HEIGHT_CM, record.heightCm)
            put(COL_G_BMI_VALUE, record.bmiValue)
            put(COL_G_STATUS_TEXT, record.statusText)
        }
        insertProfileMeasurement(
            ProfileMeasurement(
                memberId = record.memberId,
                timestamp = record.timestamp,
                metricType = "WEIGHT",
                valueNumeric = record.weightKg,
                secondaryValue = record.heightCm,
                unitLabel = "kg",
                statusLabel = record.statusText
            )
        )
        insertProfileMeasurement(
            ProfileMeasurement(
                memberId = record.memberId,
                timestamp = record.timestamp,
                metricType = "HEIGHT",
                valueNumeric = record.heightCm,
                secondaryValue = record.weightKg,
                unitLabel = "cm",
                statusLabel = record.statusText
            )
        )
        insertProfileMeasurement(
            ProfileMeasurement(
                memberId = record.memberId,
                timestamp = record.timestamp,
                metricType = "BMI",
                valueNumeric = record.bmiValue,
                secondaryValue = record.weightKg,
                unitLabel = "kg/m²",
                statusLabel = record.statusText
            )
        )
        return db.insert(TABLE_GROWTH_RECORDS, null, values)
    }
    fun getGrowthRecordsForMember(memberId: Long): List<GrowthRecord> {
        val list = mutableListOf<GrowthRecord>()
        val db = readableDatabase
        val cursor = db.query(TABLE_GROWTH_RECORDS, null, "$COL_G_MEMBER_ID = ?", arrayOf(memberId.toString()), null, null, "$COL_G_TIMESTAMP ASC")
        cursor.use {
            if (it.moveToFirst()) {
                val idIdx = it.getColumnIndexOrThrow(COL_G_ID)
                val memberIdIdx = it.getColumnIndexOrThrow(COL_G_MEMBER_ID)
                val timeIdx = it.getColumnIndexOrThrow(COL_G_TIMESTAMP)
                val weightIdx = it.getColumnIndexOrThrow(COL_G_WEIGHT_KG)
                val heightIdx = it.getColumnIndexOrThrow(COL_G_HEIGHT_CM)
                val bmiIdx = it.getColumnIndexOrThrow(COL_G_BMI_VALUE)
                val statusIdx = it.getColumnIndexOrThrow(COL_G_STATUS_TEXT)
                do {
                    list.add(
                        GrowthRecord(
                            id = it.getLong(idIdx),
                            memberId = it.getLong(memberIdIdx),
                            timestamp = it.getLong(timeIdx),
                            weightKg = it.getDouble(weightIdx),
                            heightCm = it.getDouble(heightIdx),
                            bmiValue = it.getDouble(bmiIdx),
                            statusText = it.getString(statusIdx)
                        )
                    )
                } while (it.moveToNext())
            }
        }
        return list
    }
    fun deleteGrowthRecord(recordId: Long) {
        val db = writableDatabase
        db.delete(TABLE_GROWTH_RECORDS, "$COL_G_ID = ?", arrayOf(recordId.toString()))
    }
    fun migrateFromSharedPreferencesIfNeeded() {
        try {
            val historyPrefs = context.getSharedPreferences("bmi_history_prefs", Context.MODE_PRIVATE)
            val historyJson = historyPrefs.getString("history_list", null)
            if (!historyJson.isNullOrBlank()) {
                val jsonArray = JSONArray(historyJson)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    insertHistoryRecord(
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
                historyPrefs.edit().clear().apply()
            }
            val familyPrefs = context.getSharedPreferences("family_profiles_prefs", Context.MODE_PRIVATE)
            val membersJson = familyPrefs.getString("members_list", null)
            if (!membersJson.isNullOrBlank()) {
                val jsonArray = JSONArray(membersJson)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    insertOrUpdateFamilyMember(
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
            val growthJson = familyPrefs.getString("growth_records_list", null)
            if (!growthJson.isNullOrBlank()) {
                val jsonArray = JSONArray(growthJson)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    insertGrowthRecord(
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
            familyPrefs.edit().clear().apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
