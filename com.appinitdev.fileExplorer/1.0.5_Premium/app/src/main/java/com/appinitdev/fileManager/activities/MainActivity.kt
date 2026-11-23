class MainActivity : SimpleActivity() {
    companion object {
        private const val BACK_PRESS_TIMEOUT = 5000
        private const val PICKED_PATH = "picked_path"
    }
    private val binding by viewBinding(ActivityMainBinding::inflate)
    private var wasBackJustPressed = false
    private var mTabsToShow = ArrayList<Int>()
    private var mStoredFontSize = 0
    private var mStoredDateFormat = ""
    private var mStoredTimeFormat = ""
    private var mStoredShowTabs = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        appLaunched(BuildConfig.APPLICATION_ID)
        setupOptionsMenu()
        refreshMenuItems()
        mTabsToShow = getTabsList()
        if (!config.wasStorageAnalysisTabAdded) {
            config.wasStorageAnalysisTabAdded = true
            if (config.showTabs and TAB_STORAGE_ANALYSIS == 0) {
                config.showTabs += TAB_STORAGE_ANALYSIS
            }
        }
        storeStateVariables()
        setupTabs()
        updateMaterialActivityViews(binding.mainCoordinator, null, useTransparentNavigation = false, useTopSearchMenu = true)
        if (savedInstanceState == null) {
            config.temporarilyShowHidden = false
            initFragments()
            tryInitFileManager()
            checkWhatsNewDialog()
            checkIfRootAvailable()
            checkInvalidFavorites()
        }
        onBackPressedDispatcher.addCallback(this) {
            val currentFragment = getCurrentFragment()
            if (binding.mainMenu.isSearchOpen) {
                binding.mainMenu.closeSearch()
            } else if (currentFragment is RecentsFragment || currentFragment is StorageFragment) {
                finish() 
            } else if ((currentFragment as ItemsFragment).getBreadcrumbs().getItemCount() <= 1) {
                if (!wasBackJustPressed && config.pressBackTwice) {
                    wasBackJustPressed = true
                    toast(R.string.press_back_again)
                    Handler(Looper.getMainLooper()).postDelayed({
                        wasBackJustPressed = false
                    }, BACK_PRESS_TIMEOUT.toLong())
                } else {
                    appLockManager.lock()
                    finish()
                }
            } else {
                currentFragment.getBreadcrumbs().removeBreadcrumb()
                openPath(currentFragment.getBreadcrumbs().getLastItem().path)
            }
        }
    }
    override fun onResume() {
        super.onResume()
        if (mStoredShowTabs != config.showTabs) {
            config.lastUsedViewPagerPage = 0
            System.exit(0)
            return
        }
        refreshMenuItems()
        updateMenuColors()
        setupTabColors()
        getAllFragments().forEach {
            it?.onResume(getProperTextColor())
        }
        if (mStoredFontSize != config.fontSize) {
            getAllFragments().forEach {
                (it as? ItemOperationsListener)?.setupFontSize()
            }
        }
        if (mStoredDateFormat != config.dateFormat || mStoredTimeFormat != getTimeFormat()) {
            getAllFragments().forEach {
                (it as? ItemOperationsListener)?.setupDateTimeFormat()
            }
        }
        if (binding.mainViewPager.adapter == null) {
            initFragments()
        }
    }
    override fun onPause() {
        super.onPause()
        storeStateVariables()
        config.lastUsedViewPagerPage = binding.mainViewPager.currentItem
    }
    fun refreshMenuItems() {
        val currentFragment = getCurrentFragment() ?: return
        val isCreateDocumentIntent = intent.action == Intent.ACTION_CREATE_DOCUMENT
        val currentViewType = config.getFolderViewType(currentFragment.currentPath)
        val favorites = config.favorites
        binding.mainMenu.getToolbar().menu.apply {
            findItem(R.id.sort).isVisible = currentFragment is ItemsFragment
            findItem(R.id.change_view_type).isVisible = currentFragment !is StorageFragment
            findItem(R.id.add_favorite).isVisible = currentFragment is ItemsFragment && !favorites.contains(currentFragment.currentPath)
            findItem(R.id.remove_favorite).isVisible = currentFragment is ItemsFragment && favorites.contains(currentFragment.currentPath)
            findItem(R.id.go_to_favorite).isVisible = currentFragment is ItemsFragment && favorites.isNotEmpty()
            findItem(R.id.toggle_filename).isVisible = currentViewType == VIEW_TYPE_GRID && currentFragment !is StorageFragment
            findItem(R.id.go_home).isVisible = currentFragment is ItemsFragment && currentFragment.currentPath != config.homeFolder
            findItem(R.id.set_as_home).isVisible = currentFragment is ItemsFragment && currentFragment.currentPath != config.homeFolder
            findItem(R.id.temporarily_show_hidden).isVisible = !config.shouldShowHidden() && currentFragment !is StorageFragment
            findItem(R.id.stop_showing_hidden).isVisible = config.temporarilyShowHidden && currentFragment !is StorageFragment
            findItem(R.id.column_count).isVisible = currentViewType == VIEW_TYPE_GRID && currentFragment !is StorageFragment
            findItem(R.id.settings).isVisible = !isCreateDocumentIntent
            findItem(R.id.ftp_server).isVisible = !isCreateDocumentIntent
            findItem(R.id.http_server).isVisible = !isCreateDocumentIntent
        }
    }
    private fun setupOptionsMenu() {
        binding.mainMenu.apply {
            getToolbar().inflateMenu(R.menu.menu)
            toggleHideOnScroll(false)
            setupMenu()
            onSearchClosedListener = {
                getAllFragments().forEach {
                    it?.searchQueryChanged("")
                }
            }
            onSearchTextChangedListener = { text ->
                getCurrentFragment()?.searchQueryChanged(text)
            }
            getToolbar().setOnMenuItemClickListener { menuItem ->
                if (getCurrentFragment() == null) {
                    return@setOnMenuItemClickListener true
                }
                when (menuItem.itemId) {
                    R.id.go_home -> goHome()
                    R.id.go_to_favorite -> goToFavorite()
                    R.id.sort -> showSortingDialog()
                    R.id.add_favorite -> addFavorite()
                    R.id.remove_favorite -> removeFavorite()
                    R.id.toggle_filename -> toggleFilenameVisibility()
                    R.id.set_as_home -> setAsHome()
                    R.id.change_view_type -> changeViewType()
                    R.id.temporarily_show_hidden -> tryToggleTemporarilyShowHidden()
                    R.id.stop_showing_hidden -> tryToggleTemporarilyShowHidden()
                    R.id.column_count -> changeColumnCount()
                    R.id.settings -> launchSettings()
                    R.id.ftp_server -> launchFtpServer()
                    R.id.http_server -> launchHttpServer()
                    else -> return@setOnMenuItemClickListener false
                }
                return@setOnMenuItemClickListener true
            }
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(PICKED_PATH, getItemsFragment()?.currentPath ?: "")
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val path = savedInstanceState.getString(PICKED_PATH) ?: internalStoragePath
        if (binding.mainViewPager.adapter == null) {
            binding.mainViewPager.onGlobalLayout {
                openPath(path, true)
            }
        } else {
            openPath(path, true)
        }
    }
    private fun updateMenuColors() {
        updateStatusbarColor(getProperBackgroundColor())
        binding.mainMenu.updateColors()
    }
    private fun storeStateVariables() {
        config.apply {
            mStoredFontSize = fontSize
            mStoredDateFormat = dateFormat
            mStoredTimeFormat = context.getTimeFormat()
            mStoredShowTabs = showTabs
        }
    }
    private fun tryInitFileManager() {
        val hadPermission = hasStoragePermission()
        handleStoragePermission {
            checkOTGPath()
            if (it) {
                if (binding.mainViewPager.adapter == null) {
                    initFragments()
                }
                binding.mainViewPager.onGlobalLayout {
                    initFileManager(!hadPermission)
                }
            } else {
                toast(C.string.no_storage_permissions)
                finish()
            }
        }
    }
    private fun initFileManager(refreshRecents: Boolean) {
        if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
            val data = intent.data
            if (data?.scheme == "file") {
                openPath(data.path!!)
            } else {
                val path = getRealPathFromURI(data!!)
                if (path != null) {
                    openPath(path)
                } else {
                    openPath(config.homeFolder)
                }
            }
            if (!File(data.path!!).isDirectory) {
                tryOpenPathIntent(data.path!!, false, finishActivity = true)
            }
            binding.mainViewPager.currentItem = 0
        } else {
            openPath(config.homeFolder)
        }
        if (refreshRecents) {
            getRecentsFragment()?.refreshFragment()
        }
    }
    private fun initFragments() {
        binding.mainViewPager.apply {
            adapter = ViewPagerAdapter(this@MainActivity, mTabsToShow)
            offscreenPageLimit = 2
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {}
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
                override fun onPageSelected(position: Int) {
                    binding.mainTabsHolder.getTabAt(position)?.select()
                    getAllFragments().forEach {
                        (it as? ItemOperationsListener)?.finishActMode()
                    }
                    refreshMenuItems()
                }
            })
            currentItem = config.lastUsedViewPagerPage
            onGlobalLayout {
                refreshMenuItems()
            }
        }
    }
    private fun setupTabs() {
        binding.mainTabsHolder.removeAllTabs()
        val action = intent.action
        val isPickFileIntent = action == RingtoneManager.ACTION_RINGTONE_PICKER
                || action == Intent.ACTION_GET_CONTENT
                || action == Intent.ACTION_PICK
                || action == Intent.ACTION_OPEN_DOCUMENT
        val isCreateDocumentIntent = action == Intent.ACTION_CREATE_DOCUMENT
        if (isPickFileIntent) {
            mTabsToShow.remove(TAB_STORAGE_ANALYSIS)
            if (mTabsToShow.none { it and config.showTabs != 0 }) {
                config.showTabs = TAB_FILES
                mStoredShowTabs = TAB_FILES
                mTabsToShow = arrayListOf(TAB_FILES)
            }
        } else if (isCreateDocumentIntent) {
            mTabsToShow.clear()
            mTabsToShow = arrayListOf(TAB_FILES)
        }
        mTabsToShow.forEachIndexed { index, value ->
            if (config.showTabs and value != 0) {
                binding.mainTabsHolder.newTab().setCustomView(C.layout.bottom_tablayout_item).apply {
                    customView?.findViewById<ImageView>(C.id.tab_item_icon)?.setImageDrawable(getTabIcon(index))
                    customView?.findViewById<TextView>(C.id.tab_item_label)?.text = getTabLabel(index)
                    AutofitHelper.create(customView?.findViewById(C.id.tab_item_label))
                    binding.mainTabsHolder.addTab(this)
                }
            }
        }
        binding.mainTabsHolder.apply {
            onTabSelectionChanged(
                tabUnselectedAction = {
                    updateBottomTabItemColors(it.customView, false, getDeselectedTabDrawableIds()[it.position])
                },
                tabSelectedAction = {
                    binding.mainMenu.closeSearch()
                    binding.mainViewPager.currentItem = it.position
                    updateBottomTabItemColors(it.customView, true, getSelectedTabDrawableIds()[it.position])
                }
            )
            beGoneIf(tabCount == 1)
        }
    }
    private fun setupTabColors() {
        binding.apply {
            val activeView = mainTabsHolder.getTabAt(mainViewPager.currentItem)?.customView
            updateBottomTabItemColors(activeView, true, getSelectedTabDrawableIds()[mainViewPager.currentItem])
            getInactiveTabIndexes(mainViewPager.currentItem).forEach { index ->
                val inactiveView = mainTabsHolder.getTabAt(index)?.customView
                updateBottomTabItemColors(inactiveView, false, getDeselectedTabDrawableIds()[index])
            }
            val bottomBarColor = getBottomNavigationBackgroundColor()
            updateNavigationBarColor(bottomBarColor)
            mainTabsHolder.setBackgroundColor(bottomBarColor)
        }
    }
    private fun getTabIcon(position: Int): Drawable {
        val drawableId = when (position) {
            0 -> C.drawable.ic_folder_vector
            1 -> C.drawable.ic_clock_vector
            else -> R.drawable.ic_storage_vector
        }
        return resources.getColoredDrawableWithColor(drawableId, getProperTextColor())
    }
    private fun getTabLabel(position: Int): String {
        val stringId = when (position) {
            0 -> C.string.files_tab
            1 -> R.string.recents
            else -> C.string.storage
        }
        return resources.getString(stringId)
    }
    private fun checkOTGPath() {
        ensureBackgroundThread {
            if (!config.wasOTGHandled && hasPermission(PERMISSION_WRITE_STORAGE) && hasOTGConnected() && config.OTGPath.isEmpty()) {
                getStorageDirectories().firstOrNull { it.trimEnd('/') != internalStoragePath && it.trimEnd('/') != sdCardPath }?.apply {
                    config.wasOTGHandled = true
                    config.OTGPath = trimEnd('/')
                }
            }
        }
    }
    private fun openPath(path: String, forceRefresh: Boolean = false) {
        var newPath = path
        val file = File(path)
        if (config.OTGPath.isNotEmpty() && config.OTGPath == path.trimEnd('/')) {
            newPath = path
        } else if (file.exists() && !file.isDirectory) {
            newPath = file.parent
        } else if (!file.exists() && !isPathOnOTG(newPath)) {
            newPath = internalStoragePath
        }
        getItemsFragment()?.openPath(newPath, forceRefresh)
    }
    private fun goHome() {
        if (config.homeFolder != getCurrentFragment()!!.currentPath) {
            openPath(config.homeFolder)
        }
    }
    private fun showSortingDialog() {
        ChangeSortingDialog(this, getCurrentFragment()!!.currentPath) {
            (getCurrentFragment() as? ItemsFragment)?.refreshFragment()
        }
    }
    private fun addFavorite() {
        config.addFavorite(getCurrentFragment()!!.currentPath)
        refreshMenuItems()
    }
    private fun removeFavorite() {
        config.removeFavorite(getCurrentFragment()!!.currentPath)
        refreshMenuItems()
    }
    private fun toggleFilenameVisibility() {
        config.displayFilenames = !config.displayFilenames
        getAllFragments().forEach {
            (it as? ItemOperationsListener)?.toggleFilenameVisibility()
        }
    }
    private fun changeColumnCount() {
        val items = ArrayList<RadioItem>()
        for (i in 1..MAX_COLUMN_COUNT) {
            items.add(RadioItem(i, resources.getQuantityString(C.plurals.column_counts, i, i)))
        }
        val currentColumnCount = config.fileColumnCnt
        RadioGroupDialog(this, items, config.fileColumnCnt) {
            val newColumnCount = it as Int
            if (currentColumnCount != newColumnCount) {
                config.fileColumnCnt = newColumnCount
                getAllFragments().forEach {
                    (it as? ItemOperationsListener)?.columnCountChanged()
                }
            }
        }
    }
    fun updateFragmentColumnCounts() {
        getAllFragments().forEach {
            (it as? ItemOperationsListener)?.columnCountChanged()
        }
    }
    private fun goToFavorite() {
        val favorites = config.favorites
        val items = ArrayList<RadioItem>(favorites.size)
        var currFavoriteIndex = -1
        favorites.forEachIndexed { index, path ->
            val visiblePath = humanizePath(path).replace("/", " / ")
            items.add(RadioItem(index, visiblePath, path))
            if (path == getCurrentFragment()!!.currentPath) {
                currFavoriteIndex = index
            }
        }
        RadioGroupDialog(this, items, currFavoriteIndex, R.string.go_to_favorite) {
            openPath(it.toString())
        }
    }
    private fun setAsHome() {
        config.homeFolder = getCurrentFragment()!!.currentPath
        toast(R.string.home_folder_updated)
    }
    private fun changeViewType() {
        ChangeViewTypeDialog(this, getCurrentFragment()!!.currentPath, getCurrentFragment() is ItemsFragment) {
            getAllFragments().forEach {
                it?.refreshFragment()
            }
        }
    }
    private fun tryToggleTemporarilyShowHidden() {
        if (config.temporarilyShowHidden) {
            toggleTemporarilyShowHidden(false)
        } else {
            handleHiddenFolderPasswordProtection {
                toggleTemporarilyShowHidden(true)
            }
        }
    }
    private fun toggleTemporarilyShowHidden(show: Boolean) {
        config.temporarilyShowHidden = show
        getAllFragments().forEach {
            it?.refreshFragment()
        }
    }
    private fun launchSettings() {
        hideKeyboard()
        startActivity(Intent(applicationContext, SettingsActivity::class.java))
    }
    private fun launchFtpServer() {
        startActivity(Intent(this, FtpServerActivity::class.java))
    }
    private fun launchHttpServer() {
        startActivity(Intent(this, HttpServerActivity::class.java))
    }
    private fun launchAbout() {
        val licenses = LICENSE_GLIDE or LICENSE_PATTERN or LICENSE_REPRINT or LICENSE_GESTURE_VIEWS or LICENSE_AUTOFITTEXTVIEW or LICENSE_ZIP4J
        val faqItems = arrayListOf(
            FAQItem(C.string.faq_3_title_commons, C.string.faq_3_text_commons),
            FAQItem(C.string.faq_9_title_commons, C.string.faq_9_text_commons)
        )
    }
    private fun checkIfRootAvailable() {
        ensureBackgroundThread {
            config.isRootAvailable = RootTools.isRootAvailable()
            if (config.isRootAvailable && config.enableRootAccess) {
                RootHelpers(this).askRootIfNeeded {
                    config.enableRootAccess = it
                }
            }
        }
    }
    private fun checkInvalidFavorites() {
        ensureBackgroundThread {
            config.favorites.forEach {
                if (!isPathOnOTG(it) && !isPathOnSD(it) && !File(it).exists()) {
                    config.removeFavorite(it)
                }
            }
        }
    }
    fun pickedPath(path: String) {
        val resultIntent = Intent()
        val uri = getFilePublicUri(File(path), BuildConfig.APPLICATION_ID)
        val type = path.getMimeType()
        resultIntent.setDataAndType(uri, type)
        resultIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
    fun createDocumentConfirmed(path: String) {
        val filename = intent.getStringExtra(Intent.EXTRA_TITLE) ?: ""
        if (filename.isEmpty()) {
            InsertFilenameDialog(this, internalStoragePath) { newFilename ->
                finishCreateDocumentIntent(path, newFilename)
            }
        } else {
            finishCreateDocumentIntent(path, filename)
        }
    }
    private fun finishCreateDocumentIntent(path: String, filename: String) {
        val resultIntent = Intent()
        val uri = getFilePublicUri(File(path, filename), BuildConfig.APPLICATION_ID)
        val type = path.getMimeType()
        resultIntent.setDataAndType(uri, type)
        resultIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
    fun pickedRingtone(path: String) {
        val uri = getFilePublicUri(File(path), BuildConfig.APPLICATION_ID)
        val type = path.getMimeType()
        Intent().apply {
            setDataAndType(uri, type)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            putExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, uri)
            setResult(Activity.RESULT_OK, this)
        }
        finish()
    }
    fun pickedPaths(paths: ArrayList<String>) {
        val newPaths = paths.map { getFilePublicUri(File(it), BuildConfig.APPLICATION_ID) } as ArrayList
        val clipData = ClipData("Attachment", arrayOf(paths.getMimeType()), ClipData.Item(newPaths.removeAt(0)))
        newPaths.forEach {
            clipData.addItem(ClipData.Item(it))
        }
        Intent().apply {
            this.clipData = clipData
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            setResult(Activity.RESULT_OK, this)
        }
        finish()
    }
    fun openedDirectory() {
        if (binding.mainMenu.isSearchOpen) {
            binding.mainMenu.closeSearch()
        }
    }
    private fun getInactiveTabIndexes(activeIndex: Int) = (0 until binding.mainTabsHolder.tabCount).filter { it != activeIndex }
    private fun getSelectedTabDrawableIds(): ArrayList<Int> {
        val showTabs = config.showTabs
        val icons = ArrayList<Int>()
        if (showTabs and TAB_FILES != 0) {
            icons.add(C.drawable.ic_folder_vector)
        }
        if (showTabs and TAB_RECENT_FILES != 0) {
            icons.add(C.drawable.ic_clock_filled_vector)
        }
        if (showTabs and TAB_STORAGE_ANALYSIS != 0) {
            icons.add(R.drawable.ic_storage_vector)
        }
        return icons
    }
    private fun getDeselectedTabDrawableIds(): ArrayList<Int> {
        val showTabs = config.showTabs
        val icons = ArrayList<Int>()
        if (showTabs and TAB_FILES != 0) {
            icons.add(C.drawable.ic_folder_outline_vector)
        }
        if (showTabs and TAB_RECENT_FILES != 0) {
            icons.add(C.drawable.ic_clock_vector)
        }
        if (showTabs and TAB_STORAGE_ANALYSIS != 0) {
            icons.add(R.drawable.ic_storage_vector)
        }
        return icons
    }
    private fun getRecentsFragment() = findViewById<RecentsFragment>(R.id.recents_fragment)
    private fun getItemsFragment() = findViewById<ItemsFragment>(R.id.items_fragment)
    private fun getStorageFragment() = findViewById<StorageFragment>(R.id.storage_fragment)
    private fun getAllFragments(): ArrayList<MyViewPagerFragment<*>?> =
        arrayListOf(getItemsFragment(), getRecentsFragment(), getStorageFragment())
    private fun getCurrentFragment(): MyViewPagerFragment<*>? {
        val showTabs = config.showTabs
        val fragments = arrayListOf<MyViewPagerFragment<*>>()
        if (showTabs and TAB_FILES != 0) {
            fragments.add(getItemsFragment())
        }
        if (showTabs and TAB_RECENT_FILES != 0) {
            fragments.add(getRecentsFragment())
        }
        if (showTabs and TAB_STORAGE_ANALYSIS != 0) {
            fragments.add(getStorageFragment())
        }
        return fragments.getOrNull(binding.mainViewPager.currentItem)
    }
    private fun getTabsList() = arrayListOf(TAB_FILES, TAB_RECENT_FILES, TAB_STORAGE_ANALYSIS)
    private fun checkWhatsNewDialog() {
        arrayListOf<Release>().apply {
            checkWhatsNew(this, BuildConfig.VERSION_CODE)
        }
    }
}
