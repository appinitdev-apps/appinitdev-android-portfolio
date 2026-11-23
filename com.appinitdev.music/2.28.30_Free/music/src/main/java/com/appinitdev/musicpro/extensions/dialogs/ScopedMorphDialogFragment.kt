
 



















abstract class ScopedMorphDialogFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {

     




    var anchorView: View? = null

    private var dialogContentView: View? = null
    private var scrimView: View? = null
    private var isDismissing = false

    private var scaleXAnimation: SpringAnimation? = null
    private var scaleYAnimation: SpringAnimation? = null
    private var translationXAnimation: SpringAnimation? = null
    private var translationYAnimation: SpringAnimation? = null

    companion object {
        private const val TRANSITION_NAME = "morph_dialog_transition"
        private const val DURATION = 350L
        private const val END_ELEVATION = 0f
        private const val MAX_WIGGLE_THRESHOLD = 72f
        private const val MAX_FINGER_DISTANCE = 0.05f
        private val INTERPOLATOR = DecelerateInterpolator(3F)
    }

     













    abstract fun onCreateDialogContentView(inflater: LayoutInflater, container: ViewGroup): View

     







    open fun onDialogViewCreated(view: View, savedInstanceState: Bundle?) {}

     




    final override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val root = FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val scrim = View(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor("#80000000".toColorInt())
            alpha = 0f
            isClickable = true
            setOnClickListener { dismiss() }
        }
        scrimView = scrim
        root.addView(scrim)

        val content = onCreateDialogContentView(inflater, root)
        dialogContentView = content

        val displayMetrics = requireContext().resources.displayMetrics
        val cardWidth = (displayMetrics.widthPixels * 0.85f).toInt()
        val margin = (16 * displayMetrics.density).toInt()

        content.layoutParams = FrameLayout.LayoutParams(
                cardWidth,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        ).apply {
            setMargins(0, margin, 0, margin)
        }

        ViewCompat.setTransitionName(content, TRANSITION_NAME)
        content.visibility = View.INVISIBLE

        @SuppressLint("ClickableViewAccessibility")
        content.setOnTouchListener(createWiggleTouchListener())

        root.addView(content)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireHiddenMiniPlayer()
        setupBackPressCallback()
        onDialogViewCreated(view, savedInstanceState)

        val card = dialogContentView ?: return
        val anchor = anchorView

        
        scrimView?.animate()
            ?.alpha(1f)
            ?.setInterpolator(INTERPOLATOR)
            ?.setDuration(DURATION)
            ?.start()

        if (anchor == null || !anchor.isAttachedToWindow) {
            
            card.visibility = View.VISIBLE
            return
        }

        ViewCompat.setTransitionName(anchor, TRANSITION_NAME)
        val appContainer: ViewGroup = requireActivity().findViewById(R.id.app_container)

        
        
        card.doOnLayout {
            val transform = MaterialContainerTransform().apply {
                startView = anchor
                endView = card
                addTarget(card)
                duration = DURATION
                scrimColor = Color.TRANSPARENT
                containerColor = Color.TRANSPARENT
                fadeMode = MaterialContainerTransform.FADE_MODE_CROSS
                startElevation = END_ELEVATION
                endElevation = END_ELEVATION
                interpolator = INTERPOLATOR
            }

            
            
            TransitionManager.beginDelayedTransition(appContainer, transform)
            anchor.visibility = View.INVISIBLE
            card.visibility = View.VISIBLE
        }
    }

     



    fun dismiss() {
        if (isDismissing) return
        isDismissing = true

        val anchor = anchorView
        val card = dialogContentView

        if (anchor == null || card == null || !isAdded || !anchor.isAttachedToWindow) {
            simplyDismiss()
            return
        }

        val appContainer: ViewGroup = requireActivity().findViewById(R.id.app_container)

        val reverseTransform = MaterialContainerTransform().apply {
            startView = card
            endView = anchor
            addTarget(card)
            duration = DURATION
            scrimColor = Color.TRANSPARENT
            containerColor = Color.TRANSPARENT
            fadeMode = MaterialContainerTransform.FADE_MODE_CROSS
            startElevation = END_ELEVATION
            endElevation = END_ELEVATION
            interpolator = INTERPOLATOR
        }

        reverseTransform.addListener(object : Transition.TransitionListener {
            override fun onTransitionEnd(transition: Transition) {
                anchor.visibility = View.VISIBLE
                removeFromBackStack()
                reverseTransform.removeListener(this)
                isDismissing = false
            }

            override fun onTransitionStart(t: Transition) {
                scrimView?.animate()
                    ?.alpha(0f)
                    ?.setDuration(DURATION / 2)
                    ?.setInterpolator(INTERPOLATOR)
                    ?.start()
            }

            override fun onTransitionCancel(t: Transition) {
                anchor.visibility = View.VISIBLE
                removeFromBackStack()
                reverseTransform.removeListener(this)
                isDismissing = false
            }

            override fun onTransitionPause(t: Transition) {}
            override fun onTransitionResume(t: Transition) {}
        })

        
        
        TransitionManager.beginDelayedTransition(appContainer, reverseTransform)
        card.visibility = View.INVISIBLE
        anchor.visibility = View.VISIBLE
    }

     



    fun simplyDismiss() {
        isDismissing = false
        anchorView?.visibility = View.VISIBLE
        removeFromBackStack()
    }

     







    fun show(fragmentManager: FragmentManager, tag: String) {
        if (fragmentManager.findFragmentByTag(tag) == null) {
            fragmentManager.beginTransaction()
                .add(R.id.app_container, this, tag)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        
        
        anchorView?.let { anchor ->
            if (anchor.isInvisible) {
                anchor.visibility = View.VISIBLE
            }
        }
        dialogContentView = null
        scrimView = null
    }

    override fun onResume() {
        super.onResume()
        registerSharedPreferenceChangeListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterSharedPreferenceChangeListener()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {}

    private fun removeFromBackStack() {
        if (isAdded) {
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commitAllowingStateLoss()
        }
    }

    private fun setupBackPressCallback() {
        requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (!isDismissing) dismiss()
                    }
                }
        )
    }

    private fun requireHiddenMiniPlayer() {
        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                (requireActivity() as MainActivity).onHideMiniPlayer()
            }

            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                if (!requireActivity().isChangingConfigurations) {
                    (requireActivity() as MainActivity).onShowMiniPlayer()
                }
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createWiggleTouchListener(): View.OnTouchListener {
        var initialX = 0f
        var initialY = 0f
        var isInitialTouch = true

        return View.OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    if (isInitialTouch) {
                        initialX = event.rawX
                        initialY = event.rawY
                        isInitialTouch = false
                        translationXAnimation?.cancel()
                        translationYAnimation?.cancel()
                        scaleXAnimation?.cancel()
                        scaleYAnimation?.cancel()
                    }

                    val dx = event.rawX - initialX
                    val dy = event.rawY - initialY
                    val dampX = dx * MAX_FINGER_DISTANCE
                    val dampY = dy * MAX_FINGER_DISTANCE
                    val nx = (abs(dampX) / MAX_WIGGLE_THRESHOLD).coerceAtMost(1f)
                    val ny = (abs(dampY) / MAX_WIGGLE_THRESHOLD).coerceAtMost(1f)
                    val easedX = easeOutDecay(nx) * MAX_WIGGLE_THRESHOLD * sign(dampX)
                    val easedY = easeOutDecay(ny) * MAX_WIGGLE_THRESHOLD * sign(dampY)

                    v.translationX = easedX
                    v.translationY = easedY

                    val intensity = max(nx, ny)
                    val minScale = 0.85f
                    val easedScaleFactor = 1f - (easeOutDecay(intensity) * (1f - minScale))
                    v.scaleX = easedScaleFactor
                    v.scaleY = easedScaleFactor
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    translationXAnimation = startSpringAnimation(v, SpringAnimation.TRANSLATION_X, 0f, v.translationX)
                    translationYAnimation = startSpringAnimation(v, SpringAnimation.TRANSLATION_Y, 0f, v.translationY)
                    scaleXAnimation = startSpringAnimation(v, SpringAnimation.SCALE_X, 1f, v.scaleX)
                    scaleYAnimation = startSpringAnimation(v, SpringAnimation.SCALE_Y, 1f, v.scaleY)
                    isInitialTouch = true
                }
            }
            false
        }
    }

    private fun startSpringAnimation(
            view: View,
            property: FloatPropertyCompat<View>,
            finalPosition: Float,
            startValue: Float
    ): SpringAnimation {
        return SpringAnimation(view, property).apply {
            spring = SpringForce(finalPosition).apply {
                stiffness = SpringForce.STIFFNESS_VERY_LOW
                dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
            }
            setStartValue(startValue)
            start()
        }
    }

    private fun easeOutDecay(normalized: Float): Float {
        return 1f - (1f - normalized).pow(5)
    }
}

