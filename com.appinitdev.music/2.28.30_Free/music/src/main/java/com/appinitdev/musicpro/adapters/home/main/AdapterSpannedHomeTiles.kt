class AdapterSpannedHomeTiles(
        private val tiles: MutableList<SpannedTile>
) : RecyclerView.Adapter<VerticalListViewHolder>() {
    private var callbacks: SpannedHomeTileCallbacks? = null
    override fun getItemViewType(position: Int): Int {
        return when (tiles[position]) {
            is SpannedTile.SongTile -> TYPE_SONG
            is SpannedTile.PanelTile -> TYPE_PANEL
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            TYPE_SONG -> SongHolder(
                    AdapterGridImageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            TYPE_PANEL -> PanelHolder(
                    AdapterSpannedPanelTileBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> throw IllegalArgumentException("Unknown tile type at viewType=$viewType")
        }
    }
    override fun onBindViewHolder(holder: VerticalListViewHolder, position: Int) {
        when (val tile = tiles[position]) {
            is SpannedTile.SongTile -> {
                val songHolder = holder as SongHolder
                songHolder.binding.art.loadArtCover(
                        item = tile.audio,
                        shadow = false,
                        roundedCorners = false,
                        darken = false)
                songHolder.binding.art.setOnClickListener {
                    val songs = tiles.filterIsInstance<SpannedTile.SongTile>().map { it.audio }
                    val songIndex = songs.indexOfFirst { it.id == tile.audio.id }.coerceAtLeast(0)
                    callbacks?.onSongTileClicked(songs, songIndex)
                }
                songHolder.binding.art.setOnLongClickListener {
                    callbacks?.onSongTileLongClicked(tile.audio, songHolder.binding.art)
                    true
                }
            }
            is SpannedTile.PanelTile -> {
                val panelHolder = holder as PanelHolder
                panelHolder.binding.icon.setImageResource(tile.iconRes)
                panelHolder.binding.title.setText(tile.titleRes)
                panelHolder.binding.container.setOnClickListener {
                    callbacks?.onPanelTileClicked(tile)
                }
            }
        }
    }
    override fun getItemCount(): Int = tiles.size
    override fun getItemId(position: Int): Long {
        return when (val tile = tiles[position]) {
            is SpannedTile.SongTile -> tile.audio.id
            is SpannedTile.PanelTile -> tile.titleRes.toLong().inv()
        }
    }
    fun updateSongs(newSongs: List<Audio>) {
        var songIndex = 0
        tiles.forEachIndexed { position, tile ->
            if (tile is SpannedTile.SongTile && songIndex < newSongs.size) {
                tiles[position] = SpannedTile.SongTile(newSongs[songIndex++])
                notifyItemChanged(position)
            }
        }
    }
    fun setCallbacks(callbacks: SpannedHomeTileCallbacks) {
        this.callbacks = callbacks
    }
    inner class SongHolder(val binding: AdapterGridImageBinding) :
            VerticalListViewHolder(binding.root)
    inner class PanelHolder(val binding: AdapterSpannedPanelTileBinding) :
            VerticalListViewHolder(binding.root)
    sealed class SpannedTile {
        data class SongTile(val audio: Audio) : SpannedTile()
        data class PanelTile(val titleRes: Int, val iconRes: Int) : SpannedTile() {
            fun getPanel(): Panel {
                return Panel(titleRes, iconRes)
            }
        }
    }
    interface SpannedHomeTileCallbacks {
        fun onSongTileClicked(songs: List<Audio>, position: Int)
        fun onSongTileLongClicked(audio: Audio, imageView: android.widget.ImageView)
        fun onPanelTileClicked(panelTile: SpannedTile.PanelTile)
    }
    companion object {
        private const val TYPE_SONG = 0
        private const val TYPE_PANEL = 1
        const val DESIRED_SONG_COUNT = 35
        const val MAX_PANEL_COUNT = 30
        private const val BIG_TILE_EVERY_N = 6
        private const val SPAN_COUNT = 3
        private val LAYOUT: LayoutSpec by lazy { buildLayoutSpec() }
        val BIG_TILE_POSITIONS: Set<Int> get() = LAYOUT.bigTilePositions
        val PANEL_POSITIONS: Set<Int> get() = LAYOUT.panelPositions
        val TOTAL_TILE_COUNT: Int get() = LAYOUT.totalCount
        val SONG_TILE_COUNT: Int get() = TOTAL_TILE_COUNT - PANEL_POSITIONS.size
        private data class LayoutSpec(
                val bigTilePositions: Set<Int>,
                val panelPositions: Set<Int>,
                val totalCount: Int
        )
        private fun buildLayoutSpec(): LayoutSpec {
            val bigTileCount = DESIRED_SONG_COUNT / BIG_TILE_EVERY_N
            val extraPanelCount = minOf(
                    MAX_PANEL_COUNT - bigTileCount,
                    (DESIRED_SONG_COUNT - bigTileCount) / 2
            ).coerceAtLeast(0)
            val songsInSongRows = DESIRED_SONG_COUNT - bigTileCount - 2 * extraPanelCount
            val songRowCount = (songsInSongRows + SPAN_COUNT - 1) / SPAN_COUNT  
            val totalRows = bigTileCount + extraPanelCount + songRowCount
            val rowTypes = Array(totalRows) { 'S' }
            for (i in 0 until bigTileCount) {
                rowTypes[i * totalRows / bigTileCount] = 'B'
            }
            val panelStep = if (extraPanelCount > 0) totalRows.toFloat() / extraPanelCount else Float.MAX_VALUE
            for (i in 0 until extraPanelCount) {
                var idx = (i * panelStep).toInt()
                while (idx < totalRows && rowTypes[idx] != 'S') idx++
                if (idx < totalRows) rowTypes[idx] = 'P'
            }
            val bigPositions = mutableSetOf<Int>()
            val panelPositions = mutableSetOf<Int>()
            var pos = 0
            for (type in rowTypes) {
                when (type) {
                    'B' -> {
                        bigPositions.add(pos++)
                        panelPositions.add(pos++)
                    }
                    'P' -> {
                        pos++
                        panelPositions.add(pos++)
                        pos++
                    }
                    else -> {
                        pos += SPAN_COUNT
                    }
                }
            }
            return LayoutSpec(bigPositions, panelPositions, pos)
        }
        fun buildTiles(
                songs: List<Audio>,
                panels: List<SpannedTile.PanelTile>
        ): MutableList<SpannedTile> {
            val result = ArrayList<SpannedTile>(TOTAL_TILE_COUNT)
            var songIndex = 0
            var panelIndex = 0
            for (pos in 0 until TOTAL_TILE_COUNT) {
                val isReservedForPanel = pos in PANEL_POSITIONS
                if (isReservedForPanel && panelIndex < panels.size) {
                    result.add(panels[panelIndex++])
                } else if (songs.isNotEmpty()) {
                    result.add(SpannedTile.SongTile(songs[songIndex % songs.size]))
                    songIndex++
                }
            }
            return result
        }
    }
}
