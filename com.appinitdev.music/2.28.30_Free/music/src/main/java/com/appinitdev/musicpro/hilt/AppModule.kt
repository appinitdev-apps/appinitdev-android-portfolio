
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAlbumRepository(@ApplicationContext context: Context): AlbumRepository {
        return AlbumRepository(context)
    }

    @Provides
    @Singleton
    fun provideArtistRepository(@ApplicationContext context: Context): ArtistRepository {
        return ArtistRepository(context)
    }

    @Provides
    @Singleton
    fun provideAudioDatabaseLoader(@ApplicationContext context: Context): AudioDatabaseLoader {
        return AudioDatabaseLoader(context)
    }

     





    @Provides
    @Singleton
    fun providePlaylistDatabaseLoader(@ApplicationContext context: Context): PlaylistDatabaseLoader {
        return PlaylistDatabaseLoader(context)
    }

    @Provides
    @Singleton
    fun provideAudioRepository(@ApplicationContext context: Context): AudioRepository {
        return AudioRepository(context)
    }

    @Provides
    @Singleton
    fun providesLrcRepository(@ApplicationContext context: Context): LrcRepository {
        return LrcRepository(context)
    }

    @Provides
    @Singleton
    fun providesBookmarkRepository(@ApplicationContext context: Context): BookmarkRepository {
        return BookmarkRepository(context)
    }
}