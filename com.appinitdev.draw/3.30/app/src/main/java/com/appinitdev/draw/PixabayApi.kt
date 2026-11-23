interface PixabayApi {
    @GET("/api/")
    suspend fun searchImages(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("image_type") imageType: String = "photo",
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1
    ): ImageResponse
}
data class ImageResponse(
    val hits: List<Image>
)
data class Image(
    val id: Int,
    val webformatURL: String,
    val largeImageURL: String,
    val tags: String
)
