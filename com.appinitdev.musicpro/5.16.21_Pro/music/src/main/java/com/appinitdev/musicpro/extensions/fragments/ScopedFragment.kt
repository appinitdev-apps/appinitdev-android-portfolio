abstract class ScopedFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFragmentTransition()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (BehaviourPreferences.isPredictiveBackEnabled()) {
            setupPredictiveBackObserver()
        }
    }
    override fun onResume() {
        super.onResume()
        clearTransitions()
        applyFragmentTransition()
        registerSharedPreferenceChangeListener()
    }
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        unregisterSharedPreferenceChangeListener()
    }
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            BehaviourPreferences.FRAGMENT_TRANSITION -> {
                clearTransitions()
                applyFragmentTransition()
            }
        }
    }
    fun openFragment(fragment: ScopedFragment, tag: String? = null) {
        val nextTransitionType = fragment.getTransitionType()
        when (nextTransitionType) {
            TransitionType.SHARED_AXIS -> setTransitions()
            TransitionType.SLIDE -> setSlideTransitions()
            TransitionType.DRIFT -> setDriftTransitions()
        }
        fragment.applyFragmentTransition()
        try {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.setReorderingAllowed(true)
            transaction.replace(R.id.fragment_container, fragment, tag)
            if (tag.isNotNull()) {
                transaction.addToBackStack(tag)
            }
            transaction.commit()
        } catch (e: IllegalStateException) {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.setReorderingAllowed(true)
            transaction.replace(R.id.fragment_container, fragment, tag)
            if (tag.isNotNull()) {
                transaction.addToBackStack(tag)
            }
            transaction.commitAllowingStateLoss()
        }
    }
    internal fun clearExitTransition() {
        exitTransition = null
    }
    private fun clearEnterTransition() {
        enterTransition = null
    }
    internal fun clearReEnterTransition() {
        reenterTransition = null
    }
    internal fun openPreferencesPanel() {
        openFragment(Preferences.newInstance(), Preferences.TAG)
    }
    fun clearTransitions() {
        clearEnterTransition()
        clearExitTransition()
        clearReEnterTransition()
    }
    open fun setTransitions() {
        when (BehaviourPreferences.getFragmentTransition()) {
            BehaviourPreferences.TRANSITION_X -> {
                enterTransition = SeekableSharedAxisXTransition(true)
                exitTransition = SeekableSharedAxisXTransition(true)
                reenterTransition = SeekableSharedAxisXTransition(false)
                returnTransition = SeekableSharedAxisXTransition(false)
            }
            BehaviourPreferences.TRANSITION_FADE -> {
                enterTransition = SeekableSharedAxisFadeTransition(true)
                exitTransition = SeekableSharedAxisFadeTransition(true)
                reenterTransition = SeekableSharedAxisFadeTransition(false)
                returnTransition = SeekableSharedAxisFadeTransition(false)
            }
            else -> {
                enterTransition = SeekableSharedAxisZTransition(true)
                exitTransition = SeekableSharedAxisZTransition(true)
                reenterTransition = SeekableSharedAxisZTransition(false)
                returnTransition = SeekableSharedAxisZTransition(false)
            }
        }
    }
    open fun setSlideTransitions() {
        clearTransitions()
        enterTransition = SeekableSlideTransition(true)
        exitTransition = SeekableSlideTransition(true)
        reenterTransition = SeekableSlideTransition(false)
        returnTransition = SeekableSlideTransition(false)
    }
    open fun setDriftTransitions() {
        clearTransitions()
        enterTransition = SeekableSharedAxisXTransition(true)
        exitTransition = SeekableSharedAxisXTransition(true)
        reenterTransition = SeekableSharedAxisXTransition(false)
        returnTransition = SeekableSharedAxisXTransition(false)
    }
    protected fun getInteger(@IntegerRes resId: Int): Int {
        return resources.getInteger(resId)
    }
    @Suppress("unused", "UNUSED_VARIABLE")
    @RequiresApi(Build.VERSION_CODES.R)
    protected fun View.setKeyboardChangeListener() {
        val cb = object : WindowInsetsAnimation.Callback(DISPATCH_MODE_STOP) {
            var startBottom = 0
            var endBottom = 0
            override fun onPrepare(animation: WindowInsetsAnimation) {
            }
            override fun onStart(animation: WindowInsetsAnimation, bounds: WindowInsetsAnimation.Bounds): WindowInsetsAnimation.Bounds {
                return bounds
            }
            override fun onProgress(insets: WindowInsets, runningAnimations: List<WindowInsetsAnimation>): WindowInsets {
                return insets
            }
            override fun onEnd(animation: WindowInsetsAnimation) {
            }
        }
    }
    open fun showWarning(warning: String) {
        requireActivity().supportFragmentManager.showWarning(warning)
    }
    open fun showWarning(@StringRes warning: Int) {
        requireActivity().supportFragmentManager.showWarning(warning)
    }
    private fun setupPredictiveBackObserver() {
        val backStackListener = object : FragmentManager.OnBackStackChangedListener {
            override fun onBackStackChanged() {
            }
            override fun onBackStackChangeStarted(fragment: Fragment, pop: Boolean) {
                if (pop && fragment === this@ScopedFragment) {
                    onStartPredictiveBack()
                }
            }
            override fun onBackStackChangeProgressed(backEvent: BackEventCompat) {
                onProgressPredictiveBack(backEvent.progress)
            }
            override fun onBackStackChangeCancelled() {
                onCancelPredictiveBack()
            }
            override fun onBackStackChangeCommitted(fragment: Fragment, pop: Boolean) {
                if (pop && fragment === this@ScopedFragment) {
                    onConfirmPredictiveBack()
                }
            }
        }
        requireActivity().supportFragmentManager.addOnBackStackChangedListener(backStackListener)
        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                requireActivity().supportFragmentManager
                    .removeOnBackStackChangedListener(backStackListener)
            }
        })
    }
    protected fun startPostViewTransition(view: View) {
        (view.parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }
    protected fun startPostViewTransition(view: View, onPreDraw: () -> Unit) {
        (view.parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
            onPreDraw()
        }
    }
    protected fun postDelayed(delayMillis: Long = 500L, action: () -> Unit) {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed(action, delayMillis)
    }
    protected fun goBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }
    protected fun popBackStack() {
        requireActivity().supportFragmentManager.popBackStack()
    }
    open fun onStartPredictiveBack() {
    }
    open fun onProgressPredictiveBack(progress: Float) {
    }
    open fun onCancelPredictiveBack() {
    }
    open fun onConfirmPredictiveBack() {
    }
    protected fun startTransitionOnPreDraw(view: View, onPreDraw: () -> Unit) {
        (view.parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
            onPreDraw()
        }
    }
    protected fun View.startTransitionOnPreDraw() {
        (parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }
    protected fun requireContainerView(): ViewGroup {
        return requireActivity().findViewById(R.id.app_container)
    }
    protected fun requireContentResolver(): ContentResolver {
        return requireActivity().contentResolver
    }
    protected fun requireLightBarIcons() {
        ThemeUtils.setDarkBars(
                lifecycleOwner = viewLifecycleOwner,
                window = requireActivity().window,
                resources = requireContext().resources)
    }
    protected fun requireDarkBarIcons() {
        ThemeUtils.setLightBars(
                lifecycleOwner = viewLifecycleOwner,
                window = requireActivity().window,
                resources = requireContext().resources)
    }
    protected fun requireImmersiveMode() {
        if (!UserInterfacePreferences.isImmersiveMode()) return
        val window = requireActivity().window
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            override fun onDestroy(owner: LifecycleOwner) {
                controller.show(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            }
        })
    }
    protected open fun getTransitionType(): TransitionType = TransitionType.SHARED_AXIS
    protected fun applyFragmentTransition() {
        when (getTransitionType()) {
            TransitionType.SHARED_AXIS -> setTransitions()
            TransitionType.SLIDE -> setSlideTransitions()
            TransitionType.DRIFT -> setDriftTransitions()
        }
    }
    protected fun getDialogWidthRation(): Float {
        return if (BarHeight.isLandscape(requireContext())) {
            0.5F
        } else {
            SharedImageDialogMenu.DEFAULT_WIDTH_RATIO
        }
    }
    enum class TransitionType {
        SHARED_AXIS, SLIDE, DRIFT
    }
    companion object {
        private const val TAG = "ScopedFragment"
    }
}
