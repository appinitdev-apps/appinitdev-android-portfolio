
 





data class SearchResults(
        val songs: List<Audio> = emptyList(),
        val albums: List<Album> = emptyList(),
        val artists: List<Artist> = emptyList(),
        val genres: List<Genre> = emptyList()
) {
    fun isEmpty(results: SearchResults): Boolean {
        return results.songs.isEmpty() &&
                results.albums.isEmpty() &&
                results.artists.isEmpty() &&
                results.genres.isEmpty()
    }

    fun isSmallDataSet(): Boolean {
        val totalResults = songs.size + albums.size + artists.size + genres.size
        return totalResults < 10
    }

    companion object {
         



        fun empty(): SearchResults = SearchResults()
    }
}

 





data class SearchCategoryFilter(
        val songsEnabled: Boolean = true,
        val albumsEnabled: Boolean = true,
        val artistsEnabled: Boolean = true,
        val genresEnabled: Boolean = true
)

