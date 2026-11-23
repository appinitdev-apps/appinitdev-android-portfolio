open class ScopedDialogFragment : DialogFragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    lateinit var packageInfo: PackageInfo
    internal val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val window = dialog!!.window ?: return
        val displayMetrics = DisplayMetrics()
        window.attributes.windowAnimations = R.style.DialogAnimation
        window.attributes.height = FrameLayout.LayoutParams.WRAP_CONTENT
        @Suppress("deprecation")
        window.windowManager.defaultDisplay.getMetrics(displayMetrics)
        dialog?.window?.setDimAmount(ViewUtils.DIM_AMOUNT)
        window.attributes.gravity = Gravity.CENTER
        if (BarHeight.isLandscape(requireContext())) {
            window.attributes.width = (displayMetrics.widthPixels * 1f / 100f * 60f).toInt()
        } else {
            window.attributes.width = (displayMetrics.widthPixels * 1f / 100f * 85f).toInt()
        }
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
    @Suppress("unused")
    protected fun requireApplication(): Application {
        return requireActivity().application
    }
}
