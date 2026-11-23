
fun RssItem.toDFeedEntry(
    feedId: String,
    feedUrl: String,
): DFeedEntry {
    val item = DFeedEntry()
    item.rawId =
        CryptoHelper.sha1(
            (feedId + "_" + (link ?: title ?: UUID.randomUUID().toString())).toByteArray(),
        )
    item.feedId = feedId
    if (title != null) {
        item.title = HtmlCompat.fromHtml(title!!, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().replace("\n", " ").trim()
    } else {
        item.title = ""
    }

    val feedBaseUrl = HtmlUtils.getBaseUrl(feedUrl)
    item.description = HtmlUtils.improveHtmlContent(content ?: description ?: "", feedBaseUrl)
    item.description = MDConverter().convert(item.description)
    item.url = link ?: ""

    item.image = image ?: ""

    item.author = author?.ifEmpty { sourceName ?: "" } ?: ""

    if (pubDate != null) {
        val date = DateParser.parseDate(pubDate!!, Locale.US)
        if (date != null && date.time < item.publishedAt.toEpochMilliseconds()) {
            item.publishedAt = Instant.fromEpochMilliseconds(date.time)
        }
    }

    return item
}

suspend fun DFeedEntry.fetchContentAsync(): ApiResult {
    try {
        val httpClient = HttpClientManager.browserClient()
        val response = httpClient.get(url)

        if (response.isOk()) {
            val input = response.body<String>()
            Readability4J.parse(
                url,
                input
            ).articleContent?.let { articleContent ->
                articleContent.selectFirst("h1")?.remove()
                val c = articleContent.toString()
                val mobilizedHtml = HtmlUtils.improveHtmlContent(c, HtmlUtils.getBaseUrl(url))
                val summary = getSummary()
                if (summary.isEmpty() || c.length >= summary.length) { 
                    val imagesList = HtmlUtils.getImageURLs(mobilizedHtml)
                    if (imagesList.isNotEmpty()) {
                        if (image.isEmpty()) {
                            image = HtmlUtils.getMainImageURL(imagesList)
                        }
                    }

                    if (image.isNotEmpty() && !image.startsWith("/")) {
                        try {
                            val r = httpClient.get(image)
                            if (r.isOk()) {
                                val dir = MainApp.instance.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!.path + "/feeds/${feedId}"
                                File(dir).mkdirs()
                                var path = "$dir/main-${CryptoHelper.sha1(image.toByteArray())}"
                                val extension = image.getFilenameExtension()
                                if (extension.isNotEmpty()) {
                                    path += ".$extension"
                                }
                                val file = File(path)
                                file.createNewFile()
                                r.bodyAsChannel().copyAndClose(file.writeChannel())
                                image = path
                            }
                        } catch (ex: Exception) {
                            LogCat.e(ex.toString())
                            ex.printStackTrace()
                        }
                    }
                    val md = MDConverter().convert(mobilizedHtml)
                    if (md.length >= description.length) {
                        content = md
                    } else if (content.isEmpty()) {
                        content = description
                    }
                    updatedAt = TimeHelper.now()
                    FeedEntryHelper.updateAsync(this)
                }
            }
        }

        return ApiResult(response)
    } catch (ex: Throwable) {
        ex.printStackTrace()
        return ApiResult(null, ex)
    }
}
