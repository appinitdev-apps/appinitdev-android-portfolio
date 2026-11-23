
fun TextView.markdown(content: String, previewerState: MediaPreviewerState) {
    this.movementMethod = LinkMovementMethod.getInstance()
    val markdown = Markwon.builder(context)
        .usePlugin(CoilImagesPlugin(context))
        .usePlugin(
            ImagesPlugin.create { plugin ->
                plugin.addSchemeHandler(AppImageSchemeHandler(context))
                plugin.addSchemeHandler(NetworkSchemeHandler())
            },
        )
        .usePlugin(
            object : AbstractMarkwonPlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    builder
                        .bulletWidth(context.dp2px(5))
                }
            },
        )
        .usePlugin(HtmlPlugin.create { plugin ->
            plugin.addHandler(FontTagHandler()).addHandler(AppImageHandler.create())
        })
        .usePlugin(LinkifyPlugin.create(Linkify.EMAIL_ADDRESSES or Linkify.PHONE_NUMBERS))
        .usePlugin(StrikethroughPlugin.create())
        .usePlugin(TablePlugin.create(context))
        .usePlugin(TaskListPlugin.create(context))
        .usePlugin(MarkwonInlineParserPlugin.create())
        .usePlugin(JLatexMathPlugin.create(this.textSize) { builder -> builder.inlinesEnabled(true) })
        .usePlugin(
            object : AbstractMarkwonPlugin() {
                override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                    builder.linkResolver { _, link ->
                        WebHelper.open(context, link)
                    }
                }
            },
        )
        .usePlugin(
            object : AbstractMarkwonPlugin() {
                override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                    builder.appendFactory(Image::class.java) { configuration, props ->
                        LinkSpan(
                            configuration.theme(),
                            ImageProps.DESTINATION.require(props),
                        ) { _, link ->
                            coMain {
                                val links = extractImageLinksFromHtml(convertMarkdownToHtml(content)).map { PreviewItem(it, it.getFinalPath(context)) }
                                MediaPreviewData.items = links
                                previewerState.open(index = links.indexOfFirst { it.id == link })
                            }
                        }
                    }
                }
            },
        )
        .usePlugin(
            object : AbstractMarkwonPlugin() {
                override fun configureVisitor(builder: MarkwonVisitor.Builder) {
                    builder.on(SoftLineBreak::class.java) { visitor, _ -> visitor.forceNewLine() }
                }
            },
        )
        .build()

    markdown.setMarkdown(this, content)
}

fun extractImageLinksFromHtml(htmlContent: String): List<String> {
    val imageLinks = mutableListOf<String>()

    
    val doc = Jsoup.parse(htmlContent)

    
    val imgTags = doc.select("img")

    
    for (imgTag in imgTags) {
        val imageUrl = imgTag.attr("src")
        imageLinks.add(imageUrl)
    }

    return imageLinks
}

fun convertMarkdownToHtml(markdownText: String): String {
    val parser = Parser.builder().build()
    val renderer = HtmlRenderer.builder().build()
    val document = parser.parse(markdownText)
    return renderer.render(document)
}