fun SchemaBuilder.addAudioSchema() {
    query("audios") {
        configure {
            executor = Executor.DataLoaderPrepared
        }
        resolver { offset: Int, limit: Int, query: String, sortBy: FileSortBy ->
            val context = MainApp.instance
            Permission.WRITE_EXTERNAL_STORAGE.checkAsync(context)
            AudioMediaStoreHelper.searchAsync(context, query, limit, offset, sortBy).map { it.toModel() }
        }
        type<Audio> {
            dataProperty("tags") {
                prepare { item -> item.id.value }
                loader { ids ->
                    TagsLoader.load(ids, DataType.AUDIO)
                }
            }
        }
    }
    query("audioCount") {
        resolver { query: String ->
            if (Permission.WRITE_EXTERNAL_STORAGE.enabledAndCanAsync(MainApp.instance)) {
                AudioMediaStoreHelper.countAsync(MainApp.instance, query)
            } else {
                0
            }
        }
    }
    mutation("playAudio") {
        resolver { path: String ->
            val context = MainApp.instance
            val audio = DPlaylistAudio.fromPath(context, path)
            AudioPlayingPreference.putAsync(audio.path)
            if (!AudioPlaylistPreference.getValueAsync().any { it.path == audio.path }) {
                AudioPlaylistPreference.addAsync(listOf(audio))
            }
            audio.toModel()
        }
    }
    mutation("updateAudioPlayMode") {
        resolver { mode: MediaPlayMode ->
            AudioPlayModePreference.putAsync(mode)
            true
        }
    }
    mutation("clearAudioPlaylist") {
        resolver { ->
            val context = MainApp.instance
            AudioPlayingPreference.putAsync("")
            AudioPlaylistPreference.putAsync(arrayListOf())
            coMain {
                AudioPlayer.clear()
            }
            sendEvent(ClearAudioPlaylistEvent())
            true
        }
    }
    mutation("deletePlaylistAudio") {
        resolver { path: String ->
            AudioPlaylistPreference.deleteAsync(setOf(path))
            true
        }
    }
    mutation("addPlaylistAudios") {
        resolver { query: String ->
            val context = MainApp.instance
            val items = AudioMediaStoreHelper.searchAsync(context, query, 1000, 0, AudioSortByPreference.getValueAsync())
            AudioPlaylistPreference.addAsync(items.map { it.toPlaylistAudio() })
            true
        }
    }
    mutation("reorderPlaylistAudios") {
        resolver { paths: List<String> ->
            val context = MainApp.instance
            val currentPlaylist = AudioPlaylistPreference.getValueAsync()
            if (currentPlaylist.isEmpty() || paths.isEmpty()) {
                return@resolver true
            }
            val audioMap = currentPlaylist.associateBy { it.path }
            val reorderedPlaylist = mutableListOf<DPlaylistAudio>()
            paths.forEach { path ->
                audioMap[path]?.let { audio ->
                    reorderedPlaylist.add(audio)
                }
            }
            currentPlaylist.forEach { audio ->
                if (!paths.contains(audio.path)) {
                    reorderedPlaylist.add(audio)
                }
            }
            AudioPlaylistPreference.putAsync(reorderedPlaylist)
            true
        }
    }
}
