package com.example.voco.ui

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.voco.R
import com.example.voco.adapter.ProductAdapter
import com.example.voco.api.ApiInterface
import com.example.voco.api.RetrofitClient
import com.example.voco.model.ApiRes
import com.example.voco.model.Product
import com.example.voco.model.ScanDTO
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity(), OnInitListener {

    private lateinit var previewView: PreviewView
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var modalResult: RelativeLayout
    private lateinit var btnClose: CardView
    private lateinit var btnSeeAll: CardView
    private lateinit var retrofit: Retrofit
    private lateinit var apiInterface: ApiInterface
    private lateinit var resultImage: ImageView
    private lateinit var resultTitle : TextView
    private lateinit var resultDesc : TextView

    private var stateIsOpen = false
    private lateinit var product: Product

    private val barcodeScanner: BarcodeScanner by lazy {
        BarcodeScanning.getClient()
    }

    private var textToSpeech: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_camera)

        previewView = findViewById(R.id.previewView)
        modalResult = findViewById(R.id.modalResult)
        btnClose = findViewById(R.id.btnClose)
        btnSeeAll = findViewById(R.id.btnSeeAll)
        resultImage = findViewById(R.id.resultImage)
        resultTitle = findViewById(R.id.resultTitle)
        resultDesc = findViewById(R.id.resultDesc)

        retrofit = RetrofitClient.getClient(this)
        apiInterface = retrofit.create(ApiInterface::class.java)

        cameraExecutor = Executors.newSingleThreadExecutor()

        btnClose.setOnClickListener { onClosePressed() }

        textToSpeech = TextToSpeech(this, this)

        startCamera()

        btnSeeAll.setOnClickListener {
            val intent = Intent(this, ProductActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, { imageProxy ->
                        processImageProxy(imageProxy)
                    })
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (e: Exception) {
                Log.e("CameraActivity", "Gagal memulai kamera: ${e.message}", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage =
                InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        if (barcode.valueType == Barcode.TYPE_TEXT || barcode.valueType == Barcode.TYPE_WIFI || barcode.valueType == Barcode.TYPE_URL) {
                            onQRCodeDetected(barcode)
                            break
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("CameraActivity", "Deteksi QR gagal: ${e.message}", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    private fun onQRCodeDetected(barcode: Barcode) {
        if (!stateIsOpen) {
            modalResult.visibility = RelativeLayout.VISIBLE
            stateIsOpen = true
            if (barcode.displayValue != null) {
                fetchData(barcode.displayValue!!)
            }else{
                Toast.makeText(this@CameraActivity, "Terjadi kesalahan", Toast.LENGTH_SHORT)
                    .show()
                stateIsOpen = false
                modalResult.visibility = RelativeLayout.GONE
            }
        }
    }

    private fun onClosePressed() {
        stateIsOpen = false
        modalResult.visibility = RelativeLayout.GONE
        textToSpeech?.stop()
        resultTitle.text = "Loading..."
        resultDesc.text = "Loading..."
        Glide.with(this@CameraActivity)
            .load(R.drawable.placeholder)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .into(resultImage)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val langResult = textToSpeech?.setLanguage(java.util.Locale("in", "ID"))
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("CameraActivity", "Bahasa tidak didukung")
            }
        } else {
            Log.e("CameraActivity", "Inisialisasi TTS gagal")
        }
    }

    private fun textToSpeech(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }

    private fun fetchData(qr: String) {
        val requestBody = ScanDTO(qr)
        val call = apiInterface.scanQRCode(requestBody)
        call.enqueue(object : Callback<ApiRes<Product>> {
            override fun onResponse(
                call: Call<ApiRes<Product>>,
                response: Response<ApiRes<Product>>
            ) {

                if (response.isSuccessful) {
                    val data = response.body()
                    if (data?.success == true && data.data != null) {
                        resultTitle.text = "${data.data.brand} - ${data.data.variant}"
                        resultDesc.text = data.data.text
                        Glide.with(this@CameraActivity)
                            .load(data.data.image)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .into(resultImage)
                        textToSpeech(data.data.text)
                    } else {
                        resultTitle.text = "Tidak ditemukan"
                        resultDesc.text = "QR Code yang anda scan tidak valid"
                        Toast.makeText(this@CameraActivity, data?.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Log.e("Error", response.errorBody().toString())
                    resultTitle.text = "Tidak ditemukan"
                    resultDesc.text = "QR Code yang anda scan tidak valid"
                    Toast.makeText(
                        this@CameraActivity,
                        "Terjadi kesalahan",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

            override fun onFailure(call: Call<ApiRes<Product>>, t: Throwable) {
                Log.e("Error", t.message.toString())
                resultTitle.text = "Tidak ditemukan"
                resultDesc.text = "QR Code yang anda scan tidak valid"
                Toast.makeText(this@CameraActivity, "Terjadi kesalahan", Toast.LENGTH_SHORT)
                    .show()

            }
        })

    }
}
