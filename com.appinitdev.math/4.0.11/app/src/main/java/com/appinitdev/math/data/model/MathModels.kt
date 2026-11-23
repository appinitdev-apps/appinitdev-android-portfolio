
enum class MathDomain(val displayName: String) {
    ALL("Todos"),
    ALGEBRA("Álgebra y Aritmética"),
    CALCULUS("Cálculo y Análisis"),
    TRIGONOMETRY("Trigonometría y Funciones"),
    GEOMETRY("Geometría y Vectores"),
    LOGIC_STATS("Lógica y Probabilidad")
}

data class MathCategory(
    val id: String,                 
    val title: String,              
    val description: String,        
    val domain: MathDomain,         
    val iconName: String = "function",
    val topicCount: Int = 0
)

data class MathTopic(
    val id: String,                 
    val categoryId: String,         
    val fileName: String,           
    val title: String,              
    val filePath: String,           
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
    val category: String, 
    val level: Int,       
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
    val category: String, 
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
