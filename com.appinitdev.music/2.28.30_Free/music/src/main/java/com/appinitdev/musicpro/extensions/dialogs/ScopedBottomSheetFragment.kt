
abstract class ScopedBottomSheetFragment : BottomSheetDialogFragment(),
                                           SharedPreferences.OnSharedPreferenceChangeListener {

    open val handler = Handler(Looper.getMainLooper())
    private var pendingRunnable: Runnable? = null

    protected val isLandscape: Boolean by lazy {
        BarHeight.isLandscape(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.attributes?.windowAnimations = R.style.BottomDialogAnimation
        dialog?.window?.setDimAmount(ViewUtils.DIM_AMOUNT)

        dialog?.setOnShowListener { dialog ->
             



            val sheetDialog = dialog as BottomSheetDialog

             



            val bottomSheet = sheetDialog.findViewById<View>(design_bottom_sheet) as FrameLayout

             



            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED

             



            BottomSheetBehavior.from(bottomSheet).addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HALF_EXPANDED) {
                        dismiss()
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        registerSharedPreferenceChangeListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterSharedPreferenceChangeListener()
        handler.removeCallbacksAndMessages(null)
    }

     





    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {}

     


    @Suppress("unused")
    protected fun requireApplication(): Application {
        return requireActivity().application
    }

    protected fun postDelayed(runnable: () -> Unit) {
        pendingRunnable?.let { handler.removeCallbacks(it) }
        pendingRunnable = Runnable { runnable() }
        handler.postDelayed(pendingRunnable!!, 250)
    }

    protected fun postDelayed(delayMillis: Long = 250L, runnable: () -> Unit) {
        pendingRunnable?.let { handler.removeCallbacks(it) }
        pendingRunnable = Runnable { runnable() }
        handler.postDelayed(pendingRunnable!!, delayMillis)
    }

     


    protected fun openAppSettings() {
        try {
            (parentFragment as ScopedFragment).openPreferencesPanel().also {
                
                
                dismiss()
            }
        } catch (e: Exception) {
            Log.e("ScopedBottomSheetFragment", "openAppSettings: ${e.message}")
        }
    }
}
