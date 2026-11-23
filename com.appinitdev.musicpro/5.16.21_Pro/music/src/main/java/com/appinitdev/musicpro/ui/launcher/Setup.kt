
 







class Setup : MediaFragment() {

    private lateinit var binding: FragmentSetupBinding

    private val notificationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
    ) { _ ->
        updateNotificationPermissionStatus()
    }

    private val readMediaAudioPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
    ) { _ ->
        updateReadMediaAudioPermissionStatus()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireHiddenMiniPlayer()

        updateNotificationPermissionStatus()
        updateReadMediaAudioPermissionStatus()

        binding.grantPostNotifications.setOnClickListener {
            requestNotificationPermission()
        }

        binding.grantReadMediaAudio.setOnClickListener {
            requestReadMediaAudioPermission()
        }

        binding.startAppNow.setOnClickListener {
            if (areRequiredPermissionsGranted()) {
                AudioDatabaseService.startScan(requireContext())
                showWarning(R.string.scanning_library)
                proceedToHome()
            }
        }

        updateStartButtonState()

        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomBarContainer) { bottomView, insets ->
            
            val navigationBarsInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            
            val originalPadding = resources.getDimensionPixelSize(R.dimen.padding_15)

            
            bottomView.setPadding(
                    bottomView.paddingLeft,
                    bottomView.paddingTop,
                    bottomView.paddingRight,
                    originalPadding + navigationBarsInsets.bottom
            )

            insets
        }
    }

    override fun onResume() {
        super.onResume()
        updateNotificationPermissionStatus()
        updateReadMediaAudioPermissionStatus()
        updateStartButtonState()
    }

     


    private fun areRequiredPermissionsGranted(): Boolean {
        return isPostNotificationsPermissionGranted() && isReadMediaAudioPermissionGranted()
    }

    private fun updateStartButtonState() {
        val enabled = areRequiredPermissionsGranted()
        binding.startAppNow.isClickable = enabled
        binding.startAppNow.alpha = if (enabled) 1.0f else 0.5f
    }

    private fun proceedToHome() {
        (requireActivity() as? MainActivity)?.showHome()
    }

    private fun requestNotificationPermission() {
        if (isPostNotificationsPermissionGranted()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun requestReadMediaAudioPermission() {
        if (isReadMediaAudioPermissionGranted()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            readMediaAudioPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            readMediaAudioPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun updateReadMediaAudioPermissionStatus() {
        if (isReadMediaAudioPermissionGranted()) {
            binding.statusReadMediaAudio.setText(R.string.granted)
        } else {
            binding.statusReadMediaAudio.setText(R.string.not_granted)
        }
        updateStartButtonState()
    }

    private fun updateNotificationPermissionStatus() {
        if (isPostNotificationsPermissionGranted()) {
            binding.statusPostNotifications.setText(R.string.granted)
        } else {
            binding.statusPostNotifications.setText(R.string.not_granted)
        }
        updateStartButtonState()
    }

    override val wantsMiniPlayerVisible: Boolean
        get() = false

    companion object {
        fun newInstance(): Setup {
            val args = Bundle()
            val fragment = Setup()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "Setup"
    }
}
