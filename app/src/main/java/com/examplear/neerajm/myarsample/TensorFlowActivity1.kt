package com.examplear.neerajm.myarsample

import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.wonderkiln.camerakit.*
import java.util.concurrent.Executors

class TensorFlowActivity1 : AppCompatActivity() {

    private val MODEL_PATH = "mobilenet_quant_v1_224.tflite"
    private val LABEL_PATH = "labels.txt"
    private val INPUT_SIZE = 224

    private var classifier: Classifier? = null

    private val executor = Executors.newSingleThreadExecutor()
    private var textViewResult: TextView? = null
    private var btnDetectObject: Button? = null
    private var btnToggleCamera:Button? = null
    private var btnAR:Button? = null
    private var imageViewResult: ImageView? = null
    private var cameraView: CameraView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tensor_flow1)
        cameraView = this.findViewById(R.id.cameraView)
        imageViewResult = findViewById(R.id.imageViewResult)
        textViewResult = findViewById(R.id.textViewResult)
        textViewResult!!.movementMethod = ScrollingMovementMethod()

        btnToggleCamera = findViewById<Button>(R.id.btnToggleCamera)
        btnAR = findViewById<Button>(R.id.btnAR)
        btnDetectObject = findViewById(R.id.btnDetectObject)

        cameraView!!.addCameraKitListener(object : CameraKitEventListener {
            override fun onEvent(cameraKitEvent: CameraKitEvent) {

            }

            override fun onError(cameraKitError: CameraKitError) {

            }

            override fun onImage(cameraKitImage: CameraKitImage) {

                var bitmap = cameraKitImage.bitmap

                bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false)

                imageViewResult!!.setImageBitmap(bitmap)

                val results = classifier!!.recognizeImage(bitmap)


                textViewResult!!.text = results[0].title
                if(results[0].title.contains("water")|| results[0].title.contains("bottle"))
                {
                    btnAR!!.setVisibility(View.VISIBLE)
                }else
                {
                    Toast.makeText(this@TensorFlowActivity1,"OOps!!",Toast.LENGTH_SHORT).show()
                }
            }

            override fun onVideo(cameraKitVideo: CameraKitVideo) {

            }
        })

        btnToggleCamera!!.setOnClickListener(View.OnClickListener { cameraView!!.toggleFacing() })

        btnDetectObject!!.setOnClickListener { cameraView!!.captureImage() }
        btnAR!!.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@TensorFlowActivity1, MainActivity::class.java)
            startActivity(intent)
        })

        initTensorFlowAndLoadModel()
    }

    override fun onResume() {
        super.onResume()
        cameraView!!.start()
    }

    override fun onPause() {
        cameraView!!.stop()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.execute { classifier!!.close() }
    }

    private fun initTensorFlowAndLoadModel() {
        executor.execute {
            try {
                classifier = TensorFlowImageClassifier.create(
                        assets,
                        MODEL_PATH,
                        LABEL_PATH,
                        INPUT_SIZE)
                makeButtonVisible()
            } catch (e: Exception) {
                throw RuntimeException("Error initializing TensorFlow!", e)
            }
        }
    }

    private fun makeButtonVisible() {
        runOnUiThread { btnDetectObject!!.visibility = View.VISIBLE }
    }
}
