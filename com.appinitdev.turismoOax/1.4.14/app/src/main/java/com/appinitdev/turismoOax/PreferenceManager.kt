
class PreferenceManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    // Guardar el estado de la membresía
    fun guardarMembresia(comprada: Boolean) {
        sharedPreferences.edit().putBoolean("membresia_comprada", comprada).apply()
    }

    // Verificar si la membresía ha sido comprada
    fun esMembresiaComprada(): Boolean {
        return sharedPreferences.getBoolean("membresia_comprada", false)
    }

    // Limpiar el estado de la membresía (opcional, para pruebas o logout)
    fun limpiarMembresia() {
        sharedPreferences.edit().remove("membresia_comprada").apply()
    }
}