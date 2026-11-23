fun SchemaBuilder.addPackageSchema() {
    query("packages") {
        resolver { offset: Int, limit: Int, query: String, sortBy: FileSortBy ->
            Permissions.checkAsync(MainApp.instance, setOf(Permission.QUERY_ALL_PACKAGES))
            PackageHelper.searchAsync(query, limit, offset, sortBy).map { it.toModel() }
        }
    }
    query("packageStatuses") {
        resolver { ids: List<ID> ->
            Permissions.checkAsync(MainApp.instance, setOf(Permission.QUERY_ALL_PACKAGES))
            PackageHelper.getPackageInfoMap(ids.map { it.value }).map {
                val pkg = it.value
                val updatedAt = if (pkg != null) Instant.fromEpochMilliseconds(pkg.lastUpdateTime) else null
                PackageStatus(ID(it.key), pkg != null, updatedAt)
            }
        }
    }
    query("packageCount") {
        resolver { query: String ->
            if (Permission.QUERY_ALL_PACKAGES.enabledAndCanAsync(MainApp.instance)) {
                PackageHelper.count(query)
            } else {
                0
            }
        }
    }
    mutation("uninstallPackages") {
        resolver { ids: List<ID> ->
            Permissions.checkAsync(MainApp.instance, setOf(Permission.QUERY_ALL_PACKAGES))
            ids.forEach {
                PackageHelper.uninstall(MainActivity.instance.get()!!, it.value)
            }
            true
        }
    }
    mutation("installPackage") {
        resolver { path: String ->
            Permissions.checkAsync(MainApp.instance, setOf(Permission.QUERY_ALL_PACKAGES))
            val file = File(path)
            if (!file.exists()) {
                throw GraphQLError("File does not exist")
            }
            try {
                val context = MainActivity.instance.get()!!
                if (file.name.endsWith(".apk", ignoreCase = true)) {
                    LogCat.d("Installing APK file: ${file.name}")
                    val apkMeta = ApkParsers.getMetaInfo(file)
                        ?: throw GraphQLError("Failed to parse APK package ID")
                    PackageHelper.install(context, file)
                    val packageName = apkMeta.packageName ?: ""
                    try {
                        val pkg = packageManager.getPackageInfo(packageName, 0)
                        PackageInstallPending(packageName, Instant.fromEpochMilliseconds(pkg.lastUpdateTime), isNew = false)
                    } catch (e: Exception) {
                        PackageInstallPending(packageName, null, isNew = true)
                    }
                } else {
                    throw GraphQLError("Unsupported file format. Only APK files are supported.")
                }
            } catch (e: Exception) {
                LogCat.e("Installation failed: ${e.message}", e)
                throw GraphQLError("Installation failed: ${e.message}")
            }
        }
    }
}
