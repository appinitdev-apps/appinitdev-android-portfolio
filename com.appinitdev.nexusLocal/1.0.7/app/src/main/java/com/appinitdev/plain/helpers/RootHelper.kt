
 







object RootHelper {
    @Volatile
    private var rootAvailable: Boolean? = null

    
    private val EMULATED_RE = Regex("^/storage/emulated/(\\d+)(.*?)/?$")
    
    private val MEDIA_RE = Regex("^/data/media/(\\d+)(.*?)/?$")

    fun isRooted(): Boolean {
        return rootAvailable ?: synchronized(this) {
            rootAvailable ?: run {
                val result = checkRoot()
                rootAvailable = result
                result
            }
        }
    }

    private fun checkRoot(): Boolean {
        return try {
            exec("id").contains("uid=0")
        } catch (e: Exception) {
            LogCat.d("RootHelper: root check failed: ${e.message}")
            false
        }
    }

     





    private fun exec(command: String): String {
        var proc: Process? = null
        return try {
            proc = Runtime.getRuntime().exec("su")
            var output = ""
            
            val readerThread = Thread {
                output = proc.inputStream.bufferedReader().readText()
            }
            readerThread.start()
            val stdin = DataOutputStream(proc.outputStream)
            stdin.writeBytes("$command\nexit\n")
            stdin.flush()
            stdin.close()
            readerThread.join(15_000)
            output
        } catch (e: Exception) {
            LogCat.d("RootHelper: exec failed: ${e.message}")
            ""
        } finally {
            try { proc?.destroy() } catch (_: Exception) {}
        }
    }

     



    private fun toRealPath(path: String): String {
        val m = EMULATED_RE.matchEntire(path.trimEnd('/')) ?: return path
        return "/data/media/${m.groupValues[1]}${m.groupValues[2]}"
    }

     



    private fun fromRealPath(path: String): String {
        val m = MEDIA_RE.matchEntire(path) ?: return path
        return "/storage/emulated/${m.groupValues[1]}${m.groupValues[2]}"
    }

     



    fun listFiles(dir: String, showHidden: Boolean): List<DFile> {
        val realDir = toRealPath(dir)
        
        val safeDir = realDir.replace("\"", "\\\"")
        
        
        
        
        val cmd = buildString {
            append("stat -c '%n|%F|%s|%Y' \"$safeDir\"/* 2>/dev/null")
            if (showHidden) {
                
                
                append("; stat -c '%n|%F|%s|%Y' \"$safeDir\"/.[!.]* \"$safeDir\"/..?* 2>/dev/null")
            }
        }
        val output = exec(cmd)
        if (output.isBlank()) return emptyList()

        val seen = mutableSetOf<String>()
        return output.lines()
            .filter { it.contains('|') }
            .mapNotNull { line ->
                val parts = line.split("|", limit = 4)
                if (parts.size < 4) return@mapNotNull null
                val rawPath = parts[0].trim()
                val name = rawPath.substringAfterLast('/')
                if (name.isEmpty() || name == "." || name == "..") return@mapNotNull null
                if (!seen.add(rawPath)) return@mapNotNull null 
                val typeName = parts[1]
                val size = parts[2].toLongOrNull() ?: 0L
                val epochSeconds = parts[3].trim().toLongOrNull() ?: 0L
                
                val displayPath = fromRealPath(rawPath)
                val isDir = typeName.contains("directory")
                DFile(
                    name = name,
                    path = displayPath,
                    permission = "",
                    createdAt = null,
                    updatedAt = Instant.fromEpochMilliseconds(epochSeconds * 1000L),
                    size = if (isDir) 0L else size,
                    isDir = isDir,
                    children = 0,
                )
            }
    }
}
