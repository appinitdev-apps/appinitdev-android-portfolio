@Composable
fun PdfView(
    uri: Uri,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val darkTheme = LocalDarkTheme.current
    val isDarkTheme = DarkTheme.isDarkTheme(darkTheme)
    AndroidView(
        modifier = modifier,
        factory = { factoryContext ->
            val v = PDFView(factoryContext)
            v.fromUri(uri)
                .defaultPage(0)
                .enableAnnotationRendering(true)
                .scrollHandle(MinimalScrollHandle(factoryContext))
                .spacing(10)
                .onPageError(object : OnPageErrorListener {
                    override fun onPageError(
                        page: Int,
                        t: Throwable?,
                    ) {
                        LogCat.e(page.toString())
                    }
                })
                .pageFitPolicy(FitPolicy.BOTH)
                .nightMode(isDarkTheme)
                .load()
            v
        }
    )
}
