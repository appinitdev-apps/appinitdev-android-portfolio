
class BmiHistoryManager(context: Context) {

    private val dbHelper = BmiDatabaseHelper(context)

    init {
        dbHelper.migrateFromSharedPreferencesIfNeeded()
    }

    fun saveRecord(record: BmiRecord) {
        dbHelper.insertHistoryRecord(record)
    }

    fun getRecords(): List<BmiRecord> {
        return dbHelper.getAllHistoryRecords()
    }

    fun clearHistory() {
        dbHelper.clearAllHistory()
    }

    fun deleteRecord(id: Long) {
        dbHelper.deleteHistoryRecord(id)
    }
}
