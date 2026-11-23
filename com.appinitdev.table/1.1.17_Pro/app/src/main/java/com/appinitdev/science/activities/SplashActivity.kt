
class SplashActivity : AppCompatActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        
        proceedToMain()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val hasRequestedPermission = prefs.getBoolean("notification_permission_requested", false)
            
            if (!hasRequestedPermission) {
                
                prefs.edit().putBoolean("notification_permission_requested", true).apply()
                
                
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    return
                }
            }
        }

        
        proceedToMain()
    }

    private fun proceedToMain() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val hasRequestedPermission = prefs.getBoolean("notification_permission_requested", false)
        val hasShownIntroduction = prefs.getBoolean("introduction_shown", false)
        
        
        
        val intent = 


            Intent(this, MainActivity::class.java)
        
        startActivity(intent)
        finish()
    }
}