
val Context.cartDataStore by preferencesDataStore(name = "cart_prefs")

private val CART_KEY = stringPreferencesKey("cart_items")

object CartItemSerializer {

    
    private val localDateTimeAdapter = object : JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

        override fun serialize(src: LocalDateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            return JsonPrimitive(src?.format(formatter))
        }

        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LocalDateTime? {
            return json?.asString?.let { LocalDateTime.parse(it, formatter) }
        }
    }

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, localDateTimeAdapter)
        .create()

    fun toJson(cartItems: List<CartItem>): String {
        return gson.toJson(cartItems)
    }

    fun fromJson(json: String): List<CartItem> {
        val type = object : TypeToken<List<CartItem>>() {}.type
        return gson.fromJson<List<CartItem>>(json, type) ?: emptyList()
    }
}


suspend fun saveCart(context: Context, items: List<CartItem>) {
    val json = CartItemSerializer.toJson(items)
    context.cartDataStore.edit { prefs ->
        prefs[CART_KEY] = json
    }
}


fun loadCart(context: Context): Flow<List<CartItem>> {
    return context.cartDataStore.data.map { prefs ->
        prefs[CART_KEY]?.let { CartItemSerializer.fromJson(it) } ?: emptyList()
    }
}
