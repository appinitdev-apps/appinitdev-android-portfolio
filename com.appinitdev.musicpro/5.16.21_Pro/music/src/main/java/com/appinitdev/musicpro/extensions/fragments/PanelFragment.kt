
open class PanelFragment : MediaFragment() {

    protected val isLandscape: Boolean by lazy {
        BarHeight.isLandscape(requireContext())
    }

    protected fun RecyclerView.requireAttachedSectionScroller(
            sections: List<SectionedFastScroller.Position>,
            header: AppHeader,
            view: View) {
        val sectionedFastScroller = SectionedFastScroller.attach(this)
        sectionedFastScroller.setPositions(sections)
        sectionedFastScroller.setOnPositionSelectedListener { position ->
            val layoutManager = this.layoutManager as? GridLayoutManager ?: return@setOnPositionSelectedListener
            layoutManager.scrollToPositionWithOffset(
                      position.index,
                      resources.getDimensionPixelSize(R.dimen.padding_8) + header.height + paddingTop)

            if (position.index > 10) {
                header.hideHeader()
            } else {
                header.showHeader()
            }

            header.resumeAutoBehavior()

            post {
                val currentSectionIndex = sections.indexOf(position)
                val nextIndex = if (currentSectionIndex + 1 < sections.size) {
                    sections[currentSectionIndex + 1].index
                } else {
                    sections.last().index
                }

                for (i in position.index until nextIndex) {
                    try {
                        val itemView = layoutManager.findViewByPosition(i)
                        itemView?.let { view ->
                            val blink = AlphaAnimation(1f, 0f).apply {
                                duration = 300
                                repeatCount = 3 
                                repeatMode = Animation.REVERSE
                            }
                            view.startAnimation(blink)
                        }
                    } catch (_: IndexOutOfBoundsException) {
                        
                    }
                }
            }
        }

        sectionedFastScroller.setVisibilityListener(object : SectionedFastScroller.VisibilityListener {
            override fun onShowStart() {
                super.onShowStart()
                hideMiniPlayer()
            }

            override fun onHideStart() {
                super.onHideStart()
                showMiniPlayer()
            }
        })

        view.setOnClickListener {
            sectionedFastScroller.show(animated = true)
        }
    }

    fun AppCompatTextView.setGridTypeValue(gridType: Int) {
        when (gridType) {
            CommonPreferencesConstants.GRID_TYPE_LIST -> {
                text = getString(R.string.list)
                setStartDrawable(R.drawable.ic_list_16dp)
            }
            CommonPreferencesConstants.GRID_TYPE_GRID -> {
                text = getString(R.string.grid)
                setStartDrawable(R.drawable.ic_grid_16dp)
            }
            else -> {
                text = getString(R.string.list) 
                setStartDrawable(R.drawable.ic_list_16dp)
            }
        }
    }

    protected fun AppCompatTextView.setAppLabel() {
        this.text = MainPreferences.getAppLabel(requireContext())
    }

    protected fun openSearch() {
        openFragment(Search.newInstance(), Search.TAG)
    }

    protected fun View.hideOnUnfavorableSort(sorts: List<Int>, preference: Int) {
        if (sorts.contains(preference)) {
            visible(animate = true)
        } else {
            gone(animate = true)
        }
    }

     


    protected fun RecyclerView.attachSlideFastScroller() {
        SlideFastScroller.attach(this).apply {
            setFadeToIdleMode(enabled = BehaviourPreferences.getFastScrollBehavior() == BehaviourPreferences.FADE_FAST_SCROLLBAR)
            setEnabledWhileEmpty(enable = false)
            setIdleAlpha(0.4F)
        }
    }
}