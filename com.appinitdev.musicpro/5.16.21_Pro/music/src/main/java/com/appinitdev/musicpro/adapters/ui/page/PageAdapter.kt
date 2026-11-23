
 



class PageAdapter(
        private var data: PageData,
        private val pageType: PageType
) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private var listener: GeneralAdapterCallbacks? = null
    private val items = mutableListOf<PageItem>()

     
    private var artistInfo: MusicBrainzArtistInfo? = null

     
    private var albumInfo: MusicBrainzAlbumInfo? = null

     




    private var livePlaylist: Playlist? = (pageType as? PageType.PlaylistPage)?.playlist

     


    sealed class PageType {
        data class AlbumPage(val album: Album) : PageType()
        data class ArtistPage(val artist: Artist) : PageType()
        data class ComposerPage(val composer: Artist) : PageType()
        data class GenrePage(val genre: Genre) : PageType()
        data class FolderPage(val folder: Folder) : PageType()
        data class YearPage(val yearGroup: YearGroup) : PageType()
        data class PlaylistPage(val playlist: Playlist) : PageType()
    }

    init {
        buildItemsList()
    }

     



    fun updateData(newData: PageData) {
        Log.d(TAG, "updateData: Updating with ${newData.songs.size} songs, ${newData.albums.size} albums, ${newData.artists.size} artists")

        val oldItems = items.toList()
        this.data = newData
        buildItemsList()

        val diffCallback = PageDiffCallback(oldItems, items)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        diffResult.dispatchUpdatesTo(this)
        Log.d(TAG, "updateData: DiffUtil updates dispatched")
    }

     


    private class PageDiffCallback(
            private val oldList: List<PageItem>,
            private val newList: List<PageItem>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]

            
            return when {
                oldItem is PageItem.Header && newItem is PageItem.Header -> true
                oldItem is PageItem.SongItem && newItem is PageItem.SongItem ->
                    oldItem.audio.id == newItem.audio.id
                oldItem is PageItem.AlbumsSection && newItem is PageItem.AlbumsSection -> true
                oldItem is PageItem.ArtistsSection && newItem is PageItem.ArtistsSection -> true
                oldItem is PageItem.GenresSection && newItem is PageItem.GenresSection -> true
                oldItem is PageItem.ArtistInfoSection && newItem is PageItem.ArtistInfoSection -> true
                oldItem is PageItem.AlbumInfoSection && newItem is PageItem.AlbumInfoSection -> true
                else -> false
            }
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]

            return when (oldItem) {
                is PageItem.Header if newItem is PageItem.Header ->
                    oldItem == newItem
                is PageItem.SongItem if newItem is PageItem.SongItem ->
                    oldItem.audio == newItem.audio && oldItem.position == newItem.position
                is PageItem.AlbumsSection if newItem is PageItem.AlbumsSection ->
                    oldItem.albums == newItem.albums
                is PageItem.ArtistsSection if newItem is PageItem.ArtistsSection ->
                    oldItem.artists == newItem.artists
                is PageItem.GenresSection if newItem is PageItem.GenresSection ->
                    oldItem.genres == newItem.genres
                is PageItem.ArtistInfoSection if newItem is PageItem.ArtistInfoSection ->
                    oldItem.info == newItem.info
                is PageItem.AlbumInfoSection if newItem is PageItem.AlbumInfoSection ->
                    oldItem.info == newItem.info
                else -> false
            }
        }
    }

    private fun buildItemsList() {
        items.clear()

        
        when (pageType) {
            is PageType.AlbumPage -> {
                items.add(PageItem.Header(
                        album = pageType.album,
                        totalSongs = data.songs.size,
                        totalDuration = data.songs.sumOf { it.duration },
                        albumArtists = data.artists,
                        songs = data.songs
                ))
            }
            is PageType.ArtistPage -> {
                items.add(PageItem.Header(
                        album = Album(
                                id = pageType.artist.id,
                                name = pageType.artist.name,
                                artist = pageType.artist.name,
                                artistId = pageType.artist.id
                        ),
                        totalSongs = data.songs.size,
                        totalDuration = data.songs.sumOf { it.duration },
                        albumArtists = data.artists,
                        songs = data.songs
                ))
            }
            is PageType.ComposerPage -> {
                items.add(PageItem.Header(
                        album = Album(
                                id = pageType.composer.id,
                                name = pageType.composer.name,
                                artist = pageType.composer.name,
                                artistId = pageType.composer.id
                        ),
                        totalSongs = data.songs.size,
                        totalDuration = data.songs.sumOf { it.duration },
                        albumArtists = data.artists,
                        songs = data.songs
                ))
            }
            is PageType.GenrePage -> {
                items.add(PageItem.Header(
                        album = Album(
                                id = pageType.genre.id,
                                name = pageType.genre.name,
                                artist = "",
                                artistId = 0L
                        ),
                        totalSongs = data.songs.size,
                        totalDuration = data.songs.sumOf { it.duration },
                        albumArtists = data.artists,
                        songs = data.songs
                ))
            }
            is PageType.FolderPage -> {
                items.add(PageItem.Header(
                        album = Album(
                                id = pageType.folder.id,
                                name = pageType.folder.name,
                                artist = pageType.folder.path,
                                artistId = 0L
                        ),
                        totalSongs = data.songs.size,
                        totalDuration = data.songs.sumOf { it.duration },
                        albumArtists = data.artists,
                        songs = data.songs
                ))
            }
            is PageType.YearPage -> {
                items.add(PageItem.Header(
                        album = Album(
                                id = pageType.yearGroup.id,
                                name = pageType.yearGroup.year,
                                artist = "",
                                artistId = 0L
                        ),
                        totalSongs = data.songs.size,
                        totalDuration = data.songs.sumOf { it.duration },
                        albumArtists = data.artists,
                        songs = data.songs
                ))
            }
            is PageType.PlaylistPage -> {
                items.add(PageItem.Header(
                        album = Album(
                                id = pageType.playlist.id,
                                name = pageType.playlist.name,
                                artist = pageType.playlist.description ?: "",
                                artistId = 0L
                        ),
                        totalSongs = data.songs.size,
                        totalDuration = data.songs.sumOf { it.duration },
                        albumArtists = data.artists,
                        songs = data.songs
                ))
            }
        }

        
        data.songs.forEachIndexed { index, audio ->
            items.add(PageItem.SongItem(
                    audio = audio,
                    position = index,
                    allSongs = data.songs
            ))
        }

        
        val currentArtistInfo = artistInfo
        if (currentArtistInfo != null && (pageType is PageType.ArtistPage || pageType is PageType.ComposerPage)) {
            items.add(1, PageItem.ArtistInfoSection(currentArtistInfo))
        }

        
        val currentAlbumInfo = albumInfo
        if (currentAlbumInfo != null && pageType is PageType.AlbumPage) {
            items.add(1, PageItem.AlbumInfoSection(currentAlbumInfo))
        }

        
        if (data.albums.isNotEmpty()) {
            val artistName = when (pageType) {
                is PageType.AlbumPage -> pageType.album.name
                is PageType.ArtistPage -> pageType.artist.name
                is PageType.ComposerPage -> pageType.composer.name
                is PageType.GenrePage -> pageType.genre.name
                is PageType.FolderPage -> pageType.folder.name
                is PageType.YearPage -> pageType.yearGroup.year
                is PageType.PlaylistPage -> pageType.playlist.name
            }
            items.add(PageItem.AlbumsSection(
                    albums = data.albums,
                    artistName = artistName
            ))
        }

        
        if (data.artists.isNotEmpty()) {
            items.add(PageItem.ArtistsSection(
                    artists = data.artists
            ))
        }

        
        if (data.genres.isNotEmpty()) {
            items.add(PageItem.GenresSection(
                    genres = data.genres
            ))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            PageConstants.VIEW_TYPE_HEADER -> {
                when (pageType) {
                    is PageType.GenrePage, is PageType.FolderPage, is PageType.YearPage -> {
                        GenreHeader(AdapterHeaderArtistPageBinding.inflate(inflater, parent, false))
                    }
                    else -> {
                        Header(AdapterHeaderArtistPageBinding.inflate(inflater, parent, false))
                    }
                }
            }
            PageConstants.VIEW_TYPE_ALBUMS -> {
                Albums(AdapterGenreAlbumsBinding.inflate(inflater, parent, false))
            }
            PageConstants.VIEW_TYPE_ARTISTS -> {
                Artists(AdapterGenreAlbumsBinding.inflate(inflater, parent, false))
            }
            PageConstants.VIEW_TYPE_GENRES -> {
                Genres(AdapterGenreAlbumsBinding.inflate(inflater, parent, false))
            }
            PageConstants.VIEW_TYPE_SONG -> {
                Song(AdapterStyleListBinding.inflate(inflater, parent, false))
            }
            PageConstants.VIEW_TYPE_ARTIST_INFO -> {
                ArtistInfo(AdapterArtistInfoBinding.inflate(inflater, parent, false))
            }
            PageConstants.VIEW_TYPE_ALBUM_INFO -> {
                AlbumInfo(AdapterAlbumInfoBinding.inflate(inflater, parent, false))
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position: Int) {
        val item = items[position]

        when (holder) {
            is Header -> {
                val headerItem = item as PageItem.Header
                holder.bind(headerItem, data, pageType)
            }
            is GenreHeader -> {
                val headerItem = item as PageItem.Header
                holder.bind(headerItem, data, pageType)
            }
            is Albums -> {
                val albumsItem = item as PageItem.AlbumsSection
                holder.bind(albumsItem, listener, pageType)
            }
            is Artists -> {
                val artistsItem = item as PageItem.ArtistsSection
                holder.bind(artistsItem, listener, pageType)
            }
            is Genres -> {
                val genresItem = item as PageItem.GenresSection
                holder.bind(genresItem, listener)
            }
            is Song -> {
                val songItem = item as PageItem.SongItem
                val showTrackInfo = pageType is PageType.AlbumPage
                holder.bind(songItem.audio, showTrackInfo, songItem.allSongs.size)
                holder.binding.container.setOnClickListener {
                    listener?.onSongClicked(songItem.allSongs, songItem.position, holder.binding.cover)
                }

                holder.binding.container.setOnLongClickListener {
                    listener?.onSongLongClicked(songItem.allSongs, songItem.position, holder.binding.cover)
                    true
                }
            }
            is ArtistInfo -> {
                val infoItem = item as PageItem.ArtistInfoSection
                holder.bind(infoItem.info)
            }
            is AlbumInfo -> {
                val infoItem = item as PageItem.AlbumInfoSection
                holder.bind(infoItem.info)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onViewRecycled(holder: VerticalListViewHolder) {
        super.onViewRecycled(holder)
        when (holder) {
            is Song -> {
                Glide.with(holder.binding.cover).clear(holder.binding.cover)
            }
            is Albums -> {
                holder.cleanup()
            }
            is Artists -> {
                holder.cleanup()
            }
            is Genres -> {
                holder.cleanup()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is PageItem.Header -> PageConstants.VIEW_TYPE_HEADER
            is PageItem.AlbumsSection -> PageConstants.VIEW_TYPE_ALBUMS
            is PageItem.ArtistsSection -> PageConstants.VIEW_TYPE_ARTISTS
            is PageItem.GenresSection -> PageConstants.VIEW_TYPE_GENRES
            is PageItem.SongItem -> PageConstants.VIEW_TYPE_SONG
            is PageItem.ArtistInfoSection -> PageConstants.VIEW_TYPE_ARTIST_INFO
            is PageItem.AlbumInfoSection -> PageConstants.VIEW_TYPE_ALBUM_INFO
        }
    }

    inner class Albums(val binding: AdapterGenreAlbumsBinding) : VerticalListViewHolder(binding.root) {
        private var adapter: AdapterCarouselItems? = null

        fun bind(item: PageItem.AlbumsSection, adapterListener: GeneralAdapterCallbacks?, pageType: PageType) {
            if (item.albums.isEmpty()) {
                binding.root.visibility = View.GONE
                return
            }

            binding.root.visibility = View.VISIBLE

            
            binding.title.text = when (pageType) {
                is PageType.AlbumPage -> context.getString(
                        R.string.albums_from_artist,
                        item.artistName ?: context.getString(R.string.unknown)
                )
                is PageType.ArtistPage -> context.getString(
                        R.string.albums_from_artist,
                        item.artistName ?: context.getString(R.string.unknown)
                )
                is PageType.ComposerPage -> context.getString(
                        R.string.albums_from_artist,
                        item.artistName ?: context.getString(R.string.unknown)
                )
                is PageType.GenrePage -> context.getString(
                        R.string.albums_in_genre,
                        item.artistName ?: context.getString(R.string.unknown)
                )
                is PageType.FolderPage -> context.getString(
                        R.string.albums_in_folder,
                        item.artistName ?: context.getString(R.string.unknown)
                )
                is PageType.YearPage -> context.getString(
                        R.string.albums_in_folder,
                        item.artistName ?: context.getString(R.string.unknown)
                )
                is PageType.PlaylistPage -> context.getString(
                        R.string.albums_in_playlist,
                        item.artistName ?: context.getString(R.string.unknown)
                )
            }

            if (binding.recyclerView.adapter == null) {
                binding.recyclerView.setHasFixedSize(true)
                binding.recyclerView.addItemDecoration(LinearHorizontalSpacingDecoration(24))
            }

            binding.recyclerView.setUniqueKey("albums_carousel")

            adapter = AdapterCarouselItems(ArtFlowData(R.string.unknown, item.albums))
            binding.recyclerView.adapter = adapter

            adapter?.setAdapterCarouselCallbacks(object : AdapterCarouselItems.Companion.AdapterCarouselCallbacks {
                override fun onClicked(view: View, position: Int) {
                    adapterListener?.onAlbumClicked(item.albums, position, view)
                }
            })
        }

        fun cleanup() {
            adapter = null
        }
    }

    inner class Artists(val binding: AdapterGenreAlbumsBinding) : VerticalListViewHolder(binding.root) {
        private var adapter: AdapterCarouselItems? = null

        fun bind(item: PageItem.ArtistsSection, adapterListener: GeneralAdapterCallbacks?, pageType: PageType) {
            if (item.artists.isEmpty()) {
                binding.root.visibility = View.GONE
                return
            }

            binding.root.visibility = View.VISIBLE

            
            binding.title.text = when (pageType) {
                is PageType.AlbumPage -> context.getString(R.string.album_artists)
                is PageType.ArtistPage -> context.getString(
                        R.string.with_other_artists,
                        pageType.artist.name ?: context.getString(R.string.unknown)
                )
                is PageType.ComposerPage -> context.getString(
                        R.string.with_other_artists,
                        pageType.composer.name ?: context.getString(R.string.unknown)
                )
                is PageType.GenrePage -> context.getString(
                        R.string.artists_in_genre,
                        pageType.genre.name ?: context.getString(R.string.unknown)
                )
                is PageType.FolderPage -> context.getString(
                        R.string.artists_in_folder,
                        pageType.folder.name
                )
                is PageType.YearPage -> context.getString(
                        R.string.artists_in_folder,
                        pageType.yearGroup.year
                )
                is PageType.PlaylistPage -> context.getString(
                        R.string.artists_in_playlist,
                        pageType.playlist.name
                )
            }

            if (binding.recyclerView.adapter == null) {
                binding.recyclerView.setHasFixedSize(true)
                binding.recyclerView.addItemDecoration(LinearHorizontalSpacingDecoration(24))
            }

            binding.recyclerView.setUniqueKey("artists_carousel")

            adapter = AdapterCarouselItems(ArtFlowData(R.string.unknown, item.artists))
            binding.recyclerView.adapter = adapter

            adapter?.setAdapterCarouselCallbacks(object : AdapterCarouselItems.Companion.AdapterCarouselCallbacks {
                override fun onClicked(view: View, position: Int) {
                     




                    if (pageType is PageType.AlbumPage) {
                        adapterListener?.onAlbumArtistClicked(item.artists, position, view)
                    } else {
                        adapterListener?.onArtistClicked(item.artists, position, view)
                    }
                }
            })
        }

        fun cleanup() {
            adapter = null
        }
    }

    inner class Genres(val binding: AdapterGenreAlbumsBinding) : VerticalListViewHolder(binding.root) {
        private var adapter: AdapterCarouselItems? = null

        fun bind(item: PageItem.GenresSection, adapterListener: GeneralAdapterCallbacks?) {
            if (item.genres.isEmpty()) {
                binding.root.visibility = View.GONE
                return
            }

            binding.root.visibility = View.VISIBLE
            binding.title.text = context.getString(R.string.album_genres)

            if (binding.recyclerView.adapter == null) {
                binding.recyclerView.setHasFixedSize(true)
                binding.recyclerView.addItemDecoration(LinearHorizontalSpacingDecoration(24))
            }

            binding.recyclerView.setUniqueKey("genres_carousel")

            adapter = AdapterCarouselItems(ArtFlowData(R.string.unknown, item.genres))
            binding.recyclerView.adapter = adapter

            adapter?.setAdapterCarouselCallbacks(object : AdapterCarouselItems.Companion.AdapterCarouselCallbacks {
                override fun onClicked(view: View, position: Int) {
                    adapterListener?.onGenreClicked(item.genres[position], view)
                }
            })
        }

        fun cleanup() {
            adapter = null
        }
    }

    inner class Header(val binding: AdapterHeaderArtistPageBinding) : VerticalListViewHolder(binding.root) {
        fun bind(item: PageItem.Header, pageData: PageData, pageType: PageType) {
            binding.apply {
                name.text = item.album.name ?: context.getString(R.string.unknown)
                songs.text = item.totalSongs.toString()
                albums.text = pageData.albums.size.toString()
                artists.text = item.albumArtists.size.toString()
                totalTime.text = item.totalDuration.toDynamicTimeString()

                when (pageType) {
                    is PageType.ArtistPage -> {
                        sortStyle.setArtistPageSort()
                    }
                    is PageType.ComposerPage -> {
                        sortStyle.setComposerPageSort()
                    }
                    is PageType.PlaylistPage -> {
                        sortStyle.setPlaylistPageSortFromOrder(livePlaylist?.sortOrder ?: pageType.playlist.sortOrder)
                    }
                    else -> {
                        sortStyle.setAlbumPageSort()
                    }
                }

                
                when (pageType) {
                    is PageType.AlbumPage, is PageType.ComposerPage, is PageType.FolderPage, is PageType.PlaylistPage -> {
                        artFlow.visibility = View.VISIBLE
                        when {
                            item.songs.isNotEmpty() -> {
                                artFlow.setAdapter(SliderAdapter(ArtFlowData(R.string.songs, item.songs)))
                            }
                            pageData.albums.isNotEmpty() -> {
                                artFlow.setAdapter(SliderAdapter(ArtFlowData(R.string.albums, pageData.albums)))
                            }
                            pageData.artists.isNotEmpty() -> {
                                artFlow.setAdapter(SliderAdapter(ArtFlowData(R.string.artists, pageData.artists)))
                            }
                        }
                        artFlow.start()
                    }
                    is PageType.ArtistPage -> {
                        artFlow.visibility = View.VISIBLE
                        val artist = Artist(
                                id = 0,
                                name = item.album.name,
                                albumCount = 0,
                                trackCount = 0,
                                songPaths = pageData.songs.map { it.uri }
                        )
                        artFlow.setAdapter(SliderAdapter(ArtFlowData(R.string.artists, listOf(artist))))
                        artFlow.start()
                    }
                    is PageType.GenrePage -> {
                        artFlow.visible(false)
                    }
                    else -> {
                        artFlow.visible(false)
                    }
                }

                play.setOnClickListener {
                    listener?.onPlayClicked(item.songs, 0)
                }
                shuffle.setOnClickListener {
                    listener?.onShuffleClicked(item.songs, 0)
                }
                shuffle.setOnLongClickListener {
                    listener?.onShuffleLongClicked(item.songs, 0)
                    true
                }
                menu.setOnClickListener {
                    listener?.onMenuClicked(it)
                }
                sortStyle.setOnClickListener {
                    listener?.onSortClicked(it)
                }
            }
        }
    }

    
    inner class GenreHeader(val binding: AdapterHeaderArtistPageBinding) : VerticalListViewHolder(binding.root) {
        fun bind(item: PageItem.Header, pageData: PageData, pageType: PageType) {
            binding.apply {
                name.text = when (pageType) {
                    is PageType.GenrePage -> pageType.genre.name ?: context.getString(R.string.unknown)
                    is PageType.FolderPage -> pageType.folder.name
                    is PageType.YearPage -> pageType.yearGroup.year
                    else -> item.album.name ?: context.getString(R.string.unknown)
                }
                songs.text = item.totalSongs.toString()
                artists.text = pageData.artists.size.toString()
                albums.text = pageData.albums.size.toString()
                totalTime.text = item.totalDuration.toDynamicTimeString()
                artFlow.setAdapter(SliderAdapter(ArtFlowData(R.string.songs, item.songs)))
                artFlow.start()

                when (pageType) {
                    is PageType.GenrePage -> {
                        sortStyle.setGenrePageSort()
                    }
                    is PageType.FolderPage -> {
                        sortStyle.setFolderPageSort()
                    }
                    is PageType.YearPage -> {
                        sortStyle.setYearPageSort()
                    }
                    is PageType.PlaylistPage -> {
                        sortStyle.setPlaylistPageSort()
                    }
                    else -> {
                        sortStyle.setArtistPageSort()
                    }
                }

                play.setOnClickListener {
                    listener?.onPlayClicked(item.songs, 0)
                }
                shuffle.setOnClickListener {
                    listener?.onShuffleClicked(item.songs, 0)
                }
                menu.setOnClickListener {
                    listener?.onMenuClicked(it)
                }
                sortStyle.setOnClickListener {
                    listener?.onSortClicked(it)
                }
            }
        }
    }

     






    private inner class SliderAdapter(private val data: ArtFlowData<Any>) : FelicityPager.PageAdapter {

        override fun getCount(): Int = data.items.size.coerceAtMost(12)

        override fun getItemId(position: Int): Long = position.toLong()

        override fun onCreateView(position: Int, parent: ViewGroup): View {
            return ImageView(parent.context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        }

        override fun onBindView(position: Int, view: View) {
            if (data.items.isNotEmpty()) {
                (view as ImageView).loadArtCover(
                        item = data.items[position],
                        roundedCorners = false,
                        blur = false,
                        crop = true
                )
            }
        }

        override fun onRecycleView(position: Int, view: View) {
            val iv = view as ImageView
            Glide.with(iv.context).clear(iv)
            iv.setImageDrawable(null)
        }
    }

    fun setArtistAdapterListener(listener: GeneralAdapterCallbacks) {
        this.listener = listener
    }

     







    fun updatePlaylist(playlist: Playlist) {
        livePlaylist = playlist
        notifyItemChanged(0)
    }

     








    fun setArtistInfo(info: MusicBrainzArtistInfo?) {
        artistInfo = info
        val oldItems = items.toList()
        buildItemsList()
        val diff = DiffUtil.calculateDiff(PageDiffCallback(oldItems, items))
        diff.dispatchUpdatesTo(this)
    }

     








    fun setAlbumInfo(info: MusicBrainzAlbumInfo?) {
        albumInfo = info
        val oldItems = items.toList()
        buildItemsList()
        val diff = DiffUtil.calculateDiff(PageDiffCallback(oldItems, items))
        diff.dispatchUpdatesTo(this)
    }

    inner class ArtistInfo(val binding: AdapterArtistInfoBinding) : VerticalListViewHolder(binding.root) {

        private var bioExpanded = false

        fun bind(info: MusicBrainzArtistInfo) {
            binding.apply {
                
                if (!info.type.isNullOrBlank()) {
                    typeBadge.text = info.type
                    typeBadge.visibility = View.VISIBLE
                } else {
                    typeBadge.visibility = View.GONE
                }

                
                if (!info.country.isNullOrBlank()) {
                    countryBadge.text = info.country
                    countryBadge.visibility = View.VISIBLE
                } else {
                    countryBadge.visibility = View.GONE
                }

                
                val yearText = when {
                    info.beginYear != null && info.endYear != null ->
                        context.getString(R.string.active_years, info.beginYear, info.endYear)
                    info.beginYear != null && !info.ended ->
                        context.getString(R.string.active_since_present, info.beginYear)
                    info.beginYear != null ->
                        context.getString(R.string.active_since, info.beginYear)
                    else -> null
                }
                if (yearText != null) {
                    yearsBadge.text = yearText
                    yearsBadge.visibility = View.VISIBLE
                } else {
                    yearsBadge.visibility = View.GONE
                }

                
                val tagList = if (info.tags.isBlank()) emptyList()
                else info.tags.split("|").filter { it.isNotBlank() }

                if (tagList.isNotEmpty()) {
                    tagsContainer.removeAllViews()
                    val inflater = LayoutInflater.from(context)
                    tagList.forEach { tag ->
                        val pill = inflater.inflate(
                                R.layout.item_tag_pill, tagsContainer, false
                        ) as com.appinitdev.musicpro.decorations.highlight.HighlightTextView
                        pill.text = tag
                        tagsContainer.addView(pill)
                    }
                    tagsScroll.visibility = View.VISIBLE
                } else {
                    tagsScroll.visibility = View.GONE
                }

                
                
                if (!info.bio.isNullOrBlank()) {
                    bio.text = info.bio
                    bio.visibility = View.VISIBLE

                    
                    bioExpanded = false

                    bio.post {
                        val ellipsisCount = bio.layout.getEllipsisCount(bio.layout.lineCount - 1)
                        val isEllipsized = ellipsisCount > 0
                        bioToggle.visibility = if (isEllipsized) View.VISIBLE else View.GONE
                    }

                    bioToggle.setOnClickListener {
                        bioExpanded = !bioExpanded
                        bio.maxLines = if (bioExpanded) Int.MAX_VALUE else 4
                        bioToggle.setText(if (bioExpanded) R.string.read_less else R.string.read_more)
                    }
                } else {
                    bio.visibility = View.GONE
                    bioToggle.visibility = View.GONE
                }
            }
        }
    }

    inner class AlbumInfo(val binding: AdapterAlbumInfoBinding) : VerticalListViewHolder(binding.root) {

        private var bioExpanded = false

        fun bind(info: MusicBrainzAlbumInfo) {
            binding.apply {
                
                val displayDate = info.releaseDate?.take(4)?.takeIf { it.isNotBlank() }
                if (displayDate != null) {
                    dateBadge.text = context.getString(R.string.released_in, displayDate)
                    dateBadge.visibility = View.VISIBLE
                } else {
                    dateBadge.visibility = View.GONE
                }

                
                if (!info.country.isNullOrBlank()) {
                    countryBadge.text = info.country
                    countryBadge.visibility = View.VISIBLE
                } else {
                    countryBadge.visibility = View.GONE
                }

                
                if (!info.status.isNullOrBlank()) {
                    statusBadge.text = info.status
                    statusBadge.visibility = View.VISIBLE
                } else {
                    statusBadge.visibility = View.GONE
                }

                
                val labelList = if (info.labels.isBlank()) emptyList()
                else info.labels.split("|").filter { it.isNotBlank() }

                if (labelList.isNotEmpty()) {
                    labelsContainer.removeAllViews()
                    val inflater = LayoutInflater.from(context)
                    labelList.forEach { label ->
                        val pill = inflater.inflate(
                                R.layout.item_tag_pill, labelsContainer, false
                        ) as com.appinitdev.musicpro.decorations.highlight.HighlightTextView
                        pill.text = label
                        labelsContainer.addView(pill)
                    }
                    labelsScroll.visibility = View.VISIBLE
                } else {
                    labelsScroll.visibility = View.GONE
                }

                
                val tagList = if (info.tags.isBlank()) emptyList()
                else info.tags.split("|").filter { it.isNotBlank() }

                if (tagList.isNotEmpty()) {
                    tagsContainer.removeAllViews()
                    val inflater = LayoutInflater.from(context)
                    tagList.forEach { tag ->
                        val pill = inflater.inflate(
                                R.layout.item_tag_pill, tagsContainer, false
                        ) as com.appinitdev.musicpro.decorations.highlight.HighlightTextView
                        pill.text = tag
                        tagsContainer.addView(pill)
                    }
                    tagsScroll.visibility = View.VISIBLE
                } else {
                    tagsScroll.visibility = View.GONE
                }

                
                if (!info.bio.isNullOrBlank()) {
                    bio.text = info.bio
                    bio.visibility = View.VISIBLE

                    bioExpanded = false

                    bio.post {
                        val ellipsisCount = bio.layout.getEllipsisCount(bio.layout.lineCount - 1)
                        val isEllipsized = ellipsisCount > 0
                        bioToggle.visibility = if (isEllipsized) View.VISIBLE else View.GONE
                    }

                    bioToggle.setOnClickListener {
                        bioExpanded = !bioExpanded
                        bio.maxLines = if (bioExpanded) Int.MAX_VALUE else 4
                        bioToggle.setText(if (bioExpanded) R.string.read_less else R.string.read_more)
                    }
                } else {
                    bio.visibility = View.GONE
                    bioToggle.visibility = View.GONE
                }
            }
        }
    }

    companion object {
        private const val TAG = "PageAdapter"
    }
}