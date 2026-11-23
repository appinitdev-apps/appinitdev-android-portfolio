
 












class AdapterSearch : FastScrollAdapter<VerticalListViewHolder>() {

    private var generalAdapterCallbacks: GeneralAdapterCallbacks? = null

     
    private var songs: MutableList<Audio> = mutableListOf()

     
    private var albums: MutableList<Album> = mutableListOf()

     
    private var artists: MutableList<Artist> = mutableListOf()

     
    private var genres: MutableList<Genre> = mutableListOf()

     
    var layoutMode: CommonPreferencesConstants.LayoutMode = SearchPreferences.getGridSize()

    private val diffCallback = object : DiffUtil.ItemCallback<SearchAdapterItem>() {
        override fun areItemsTheSame(
                oldItem: SearchAdapterItem,
                newItem: SearchAdapterItem,
        ): Boolean {
            if (oldItem::class != newItem::class) return false
            @Suppress("IntroduceWhenSubject")
            return when {
                oldItem is SearchAdapterItem.Header && newItem is SearchAdapterItem.Header ->
                    oldItem.title == newItem.title

                oldItem is SearchAdapterItem.SongItem && newItem is SearchAdapterItem.SongItem ->
                    oldItem.audio.id == newItem.audio.id

                oldItem is SearchAdapterItem.AlbumItem && newItem is SearchAdapterItem.AlbumItem ->
                    oldItem.album.id == newItem.album.id

                oldItem is SearchAdapterItem.ArtistItem && newItem is SearchAdapterItem.ArtistItem ->
                    oldItem.artist.id == newItem.artist.id

                oldItem is SearchAdapterItem.GenreItem && newItem is SearchAdapterItem.GenreItem ->
                    oldItem.genre.id == newItem.genre.id

                else -> false
            }
        }

        override fun areContentsTheSame(
                oldItem: SearchAdapterItem,
                newItem: SearchAdapterItem,
        ): Boolean = oldItem == newItem
    }

    private val listUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {
            if (count > 100) notifyDataSetChanged() else notifyItemRangeInserted(position, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            if (count > 100) notifyDataSetChanged() else notifyItemRangeRemoved(position, count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            notifyItemMoved(fromPosition, toPosition)
        }

        override fun onChanged(position: Int, count: Int, payload: Any?) {
            notifyItemRangeChanged(position, count, payload)
        }
    }

    private val differ = AsyncListDiffer(
            listUpdateCallback,
            AsyncDifferConfig.Builder(diffCallback).build()
    )

    private val items: List<SearchAdapterItem>
        get() = differ.currentList

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is SearchAdapterItem.Header -> VIEW_TYPE_HEADER
            is SearchAdapterItem.AlbumItem -> VIEW_TYPE_ALBUM
            is SearchAdapterItem.ArtistItem -> VIEW_TYPE_ARTIST
            is SearchAdapterItem.GenreItem -> VIEW_TYPE_GENRE
            is SearchAdapterItem.SongItem -> when {
                layoutMode.isLabel -> VIEW_TYPE_SONG_LABEL
                layoutMode.isGrid -> VIEW_TYPE_SONG_GRID
                else -> VIEW_TYPE_SONG_LIST
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER ->
                HeaderHolder(AdapterSearchSectionHeaderBinding.inflate(inflater, parent, false))

            VIEW_TYPE_SONG_GRID ->
                SongGridHolder(AdapterStyleGridBinding.inflate(inflater, parent, false))

            VIEW_TYPE_SONG_LABEL ->
                SongLabelHolder(AdapterStyleLabelsBinding.inflate(inflater, parent, false))

            VIEW_TYPE_ALBUM ->
                AlbumHolder(AdapterStyleListBinding.inflate(inflater, parent, false))

            VIEW_TYPE_ARTIST ->
                ArtistHolder(AdapterStyleListBinding.inflate(inflater, parent, false))

            VIEW_TYPE_GENRE ->
                GenreHolder(AdapterStyleListBinding.inflate(inflater, parent, false))

            else ->
                SongListHolder(AdapterStyleListBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBind(holder: VerticalListViewHolder, position: Int, isLightBind: Boolean) {
        when (holder) {
            is HeaderHolder -> holder.bind(items[position] as SearchAdapterItem.Header)
            is SongListHolder -> holder.bind(items[position] as SearchAdapterItem.SongItem, isLightBind)
            is SongGridHolder -> holder.bind(items[position] as SearchAdapterItem.SongItem, isLightBind)
            is SongLabelHolder -> holder.bind(items[position] as SearchAdapterItem.SongItem, isLightBind)
            is AlbumHolder -> holder.bind(items[position] as SearchAdapterItem.AlbumItem, isLightBind)
            is ArtistHolder -> holder.bind(items[position] as SearchAdapterItem.ArtistItem, isLightBind)
            is GenreHolder -> holder.bind(items[position] as SearchAdapterItem.GenreItem)
        }
    }

    override fun onViewRecycled(holder: VerticalListViewHolder) {
        holder.itemView.clearAnimation()
        super.onViewRecycled(holder)
        when (holder) {
            is SongListHolder -> Glide.with(holder.binding.cover).clear(holder.binding.cover)
            is SongGridHolder -> Glide.with(holder.binding.albumArt).clear(holder.binding.albumArt)
            is AlbumHolder -> Glide.with(holder.binding.cover).clear(holder.binding.cover)
            is ArtistHolder -> Glide.with(holder.binding.cover).clear(holder.binding.cover)
            is GenreHolder -> Glide.with(holder.binding.cover).clear(holder.binding.cover)
            else -> Unit
        }
    }

     









    fun submitResults(
            results: SearchResults,
            songsHeader: String,
            albumsHeader: String,
            artistsHeader: String,
            genresHeader: String,
    ) {
        songs = results.songs.toMutableList()
        albums = results.albums.toMutableList()
        artists = results.artists.toMutableList()
        genres = results.genres.toMutableList()

        val newItems = buildList {
            if (songs.isNotEmpty()) {
                add(SearchAdapterItem.Header(songsHeader))
                songs.forEach { add(SearchAdapterItem.SongItem(it)) }
            }
            if (albums.isNotEmpty()) {
                add(SearchAdapterItem.Header(albumsHeader))
                albums.forEach { add(SearchAdapterItem.AlbumItem(it)) }
            }
            if (artists.isNotEmpty()) {
                add(SearchAdapterItem.Header(artistsHeader))
                artists.forEach { add(SearchAdapterItem.ArtistItem(it)) }
            }
            if (genres.isNotEmpty()) {
                add(SearchAdapterItem.Header(genresHeader))
                genres.forEach { add(SearchAdapterItem.GenreItem(it)) }
            }
        }

        differ.submitList(newItems)
    }

     






    fun getSectionPosition(headerTitle: String): Int {
        return items.indexOfFirst { it is SearchAdapterItem.Header && it.title == headerTitle }
    }

     




    fun setGeneralAdapterCallbacks(callbacks: GeneralAdapterCallbacks) {
        this.generalAdapterCallbacks = callbacks
    }

     




    inner class HeaderHolder(val binding: AdapterSearchSectionHeaderBinding) :
            VerticalListViewHolder(binding.root) {

        fun bind(item: SearchAdapterItem.Header) {
            binding.sectionTitle.text = item.title
        }
    }

     




    inner class SongListHolder(val binding: AdapterStyleListBinding) :
            VerticalListViewHolder(binding.root) {

        fun bind(item: SearchAdapterItem.SongItem, isLightBind: Boolean) {
            val audio = item.audio
            binding.title.setTextOrUnknown(audio.getProperTitle())
            binding.secondaryDetail.setTextOrUnknown(audio.getProperArtists())
            binding.tertiaryDetail.setTextOrUnknown(audio.getProperAlbum())
            binding.title.addAudioQualityIcon(audio)
            binding.container.setAudioID(audio.id)

            if (isLightBind) return

            binding.cover.loadArtCoverWithPayload(audio)
            binding.container.setOnLongClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    generalAdapterCallbacks?.onSongLongClicked(songs, songs.indexOf(audio), binding.cover)
                }
                true
            }
            binding.container.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    generalAdapterCallbacks?.onSongClicked(songs, songs.indexOf(audio), it)
                }
            }
        }
    }

     




    inner class SongGridHolder(val binding: AdapterStyleGridBinding) :
            VerticalListViewHolder(binding.root) {

        fun bind(item: SearchAdapterItem.SongItem, isLightBind: Boolean) {
            val audio = item.audio
            binding.container.enableGridMode = true
            binding.title.setTextOrUnknown(audio.getProperTitle())
            binding.secondaryDetail.setTextOrUnknown(audio.getProperArtists())
            binding.tertiaryDetail.setTextOrUnknown(audio.getProperAlbum())
            binding.container.setAudioID(audio.id)

            if (isLightBind) return

            binding.albumArt.loadArtCoverWithPayload(audio)
            binding.container.setOnLongClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    generalAdapterCallbacks?.onSongLongClicked(songs, songs.indexOf(audio), null)
                }
                true
            }
            binding.container.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    generalAdapterCallbacks?.onSongClicked(songs, songs.indexOf(audio), it)
                }
            }
        }
    }

     




    inner class SongLabelHolder(val binding: AdapterStyleLabelsBinding) :
            VerticalListViewHolder(binding.root) {

        fun bind(item: SearchAdapterItem.SongItem, isLightBind: Boolean) {
            val audio = item.audio
            binding.title.setTextOrUnknown(audio.getProperTitle())
            binding.secondaryDetail.setTextOrUnknown(audio.getProperArtists())
            binding.tertiaryDetail.setTextOrUnknown(audio.getProperAlbum())
            binding.title.addAudioQualityIcon(audio)
            binding.container.setAudioID(audio.id)

            if (isLightBind) return

            binding.container.setOnLongClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    generalAdapterCallbacks?.onSongLongClicked(songs, songs.indexOf(audio), null)
                }
                true
            }
            binding.container.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    generalAdapterCallbacks?.onSongClicked(songs, songs.indexOf(audio), it)
                }
            }
        }
    }

     




    inner class AlbumHolder(val binding: AdapterStyleListBinding) :
            VerticalListViewHolder(binding.root) {

        fun bind(item: SearchAdapterItem.AlbumItem, isLightBind: Boolean) {
            val album = item.album
            binding.title.setTextOrUnknown(album.name)
            binding.secondaryDetail.setTextOrUnknown(album.artist)
            binding.tertiaryDetail.setTextOrUnknown(
                    context.resources.getQuantityString(R.plurals.number_of_songs, album.songCount, album.songCount)
            )

            if (isLightBind) return

            binding.cover.loadArtCoverWithPayload(album)
            binding.container.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    generalAdapterCallbacks?.onAlbumClicked(albums, albums.indexOf(album), it)
                }
            }
            binding.container.setOnLongClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    generalAdapterCallbacks?.onAlbumLongClicked(albums, albums.indexOf(album), binding.cover)
                }
                true
            }
        }
    }

     




    inner class ArtistHolder(val binding: AdapterStyleListBinding) :
            VerticalListViewHolder(binding.root) {

        fun bind(item: SearchAdapterItem.ArtistItem, isLightBind: Boolean) {
            val artist = item.artist
            binding.title.setTextOrUnknown(artist.name)
            binding.secondaryDetail.setTextOrUnknown(
                    context.resources.getQuantityString(R.plurals.number_of_songs, artist.trackCount, artist.trackCount)
            )
            binding.tertiaryDetail.setTextOrUnknown(
                    context.resources.getQuantityString(R.plurals.number_of_albums, artist.albumCount, artist.albumCount)
            )

            if (isLightBind) return

            binding.cover.loadArtCoverWithPayload(item = artist)
            binding.container.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    generalAdapterCallbacks?.onArtistClicked(artists, artists.indexOf(artist), it)
                }
            }
            binding.container.setOnLongClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    generalAdapterCallbacks?.onArtistLongClicked(artists, artists.indexOf(artist), binding.cover)
                }
                true
            }
        }
    }

     




    inner class GenreHolder(val binding: AdapterStyleListBinding) :
            VerticalListViewHolder(binding.root) {

        fun bind(item: SearchAdapterItem.GenreItem) {
            val genre = item.genre
            binding.title.text = genre.name ?: context.getString(R.string.unknown)
            binding.secondaryDetail.text = context.resources.getQuantityString(
                    R.plurals.number_of_songs, genre.songCount, genre.songCount
            )
            binding.tertiaryDetail.gone(false)
            binding.cover.loadArtCoverWithPayload(genre)
            binding.container.setOnClickListener {
                generalAdapterCallbacks?.onGenreClicked(genre, it)
            }
        }
    }

    companion object {
         
        const val VIEW_TYPE_HEADER = 0

         
        const val VIEW_TYPE_SONG_LIST = 1

         
        const val VIEW_TYPE_SONG_GRID = 2

         
        const val VIEW_TYPE_SONG_LABEL = 3

         
        const val VIEW_TYPE_ALBUM = 4

         
        const val VIEW_TYPE_ARTIST = 5

         
        const val VIEW_TYPE_GENRE = 6
    }
}

 



sealed class SearchAdapterItem {

     




    data class Header(val title: String) : SearchAdapterItem()

     




    data class SongItem(val audio: Audio) : SearchAdapterItem()

     




    data class AlbumItem(val album: Album) : SearchAdapterItem()

     




    data class ArtistItem(val artist: Artist) : SearchAdapterItem()

     




    data class GenreItem(val genre: Genre) : SearchAdapterItem()
}
