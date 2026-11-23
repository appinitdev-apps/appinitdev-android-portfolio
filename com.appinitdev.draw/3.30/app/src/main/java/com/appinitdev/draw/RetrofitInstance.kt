object RetrofitInstance {
    private const val BASE_URL = "https://pixabay.com"
    val api: PixabayApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PixabayApi::class.java)
    }
}
