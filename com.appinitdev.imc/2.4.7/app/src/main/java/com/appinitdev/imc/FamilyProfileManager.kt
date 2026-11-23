
class FamilyProfileManager(context: Context) {

    private val dbHelper = BmiDatabaseHelper(context)
    private val prefs: SharedPreferences =
        context.getSharedPreferences("active_profile_prefs", Context.MODE_PRIVATE)

    init {
        dbHelper.migrateFromSharedPreferencesIfNeeded()
    }

    fun getActiveMemberId(): Long {
        val members = getMembers()
        if (members.isEmpty()) return 0L
        val savedId = prefs.getLong("active_member_id", -1L)
        val exists = members.any { it.id == savedId }
        return if (exists) savedId else members.first().id
    }

    fun setActiveMemberId(id: Long) {
        prefs.edit().putLong("active_member_id", id).apply()
    }

    fun getActiveMember(): FamilyMember? {
        val members = getMembers()
        if (members.isEmpty()) return null
        val activeId = getActiveMemberId()
        return members.find { it.id == activeId } ?: members.first()
    }

    fun saveMember(member: FamilyMember): Long {
        val id = dbHelper.insertOrUpdateFamilyMember(member)
        if (getActiveMemberId() == 0L || getActiveMemberId() == -1L) {
            setActiveMemberId(id)
        }
        return id
    }

    fun getMembers(): List<FamilyMember> {
        return dbHelper.getAllFamilyMembers()
    }

    fun deleteMember(memberId: Long) {
        dbHelper.deleteFamilyMember(memberId)
        val remaining = getMembers()
        if (remaining.isNotEmpty()) {
            if (getActiveMemberId() == memberId) {
                setActiveMemberId(remaining.first().id)
            }
        } else {
            setActiveMemberId(-1L)
        }
    }

    fun saveGrowthRecord(record: GrowthRecord): Long {
        return dbHelper.insertGrowthRecord(record)
    }

    fun getGrowthRecordsForMember(memberId: Long): List<GrowthRecord> {
        return dbHelper.getGrowthRecordsForMember(memberId)
    }

    fun deleteGrowthRecord(recordId: Long) {
        dbHelper.deleteGrowthRecord(recordId)
    }
}
