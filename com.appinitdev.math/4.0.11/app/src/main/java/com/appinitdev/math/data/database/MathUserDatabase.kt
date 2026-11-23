
class MathUserDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "math_user_study.db"
        private const val DATABASE_VERSION = 2

        private const val TABLE_FLASHCARDS = "user_flashcards"
        private const val TABLE_GLOSSARY = "user_glossary"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createFlashcardsTable = """
            CREATE TABLE $TABLE_FLASHCARDS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category TEXT NOT NULL,
                level INTEGER NOT NULL,
                question TEXT NOT NULL,
                formula_latex TEXT NOT NULL,
                explanation TEXT NOT NULL,
                is_favorite INTEGER DEFAULT 0
            );
        """.trimIndent()

        val createGlossaryTable = """
            CREATE TABLE $TABLE_GLOSSARY (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                symbol TEXT NOT NULL,
                name TEXT NOT NULL,
                category TEXT NOT NULL,
                description TEXT NOT NULL,
                example TEXT NOT NULL,
                is_favorite INTEGER DEFAULT 0
            );
        """.trimIndent()

        db.execSQL(createFlashcardsTable)
        db.execSQL(createGlossaryTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FLASHCARDS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GLOSSARY")
        onCreate(db)
    }

    
    fun insertFlashcard(card: Flashcard): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("category", card.category)
            put("level", card.level)
            put("question", card.question)
            put("formula_latex", card.formulaLatex)
            put("explanation", card.explanation)
            put("is_favorite", if (card.isFavorite) 1 else 0)
        }
        val id = db.insert(TABLE_FLASHCARDS, null, values)
        db.close()
        return id
    }

    fun deleteFlashcard(id: Long) {
        val db = writableDatabase
        db.delete(TABLE_FLASHCARDS, "id = ?", arrayOf(id.toString()))
        db.close()
    }

    fun toggleFlashcardFavorite(id: Long, currentFav: Boolean) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("is_favorite", if (!currentFav) 1 else 0)
        }
        db.update(TABLE_FLASHCARDS, values, "id = ?", arrayOf(id.toString()))
        db.close()
    }

    fun getUserFlashcards(): List<Flashcard> {
        val list = mutableListOf<Flashcard>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_FLASHCARDS ORDER BY id DESC", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val category = cursor.getString(cursor.getColumnIndexOrThrow("category"))
                val level = cursor.getInt(cursor.getColumnIndexOrThrow("level"))
                val question = cursor.getString(cursor.getColumnIndexOrThrow("question"))
                val formulaLatex = cursor.getString(cursor.getColumnIndexOrThrow("formula_latex"))
                val explanation = cursor.getString(cursor.getColumnIndexOrThrow("explanation"))
                val isFav = cursor.getInt(cursor.getColumnIndexOrThrow("is_favorite")) == 1

                list.add(
                    Flashcard(
                        id = id,
                        category = category,
                        level = level,
                        question = question,
                        formulaLatex = formulaLatex,
                        explanation = explanation,
                        isFavorite = isFav
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    
    fun insertGlossaryTerm(term: SymbolItem): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("symbol", term.symbol)
            put("name", term.name)
            put("category", term.category)
            put("description", term.description)
            put("example", term.example)
            put("is_favorite", if (term.isFavorite) 1 else 0)
        }
        val id = db.insert(TABLE_GLOSSARY, null, values)
        db.close()
        return id
    }

    fun deleteGlossaryTerm(id: Long) {
        val db = writableDatabase
        db.delete(TABLE_GLOSSARY, "id = ?", arrayOf(id.toString()))
        db.close()
    }

    fun toggleGlossaryFavorite(id: Long, currentFav: Boolean) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("is_favorite", if (!currentFav) 1 else 0)
        }
        db.update(TABLE_GLOSSARY, values, "id = ?", arrayOf(id.toString()))
        db.close()
    }

    fun getUserGlossaryTerms(): List<SymbolItem> {
        val list = mutableListOf<SymbolItem>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_GLOSSARY ORDER BY id DESC", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val symbol = cursor.getString(cursor.getColumnIndexOrThrow("symbol"))
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val category = cursor.getString(cursor.getColumnIndexOrThrow("category"))
                val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
                val example = cursor.getString(cursor.getColumnIndexOrThrow("example"))
                val isFav = cursor.getInt(cursor.getColumnIndexOrThrow("is_favorite")) == 1

                list.add(
                    SymbolItem(
                        id = id,
                        symbol = symbol,
                        name = name,
                        category = category,
                        description = description,
                        example = example,
                        isFavorite = isFav
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }
}
