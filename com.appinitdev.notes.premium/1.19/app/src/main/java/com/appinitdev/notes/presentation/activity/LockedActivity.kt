abstract class LockedActivity<T : ViewBinding> : AppCompatActivity() {
    private lateinit var notallyXApplication: NotallyXApplication
    private lateinit var biometricAuthenticationActivityResultLauncher:
        ActivityResultLauncher<Intent>
    protected lateinit var binding: T
    protected lateinit var preferences: NotallyXPreferences
    val baseModel: BaseNoteModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notallyXApplication = (application as NotallyXApplication)
        preferences = NotallyXPreferences.getInstance(application)
        biometricAuthenticationActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    unlock()
                } else {
                    finish()
                }
            }
    }
    override fun onResume() {
        if (preferences.biometricLock.value == BiometricLock.ENABLED) {
            if (hasToAuthenticateWithBiometric()) {
                hide()
                showLockScreen()
            } else {
                show()
            }
        }
        super.onResume()
    }
    override fun onPause() {
        super.onPause()
        if (
            preferences.biometricLock.value == BiometricLock.ENABLED &&
                notallyXApplication.locked.value
        ) {
            hide()
        }
    }
    open fun showLockScreen() {
        showBiometricOrPinPrompt(
            true,
            preferences.iv.value!!,
            biometricAuthenticationActivityResultLauncher,
            R.string.unlock,
            onSuccess = { unlock() },
        ) { errorCode ->
            when (errorCode) {
                BIOMETRIC_ERROR_NO_BIOMETRICS -> {
                    MaterialAlertDialogBuilder(this)
                        .setMessage(R.string.unlock_with_biometrics_not_setup)
                        .setPositiveButton(R.string.disable) { _, _ ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                baseModel.disableBiometricLock()
                            }
                            show()
                        }
                        .setNegativeButton(R.string.tap_to_set_up) { _, _ ->
                            val intent =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    Intent(Settings.ACTION_BIOMETRIC_ENROLL)
                                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    Intent(Settings.ACTION_FINGERPRINT_ENROLL)
                                } else {
                                    Intent(Settings.ACTION_SECURITY_SETTINGS)
                                }
                            startActivity(intent)
                        }
                        .show()
                }
                BIOMETRIC_ERROR_HW_NOT_PRESENT -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        baseModel.disableBiometricLock()
                        showToast(R.string.biometrics_disable_success)
                    }
                    show()
                }
                else -> finish()
            }
        }
    }
    private fun unlock() {
        notallyXApplication.locked.value = false
        show()
    }
    protected fun show() {
        binding.root.visibility = VISIBLE
    }
    protected fun hide() {
        binding.root.visibility = INVISIBLE
    }
    private fun hasToAuthenticateWithBiometric(): Boolean {
        return ContextCompat.getSystemService(this, KeyguardManager::class.java)?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                (it.isDeviceLocked || notallyXApplication.locked.value)
            } else {
                false
            }
        } ?: false
    }
}
