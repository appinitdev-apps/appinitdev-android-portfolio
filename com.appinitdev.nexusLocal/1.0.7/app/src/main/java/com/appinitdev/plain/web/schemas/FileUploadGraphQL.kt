
fun SchemaBuilder.addFileUploadSchema() {
    val uploadTmpDir = File(MainApp.instance.filesDir, "upload_tmp")

    query("uploadedChunks") {
        resolver { fileId: String ->
            val chunkDir = File(uploadTmpDir, fileId)
            if (!chunkDir.exists()) return@resolver emptyList<String>()

            chunkDir.listFiles()
                ?.filter { it.name.startsWith("chunk_") } 
                ?.mapNotNull { file ->
                    val index = file.name.removePrefix("chunk_").toIntOrNull()
                    if (index != null) "${index}:${file.length()}" else null
                }
                ?.sortedBy { it.substringBefore(':').toInt() }
                ?: emptyList()
        }
    }
    mutation("deleteChunks") {
        resolver { fileId: String ->
            val chunkDir = File(uploadTmpDir, fileId)
            if (chunkDir.exists()) {
                chunkDir.deleteRecursively()
            }
            true
        }
    }
    mutation("mergeChunks") {
        resolver { fileId: String, totalChunks: Int, path: String, replace: Boolean, isAppFile: Boolean ->
            val chunkDir = File(uploadTmpDir, fileId)
            if (!chunkDir.exists()) {
                throw GraphQLError("No chunks found for $fileId")
            }

            
            var expectedMergedSize = 0L
            for (i in 0 until totalChunks) {
                val chunkFile = File(chunkDir, "chunk_$i")
                if (!chunkFile.exists()) {
                    throw GraphQLError("Missing chunk $i")
                }
                expectedMergedSize += chunkFile.length()
            }

            val outputFile = if (replace) {
                File(path)
            } else {
                val originalFile = File(path)
                if (originalFile.exists()) {
                    File(originalFile.newPath())
                } else {
                    originalFile
                }
            }
            outputFile.parentFile?.mkdirs()

            
            
            val tempMergeFile = File(outputFile.parentFile, ".merge_tmp_${fileId}_${System.currentTimeMillis()}")
            try {
                FileOutputStream(tempMergeFile).use { fos ->
                    val outputChannel = fos.channel
                    for (i in 0 until totalChunks) {
                        val chunkFile = File(chunkDir, "chunk_$i")

                        chunkFile.inputStream().channel.use { inputChannel ->
                            var position = 0L
                            val size = inputChannel.size()
                            while (position < size) {
                                val transferred = inputChannel.transferTo(position, size - position, outputChannel)
                                if (transferred < 0) throw IOException("transferTo failed at position $position")
                                if (transferred == 0L) {
                                    
                                    Thread.sleep(1)
                                    continue
                                }
                                position += transferred
                            }
                        }
                    }
                    
                    fos.fd.sync()
                }

                val mergedSize = tempMergeFile.length()

                
                if (mergedSize != expectedMergedSize) {
                    tempMergeFile.delete()
                    throw GraphQLError("Merge integrity failed: expected $expectedMergedSize, got $mergedSize")
                }

                
                if (outputFile.exists() && replace) {
                    outputFile.delete()
                }
                if (!tempMergeFile.renameTo(outputFile)) {
                    
                    
                    if (tempMergeFile.exists()) {
                        tempMergeFile.copyTo(outputFile, overwrite = true)
                        tempMergeFile.delete()
                    } else if (!outputFile.exists()) {
                        throw GraphQLError("Failed to save merged file: rename failed and source file is missing")
                    }
                }
            } catch (e: Exception) {
                tempMergeFile.delete()
                throw e
            }

            val mergedSize = outputFile.length()

            chunkDir.deleteRecursively()
            if (isAppFile) {
                
                val dFile = AppFileStore.importFile(MainApp.instance, outputFile, "", deleteSrc = true)
                val fidSuffix = java.io.File(dFile.realPath).name  
                "${fidSuffix}:$mergedSize"
            } else {
                MainApp.instance.scanFileByConnection(outputFile, null)
                
                "${outputFile.name}:$mergedSize"
            }
        }
    }
}
