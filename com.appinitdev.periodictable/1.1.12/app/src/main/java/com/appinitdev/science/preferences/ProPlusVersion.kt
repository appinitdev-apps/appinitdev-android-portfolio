class ProPlusVersion(context : Context) {
    val PREFERENCE_NAME = "Pro_Plus_Preference"
    val PREFERENCE_VALUE = "Pro_Plus_Value"
    val preference = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    fun getValue() : Int{
        return preference.getInt (PREFERENCE_VALUE, 1) 
    }
    fun setValue(count:Int) {
        val editor = preference.edit()
        editor.putInt(PREFERENCE_VALUE, count)
        editor.apply()
    }
}