
enum class MathDomain(val displayName: String) {
    ALL("Todos"),
    ALGEBRA("Álgebra y Aritmética"),
    CALCULUS("Cálculo y Análisis"),
    TRIGONOMETRY("Trigonometría y Funciones"),
    GEOMETRY("Geometría y Vectores"),
    LOGIC_STATS("Lógica y Probabilidad")
}

data class MathCategory(
    val id: String,                 // e.g. "trigonometry", "derivatives"
    val title: String,              // e.g. "Trigonometría"
    val description: String,        // Brief description in Spanish
    val domain: MathDomain,         // Math domain
    val iconName: String = "function",
    val topicCount: Int = 0
)

data class MathTopic(
    val id: String,                 // e.g. "trigonometry/law-of-sines"
    val categoryId: String,         // e.g. "trigonometry"
    val fileName: String,           // e.g. "law-of-sines.md"
    val title: String,              // e.g. "Ley de los Senos"
    val filePath: String,           // relative path in assets, e.g. "Data/trigonometry/law-of-sines.md"
    val summarySnippet: String = "",
    val hasExamples: Boolean = false,
    val exampleCount: Int = 0,
    val isFavorite: Boolean = false
)

data class SearchResultMatch(
    val topic: MathTopic,
    val matchedSubtopic: String?,
    val matchSnippet: String,
    val matchType: String
)

data class Flashcard(
    val category: String, // "DERIVATIVES", "INTEGRALS", "DIFF_EQ", "EQUATIONS", "FUNCTIONS", "LIMITS", "POLYNOMIALS", "VECTORS_MATRICES", "TRIGONOMETRY", "COMPLEX"
    val level: Int,       // 1: Básico, 2: Intermedio, 3: Avanzado
    val question: String,
    val formulaLatex: String,
    val explanation: String,
    val id: Long = 0L,
    val isFavorite: Boolean = false,
)

data class SymbolItem(
    val id: Long = 0L,
    val symbol: String,
    val name: String,
    val category: String, // "CALCULUS", "ALGEBRA", "TRIGONOMETRY", "CONSTANTS", "SYMBOLS"
    val description: String,
    val example: String,
    val isFavorite: Boolean = false,
)

enum class DetailTab(val label: String, val icon: String) {
    ALL("Todo", "📄"),
    THEORY("Teoría", "📖"),
    EXAMPLES("Ejemplos", "💡"),
    FORMULAS("Fórmulas", "∑"),
    CALCULATOR("Calculadora", "🧮")
}

enum class Screen {
    HOME,
    CATEGORY_DETAIL,
    TOPIC_DETAIL,
    SEARCH,
    FAVORITES,
    GLOSSARY,
    FLASHCARDS,
    UNIT_CONVERTER,
    PLOTTER
}
