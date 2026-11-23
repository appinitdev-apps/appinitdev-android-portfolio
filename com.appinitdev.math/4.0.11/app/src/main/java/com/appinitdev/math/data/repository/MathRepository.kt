
class MathRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("math_app_prefs", Context.MODE_PRIVATE)

    private var cachedTopics: List<MathTopic>? = null
    private var cachedCategories: List<MathCategory>? = null
    private var cachedLang: String? = null

    fun getSelectedLanguage(): String {
        return prefs.getString("selected_lang", "es") ?: "es"
    }

    fun setSelectedLanguage(langCode: String) {
        prefs.edit().putString("selected_lang", langCode).apply()
        clearCache()
    }

    fun clearCache() {
        cachedTopics = null
        cachedCategories = null
    }

    private fun getLanguageBasePath(): String {
        val lang = getSelectedLanguage()
        val availableLangs = listOf("es", "en", "de", "fr", "pt", "zh", "ja", "ko", "hi", "id", "ru", "nl", "sv", "no", "da", "pl", "th", "tr", "uk", "vi")
        val activeLang = if (availableLangs.contains(lang)) lang else "es"
        return "Data/$activeLang"
    }

    fun getCategories(): List<MathCategory> {
        val currentLang = getSelectedLanguage()
        if (cachedCategories != null && cachedLang == currentLang) {
            return cachedCategories!!
        }

        val basePath = getLanguageBasePath()
        val allTopics = getAllTopics()
        val categoryFolders = try {
            context.assets.list(basePath)?.filter {
                (it != "category") && (!it.contains("."))
            } ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }

        val list = categoryFolders.map { folder ->
            val info = getCategoryInfo(folder)
            val topicCount = allTopics.count { it.categoryId == folder }
            MathCategory(
                id = folder,
                title = info.title,
                description = info.description,
                domain = info.domain,
                topicCount = topicCount
            )
        }.filter { it.topicCount > 0 }
            .sortedBy { it.title }

        cachedCategories = list
        cachedLang = currentLang
        return list
    }

    fun getAllTopics(): List<MathTopic> {
        val currentLang = getSelectedLanguage()
        if (cachedTopics != null && cachedLang == currentLang) {
            val favorites = getFavoritesSet()
            return cachedTopics!!.map { topic ->
                topic.copy(isFavorite = favorites.contains(topic.id))
            }
        }

        val basePath = getLanguageBasePath()
        val topicList = mutableListOf<MathTopic>()
        val favorites = getFavoritesSet()

        try {
            val categories = context.assets.list(basePath) ?: emptyArray()
            for (category in categories) {
                if (category == "category") continue
                val categoryPath = "$basePath/$category"
                val files = context.assets.list(categoryPath) ?: continue

                for (file in files) {
                    if (file.endsWith(".md")) {
                        val filePath = "$categoryPath/$file"
                        val topicId = "$category/$file"

                        val title = getTopicTitleFromFile(filePath, file)
                        val analysis = analyzeMarkdownFile(filePath)

                        topicList.add(
                            MathTopic(
                                id = topicId,
                                categoryId = category,
                                fileName = file,
                                title = title,
                                filePath = filePath,
                                summarySnippet = analysis.snippet,
                                hasExamples = analysis.exampleCount > 0,
                                exampleCount = analysis.exampleCount,
                                isFavorite = favorites.contains(topicId)
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val sortedList = topicList.sortedBy { it.title }
        cachedTopics = sortedList
        cachedLang = currentLang
        return sortedList
    }

    private val genericTitleWords = setOf(
        "definición", "definicion", "definition", "definitionen", "definição", "definicao", "définition", "definizione",
        "ejemplo", "ejemplo 1", "ejemplo 2", "example", "example 1", "example 2", "beispiel", "beispiel 1", "exemple", "exemplo",
        "introducción", "introduccion", "introduction", "einführung", "einfuehrung", "introdução", "introducao", "введение", "вступ",
        "demostración", "demostracion", "proof", "beweis", "preuve", "demonstração",
        "solución", "solucion", "solution", "lösung", "solução",
        "propiedades", "properties", "eigenschaften", "propriétés", "propriedades",
        "statement of the theorem", "there is always a way", "order of an infinitesimal"
    )

    private fun getTopicTitleFromFile(filePath: String, fileName: String): String {
        val formatted = formatTopicTitle(fileName)
        val formattedLower = formatted.lowercase().trim()
        val isFormattedGeneric = genericTitleWords.any { formattedLower == it || formattedLower.startsWith("$it ") || formattedLower.startsWith("$it:") }
        if (!isFormattedGeneric && formatted.isNotBlank()) {
            return formatted
        }

        try {
            val inputStream = context.assets.open(filePath)
            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val trimmed = line!!.trim()
                if (trimmed.startsWith("# ") || trimmed.startsWith("## ")) {
                    val candidate = trimmed.replace("#", "").trim()
                    val candidateLower = candidate.lowercase()
                    val isGeneric = genericTitleWords.any { candidateLower == it || candidateLower.startsWith("$it ") || candidateLower.startsWith("$it:") }
                    if (candidate.isNotBlank() && !isGeneric) {
                        return candidate
                    }
                }
            }
        } catch (_: Exception) {
            
        }
        return fileName.removeSuffix(".md").replace("-", " ").capitalizeWords()
    }

    fun advancedSearch(query: String, filterOnlyExamples: Boolean = false): List<SearchResultMatch> {
        if (query.isBlank()) return emptyList()
        val q = query.trim().lowercase()
        val allTopics = getAllTopics()
        val results = mutableListOf<SearchResultMatch>()

        for (topic in allTopics) {
            if (filterOnlyExamples && !topic.hasExamples) continue

            val titleMatch = topic.title.lowercase().contains(q)
            val categoryMatch = topic.categoryId.lowercase().contains(q)

            val markdown = getTopicMarkdownContent(topic.filePath)
            val lines = markdown.split("\n")

            var matchedSubtopic: String? = null
            var matchedLineSnippet: String? = null

            for (line in lines) {
                if (line.trim().startsWith("##") && line.lowercase().contains(q)) {
                    matchedSubtopic = line.replace("#", "").trim()
                    break
                } else if (line.lowercase().contains(q) && matchedLineSnippet == null && !line.startsWith("#") && !line.startsWith("<") && !line.startsWith("$$")) {
                    matchedLineSnippet = line.replace("$", "").trim().take(120)
                }
            }

            if (titleMatch || categoryMatch || matchedSubtopic != null || matchedLineSnippet != null) {
                val matchType = when {
                    titleMatch -> "Título"
                    matchedSubtopic != null -> "Subtema: $matchedSubtopic"
                    else -> "Contenido"
                }
                val snippet = matchedSubtopic ?: matchedLineSnippet ?: topic.summarySnippet

                results.add(
                    SearchResultMatch(
                        topic = topic,
                        matchedSubtopic = matchedSubtopic,
                        matchSnippet = snippet,
                        matchType = matchType
                    )
                )
            }
        }
        return results
    }

    fun getTopicsForCategory(categoryId: String): List<MathTopic> {
        return getAllTopics().filter { it.categoryId == categoryId }
    }

    fun getTopicMarkdownContent(filePath: String): String {
        return try {
            val inputStream = context.assets.open(filePath)
            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            reader.use { it.readText() }
        } catch (e: Exception) {
            "# Error\nNo se pudo cargar el contenido de $filePath"
        }
    }

    fun toggleFavorite(topicId: String): Boolean {
        val favorites = getFavoritesSet().toMutableSet()
        val isFav = if (favorites.contains(topicId)) {
            favorites.remove(topicId)
            false
        } else {
            favorites.add(topicId)
            true
        }
        prefs.edit().putStringSet("favorites", favorites).apply()
        return isFav
    }

    fun getFavoriteTopics(): List<MathTopic> {
        val favorites = getFavoritesSet()
        return getAllTopics().filter { favorites.contains(it.id) }
    }

    fun addRecentTopic(topicId: String) {
        val current = getRecentTopicIds().toMutableList()
        current.remove(topicId)
        current.add(0, topicId)
        val trimmed = current.take(10)
        prefs.edit().putString("recent_topics", trimmed.joinToString(",")).apply()
    }

    private fun getRecentTopicIds(): List<String> {
        val str = prefs.getString("recent_topics", "") ?: ""
        if (str.isBlank()) return emptyList()
        return str.split(",").filter { it.isNotBlank() }
    }

    fun getRecentTopics(): List<MathTopic> {
        val ids = getRecentTopicIds()
        val all = getAllTopics()
        return ids.mapNotNull { id -> all.find { it.id == id } }
    }

    fun getPinnedTopicIds(): List<String> {
        val str = prefs.getString("pinned_topics", "") ?: ""
        if (str.isBlank()) return emptyList()
        return str.split(",").filter { it.isNotBlank() }
    }

    fun togglePinnedTopic(topicId: String): Boolean {
        val current = getPinnedTopicIds().toMutableList()
        val isPinned = if (current.contains(topicId)) {
            current.remove(topicId)
            false
        } else {
            current.add(0, topicId)
            true
        }
        val trimmed = current.take(5)
        prefs.edit().putString("pinned_topics", trimmed.joinToString(",")).apply()
        return isPinned
    }

    fun getPinnedTopics(): List<MathTopic> {
        val ids = getPinnedTopicIds()
        val all = getAllTopics()
        return ids.mapNotNull { id -> all.find { it.id == id } }
    }

    fun isTopicPinned(topicId: String): Boolean {
        return getPinnedTopicIds().contains(topicId)
    }

    fun getNoteForTopic(topicId: String): String {
        return prefs.getString("note_$topicId", "") ?: ""
    }

    fun saveNoteForTopic(topicId: String, note: String) {
        prefs.edit().putString("note_$topicId", note).apply()
    }

    fun getThemeMode(): String {
        return prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM"
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
    }

    private fun getFavoritesSet(): Set<String> {
        return prefs.getStringSet("favorites", emptySet()) ?: emptySet()
    }

    private data class FileAnalysis(val snippet: String, val exampleCount: Int)

    private fun analyzeMarkdownFile(filePath: String): FileAnalysis {
        var snippet = ""
        var exampleCount = 0
        try {
            val inputStream = context.assets.open(filePath)
            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            val lines = reader.readLines()

            val exampleKeywords = listOf(
                "example", "ejemplo", "beispiel", "exemple", "exemplo",
                "пример", "приклад", "przykład", "eksempel", "örnek",
                "ví dụ", "contoh", "उदाहरण", "示例", "例子", "例", "예제", "예",
                "solución", "solution", "lösung", "solução", "решение", "розв'язок"
            )

            for (line in lines) {
                val trimmed = line.trim()
                val lower = trimmed.lowercase()

                if (trimmed.startsWith("## ")) {
                    if (exampleKeywords.any { lower.contains(it) }) {
                        exampleCount++
                    }
                } else if (snippet.isEmpty() && trimmed.isNotBlank() && !trimmed.startsWith("#") && !trimmed.startsWith("<") && !trimmed.startsWith("$$")) {
                    snippet = trimmed.replace("$", "").replace("#", "").trim().take(140)
                }
            }
        } catch (_: Exception) {
            
        }
        return FileAnalysis(snippet, exampleCount)
    }

    private data class CategoryMetadata(val title: String, val description: String, val domain: MathDomain)

    private fun getCategoryInfo(folder: String): CategoryMetadata {
        val lang = getSelectedLanguage()
        return when (folder) {
            "introduction" -> CategoryMetadata(
                title = when (lang) {
                    "en" -> "Introduction to Mathematics"
                    "de" -> "Einführung in die Mathematik"
                    "fr" -> "Introduction aux Mathématiques"
                    "pt" -> "Introdução à Matemática"
                    "zh" -> "数学导论"
                    "ja" -> "数学入門"
                    "ko" -> "수학 입문"
                    "ru" -> "Введение в математику"
                    "hi" -> "गणित का परिचय"
                    "id" -> "Pengantar Matematika"
                    "nl" -> "Inleiding tot de Wiskunde"
                    "sv" -> "Introduktion till Matematik"
                    "no" -> "Introduksjon til Matematikk"
                    "da" -> "Introduktion til Matematik"
                    "pl" -> "Wprowadzenie do Matematyki"
                    "th" -> "คณิตศาสตร์เบื้องต้น"
                    "tr" -> "Matematiğe Giriş"
                    "uk" -> "Вступ до математики"
                    "vi" -> "Nhập môn Toán học"
                    else -> "Introducción a las Matemáticas"
                },
                description = when (lang) {
                    "en" -> "Fundamentals, basic concepts and mathematical structure"
                    "de" -> "Grundlagen und mathematische Struktur"
                    "fr" -> "Principes fondamentaux et structure mathématique"
                    "pt" -> "Fundamentos e estrutura matemática"
                    "zh" -> "基础概念与数学结构"
                    "ja" -> "基本概念と数学的構造"
                    "ko" -> "기초 개념 및 수학적 구조"
                    "ru" -> "Основы и математическая структура"
                    "hi" -> "मूल बातें और गणितीय संरचना"
                    "id" -> "Dasar-dasar dan struktur matematika"
                    "nl" -> "Grondbeginselen en wiskundige structuur"
                    "sv" -> "Grunder och matematisk struktur"
                    "no" -> "Grunnleggende begreper og matematisk struktur"
                    "da" -> "Grundlæggende begreber og matematisk struktur"
                    "pl" -> "Podstawy i struktura matematyczna"
                    "th" -> "พื้นฐานและโครงสร้างทางคณิตศาสตร์"
                    "tr" -> "Temel kavramlar ve matematiksel yapı"
                    "uk" -> "Основи та математична структура"
                    "vi" -> "Nền tảng và cấu trúc toán học"
                    else -> "Fundamentos, conceptos básicos y estructura matemática"
                },
                domain = MathDomain.ALGEBRA
            )
            "sets-and-numbers" -> CategoryMetadata(
                title = when (lang) {
                    "en" -> "Sets and Numbers"
                    "de" -> "Mengen und Zahlen"
                    "fr" -> "Ensembles et Nombres"
                    "pt" -> "Conjuntos e Números"
                    "zh" -> "集合与数字"
                    "ja" -> "集合と数"
                    "ko" -> "집합과 수"
                    "ru" -> "Множества и числа"
                    "hi" -> "समुच्चय और संख्याएं"
                    "id" -> "Himpunan dan Angka"
                    "nl" -> "Verzamelingen en Getallen"
                    "sv" -> "Mängder och Tal"
                    "no" -> "Mengder og Tall"
                    "da" -> "Mængder og Tal"
                    "pl" -> "Zbiory i Liczby"
                    "th" -> "เซตและจำนวน"
                    "tr" -> "Kümeler ve Sayılar"
                    "uk" -> "Множини та числа"
                    "vi" -> "Tập hợp và Số"
                    else -> "Conjuntos y Números"
                },
                description = when (lang) {
                    "en" -> "Set theory, real numbers, integers and properties"
                    "de" -> "Mengenlehre, reelle und ganze Zahlen"
                    "fr" -> "Théorie des ensembles, nombres réels et entiers"
                    "pt" -> "Teoria dos conjuntos, números reais e inteiros"
                    "zh" -> "集合论、实数与整数性质"
                    "ja" -> "集合論、実数、整数とその性質"
                    "ko" -> "집합론, 실수, 정수 및 성질"
                    "ru" -> "Теория множеств, вещественные и целые числа"
                    "hi" -> "समुच्चय सिद्धांत, वास्तविक और पूर्णांक संख्याएं"
                    "id" -> "Teori himpunan, bilangan real dan bulat"
                    "nl" -> "Verzamelingenleer, reële en gehele getallen"
                    "sv" -> "Mängdlära, reella och heltal"
                    "no" -> "Mengdelære, reelle og heltall"
                    "da" -> "Mængdelære, reelle og heltal"
                    "pl" -> "Teoria zbiorów, liczby rzeczywiste i całkowite"
                    "th" -> "ทฤษฎีเซต จำนวนจริง และจำนวนเต็ม"
                    "tr" -> "Kümeler teorisi, reel ve tam sayılar"
                    "uk" -> "Теорія множин, дійсні та цілі числа"
                    "vi" -> "Lý thuyết tập hợp, số thực và số nguyên"
                    else -> "Teoría de conjuntos, números reales, enteros y propiedades"
                },
                domain = MathDomain.ALGEBRA
            )
            "powers-radicals-logarithms" -> CategoryMetadata(
                title = when (lang) {
                    "en" -> "Powers, Radicals and Logarithms"
                    "de" -> "Potenzen, Wurzeln und Logarithmen"
                    "fr" -> "Puissances, Racines et Logarithmes"
                    "pt" -> "Potências, Radicais e Logaritmos"
                    "zh" -> "幂、根号与对数"
                    "ja" -> "累乗、根号、対数"
                    "ko" -> "거듭제곱, 거듭제곱근 및 로그"
                    "ru" -> "Степени, радикалы и логарифмы"
                    "hi" -> "घात, करणी और लघुगणक"
                    "id" -> "Pangkat, Akar, dan Logaritma"
                    "nl" -> "Machten, Wortels en Logaritmen"
                    "sv" -> "Potenser, Radikaler och Logaritmer"
                    "no" -> "Potenser, Rottegn og Logaritmer"
                    "da" -> "Potenser, Rødder og Logaritmer"
                    "pl" -> "Potęgi, Pierwiastki i Logarytmy"
                    "th" -> "เลขยกกำลัง ราก และลอการิทึม"
                    "tr" -> "Üslü, Köklü ve Logaritmik İfadeler"
                    "uk" -> "Степені, радикали та логарифми"
                    "vi" -> "Lũy thừa, Căn thức và Lôgarit"
                    else -> "Potencias, Radicales y Logaritmos"
                },
                description = when (lang) {
                    "en" -> "Exponent laws, simplification and logarithmic properties"
                    "de" -> "Potenzgesetze und Logarithmeneigenschaften"
                    "fr" -> "Lois des exposants et propriétés logarithmiques"
                    "pt" -> "Leis dos expoentes e propriedades logarítmicas"
                    "zh" -> "指数法则、化简与对数性质"
                    "ja" -> "指数の法則と対数の性質"
                    "ko" -> "지수 법칙 및 로그의 성질"
                    "ru" -> "Законы степеней и свойства логарифмов"
                    "hi" -> "घातांक नियम और लघुगणक के गुण"
                    "id" -> "Hukum eksponen dan sifat logaritma"
                    "nl" -> "Machtswetten en logaritmische eigenschappen"
                    "sv" -> "Potenslagar och logaritmiska egenskaper"
                    "no" -> "Potensregler og logaritmeegenskaper"
                    "da" -> "Potensregler og logaritmiske egenskaber"
                    "pl" -> "Prawa potęg i własności logarytmów"
                    "th" -> "กฎของเลขยกกำลังและคุณสมบัติของลอการิทึม"
                    "tr" -> "Üs kuralları ve logaritma özellikleri"
                    "uk" -> "Закони степенів та властивості логарифмів"
                    "vi" -> "Quy tắc số mũ và tính chất lôgarit"
                    else -> "Leyes de exponentes, simplificación y propiedades logarítmicas"
                },
                domain = MathDomain.ALGEBRA
            )
            "polynomials" -> CategoryMetadata(
                title = when (lang) {
                    "en" -> "Polynomials"
                    "de" -> "Polynome"
                    "fr" -> "Polynômes"
                    "pt" -> "Polinômios"
                    "zh" -> "多项式"
                    "ja" -> "多項式"
                    "ko" -> "다항식"
                    "ru" -> "Многочлены"
                    "hi" -> "बहुपद"
                    "id" -> "Polinomial"
                    "nl" -> "Veelterm"
                    "sv" -> "Polynom"
                    "no" -> "Polynomer"
                    "da" -> "Polynomer"
                    "pl" -> "Wielomiany"
                    "th" -> "พหุนาม"
                    "tr" -> "Polinomlar"
                    "uk" -> "Многочлени"
                    "vi" -> "Đa thức"
                    else -> "Polinomios"
                },
                description = when (lang) {
                    "en" -> "Operations, Ruffini rule, roots and factoring"
                    "de" -> "Operationen, Ruffini-Regel, Wurzeln und Faktorisierung"
                    "fr" -> "Opérations, règle de Ruffini, racines et factorisation"
                    "pt" -> "Operações, regra de Ruffini, raízes e fatoração"
                    "zh" -> "多项式运算、鲁菲尼法则与因式分解"
                    "ja" -> "多項式の演算、ルフィニの法則、因数分解"
                    "ko" -> "다항식 연산, 루피니 법칙 및 인수분해"
                    "ru" -> "Операции, правило Руффини и разложение"
                    "hi" -> "बहुपद संक्रियाएं और गुणनखंडन"
                    "id" -> "Operasi, aturan Ruffini, dan faktorisasi"
                    "nl" -> "Bewerkingen, regel van Ruffini en ontbinding"
                    "sv" -> "Operationer, Ruffinis regel och faktorisering"
                    "no" -> "Operasjoner, Ruffinis regel og faktorisering"
                    "da" -> "Operationer, Ruffinis regel og faktorisering"
                    "pl" -> "Działania, reguła Ruffiniego i rozkład na czynniki"
                    "th" -> "การดำเนินการ การแยกตัวประกอบพหุนาม"
                    "tr" -> "İşlemler, Ruffini kuralı ve çarpanlara ayırma"
                    "uk" -> "Операції, правило Руффіні та розклад"
                    "vi" -> "Phép toán và phân tích thành nhân tử"
                    else -> "Operaciones, regla de Ruffini, raíces y factorización"
                },
                domain = MathDomain.ALGEBRA
            )
            "equations" -> CategoryMetadata(
                title = when (lang) {
                    "en" -> "Equations"
                    "de" -> "Gleichungen"
                    "fr" -> "Équations"
                    "pt" -> "Equações"
                    "zh" -> "方程"
                    "ja" -> "方程式"
                    "ko" -> "방정식"
                    "ru" -> "Уравнения"
                    "hi" -> "समीकरण"
                    "id" -> "Persamaan"
                    "nl" -> "Vergelijkingen"
                    "sv" -> "Ekvationer"
                    "no" -> "Ligninger"
                    "da" -> "Ligninger"
                    "pl" -> "Równania"
                    "th" -> "สมการ"
                    "tr" -> "Denklemler"
                    "uk" -> "Рівняння"
                    "vi" -> "Phương trình"
                    else -> "Ecuaciones"
                },
                description = when (lang) {
                    "en" -> "Linear, quadratic, exponential and polynomial equations"
                    "de" -> "Lineare, quadratische und exponentielle Gleichungen"
                    "fr" -> "Équations linéaires, quadratiques et exponentielles"
                    "pt" -> "Equações lineares, quadráticas e exponenciais"
                    "zh" -> "线性、二次、指数与多项式方程"
                    "ja" -> "一次、二次、指数方程式"
                    "ko" -> "1차, 2차, 지수 및 다항 방정식"
                    "ru" -> "Линейные, квадратные и показательные уравнения"
                    "hi" -> "रेखीय, द्विघात और घातांकीय समीकरण"
                    "id" -> "Persamaan linear, kuadrat, dan eksponensial"
                    "nl" -> "Lineaire, kwadratische en exponentiële vergelijkingen"
                    "sv" -> "Lineära, kvadratiska och exponentiella ekvationer"
                    "no" -> "Lineære, kvadratiske og eksponentielle ligninger"
                    "da" -> "Lineære, kvadratiske og absolut værdi uligheder"
                    "pl" -> "Równania liniowe, kwadratowe i wykładnicze"
                    "th" -> "สมการเชิงเส้น สมการกำลังสอง และสมการเอ็กซ์โพเนนเชียล"
                    "tr" -> "Doğrusal, ikinci dereceden ve üstel denklemler"
                    "uk" -> "Лінійні, квадратні та показникові рівняння"
                    "vi" -> "Phương trình tuyến tính, bậc hai và số mũ"
                    else -> "Ecuaciones lineales, cuadráticas, exponenciales y polinómicas"
                },
                domain = MathDomain.ALGEBRA
            )
            "inequalities" -> CategoryMetadata(
                title = when (lang) {
                    "en" -> "Inequalities"
                    "de" -> "Ungleichungen"
                    "fr" -> "Inégalités"
                    "pt" -> "Desigualdades"
                    "zh" -> "不等式"
                    "ja" -> "不等式"
                    "ko" -> "부등식"
                    "ru" -> "Неравенства"
                    "hi" -> "असमानताएं"
                    "id" -> "Pertidaksamaan"
                    "nl" -> "Ongelijkheden"
                    "sv" -> "Olikheter"
                    "no" -> "Ulikheter"
                    "da" -> "Uligheder"
                    "pl" -> "Nierówności"
                    "th" -> "อสมการ"
                    "tr" -> "Eşitsizlikler"
                    "uk" -> "Нерівності"
                    "vi" -> "Bất phương trình"
                    else -> "Inecuaciones y Desigualdades"
                },
                description = when (lang) {
                    "en" -> "Linear, quadratic and absolute value inequalities"
                    "de" -> "Lineare und quadratische Ungleichungen"
                    "fr" -> "Inéquations linéaires, quadratiques et valeur absolue"
                    "pt" -> "Inequações lineares, quadráticas e valor absoluto"
                    "zh" -> "线性、二次与绝对值不等式"
                    "ja" -> "一次、二次、絶対値の不等式"
                    "ko" -> "1차, 2차 및 절댓값 부등식"
                    "ru" -> "Линейные, квадратные и неравенства с модулем"
                    "hi" -> "रेखीय, द्विघात और निरपेक्ष मान असमानताएं"
                    "id" -> "Pertidaksamaan linear, kuadrat, dan nilai mutlak"
                    "nl" -> "Lineaire, kwadratische en absolute waarde ongelijkheden"
                    "sv" -> "Linjära, kvadratiska och absolutbelopp olikheter"
                    "no" -> "Lineære, kvadratiske og absoluttverdi ulikheter"
                    "da" -> "Lineære, kvadratiske og absolut værdi uligheder"
                    "pl" -> "Nierówności liniowe, kwadratowe i z wartością bezwzględną"
                    "th" -> "อสมการเชิงเส้น อสมการกำลังสอง และอสมการค่าสัมบูรณ์"
                    "tr" -> "Doğrusal, ikinci dereceden ve mutlak değer eşitsizlikleri"
                    "uk" -> "Лінійні, квадратні нерівності та нерівності з модулем"
                    "vi" -> "Bất phương trình tuyến tính, bậc hai và giá trị tuyệt đối"
                    else -> "Inecuaciones lineales, cuadráticas y valor absoluto"
                },
                domain = MathDomain.ALGEBRA
            )
            "linear-systems" -> CategoryMetadata(
                title = when (lang) {
                    "en" -> "Linear Systems"
                    "de" -> "Lineare Systeme"
                    "fr" -> "Systèmes Linéaires"
                    "pt" -> "Sistemas Lineares"
                    "zh" -> "线性方程组"
                    "ja" -> "連立方程式"
                    "ko" -> "선형 시스템"
                    "ru" -> "Системы линейных уравнений"
                    "hi" -> "रेखीय समीकरण निकाय"
                    "id" -> "Sistem Persamaan Linear"
                    "nl" -> "Lineaire Stelsels"
                    "sv" -> "Linjära Ekvationssystem"
                    "no" -> "Lineære Likningssystemer"
                    "da" -> "Lineære Ligningssystemer"
                    "pl" -> "Układy Równań Liniowych"
                    "th" -> "ระบบสมการเชิงเส้น"
                    "tr" -> "Doğrusal Denklem Sistemleri"
                    "uk" -> "Системи лінійних рівнянь"
                    "vi" -> "Hệ Phương trình Tuyến tính"
                    else -> "Sistemas de Ecuaciones Lineales"
                },
                description = when (lang) {
                    "en" -> "Substitution, elimination and Cramer's rule"
                    "de" -> "Einsetzung, Elimination und Cramersche Regel"
                    "fr" -> "Substitution, élimination et règle de Cramer"
                    "pt" -> "Substituição, eliminação e regra de Cramer"
                    "zh" -> "代入法、消元法与克拉默法则"
                    "ja" -> "代入法、消去法、クラメルの公式"
                    "ko" -> "대입법, 소거법 및 크라머 공식"
                    "ru" -> "Метод подстановки, сложения и правило Крамера"
                    "hi" -> "प्रतिस्थापन, विलोपन और क्रैमर का नियम"
                    "id" -> "Substitusi, eliminasi, dan aturan Cramer"
                    "nl" -> "Substitutie, eliminatie en regel van Cramer"
                    "sv" -> "Substitutionsmetoden, elimineringsmetoden och Cramers regel"
                    "no" -> "Innsettingsmetoden, eliminasjonsmetoden og Cramers regel"
                    "da" -> "Substitutionsmetoden, eliminationsmetoden og Cramers regel"
                    "pl" -> "Metoda podstawiania, przeciwnych współczynników i wzory Cramera"
                    "th" -> "การแทนค่า การกำจัดตัวแปร และกฎของเครเมอร์"
                    "tr" -> "Yerine koyma, yok etme ve Cramer kuralı"
                    "uk" -> "Метод підстановки, додавання та правило Крамера"
                    "vi" -> "Phương pháp thế, cộng đại số và quy tắc Cramer"
                    else -> "Sustitución, reducción, igualación y Regla de Cramer"
                },
                domain = MathDomain.ALGEBRA
            )
            "complex-numbers" -> CategoryMetadata(
                title = when (lang) {
                    "en" -> "Complex Numbers"
                    "de" -> "Komplexe Zahlen"
                    "fr" -> "Nombres Complexes"
                    "pt" -> "Números Complexos"
                    "zh" -> "复数"
                    "ja" -> "複素数"
                    "ko" -> "복소数"
                    "ru" -> "Комплексные числа"
                    "hi" -> "मिश्रित संख्याएं"
                    "id" -> "Bilangan Kompleks"
                    "nl" -> "Complexe Getallen"
                    "sv" -> "Komplexa Tal"
                    "no" -> "Komplekse Tall"
                    "da" -> "Komplekse Tal"
                    "pl" -> "Liczby Zespolone"
                    "th" -> "จำนวนเชิงซ้อน"
                    "tr" -> "Karmaşık Sayılar"
                    "uk" -> "Комплексні числа"
                    "vi" -> "Số phức"
                    else -> "Números Complexos"
                },
                description = when (lang) {
                    "en" -> "Binomial, polar, Euler form and De Moivre theorem"
                    "de" -> "Binomial-, Polar-, Euler-Form und De Moivre"
                    "fr" -> "Forme algébrique, polaire, Euler et De Moivre"
                    "pt" -> "Forma algébrica, polar, Euler e De Moivre"
                    "zh" -> "复数的代数形式、极坐标形式与欧拉公式"
                    "ja" -> "複素数の代数形、極形式、オイラーの公式"
                    "ko" -> "복소수의 대수형, 극형식 및 오일러 공식"
                    "ru" -> "Алгебраическая, тригонометрическая формы и формула Муавра"
                    "hi" -> "ध्रुवीय रूप, यूलर सूत्र और डी मोइवर प्रमेय"
                    "id" -> "Bentuk binomial, polar, Euler, dan teorema De Moivre"
                    "nl" -> "Binomiale, polaire, Euler-vorm en stelling van De Moivre"
                    "sv" -> "Rektangulär, polär, Eulers form och De Moivres formel"
                    "no" -> "Rektangulær, polar, Eulers form og De Moivres formel"
                    "da" -> "Binomial, polar, Eulers form og De Moivres formel"
                    "pl" -> "Postać wykładnicza, biegunowa i wzór De Moivre'a"
                    "th" -> "รูปแบบพิกัดฉาก พิกัดขั้ว และทฤษฎีบทของเดอมัวฟ์"
                    "tr" -> "Kutup biçimi, Euler formu ve De Moivre teoremi"
                    "uk" -> "Алгебраїчна, тригонометрична форми та формула Муавра"
                    "vi" -> "Dạng đại số, cực, công thức Euler và định lý De Moivre"
                    else -> "Forma binómica, trigonométrica, Euler y Teorema de De Moivre"
                },
                domain = MathDomain.ALGEBRA
            )
            "vectors-and-matrices" -> CategoryMetadata(
                title = when (lang) {
                    "en" -> "Vectors and Matrices"
                    "de" -> "Vektoren und Matrizen"
                    "fr" -> "Vecteurs et Matrices"
                    "pt" -> "Vetores e Matrizes"
                    "zh" -> "向量与矩阵"
                    "ja" -> "ベクトルと行列"
                    "ko" -> "벡터 및 행렬"
                    "ru" -> "Векторы и матрицы"
                    "hi" -> "वेक्टर और मैट्रिक्स"
                    "id" -> "Vektor dan Matriks"
                    "nl" -> "Vectoren en Matrices"
                    "sv" -> "Vektorer och Matriser"
                    "no" -> "Vektorer og Matriser"
                    "da" -> "Vektorer og Matricer"
                    "pl" -> "Wektory i Macierze"
                    "th" -> "เวกเตอร์และเมทริกซ์"
                    "tr" -> "Vektörler ve Matrisler"
                    "uk" -> "Вектори та матриці"
                    "vi" -> "Véctơ và Ma trận"
                    else -> "Vectores y Matrices"
                },
                description = when (lang) {
                    "en" -> "Dot product, cross product, determinants and inverses"
                    "de" -> "Skalarprodukt, Kreuzprodukt, Determinanten"
                    "fr" -> "Produit scalaire, vectoriel, déterminants et inverses"
                    "pt" -> "Produto escalar, vetorial, determinantes e inversas"
                    "zh" -> "点积、叉积、行列式与逆矩阵"
                    "ja" -> "内積、外積、行列式、逆行列"
                    "ko" -> "내적, 외적, 행렬식 및 역행렬"
                    "ru" -> "Скалярное, векторное произведение и детерминанты"
                    "hi" -> "डॉट उत्पाद, क्रॉस उत्पाद, सारणिक और व्युत्क्रम"
                    "id" -> "Perkalian titik, perkalian silang, determinan, dan invers"
                    "nl" -> "Inproduct, kruisproduct, determinanten en inverses"
                    "sv" -> "Skalärprodukt, kryssprodukt, determinanter och inverser"
                    "no" -> "Skalarprodukt, kryssprodukt, determinanter og inverser"
                    "da" -> "Skalarprodukt, krydsprodukt, determinanter og inverser"
                    "pl" -> "Iloczyn skalarny, wektorowy, wyznaczniki i macierze odwrotne"
                    "th" -> "ผลคูณเชิงสเกลาร์ ผลคูณเชิงเวกเตอร์ ดีเทอร์มิแนนต์"
                    "tr" -> "Nokta çarpım, çapraz çarpım, determinantlar ve ters matrisler"
                    "uk" -> "Скалярний, векторний добуток та детермінанти"
                    "vi" -> "Tích vô hướng, tích có hướng, định thức và ma trận nghịch đảo"
                    else -> "Producto escalar, vectorial, determinantes e inversas"
                },
                domain = MathDomain.ALGEBRA
            )
            "trigonometry" -> CategoryMetadata(
                title = when (lang) {
                    "en" -> "Trigonometry"
                    "de" -> "Trigonometrie"
                    "fr" -> "Trigonométrie"
                    "pt" -> "Trigonometria"
                    "zh" -> "三角学"
                    "ja" -> "三角法"
                    "ko" -> "삼각법"
                    "ru" -> "Тригонометрия"
                    "hi" -> "त्रिकोणमिति"
                    "id" -> "Trigonometri"
                    "nl" -> "Goniometrie"
                    "sv" -> "Trigonometri"
                    "no" -> "Trigonometri"
                    "da" -> "Trigonometri"
                    "pl" -> "Trygonometria"
                    "th" -> "ตรีโกณมิติ"
                    "tr" -> "Trigonometri"
                    "uk" -> "Тригонометрія"
                    "vi" -> "Lượng giác"
                    else -> "Trigonometría"
                },
                description = when (lang) {
                    "en" -> "Trig ratios, identities, law of sines and cosines"
                    "de" -> "Trigonometrische Verhältnisse und Identitäten"
                    "fr" -> "Rapports trigonométriques, identités et lois"
                    "pt" -> "Razões trigonométricas, identidades e leis"
                    "zh" -> "三角比、恒等式、正弦与余弦定理"
                    "ja" -> "三角比、等式、正弦・余弦定理"
                    "ko" -> "삼각비, 항등식, 사인 및 코사인 법칙"
                    "ru" -> "Тригонометрические соотношения, тождества и теоремы"
                    "hi" -> "त्रिकोणमितीय अनुपात, सर्वसमिकाएं, साइन और कोसाइन का नियम"
                    "id" -> "Rasio trigonometri, identitas, hukum sinus dan kosinus"
                    "nl" -> "Goniometrische verhoudingen, identiteiten, sinus- en cosinusregel"
                    "sv" -> "Trigonometriska förhållanden, identiteter, sinus- och cosinuslagen"
                    "no" -> "Trigonometriske forhold, identiteter, sinus- og cosinussetningen"
                    "da" -> "Trigonometriske forhold, identiteter, sinus- og cosinusrelationen"
                    "pl" -> "Tożsamości trygonometryczne, twierdzenie sinusów i kosinusów"
                    "th" -> "อัตราส่วนตรีโกณมิติ อัตลักษณ์ กฎของไซน์และคอสไลน์"
                    "tr" -> "Trigonometrik oranlar, özdeşlikler, sinüs ve kosinüs teoremleri"
                    "uk" -> "Тригонометричні співвідношення, тотожності, теореми синусів та косинусів"
                    "vi" -> "Tỉ số lượng giác, đồng nhất thức, định lý sin và cosin"
                    else -> "Razones trigonométricas, identidades, Ley de Senos y Cosenos"
                },
                domain = MathDomain.TRIGONOMETRY
            )
            "derivatives" -> CategoryMetadata(
                title = when (lang) {
                    "en" -> "Derivatives"
                    "de" -> "Ableitungen"
                    "fr" -> "Dérivées"
                    "pt" -> "Derivadas"
                    "zh" -> "导数"
                    "ja" -> "微分"
                    "ko" -> "미분"
                    "ru" -> "Производные"
                    "hi" -> "अवकलज"
                    "id" -> "Turunan"
                    "nl" -> "Afgeleiden"
                    "sv" -> "Derivator"
                    "no" -> "Deriverte"
                    "da" -> "Afledte"
                    "pl" -> "Pochodne"
                    "th" -> "อนุพันธ์"
                    "tr" -> "Türevler"
                    "uk" -> "Похідні"
                    "vi" -> "Đạo hàm"
                    else -> "Derivadas"
                },
                description = when (lang) {
                    "en" -> "Differentiation rules, chain rule and optimization"
                    "de" -> "Ableitungsregeln, Kettenregel und Optimierung"
                    "fr" -> "Règles de dérivation, règle de la chaîne et optimisation"
                    "pt" -> "Regras de derivação, regra da cadeia e otimização"
                    "zh" -> "求导法则、链式法则与最优化"
                    "ja" -> "微分の法則、合成関数の微分、最適化"
                    "ko" -> "미분 공식, 연쇄 법칙 및 최적化"
                    "ru" -> "Правила дифференцирования и оптимизация"
                    "hi" -> "अवकलन नियम, श्रृंखला नियम और इष्टतमकरण"
                    "id" -> "Aturan diferensiasi, aturan rantai, dan optimasi"
                    "nl" -> "Differentiatieregels, kettingregel en optimalisatie"
                    "sv" -> "Deriveringsregler, kedjeregeln och optimering"
                    "no" -> "Derivasjonsregler, kjerneregelen og optimering"
                    "da" -> "Differentieringsregler, kædereglen og optimering"
                    "pl" -> "Reguły różniczkowania, reguła łańcuchowa i optymalizacja"
                    "th" -> "กฎการหาอนุพันธ์ กฎลูกโซ่ และการหาค่าสูงสุดต่ำสุด"
                    "tr" -> "Türev kuralları, zincir kuralı ve optimizasyon"
                    "uk" -> "Правила диференціювання, ланцюгове правило та оптимізація"
                    "vi" -> "Quy tắc tính đạo hàm, quy tắc chuỗi và tối ưu hóa"
                    else -> "Reglas de derivación, regla de la cadena y optimización"
                },
                domain = MathDomain.CALCULUS
            )
            "integrals" -> CategoryMetadata(
                title = when (lang) {
                    "en" -> "Integrals"
                    "de" -> "Integrale"
                    "fr" -> "Intégrales"
                    "pt" -> "Integrais"
                    "zh" -> "积分"
                    "ja" -> "積分"
                    "ko" -> "적분"
                    "ru" -> "Интегралы"
                    "hi" -> "समाकलन"
                    "id" -> "Integral"
                    "nl" -> "Integralen"
                    "sv" -> "Integraler"
                    "no" -> "Integraler"
                    "da" -> "Integraler"
                    "pl" -> "Całki"
                    "th" -> "อินทิกรัล"
                    "tr" -> "İntegraller"
                    "uk" -> "Інтеграли"
                    "vi" -> "Tích phân"
                    else -> "Integrales"
                },
                description = when (lang) {
                    "en" -> "Definite and indefinite integrals, substitution and by parts"
                    "de" -> "Bestimmte und unbestimmte Integrale"
                    "fr" -> "Intégrales définies et indéfinies, substitution"
                    "pt" -> "Integrais definidas e indefinidas, substituição"
                    "zh" -> "定积分与不定积分、换元法与分部积分"
                    "ja" -> "定積分、不定積分、置換積分、部分積分"
                    "ko" -> "정적분, 부정적분, 치환적분 및 부분적분"
                    "ru" -> "Определенные и неопределенные интегралы"
                    "hi" -> "निश्चित और अनिश्चित समाकलन"
                    "id" -> "Integral tentu dan tak tentu"
                    "nl" -> "Bepaalde en onbestemde integralen"
                    "sv" -> "Bestämda och obestämda integraler"
                    "no" -> "Bestemte og ubestemte integraler"
                    "da" -> "Bestemte og ubestemte integraler"
                    "pl" -> "Całki oznaczone i nieoznaczone"
                    "th" -> "อินทิกรัลจำกัดเขตและไม่จำกัดเขต"
                    "tr" -> "Belirli ve belirsiz integral"
                    "uk" -> "Визначені та невизначені інтеграли"
                    "vi" -> "Tích phân xác định và bất định"
                    else -> "Integrales definidas e indefinidas, sustitución y por partes"
                },
                domain = MathDomain.CALCULUS
            )
            "limits" -> CategoryMetadata(
                title = when (lang) {
                    "en" -> "Limits and Continuity"
                    "de" -> "Grenzwerte und Stetigkeit"
                    "fr" -> "Limites et Continuité"
                    "pt" -> "Limites e Continuidade"
                    "zh" -> "极限与连续性"
                    "ja" -> "極限と連続性"
                    "ko" -> "극한 및 연속성"
                    "ru" -> "Пределы и непрерывность"
                    "hi" -> "सीमा और निरंतरता"
                    "id" -> "Limit dan Kontinuitas"
                    "nl" -> "Limieten en Continuïteit"
                    "sv" -> "Gränsvärden och Kontinuitet"
                    "no" -> "Grenseverdier og Kontinuitet"
                    "da" -> "Grænseværdier og Kontinuitet"
                    "pl" -> "Granice i Ciągłość"
                    "th" -> "ลิมิตและความต่อเนื่อง"
                    "tr" -> "Limitler ve Süreklilik"
                    "uk" -> "Границі та неперервність"
                    "vi" -> "Giới hạn và Tính liên tục"
                    else -> "Límites y Continuidad"
                },
                description = when (lang) {
                    "en" -> "One-sided limits, L'Hopital's rule and asymptotes"
                    "de" -> "Einseitige Grenzwerte, L'Hopital-Regel"
                    "fr" -> "Limites unilatérales, règle de L'Hôpital"
                    "pt" -> "Limites meias-visões, regra de L'Hopital"
                    "zh" -> "单侧极限、洛必达法则与渐近线"
                    "ja" -> "片側極限、ロピタルの定理、漸近線"
                    "ko" -> "한포 극한, 로피탈 정리 및 점근선"
                    "ru" -> "Односторонние пределы и правило Лопиталя"
                    "hi" -> "एकतरफा सीमाएं और एल-हॉस्पिटल नियम"
                    "id" -> "Limit satu sisi dan aturan L'Hopital"
                    "nl" -> "Eenzijdige limieten en regel van L'Hopital"
                    "sv" -> "Ensidiga gränsvärden och L'Hopitals regel"
                    "no" -> "Ensidige grenseverdier og L'Hopitals regel"
                    "da" -> "Ensidige grænseværdier og L'Hopitals regel"
                    "pl" -> "Granice jednostronne i reguła L'Hospitala"
                    "th" -> "ลิมิตด้านเดียวและกฎของลอปิตาล"
                    "tr" -> "Tek taraflı limitler ve L'Hopital kuralı"
                    "uk" -> "Односторонні границі та правило Лопіталя"
                    "vi" -> "Giới hạn một phía và quy tắc L'Hopital"
                    else -> "Límites laterales, regla de L'Hôpital y asíntotas"
                },
                domain = MathDomain.CALCULUS
            )
            "differential-equations" -> CategoryMetadata(
                title = when (lang) {
                    "en" -> "Differential Equations"
                    "de" -> "Differentialgleichungen"
                    "fr" -> "Équations Différentielles"
                    "pt" -> "Equações Diferenciais"
                    "zh" -> "微分方程"
                    "ja" -> "微分方程式"
                    "ko" -> "미분 방정식"
                    "ru" -> "Дифференциальные уравнения"
                    "hi" -> "अवकल समीकरण"
                    "id" -> "Persamaan Diferensial"
                    "nl" -> "Differentiaalvergelijkingen"
                    "sv" -> "Differentialekvationer"
                    "no" -> "Differensiallikninger"
                    "da" -> "Differentialligninger"
                    "pl" -> "Równania Różniczkowe"
                    "th" -> "สมการเชิงอนุพันธ์"
                    "tr" -> "Diferansiyel Denklemler"
                    "uk" -> "Диференціальні рівняння"
                    "vi" -> "Phương trình Vi phân"
                    else -> "Ecuaciones Diferenciales"
                },
                description = when (lang) {
                    "en" -> "First order ODEs, separable, linear and second order"
                    "de" -> "Differentialgleichungen 1. und 2. Ordnung"
                    "fr" -> "Équations différentielles du premier et second ordre"
                    "pt" -> "Equações diferenciais de primeira e segunda ordem"
                    "zh" -> "一阶、二阶常微分方程与可分离变量"
                    "ja" -> "一階・二階常微分方程式、変数分離形"
                    "ko" -> "1계 및 2차 상미분 방정식"
                    "ru" -> "ОДУ первого и второго порядка, разделяемые переменные"
                    "hi" -> "प्रथम और द्वितीय कोटि के अवकल समीकरण"
                    "id" -> "PD tingkat pertama, terpisah, linear, dan tingkat kedua"
                    "nl" -> "Eerste en tweede orde differentiaalvergelijkingen"
                    "sv" -> "Första och andra ordningens differentialekvationer"
                    "no" -> "Første og andre ordens differensiallikninger"
                    "da" -> "Første og anden ordens differentialligninger"
                    "pl" -> "Równania różniczkowe pierwszego i drugiego rzędu"
                    "th" -> "สมการเชิงอนุพันธ์อันดับหนึ่งและอันดับสอง"
                    "tr" -> "Birinci ve ikinci mertebeden diferansiyel denklemler"
                    "uk" -> "Диференціальні рівняння першого та другого порядку"
                    "vi" -> "Phương trình vi phân cấp một và cấp hai"
                    else -> "EDOs de primer orden, separables, lineales y de segundo orden"
                },
                domain = MathDomain.CALCULUS
            )
            "probability-and-statistics" -> CategoryMetadata(
                title = when (lang) {
                    "en" -> "Probability and Statistics"
                    "de" -> "Wahrscheinlichkeit und Statistik"
                    "fr" -> "Probabilités et Statistique"
                    "pt" -> "Probabilidade e Estatística"
                    "zh" -> "概率论与数理统计"
                    "ja" -> "確率と統計"
                    "ko" -> "확률 및 통계"
                    "ru" -> "Теория вероятностей и статистика"
                    "hi" -> "प्रायिकता और सांख्यिकी"
                    "id" -> "Probabilitas dan Statistik"
                    "nl" -> "Kansrekening en Statistiek"
                    "sv" -> "Sannolikhet och Statistik"
                    "no" -> "Sannsynlighet og Statistikk"
                    "da" -> "Sandsynlighed og Statistik"
                    "pl" -> "Prawdopodobieństwo i Statystyka"
                    "th" -> "ความน่าจะเป็นและสถิติ"
                    "tr" -> "Olasılık ve İstatistik"
                    "uk" -> "Теорія ймовірностей та статистика"
                    "vi" -> "Xác suất và Thống kê"
                    else -> "Probabilidad y Estadística"
                },
                description = when (lang) {
                    "en" -> "Bayes theorem, combinations, normal distribution and variance"
                    "de" -> "Bayes-Theorem, Normalverteilung und Varianz"
                    "fr" -> "Théorème de Bayes, distribution normale et variance"
                    "pt" -> "Teorema de Bayes, distribuição normal e variância"
                    "zh" -> "贝叶斯定理、组合、正态分布与方差"
                    "ja" -> "ベイズの定理、組み合わせ、正規分布"
                    "ko" -> "베이즈 정리, 조합, 정규분포 및 분산"
                    "ru" -> "Теорема Байеса, нормальное распределение и дисперсия"
                    "hi" -> "बेयस प्रमेय, सामान्य वितरण और प्रसरण"
                    "id" -> "Teorema Bayes, kombinasi, distribusi normal, dan varians"
                    "nl" -> "Stelling van Bayes, normale verdeling en variantie"
                    "sv" -> "Bayes sats, normalfördelning och varians"
                    "no" -> "Bayes setning, normalfordeling og varians"
                    "da" -> "Bayes sætning, normalfordeling og varians"
                    "pl" -> "Twierdzenie Bayesa, rozkład normalny i wariancja"
                    "th" -> "ทฤษฎีบทของเบย์ส การแจกแจงแบบปกติและความแปรปรวน"
                    "tr" -> "Bayes teoremi, normal dağılım ve varyans"
                    "uk" -> "Теорема Байєса, нормальний розподіл та дисперсія"
                    "vi" -> "Định lý Bayes, phân phối chuẩn và phương sai"
                    else -> "Teorema de Bayes, combinatoria, distribución normal y varianza"
                },
                domain = MathDomain.ALL
            )
            "financial-math" -> CategoryMetadata(
                title = when (lang) {
                    "en" -> "Financial Mathematics"
                    "de" -> "Finanzmathematik"
                    "fr" -> "Mathématiques Financières"
                    "pt" -> "Matemática Financeira"
                    "zh" -> "金融数学"
                    "ja" -> "金融数学"
                    "ko" -> "금융 수학"
                    "ru" -> "Финансовая математика"
                    "hi" -> "वित्तीय गणित"
                    "id" -> "Matematika Keuangan"
                    "nl" -> "Financiële Wiskunde"
                    "sv" -> "Finansiell Matematik"
                    "no" -> "Finansiell Matematikk"
                    "da" -> "Finansiel Matematik"
                    "pl" -> "Matematyka Finansowa"
                    "th" -> "คณิตศาสตร์การเงิน"
                    "tr" -> "Finansal Matematik"
                    "uk" -> "Фінансова математика"
                    "vi" -> "Toán học Tài chính"
                    else -> "Matemática Financiera"
                },
                description = when (lang) {
                    "en" -> "Simple and compound interest, present value and annuities"
                    "de" -> "Einfache und Zinseszinsen, Barwert"
                    "fr" -> "Intérêts simples, composés et valeur actuelle"
                    "pt" -> "Juros simples, compostos e valor presente"
                    "zh" -> "单利、复利、现值与年金"
                    "ja" -> "単利、複利、現在価値"
                    "ko" -> "단리, 복리, 현재 가치"
                    "ru" -> "Простые и сложные проценты, приведенная стоимость"
                    "hi" -> "साधारण और चक्रवृिद्धि ब्याज, वर्तमान मूल्य"
                    "id" -> "Bunga tunggal dan majemuk, nilai sekarang"
                    "nl" -> "Enkelvoudige en samengestelde intrest, contante waarde"
                    "sv" -> "Enkel och sammansatt ränta, nuvärde"
                    "no" -> "Enkel og sammensatt rente, nåverdi"
                    "da" -> "Enkelt og sammensat rente, nutidsværdi"
                    "pl" -> "Proste i złożone procenty, wartość obecna"
                    "th" -> "ดอกเบี้ยเชิงเดี่ยวและดอกเบี้ยทบต้น มูลค่าปัจจุบัน"
                    "tr" -> "Basit ve bileşik faiz, bugünkü değer"
                    "uk" -> "Прості та складні відсотки, теперішня вартість"
                    "vi" -> "Lãi đơn, lãi kép và giá trị hiện tại"
                    else -> "Interés simple, compuesto, valor presente y anualidades"
                },
                domain = MathDomain.ALL
            )
            "logic" -> CategoryMetadata(
                title = when (lang) {
                    "en" -> "Mathematical Logic"
                    "de" -> "Mathematische Logik"
                    "fr" -> "Logique Mathématique"
                    "pt" -> "Lógica Matemática"
                    "zh" -> "数理逻辑"
                    "ja" -> "数理論理学"
                    "ko" -> "수리 논리학"
                    "ru" -> "Математическая логика"
                    "hi" -> "गणितीय तर्कशास्त्र"
                    "id" -> "Logika Matematika"
                    "nl" -> "Wiskundige Logica"
                    "sv" -> "Matematisk Logik"
                    "no" -> "Matematisk Logikk"
                    "da" -> "Matematisk Logik"
                    "pl" -> "Logika Matematyczna"
                    "th" -> "ตรรกศาสตร์คณิตศาสตร์"
                    "tr" -> "Matematiksel Mantık"
                    "uk" -> "Математична логіка"
                    "vi" -> "Logic Toán học"
                    else -> "Lógica Matemática"
                },
                description = when (lang) {
                    "en" -> "Truth tables, quantifiers, implication and De Morgan's laws"
                    "de" -> "Wahrheitstabellen, Quantoren und De Morgan-Gesetze"
                    "fr" -> "Tables de vérité, quantificateurs et lois de De Morgan"
                    "pt" -> "Tabelas verdade, quantificadores e leis de De Morgan"
                    "zh" -> "真值表、量词、逻辑蕴涵与德摩根定律"
                    "ja" -> "真理値表、量化子、論理含意、ド・モルガンの法則"
                    "ko" -> "진리표, 한정호, 논리적 함의 및 드 모르간의 법칙"
                    "ru" -> "Таблицы истинности, кванторы и законы Де Моргана"
                    "hi" -> "सत्यता सारणी, परिमाणक और डी मॉर्गन के नियम"
                    "id" -> "Tabel kebenaran, kuantifier, dan hukum De Morgan"
                    "nl" -> "Waarheidstabellen, quantoren en wetten van De Morgan"
                    "sv" -> "Sanningstabeller, kvantifikatorer och De Morgans lagar"
                    "no" -> "Sannhetstabeller, kvantifikatorer og De Morgans lover"
                    "da" -> "Sandhedstabeller, kvantifikatorer og De Morgans love"
                    "pl" -> "Tabele prawdy, kwantyfikatory i prawa De Morgana"
                    "th" -> "ตารางความจริง ตัวบ่งปรมาณู และกฎของเดอมอร์แกน"
                    "tr" -> "Doğruluk tabloları, niceleyiciler ve De Morgan yasaları"
                    "uk" -> "Таблиці істинності, квантори та закони Де Моргана"
                    "vi" -> "Bảng chân trị, lượng từ và định lý De Morgan"
                    else -> "Tablas de verdad, cuantificadores, implicación y Leyes de De Morgan"
                },
                domain = MathDomain.LOGIC_STATS
            )
            "algebraic-structures" -> CategoryMetadata(
                title = when (lang) {
                    "en" -> "Algebraic Structures"
                    "de" -> "Algebraische Strukturen"
                    "fr" -> "Structures Algébriques"
                    "pt" -> "Estruturas Algébricas"
                    "zh" -> "代数结构"
                    "ja" -> "代数的構造"
                    "ko" -> "대수적 구조"
                    "ru" -> "Алгебраические структуры"
                    "hi" -> "बीजगणितीय संरचनाएं"
                    "id" -> "Struktur Aljabar"
                    "nl" -> "Algebraïsche Structuren"
                    "sv" -> "Algebraiska Strukturer"
                    "no" -> "Algebraiske Strukturer"
                    "da" -> "Algebraiske Strukturer"
                    "pl" -> "Struktury Algebraiczne"
                    "th" -> "โครงสร้างทางพีชคณิต"
                    "tr" -> "Cebirsel Yapılar"
                    "uk" -> "Алгебраїчні структури"
                    "vi" -> "Cấu trúc Đại số"
                    else -> "Estructuras Algebraicas"
                },
                description = when (lang) {
                    "en" -> "Groups, rings, fields and vector spaces"
                    "de" -> "Gruppen, Ringe, Körper und Vektorräume"
                    "fr" -> "Groupes, anneaux, corps et espaces vectoriels"
                    "pt" -> "Grupos, anéis, corpos e espaços vetoriais"
                    "zh" -> "群、环、域与向量空间"
                    "ja" -> "群、体、環、ベクトル空間"
                    "ko" -> "군, 환, 체 및 벡터 공간"
                    "ru" -> "Группы, кольца, поля и векторные пространства"
                    "hi" -> "समूह, वलय, क्षेत्र और सदिश समष्टि"
                    "id" -> "Grup, gelanggang, lapangan, dan ruang vektor"
                    "nl" -> "Groepen, ringen, lichamen en vectorruimten"
                    "sv" -> "Grupper, ringar, kroppar och vektorrum"
                    "no" -> "Grupper, ringer, kropper og vektorrom"
                    "da" -> "Grupper, ringe, legemer og vektorrum"
                    "pl" -> "Grupy, pierścienie, ciała i przestrzenie liniowe"
                    "th" -> "กรุ๊ป ริง ฟีลด์ และสเปซเวกเตอร์"
                    "tr" -> "Gruplar, halkalar, cisimler ve vektör uzayları"
                    "uk" -> "Групи, кільця, поля та векторні простори"
                    "vi" -> "Nhóm, vòng, trường và không gian vectơ"
                    else -> "Grupos, anillos, cuerpos y espacios vectoriales"
                },
                domain = MathDomain.ALGEBRA
            )
            "functions" -> CategoryMetadata(
                title = when (lang) {
                    "en" -> "Functions"
                    "de" -> "Funktionen"
                    "fr" -> "Fonctions"
                    "pt" -> "Funções"
                    "zh" -> "函数"
                    "ja" -> "関数"
                    "ko" -> "함수"
                    "ru" -> "Функции"
                    "hi" -> "फलन"
                    "id" -> "Fungsi"
                    "nl" -> "Functies"
                    "sv" -> "Funktioner"
                    "no" -> "Funksjoner"
                    "da" -> "Funktioner"
                    "pl" -> "Funkcje"
                    "th" -> "ฟังก์ชัน"
                    "tr" -> "Fonksiyonlar"
                    "uk" -> "Функції"
                    "vi" -> "Hàm số"
                    else -> "Funciones"
                },
                description = when (lang) {
                    "en" -> "Domain, range, inverse functions and composition"
                    "de" -> "Definitionsbereich, Wertebereich, Umkehrfunktionen und Komposition"
                    "fr" -> "Domaine, ensemble d'arrivée, fonctions inverses et composition"
                    "pt" -> "Domínio, imagem, funções inversas e composição"
                    "zh" -> "定义域、值域、反函数与复合函数"
                    "ja" -> "定義域、値域、逆関数、合成関数"
                    "ko" -> "정의역, 치역, 역함수 및 합성함수"
                    "ru" -> "Область определения, область значений, обратные функции и композиция"
                    "hi" -> "प्रांत, परिसर, प्रतिलोम फलन और संयोजन"
                    "id" -> "Domain, kodomain, fungsi invers, dan komposisi"
                    "nl" -> "Domein, bereik, inverse functies en compositie"
                    "sv" -> "Definitionsmängd, målmängd, inverser och sammansättning"
                    "no" -> "Definisjonsmengde, verdimengde, omvendte funksjoner og sammensetning"
                    "da" -> "Definitionsmængde, værdimængde, omvendte funktioner og sammensætning"
                    "pl" -> "Dziedzina, przeciwdziedzina, funkcje odwrotne i składanie"
                    "th" -> "โดเมน เรนจ์ ฟังก์ชันผกผัน และฟังก์ชันประกอบ"
                    "tr" -> "Tanım kümesi, değer kümesi, ters fonksiyonlar ve bileşke"
                    "uk" -> "Область визначення, область значень, обернені функції та композиція"
                    "vi" -> "Tập xác định, tập giá trị, hàm số ngược và hợp thành"
                    else -> "Dominio, rango, funciones inversas y composición"
                },
                domain = MathDomain.ALGEBRA
            )
            "lines-planes-conic-sections" -> CategoryMetadata(
                title = when (lang) {
                    "en" -> "Lines, Planes and Conics"
                    "de" -> "Geraden, Ebenen und Kegelschnitte"
                    "fr" -> "Droites, Plans et Coniques"
                    "pt" -> "Retas, Planos e Cônicas"
                    "zh" -> "直线、平面与圆锥曲线"
                    "ja" -> "直线、平面、二次曲线"
                    "ko" -> "직선, 평면 및 이차곡선"
                    "ru" -> "Прямые, плоскости и конические сечения"
                    "hi" -> "रेखाएं, समतल और शंकु परिच्छेद"
                    "id" -> "Garis, Bidang, dan Irisan Kerucut"
                    "nl" -> "Lijnen, Vlakken en Kegelsneden"
                    "sv" -> "Linjer, Plan och Kägelsnitt"
                    "no" -> "Linjer, Plan og Kjeglesnitt"
                    "da" -> "Linjer, Planer og Keglesnit"
                    "pl" -> "Proste, Płaszczyzny i Stożkowe"
                    "th" -> "เส้นตรง ระนาบ และภาคตัดกรวย"
                    "tr" -> "Doğrular, Düzlemler ve Konikler"
                    "uk" -> "Прямі, площини та конічні перетини"
                    "vi" -> "Đường thẳng, Mặt phẳng và Đường conic"
                    else -> "Rectas, Planos y Cónicas"
                },
                description = when (lang) {
                    "en" -> "Analytic geometry, equations of lines, planes, ellipses, parabolas and hyperbolas"
                    "de" -> "Analytische Geometrie, Ellipsen, Parabeln und Hyperbeln"
                    "fr" -> "Géométrie analytique, équations de droites, ellipses, paraboles et hyperboles"
                    "pt" -> "Geometria analítica, equações de retas, elipses, parábolas e hipérboles"
                    "zh" -> "解析几何、直线方程、椭圆、抛物线与双曲线"
                    "ja" -> "解析幾何学、直線の方程式、楕円、放物線、双曲線"
                    "ko" -> "해석几何, 직선의 방정식, 타원, 포물선 및 쌍곡선"
                    "ru" -> "Аналитическая геометрия, уравнения прямых, эллипсы, параболы и гиперболы"
                    "hi" -> "वैश्लेषिक ज्यामिति, रेखाओं के समीकरण, दीर्घवृत्त, परवलय और अतिपरवलय"
                    "id" -> "Geometri analitik, persamaan garis, elips, parabola, dan hiperbola"
                    "nl" -> "Analytische meetkunde, vergelijkingen van lijnen, ellipsen, parabolen en hyperbolen"
                    "sv" -> "Analytisk geometri, linjeekvationer, ellipsoider, parabler och hyperbler"
                    "no" -> "Analytisk geometri, linjeligninger, ellipser, parabler og hyperbler"
                    "da" -> "Analytisk geometri, linjeligninger, ellipser, parabler og hyperbler"
                    "pl" -> "Geometria analityczna, równania prostych, elipsy, parabole i hiperbole"
                    "th" -> "เรขาคณิตวิเคราะห์ สมการเส้นตรง วงรี พาราโบลา และไฮเพอร์โบลา"
                    "tr" -> "Analitik geometri, doğru denklemleri, elips, parabol ve hiperboller"
                    "uk" -> "Аналітична геометрія, рівняння прямих, еліпси, параболи та гіперболи"
                    "vi" -> "Hình học giải tích, phương trình đường thẳng, elip, parabol và hiperbol"
                    else -> "Geometría analítica, ecuaciones de rectas, planos, elipses, parábolas e hipérboles"
                },
                domain = MathDomain.GEOMETRY
            )
            "differential-calculus-theorems" -> CategoryMetadata(
                title = when (lang) {
                    "en" -> "Differential Calculus Theorems"
                    "de" -> "Sätze der Differentialrechnung"
                    "fr" -> "Théorèmes du Calcul Différentiel"
                    "pt" -> "Teoremas do Cálculo Diferencial"
                    "zh" -> "微分学定理"
                    "ja" -> "微分学の定理"
                    "ko" -> "미분학 정리"
                    "ru" -> "Теоремы дифференциального исчисления"
                    "hi" -> "अवकल गणित के प्रमेय"
                    "id" -> "Teorema Kalkulus Diferensial"
                    "nl" -> "Stellingen van de Differentiaalrekening"
                    "sv" -> "Differentialkalkylens Satser"
                    "no" -> "Teoremer i Differentialkalkyle"
                    "da" -> "Sætninger i Differentialregning"
                    "pl" -> "Twierdzenia Rachunku Różniczkowego"
                    "th" -> "ทฤษฎีบทแคลคูลัสเชิงอนุพันธ์"
                    "tr" -> "Diferansiyel Hesap Teoremleri"
                    "uk" -> "Теореми диференціального числення"
                    "vi" -> "Định lý Giải tích Vi phân"
                    else -> "Teoremas de Cálculo Diferencial"
                },
                description = when (lang) {
                    "en" -> "Rolle's theorem, Mean Value Theorem, L'Hôpital's rule and Taylor series"
                    "de" -> "Satz von Rolle, Mittelwertsatz, L'Hospital und Taylor-Reihen"
                    "fr" -> "Théorème de Rolle, accroissements finis, règle de L'Hôpital et séries de Taylor"
                    "pt" -> "Teorema de Rolle, Valor Médio, regra de L'Hôpital e séries de Taylor"
                    "zh" -> "罗尔定理、中值定理、洛必达法则与泰勒级数"
                    "ja" -> "ロルの定理、平均値の定理、ロピタルの定理、テイラー展開"
                    "ko" -> "롤의 정리, 평균값 정리, 로피탈 정리 및 테일러 급수"
                    "ru" -> "Теорема Ролля, теорема о среднем, правило Лопиталя и ряды Тейлора"
                    "hi" -> "रोले का प्रमेय, माध्य मान प्रमेय, ल'हॉस्पिटल का नियम और टेलर श्रेणी"
                    "id" -> "Teorema Rolle, Nilai Rata-rata, aturan L'Hôpital, dan deret Taylor"
                    "nl" -> "Stelling van Rolle, Middelwaardestelling, regel van L'Hôpital en Taylorreeksen"
                    "sv" -> "Rolles sats, medelvärdessatsen, L'Hôpitals regel och Taylorsatser"
                    "no" -> "Rolles teorem, middelverdisetningen, L'Hôpitals regel og Taylor-rekker"
                    "da" -> "Rolles sætning, middelværdisætningen, L'Hôpitals regel og Taylor-rækker"
                    "pl" -> "Twierdzenie Rolle'a, o wartości średniej, reguła L'Hôpitala i szeregi Taylora"
                    "th" -> "ทฤษฎีบทของรอลล์ ทฤษฎีบทค่าเฉลี่ย กฎของโลปีตาล และอนุกรมเทย์เลอร์"
                    "tr" -> "Rolle teoremi, Ortalama Değer Teoremi, L'Hôpital kuralı ve Taylor serileri"
                    "uk" -> "Теорема Ролля, теорема про середнє, правило Лопіталя та ряди Тейлора"
                    "vi" -> "Định lý Rolle, Giá trị Trung bình, quy tắc L'Hôpital và chuỗi Taylor"
                    else -> "Teorema de Rolle, Valor Medio, regla de L'Hôpital y series de Taylor"
                },
                domain = MathDomain.CALCULUS
            )
            "sequences" -> CategoryMetadata(
                title = when (lang) {
                    "en" -> "Sequences"
                    "de" -> "Folgen"
                    "fr" -> "Suites"
                    "pt" -> "Sequências"
                    "zh" -> "数列"
                    "ja" -> "数列"
                    "ko" -> "수열"
                    "ru" -> "Последовательности"
                    "hi" -> "अनुक्रम"
                    "id" -> "Barisan"
                    "nl" -> "Rijen"
                    "sv" -> "Talföljder"
                    "no" -> "Tallegrupper"
                    "da" -> "Talfølge"
                    "pl" -> "Ciągi"
                    "th" -> "ลำดับ"
                    "tr" -> "Diziler"
                    "uk" -> "Послідовності"
                    "vi" -> "Dãy số"
                    else -> "Sucesiones"
                },
                description = when (lang) {
                    "en" -> "Arithmetic and geometric progressions, limits of sequences"
                    "de" -> "Arithmetische und geometrische Folgen, Grenzwerte"
                    "fr" -> "Progressions arithmétiques et géométriques, limites de suites"
                    "pt" -> "Progressões aritméticas e geométricas, limites de sequências"
                    "zh" -> "等差数列与等比数列、数列极限"
                    "ja" -> "等差数列、等比数列、数列の極限"
                    "ko" -> "등차수열, 등비수열 및 수열의 극한"
                    "ru" -> "Арифметические и геометрические прогрессии, пределы"
                    "hi" -> "समांतर और गुणोत्तर श्रेढ़ी, अनुक्रमों की सीमाएं"
                    "id" -> "Barisan aritmetika dan geometri, limit barisan"
                    "nl" -> "Rekenkundige en meetkundige rijen, limieten van rijen"
                    "sv" -> "Aritmetiska och geometriska talföljder, gränsvärden"
                    "no" -> "Aritmetiske og geometriske rekker, grenseverdier"
                    "da" -> "Aritmetiske og geometriske talfølger, grænseværdier"
                    "pl" -> "Ciągi arytmetyczne i geometryczne, granice ciągów"
                    "th" -> "ลำดับเลขคณิตและลำดับเรขาคณิต ลิมิตของลำดับ"
                    "tr" -> "Aritmetik ve geometrik diziler, dizi limitleri"
                    "uk" -> "Арифмецевтичні та геометричні прогресії, границі послідовностей"
                    "vi" -> "Cấp số cộng, cấp số nhân, giới hạn của dãy số"
                    else -> "Progresiones aritméticas, geométricas y límites de sucesiones"
                },
                domain = MathDomain.CALCULUS
            )
            "series" -> CategoryMetadata(
                title = when (lang) {
                    "en" -> "Infinite Series"
                    "de" -> "Unendliche Reihen"
                    "fr" -> "Séries Infinies"
                    "pt" -> "Séries Infinitas"
                    "zh" -> "无穷级数"
                    "ja" -> "無限級数"
                    "ko" -> "무한급수"
                    "ru" -> "Бесконечные ряды"
                    "hi" -> "अनंत श्रेणी"
                    "id" -> "Deret Tak Hingga"
                    "nl" -> "Oneindige Reeksen"
                    "sv" -> "Oändliga Serier"
                    "no" -> "Uendelige Rekker"
                    "da" -> "Uendelige Rækker"
                    "pl" -> "Szeregi Nieskończone"
                    "th" -> "อนุกรมอนันต์"
                    "tr" -> "Sonsuz Seriler"
                    "uk" -> "Нескінченні ряди"
                    "vi" -> "Chuỗi Vô hạn"
                    else -> "Series Infinitas"
                },
                description = when (lang) {
                    "en" -> "Convergence tests, power series and Fourier series"
                    "de" -> "Konvergenzkriterien, Potenzreihen und Fourier-Reihen"
                    "fr" -> "Tests de convergence, séries entières et séries de Fourier"
                    "pt" -> "Testes de convergência, séries de potências e séries de Fourier"
                    "zh" -> "敛散性审敛法、幂级数与傅里叶级数"
                    "ja" -> "収束判定法、冪級数、フーリエ級数"
                    "ko" -> "수렴 판정법, 거듭제곱 급수 및 푸리에 급수"
                    "ru" -> "Признаки сходимости, степенные ряды и ряды Фурье"
                    "hi" -> "अभिसरण परीक्षण, घात श्रेणी और फूरियर श्रेणी"
                    "id" -> "Uji konvergensi, deret pangkat, dan deret Fourier"
                    "nl" -> "Convergentiecriteria, machtreeksen en Fourierreeksen"
                    "sv" -> "Konvergenstester, potensserier och Fourierserier"
                    "no" -> "Konvergenstester, potensrekker og Fourier-rekker"
                    "da" -> "Konvergenstest, potensrækker og Fourier-rækker"
                    "pl" -> "Kryteria zbieżności, szeregi potęgowe i szeregi Fouriera"
                    "th" -> "การทดสอบการลู่เข้า อนุกรมกำลัง และอนุกรมฟูเรียร์"
                    "tr" -> "Yakınsaklık testleri, kuvvet serileri ve Fourier serileri"
                    "uk" -> "Ознаки збіжності, статечні ряди та ряди Фур'є"
                    "vi" -> "Kiểm tra sự hội tụ, chuỗi lũy thừa và chuỗi Fourier"
                    else -> "Criterios de convergencia, series de potencias y series de Fourier"
                },
                domain = MathDomain.CALCULUS
            )
            "kinematics" -> CategoryMetadata(
                title = when (lang) {
                    "en" -> "Kinematics"
                    "de" -> "Kinematik"
                    "fr" -> "Cinématique"
                    "pt" -> "Cinemática"
                    "zh" -> "运动学"
                    "ja" -> "運動学"
                    "ko" -> "운동학"
                    "ru" -> "Кинематика"
                    "hi" -> "शुद्धगतिकी"
                    "id" -> "Kinematika"
                    "nl" -> "Kinematica"
                    "sv" -> "Kinematik"
                    "no" -> "Kinematikk"
                    "da" -> "Kinematik"
                    "pl" -> "Kinematyka"
                    "th" -> "จลนศาสตร์"
                    "tr" -> "Kinematik"
                    "uk" -> "Кінематика"
                    "vi" -> "Động học"
                    else -> "Cinemática"
                },
                description = when (lang) {
                    "en" -> "Motion, velocity, acceleration and trajectories"
                    "de" -> "Bewegung, Geschwindigkeit, Beschleunigung und Flugbahnen"
                    "fr" -> "Mouvement, vitesse, accélération et trajectoires"
                    "pt" -> "Movimento, velocidade, aceleração e trajetórias"
                    "zh" -> "运动、速度、加速度与运动轨迹"
                    "ja" -> "運動、速度、加速度、軌道"
                    "ko" -> "운동, 속도, 가속도 및 궤적"
                    "ru" -> "Движение, скорость, ускорение и траектории"
                    "hi" -> "गति, वेग, त्वरण और प्रक्षेप्य पथ"
                    "id" -> "Gerak, kecepatan, percepatan, dan lintasan"
                    "nl" -> "Beweging, snelheid, versnelling en trajecten"
                    "sv" -> "Rörelse, hastighet, acceleration och banor"
                    "no" -> "Bevegelse, hastighet, akselerasjon og baner"
                    "da" -> "Bevægelse, hastighed, acceleration og baner"
                    "pl" -> "Ruch, prędkość, przyspieszenie i trajektorie"
                    "th" -> "การเคลื่อนที่ ความเร็ว ความเร่ง และวิถีการเคลื่อนที่"
                    "tr" -> "Hareket, hız, ivme ve yörüngeler"
                    "uk" -> "Рух, швидкість, прискорення та траєкторії"
                    "vi" -> "Chuyển động, vận tốc, gia tốc và quỹ đạo"
                    else -> "Movimiento, velocidad, aceleración y trayectorias"
                },
                domain = MathDomain.GEOMETRY
            )
            "various" -> CategoryMetadata(
                title = when (lang) {
                    "en" -> "Miscellaneous Topics"
                    "de" -> "Verschiedene Themen"
                    "fr" -> "Sujets Divers"
                    "pt" -> "Tópicos Diversos"
                    "zh" -> "杂项主题"
                    "ja" -> "その他のトピック"
                    "ko" -> "기타 주제"
                    "ru" -> "Разное"
                    "hi" -> "विविध विषय"
                    "id" -> "Topik Serba-serbi"
                    "nl" -> "Diverse Onderwerpen"
                    "sv" -> "Diverse Ämnen"
                    "no" -> "Diverse Emner"
                    "da" -> "Diverse Emner"
                    "pl" -> "Różne Tematy"
                    "th" -> "หัวข้อเบ็ดเตล็ด"
                    "tr" -> "Çeşitli Konular"
                    "uk" -> "Різне"
                    "vi" -> "Chủ đề Khác"
                    else -> "Temas Varios"
                },
                description = when (lang) {
                    "en" -> "Complementary formulas, constants and mathematical applications"
                    "de" -> "Ergänzende Formeln, Konstanten und Anwendungen"
                    "fr" -> "Formules complémentaires, constantes et applications"
                    "pt" -> "Fórmulas complementares, constantes e aplicações"
                    "zh" -> "补充公式、常数与数学应用"
                    "ja" -> "補足公式、定数、数学的応用"
                    "ko" -> "보충 공식, 상수 및 수학적 응용"
                    "ru" -> "Дополнительные формулы, константы и приложения"
                    "hi" -> "पूरक सूत्र, नियतांक और गणितीय अनुप्रयोग"
                    "id" -> "Formula pelengkap, konstanta, dan aplikasi matematika"
                    "nl" -> "Aanvullende formules, constanten en toepassingen"
                    "sv" -> "Kompletterande formler, konstanter och tillämpningar"
                    "no" -> "Kompletterende formler, konstanter og anvendelser"
                    "da" -> "Supplerende formler, konstanter og anvendelser"
                    "pl" -> "Formuły uzupełniające, stałe i zastosowania"
                    "th" -> "สูตรเพิ่มเติม ค่าคงที่ และการประยุกต์ใช้ทางคณิตศาสตร์"
                    "tr" -> "Tamamlayıcı formüller, sabitler ve uygulamalar"
                    "uk" -> "Додаткові формули, константи та застосування"
                    "vi" -> "Công thức bổ sung, hằng số và ứng dụng toán học"
                    else -> "Fórmulas complementarias, constantes y aplicaciones matemáticas"
                },
                domain = MathDomain.ALL
            )
            else -> CategoryMetadata(
                title = folder.replace("-", " ").capitalizeWords(),
                description = if (lang == "en") "Formulas and topics for $folder" else "Fórmulas y temas de $folder",
                domain = MathDomain.ALL
            )
        }
    }

    private fun formatTopicTitle(fileName: String): String {
        val nameWithoutExt = fileName.removeSuffix(".md")
        val lang = getSelectedLanguage()
        val map = TitleMaps.getTitleMap(lang)
        val mappedTitle = map[nameWithoutExt]

        if (mappedTitle != null) {
            val lower = mappedTitle.lowercase().trim()
            val isGeneric = genericTitleWords.any { lower == it || lower.startsWith("$it ") || lower.startsWith("$it:") }
            if (!isGeneric && mappedTitle.isNotBlank()) {
                return mappedTitle
            }
        }

        return nameWithoutExt.replace("-", " ").capitalizeWords()
    }

    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

}
