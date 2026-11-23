class Converters {
    @TypeConverter
    fun fromList(list: List<Int>): String {
        return list.joinToString(",")
    }
    @TypeConverter
    fun toList(data: String): List<Int> {
        if (data.isBlank()) return emptyList()
        return data.split(",").map { it.toInt() }
    }
}