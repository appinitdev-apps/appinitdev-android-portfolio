
sealed class PageItem {
     


    data class Header(
            val album: Album,
            val totalSongs: Int,
            val totalDuration: Long,
            val albumArtists: List<Artist>,
            val songs: List<Audio>
    ) : PageItem()

     


    data class AlbumsSection(
            val albums: List<Album>,
            val artistName: String?
    ) : PageItem()

     


    data class ArtistsSection(
            val artists: List<Artist>
    ) : PageItem()

     


    data class GenresSection(
            val genres: List<Genre>
    ) : PageItem()

     


    data class SongItem(
            val audio: Audio,
            val position: Int, 
            val allSongs: List<Audio> 
    ) : PageItem()

     



    data class ArtistInfoSection(
            val info: MusicBrainzArtistInfo
    ) : PageItem()

     




    data class AlbumInfoSection(
            val info: MusicBrainzAlbumInfo
    ) : PageItem()
}

