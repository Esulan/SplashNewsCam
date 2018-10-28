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
import com.google.android.gms.vision.face.FaceDetector
import java.util.TimerTask
import java.util.Timer
import okhttp3.*
import android.R.attr.bitmap
import android.graphics.Bitmap
import android.graphics.Matrix
import com.google.android.gms.vision.Frame
import okhttp3.RequestBody
import okhttp3.OkHttpClient
import java.io.IOException


class CameraActivity : AppCompatActivity() {

    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null
    var taking = false
    var mTimer: Timer? = null
    var mDetector: FaceDetector? = null

    fun getCameraInstance(): Camera? {
        return try {
            Camera.open() // attempt to get a Camera instance
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
            null // returns null if camera is unavailable
        }
    }

    fun sendRequest(x: Float, y: Float) {
        val url = String.format("https://765bd937.ngrok.io?x=%.2f&y=%.2f", x, y)
        val client = OkHttpClient()
        val MIMEType = MediaType.parse("application/json; charset=utf-8")
        val requestBody = RequestBody.create(MIMEType, "{}")
        val request = Request.Builder().url(url).get().build()
        val response = client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("call failure", e.toString())
            }
            override fun onResponse(call: Call, response: Response){
                Log.d("call", response.body()?.string())
            }
        })
    }

    private fun saveImg(data: ByteArray) {
        val b = BitmapFactory.decodeByteArray(data, 0, data.size)
        val matrix = Matrix()
        matrix.setScale(0.4f, 0.4f)
        val bitmap = Bitmap.createBitmap(b, 0, 0, b.width, b.height, matrix, true)
        val frame = Frame.Builder().setBitmap(bitmap).build()
        val faces = mDetector?.detect(frame)

        if (faces != null) {
            var i = 0
            val nsize = faces.size()
            while (i < nsize) {
                val face = faces.valueAt(i)
                Log.d("face", face.position.toString())
                sendRequest(face.position.x, face.position.y)
                i++
            }
        }
    }

    val mPicture = Camera.PictureCallback { data, _ ->
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
        mDetector = FaceDetector.Builder(this).setTrackingEnabled(true).build()

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
                }, 1000, 4000   //開始遅延(何ミリ秒後に開始するか)と、周期(何ミリ秒ごとに実行するか)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        mTimer?.cancel()
        mDetector?.release()
    }
}
