
 











object PhoneGeoCache {
    private const val MAX_SIZE = 1024
    private val MISS: PhoneGeo = PhoneGeo("", "", "", "", 0)
    private val cache = object : LruCache<String, PhoneGeo>(MAX_SIZE) {
        override fun sizeOf(key: String, value: PhoneGeo): Int = 1
    }

    fun lookup(number: String): PhoneGeo? {
        cache[number]?.let { return it.takeIf { it !== MISS } }
        val geo = lookupFresh(number)
        cache.put(number, geo ?: MISS)
        return geo
    }

    private fun lookupFresh(number: String): PhoneGeo? =
        PhoneNumberLookup.instance()
            .with(LookupAlgorithm.IMPL.BINARY_SEARCH)
            .lookup(number)
            ?.let { PhoneGeo(it.geoInfo.province, it.geoInfo.city, it.geoInfo.zipCode, it.geoInfo.areaCode, it.isp.code) }
}
