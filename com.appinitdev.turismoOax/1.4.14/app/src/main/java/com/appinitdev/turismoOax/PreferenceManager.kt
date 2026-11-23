
class PreferenceManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    
    fun guardarMembresia(comprada: Boolean) {
        sharedPreferences.edit().putBoolean("membresia_comprada", comprada).apply()
    }

    
    fun esMembresiaComprada(): Boolean {
        return sharedPreferences.getBoolean("membresia_comprada", false)
    }

    
    fun limpiarMembresia() {
        sharedPreferences.edit().remove("membresia_comprada").apply()
    }
}