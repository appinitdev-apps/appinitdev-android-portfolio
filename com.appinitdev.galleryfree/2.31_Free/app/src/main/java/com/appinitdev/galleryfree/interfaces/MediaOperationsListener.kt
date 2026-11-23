interface MediaOperationsListener {
    fun refreshItems()
    fun tryDeleteFiles(fileDirItems: ArrayList<FileDirItem>, skipRecycleBin: Boolean)
    fun selectedPaths(paths: ArrayList<String>)
    fun updateMediaGridDecoration(media: ArrayList<ThumbnailItem>)
}
