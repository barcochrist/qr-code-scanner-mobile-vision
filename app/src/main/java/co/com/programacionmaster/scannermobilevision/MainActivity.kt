package co.com.programacionmaster.scannermobilevision

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var cameraSource: CameraSource
    private lateinit var barcodeDetector: BarcodeDetector
    val CAM_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            askForCameraPermission()
        } else {
            setupCameraScanner()
        }
    }

    private fun setupCameraScanner() {
        barcodeDetector = BarcodeDetector.Builder(this@MainActivity)
            .setBarcodeFormats(Barcode.QR_CODE)
            .build()

        cameraSource = CameraSource.Builder(this@MainActivity, barcodeDetector)
            .setAutoFocusEnabled(true)
            .build()

        barcodeDetector.setProcessor(processor)
        cameraSurface.holder.addCallback(surfaceCallback)
    }

    private fun askForCameraPermission() {
        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(Manifest.permission.CAMERA),
            CAM_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAM_REQUEST_CODE && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupCameraScanner()
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val surfaceCallback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(surfaceHolder: SurfaceHolder?) {
            try {
                if (ActivityCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    askForCameraPermission()
                }

                cameraSource.start(surfaceHolder)
            } catch (exception: Exception) {
                Toast.makeText(applicationContext, exception.message, Toast.LENGTH_LONG).show()
            }
        }

        override fun surfaceChanged(
            surfaceHolder: SurfaceHolder?,
            format: Int,
            width: Int,
            height: Int
        ) {
            Toast.makeText(applicationContext, "", Toast.LENGTH_LONG).show()
        }

        override fun surfaceDestroyed(surfaceHolder: SurfaceHolder?) {
            cameraSource.stop()
        }
    }

    private val processor = object : Detector.Processor<Barcode> {
        override fun release() {
        }

        override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
            if (detections != null && detections.detectedItems.size() > 0) {
                textScanResult.text = detections.detectedItems.valueAt(0).displayValue
            } else {
                textScanResult.text = ""
            }
        }
    }
}