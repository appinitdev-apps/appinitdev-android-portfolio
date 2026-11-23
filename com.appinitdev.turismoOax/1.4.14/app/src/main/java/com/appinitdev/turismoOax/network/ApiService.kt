interface ApiService {
    @GET("tours.json")
    suspend fun getTours(): List<Tour>
}
