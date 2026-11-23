
class Converters {
    private val gson = Gson()
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromStoreSettings(settings: StoreSettings?): String? {
        return settings?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toStoreSettings(settings: String?): StoreSettings? {
        return settings?.let { 
            gson.fromJson(it, StoreSettings::class.java)
        }
    }

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(formatter)
    }

    @TypeConverter
    fun toLocalDateTime(dateTime: String?): LocalDateTime? {
        return dateTime?.let { LocalDateTime.parse(it, formatter) }
    }

    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? {
        return value?.let { BigDecimal(it) }
    }
}
