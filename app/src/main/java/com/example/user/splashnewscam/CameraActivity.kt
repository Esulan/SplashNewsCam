package com.example.user.splashnewscam

import android.Manifest
import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.FrameLayout
import java.util.TimerTask
import java.util.Timer
import okhttp3.*


class CameraActivity : AppCompatActivity() {

    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null
    var taking = false
    var mTimer: Timer? = null

    fun getCameraInstance(): Camera? {
        return try {
            Camera.open() // attempt to get a Camera instance
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
            null // returns null if camera is unavailable
        }
    }

    private fun saveImg(data: ByteArray) {
        val client = OkHttpClient()
        val url = "https://imagebin.ca/upload.php"
        val MIMEType = MediaType.parse("multipart/form-data")

        val fileName = "testfile.jpg"
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)

        /*
        try {
            openFileOutput(fileName,
                    Context.MODE_PRIVATE).use { fileOutputstream ->

                fileOutputstream.write(data)

            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        */


    }

    val mPicture = Camera.PictureCallback { data, _ ->
        Log.d("data", data.size.toString())
        saveImg(data)
        mCamera?.startPreview()
    }

    public fun takePicture() {

        try {
            mCamera?.takePicture(null, null, mPicture)
        }catch (e: java.lang.Exception) {
            Log.d("tag", e.toString())
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        setContentView(R.layout.activity_camera)

        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA), 12345)

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

        mTimer = Timer()
        val handler = android.os.Handler()
        mTimer?.schedule(
                object : TimerTask() {
                    override fun run() {
                        handler.post {
                            // get an image from the camera
                            takePicture()
                        }
                    }
                }, 3000, 10000   //開始遅延(何ミリ秒後に開始するか)と、周期(何ミリ秒ごとに実行するか)
        )
        val mPicture = Camera.PictureCallback { data, _ ->
            Log.d("data", data.size.toString())
            // saveImg(data)
        }
        if (!taking) {
            taking = true
            try {
                mCamera?.takePicture(null, null, mPicture)
            }catch (e: java.lang.Exception) {

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mTimer?.cancel()
    }
}