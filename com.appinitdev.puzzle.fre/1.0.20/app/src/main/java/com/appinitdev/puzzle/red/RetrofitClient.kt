
object PixabayConfig {
    const val API_KEY = ""
}

interface PixabayService {
    @GET("api/")
    suspend fun searchPhotos(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("image_type") imageType: String,
        @Query("per_page") perPage: Int
    ): PixabayResponse
}

data class PixabayResponse(
    @SerializedName("hits") val hits: List<PixabayPhoto> = emptyList()
)

data class PixabayPhoto(
    @SerializedName("id") val id: Long = 0L,
    @SerializedName("previewURL") val previewURL: String = "",
    @SerializedName("webformatURL") val webformatURL: String = "",
    @SerializedName("largeImageURL") val largeImageURL: String = "",
    @SerializedName("tags") val tags: String = ""
)

object RetrofitClient {
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    val service: PixabayService by lazy {
        Retrofit.Builder()
            .baseUrl("https://pixabay.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PixabayService::class.java)
    }
}
