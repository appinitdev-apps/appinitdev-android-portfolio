
object RetrofitInstance {
    private const val BASE_URL = "https://pixabay.com/"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val request = originalRequest.newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .build()

            Log.d("PixabayApi", "--> Requesting URL: ${request.url}")
            val response = chain.proceed(request)
            Log.d("PixabayApi", "<-- Response Code: ${response.code} for URL: ${response.request.url}")
            response
        }
        .build()

    val api: PixabayApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PixabayApi::class.java)
    }
}


