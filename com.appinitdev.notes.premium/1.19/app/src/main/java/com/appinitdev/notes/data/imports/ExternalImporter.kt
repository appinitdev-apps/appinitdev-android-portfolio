interface ExternalImporter {
    fun import(
        app: Application,
        source: Uri,
        destination: File,
        progress: MutableLiveData<ImportProgress>? = null,
    ): Pair<List<BaseNote>, File?>
}
