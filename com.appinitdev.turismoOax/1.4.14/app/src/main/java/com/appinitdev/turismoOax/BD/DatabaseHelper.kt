class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    private val gson = Gson()
    companion object {
        private const val DATABASE_NAME = "turismo_oax.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_NOTIFICACIONES = "notificaciones"
        private const val COLUMN_ID = "id"
        private const val COLUMN_ASUNTO = "asunto"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_FECHA = "fecha"
        private const val TABLE_FAVORITOS = "favoritos"
        private const val TABLE_CACHE_TOURS = "cache_tours"
        private const val COL_TOUR_ID = "tour_id"
        private const val COL_NOMBRE = "nombre"
        private const val COL_IMAGEN_PRINCIPAL = "imagenPrincipal"
        private const val COL_IMAGENES_JSON = "imagenesJson"
        private const val COL_LUGARES_JSON = "lugaresJson"
        private const val COL_DESCRIPCION = "descripcion"
        private const val COL_COSTO_COLECTIVO = "costoColectivo"
        private const val COL_COSTO_PRIVADO = "costoPrivado"
        private const val COL_FLAYER = "flayer"
    }
    override fun onCreate(db: SQLiteDatabase?) {
        val createNotificaciones = ("CREATE TABLE $TABLE_NOTIFICACIONES(" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_ASUNTO TEXT," +
                "$COLUMN_NAME TEXT," +
                "$COLUMN_FECHA TEXT)")
        val createFavoritos = ("CREATE TABLE $TABLE_FAVORITOS(" +
                "$COL_TOUR_ID TEXT PRIMARY KEY," +
                "$COL_NOMBRE TEXT," +
                "$COL_IMAGEN_PRINCIPAL TEXT," +
                "$COL_IMAGENES_JSON TEXT," +
                "$COL_LUGARES_JSON TEXT," +
                "$COL_DESCRIPCION TEXT," +
                "$COL_COSTO_COLECTIVO TEXT," +
                "$COL_COSTO_PRIVADO TEXT," +
                "$COL_FLAYER TEXT)")
        val createCacheTours = ("CREATE TABLE $TABLE_CACHE_TOURS(" +
                "$COL_TOUR_ID TEXT PRIMARY KEY," +
                "$COL_NOMBRE TEXT," +
                "$COL_IMAGEN_PRINCIPAL TEXT," +
                "$COL_IMAGENES_JSON TEXT," +
                "$COL_LUGARES_JSON TEXT," +
                "$COL_DESCRIPCION TEXT," +
                "$COL_COSTO_COLECTIVO TEXT," +
                "$COL_COSTO_PRIVADO TEXT," +
                "$COL_FLAYER TEXT)")
        db?.execSQL(createNotificaciones)
        db?.execSQL(createFavoritos)
        db?.execSQL(createCacheTours)
    }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            val createFavoritos = ("CREATE TABLE IF NOT EXISTS $TABLE_FAVORITOS(" +
                    "$COL_TOUR_ID TEXT PRIMARY KEY," +
                    "$COL_NOMBRE TEXT," +
                    "$COL_IMAGEN_PRINCIPAL TEXT," +
                    "$COL_IMAGENES_JSON TEXT," +
                    "$COL_LUGARES_JSON TEXT," +
                    "$COL_DESCRIPCION TEXT," +
                    "$COL_COSTO_COLECTIVO TEXT," +
                    "$COL_COSTO_PRIVADO TEXT," +
                    "$COL_FLAYER TEXT)")
            val createCacheTours = ("CREATE TABLE IF NOT EXISTS $TABLE_CACHE_TOURS(" +
                    "$COL_TOUR_ID TEXT PRIMARY KEY," +
                    "$COL_NOMBRE TEXT," +
                    "$COL_IMAGEN_PRINCIPAL TEXT," +
                    "$COL_IMAGENES_JSON TEXT," +
                    "$COL_LUGARES_JSON TEXT," +
                    "$COL_DESCRIPCION TEXT," +
                    "$COL_COSTO_COLECTIVO TEXT," +
                    "$COL_COSTO_PRIVADO TEXT," +
                    "$COL_FLAYER TEXT)")
            db?.execSQL(createFavoritos)
            db?.execSQL(createCacheTours)
        }
    }
    fun insertNotificaciones(asunto: String, name: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ASUNTO, asunto)
            put(COLUMN_NAME, name)
            put(COLUMN_FECHA, getCurrentDateTime())
        }
        return db.insert(TABLE_NOTIFICACIONES, null, values)
    }
    @SuppressLint("Range")
    fun getAllNotificaciones(): List<Notificacion> {
        val notificaciones = mutableListOf<Notificacion>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_NOTIFICACIONES ORDER BY $COLUMN_FECHA DESC", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID))
                val asunto = cursor.getString(cursor.getColumnIndex(COLUMN_ASUNTO))
                val name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
                val fecha = cursor.getString(cursor.getColumnIndex(COLUMN_FECHA))
                notificaciones.add(Notificacion(id, asunto, name, fecha))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return notificaciones
    }
    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
    fun toggleFavorito(tour: Tour): Boolean {
        return if (isFavorito(tour.id)) {
            removeFavorito(tour.id)
            false
        } else {
            addFavorito(tour)
            true
        }
    }
    fun isFavorito(tourId: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT 1 FROM $TABLE_FAVORITOS WHERE $COL_TOUR_ID = ?", arrayOf(tourId))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }
    fun addFavorito(tour: Tour) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_TOUR_ID, tour.id)
            put(COL_NOMBRE, tour.nombre)
            put(COL_IMAGEN_PRINCIPAL, tour.imagenPrincipal)
            put(COL_IMAGENES_JSON, gson.toJson(tour.imagenes ?: emptyList<String>()))
            put(COL_LUGARES_JSON, gson.toJson(tour.lugares ?: emptyList<String>()))
            put(COL_DESCRIPCION, tour.descripcion)
            put(COL_COSTO_COLECTIVO, tour.costoColectivo)
            put(COL_COSTO_PRIVADO, tour.costoPrivado)
            put(COL_FLAYER, tour.flayer)
        }
        db.insertWithOnConflict(TABLE_FAVORITOS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }
    fun removeFavorito(tourId: String) {
        val db = this.writableDatabase
        db.delete(TABLE_FAVORITOS, "$COL_TOUR_ID = ?", arrayOf(tourId))
    }
    fun getAllFavoritos(): List<Tour> {
        return getToursFromTable(TABLE_FAVORITOS)
    }
    fun saveCacheTours(tours: List<Tour>) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_CACHE_TOURS, null, null)
            for (tour in tours) {
                val values = ContentValues().apply {
                    put(COL_TOUR_ID, tour.id)
                    put(COL_NOMBRE, tour.nombre)
                    put(COL_IMAGEN_PRINCIPAL, tour.imagenPrincipal)
                    put(COL_IMAGENES_JSON, gson.toJson(tour.imagenes ?: emptyList<String>()))
                    put(COL_LUGARES_JSON, gson.toJson(tour.lugares ?: emptyList<String>()))
                    put(COL_DESCRIPCION, tour.descripcion)
                    put(COL_COSTO_COLECTIVO, tour.costoColectivo)
                    put(COL_COSTO_PRIVADO, tour.costoPrivado)
                    put(COL_FLAYER, tour.flayer)
                }
                db.insertWithOnConflict(TABLE_CACHE_TOURS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
    fun getCacheTours(): List<Tour> {
        return getToursFromTable(TABLE_CACHE_TOURS)
    }
    @SuppressLint("Range")
    private fun getToursFromTable(tableName: String): List<Tour> {
        val tours = mutableListOf<Tour>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $tableName", null)
        val stringListType = object : TypeToken<ArrayList<String>>() {}.type
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndex(COL_TOUR_ID)) ?: ""
                val nombre = cursor.getString(cursor.getColumnIndex(COL_NOMBRE)) ?: ""
                val imagenPrincipal = cursor.getString(cursor.getColumnIndex(COL_IMAGEN_PRINCIPAL)) ?: ""
                val imagenesJson = cursor.getString(cursor.getColumnIndex(COL_IMAGENES_JSON))
                val lugaresJson = cursor.getString(cursor.getColumnIndex(COL_LUGARES_JSON))
                val descripcion = cursor.getString(cursor.getColumnIndex(COL_DESCRIPCION)) ?: ""
                val costoColectivo = cursor.getString(cursor.getColumnIndex(COL_COSTO_COLECTIVO)) ?: ""
                val costoPrivado = cursor.getString(cursor.getColumnIndex(COL_COSTO_PRIVADO)) ?: ""
                val flayer = cursor.getString(cursor.getColumnIndex(COL_FLAYER)) ?: ""
                val imagenes: ArrayList<String>? = if (!imagenesJson.isNullOrEmpty()) {
                    gson.fromJson(imagenesJson, stringListType)
                } else ArrayList()
                val lugares: ArrayList<String>? = if (!lugaresJson.isNullOrEmpty()) {
                    gson.fromJson(lugaresJson, stringListType)
                } else ArrayList()
                tours.add(
                    Tour(
                        id = id,
                        nombre = nombre,
                        imagenPrincipal = imagenPrincipal,
                        imagenes = imagenes,
                        lugares = lugares,
                        descripcion = descripcion,
                        costoColectivo = costoColectivo,
                        costoPrivado = costoPrivado,
                        flayer = flayer,
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return tours
    }
}
