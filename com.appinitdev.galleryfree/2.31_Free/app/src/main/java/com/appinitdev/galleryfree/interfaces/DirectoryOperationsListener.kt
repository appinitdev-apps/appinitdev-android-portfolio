interface DirectoryOperationsListener {
    fun refreshItems()
    fun deleteFolders(folders: ArrayList<File>)
    fun recheckPinnedFolders()
    fun updateDirectories(directories: ArrayList<Directory>)
}
