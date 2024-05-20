package com.evo.qrgo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ainirobot.coreservice.client.RobotApi
import com.ainirobot.coreservice.client.listener.CommandListener
import com.evo.qrgo.databinding.ActivityMainBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions

class MainActivity : AppCompatActivity() {

    private val reqId = 0

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                showCamera()
            } else {
                Toast.makeText(this, "CAMERA permission required", Toast.LENGTH_SHORT).show()
            }
        }

    private val scanLauncher =
        registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
            if (result.contents == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            } else {
                setResults(result.contents)
            }
        }

    private lateinit var binding: ActivityMainBinding

    private fun setResults(string: String) {
        binding.textResult.text = string
        // Hide the ImageView after scanning
        binding.imageView.visibility = View.GONE

        // Check if the scanned string is a URL
        if (string.startsWith("http://") || string.startsWith("https://")) {
            openLink(string)
        } else {
            RobotApi.getInstance().startNavigation(0, string, 1.0, (10 * 1000).toLong(), mMotionListener)
        }
    }

    private fun showCamera() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("Scan QR Code")
        options.setCameraId(0) //default 0 back, 1 is front
        options.setBeepEnabled(false)
        options.setBarcodeImageEnabled(true)
        options.setOrientationLocked(false)

        scanLauncher.launch(options)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initViews()
    }

    private fun initViews() {
        binding.fab.setOnClickListener {
            checkPermissionCamera(this)
        }
    }

    private fun checkPermissionCamera(context: Context) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            showCamera()
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            Toast.makeText(context, "CAMERA permission required", Toast.LENGTH_SHORT).show()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun initBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private val mMotionListener: CommandListener = object : CommandListener() {
        override fun onResult(result: Int, message: String) {
            if ("succeed" == message) {
                // Add any additional success handling here
            } else {
                // Add any failure handling here
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Handle back button click
    fun onBackButtonClick(view: View) {
        onBackPressed()
    }

    private fun openLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
