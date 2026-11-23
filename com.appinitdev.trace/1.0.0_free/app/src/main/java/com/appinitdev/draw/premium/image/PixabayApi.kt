
interface PixabayApi {
    @GET("api/")
    suspend fun searchImages(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("image_type") imageType: String,
        @Query("per_page") perPage: Int,
        @Query("page") page: Int
    ): ImageResponse
}

@Keep
data class ImageResponse(
    @SerializedName("hits") val hits: List<Image>
)

@Keep
data class Image(
    @SerializedName("id") val id: Int,
    @SerializedName("webformatURL") val webformatURL: String,
    @SerializedName("largeImageURL") val largeImageURL: String,
    @SerializedName("tags") val tags: String
)



