
 











@HiltViewModel
class LyricsViewModel @Inject constructor(
        application: Application,
        private val lyricsManager: LyricsManager
) : WrappedViewModel(application) {

     




    val lrcData: StateFlow<LrcData?> = lyricsManager.lrcData

     



    val syncOffsetMs: StateFlow<Long> = lyricsManager.syncOffsetMs

     




    val loadingStatus: StateFlow<LyricsLoadingStatus> = lyricsManager.loadingStatus

     



    val syncOffset: Long get() = lyricsManager.syncOffset

     
    fun loadLrcData() = lyricsManager.loadLrcData()

     
    fun reloadLrcData() = lyricsManager.reloadLrcData()

     



    fun seekBy(deltaMs: Long) = lyricsManager.seekBy(deltaMs)

     
    fun deleteLrc(onSuccess: (() -> Unit)? = null) = lyricsManager.deleteLrc(onSuccess)
}
