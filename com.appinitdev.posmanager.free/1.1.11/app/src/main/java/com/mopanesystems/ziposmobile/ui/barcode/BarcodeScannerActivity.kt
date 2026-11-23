
class BarcodeScannerActivity : AppCompatActivity() {
    
    private lateinit var barcodeView: DecoratedBarcodeView
    private var isScanning = false
    
    companion object {
        const val EXTRA_BARCODE_RESULT = "barcode_result"
        const val CAMERA_PERMISSION_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scanner)
        
        barcodeView = findViewById(R.id.barcode_scanner)
        
        setupToolbar()
        checkCameraPermission()
    }

    private fun setupToolbar() {
        supportActionBar?.apply {
            title = getString(R.string.scan_barcode)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        } else {
            startScanning()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startScanning()
                } else {
                    Toast.makeText(this,
                        getString(R.string.camera_permission_is_required_for_barcode_scanning), Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    private fun startScanning() {
        barcodeView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                if (result != null && !isScanning) {
                    isScanning = true
                    handleBarcodeResult(result.text)
                }
            }

            override fun possibleResultPoints(resultPoints: MutableList<com.google.zxing.ResultPoint>?) {
                
            }
        })
    }

    private fun handleBarcodeResult(barcode: String) {
        
        val resultIntent = Intent().apply {
            putExtra(EXTRA_BARCODE_RESULT, barcode)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
            == PackageManager.PERMISSION_GRANTED) {
            barcodeView.resume()
        }
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
