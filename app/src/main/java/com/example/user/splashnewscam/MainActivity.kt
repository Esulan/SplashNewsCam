package com.example.user.splashnewscam


import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null

    fun getCameraInstance(): Camera? {
        return try {
            Camera.open() // attempt to get a Camera instance
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
            Log.d("hoge", "fuga")
            null // returns null if camera is unavailable
        }
    }

    private fun saveImg(data: ByteArray) {
        val client = OkHttpClient()
        val url = "https://imagebin.ca/upload.php"
        val MIMEType = MediaType.parse("multipart/form-data")

        val fileName = "testfile.jpg"
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)

        try {
            openFileOutput(fileName,
                    Context.MODE_PRIVATE).use { fileOutputstream ->

                fileOutputstream.write(data)

            }
        } catch (e: IOException) {
            e.printStackTrace()
        }


    }

    private val mPicture = Camera.PictureCallback { data, _ ->
        Log.d("data", data.size.toString())
        saveImg(data)
        mCamera?.startPreview()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA), 1234);

        // Create an instance of Camera
        mCamera = getCameraInstance()

        mPreview = mCamera?.let {
            // Create our Preview view
            CameraPreview(this, it)
        }

        // Set the Preview view as the content of our activity.
        mPreview?.also {
            val preview: FrameLayout = findViewById(R.id.camera_preview)
            preview.addView(it)
        }

        val captureButton: Button = this.findViewById(R.id.button_capture)
        captureButton.setOnClickListener {
            // get an image from the camera
            mCamera?.takePicture(null, null, mPicture)
        }
    }
}