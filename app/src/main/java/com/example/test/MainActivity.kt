package com.example.yourapp

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.test.R
import com.example.test.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.ChipGroup
import com.example.yourapp.BaseActivity
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : BaseActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var imageView: ImageView
    private lateinit var takePhotoButton: ImageView
    private lateinit var chipGroup: ChipGroup
    private lateinit var tipsLayout: View
    private lateinit var tipsText: TextView
    private lateinit var characterImage: ImageView
    private lateinit var showTipsButton: Button

//    private val CAMERA_PERMISSION_CODE = 100

    // カメラ撮影結果のコールバック
//    private val takePictureLauncher = registerForActivityResult(
//        ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        // 撮影が成功した場合
//        if (result.resultCode == Activity.RESULT_OK) {
//            // サムネイル画像を取得
//            val photo = result.data?.extras?.get("data") as? Bitmap
//            photo?.let {
//                // ImageViewに写真を表示
//                imageView.setImageBitmap(it)
//                // ヒントを表示
//                showTips("これはペットボトルです。")
//            }
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        // レイアウトの設定
        setContentView(viewBinding.root)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomMenu)
        bottomNav.selectedItemId =R.id.navigation_camera
        setupBottomNav(bottomNav)
        // Viewの参照を取得
        constraintLayout = findViewById(R.id.constraintLayout)
//        imageView = findViewById(R.id.imageView)
        takePhotoButton = findViewById(R.id.takePhotoButton)
        chipGroup = findViewById(R.id.thipGroup)
        tipsLayout = findViewById(R.id.tipsLayout)
        tipsText = findViewById(R.id.tipsText)
        characterImage = findViewById(R.id.characterImage)
//        showTipsButton = findViewById(R.id.thipsbtn)

        //権限チェック(1/3)
        if (allPermissionsGranted()) {
            startPreview()   //プレビュー開始
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        if (!allPermissionsGranted()) {
            Toast.makeText(this, "権限が必要です。設定から許可してください。", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }

        //静止画撮影ボタン（クリックリスナー）
        viewBinding.takePhotoButton.setOnClickListener { takePhoto() }

        cameraExecutor = Executors.newSingleThreadExecutor()

        // 初期状態ではヒントレイアウトを非表示に設定
        tipsLayout.visibility = View.GONE
        // Thipsボタンの位置をbottomMenu上に設定
//        adjustThipsButtonConstraint(showTips = false)

        // Thipsボタンのクリックリスナー設定
//        showTipsButton.setOnClickListener {
//            if (tipsLayout.visibility == View.VISIBLE) {
//                // ヒントが表示されている場合は非表示にする
//                hideTips()
//            } else {
//                // ヒントが非表示の場合は表示する
//                showTips("中身を出して、さっと水洗いしてください。\n" +
//                        "できるだけつぶしてお出しください。\n" +
//                        "キャップやラベルは必ずはずして、\n" +
//                        "プラスチック資源にお出しください。\n" +
//                        "キャップをはずした後ペットボトルに残る\n" +
//                        "リング状簡単にはずすことができる場合は、\n" +
//                        "はずしてプラスチック資源に、\n" +
//                        "はずせない場合は、\n" +
//                        "そのまま資源ごみでお出しください。")
//            }
//        }
    }

    //権限チェック(2/3)
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    //権限チェック(3/3)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startPreview()   //プレビュー開始
            } else {
                //必要な権限が取得できない場合はアプリを終了する
//                finish()
                Log.d("msg", "権限が取得できませんでした。")
            }
        }
    }

    //静止画撮影
    private fun takePhoto() {

        val imageCapture = this.imageCapture ?: return

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.ROOT)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Image")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Log.d("Camera X sample","撮影エラー（静止画）")
//                    Toast.makeText(baseContext, "Error", Toast.LENGTH_SHORT).show()
                }
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val msg = "撮影成功（静止画）"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    //プレビュー開始
    private fun startPreview() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({

            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            //プレビュー
            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = viewBinding.preview.surfaceProvider
                }

            //静止画撮影
            imageCapture = ImageCapture.Builder().build()

            //動画撮影
//            val recorder = Recorder.Builder()
//                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
//                .build()
//            videoCapture = VideoCapture.withOutput(recorder)

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture,
//                    videoCapture
                )

            } catch (e: Exception) {
                Log.d("Camera X sample","エラーが発生しました", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
//
//    // ChipをChipGroupに追加するメソッド
//    private fun addChip(text: String) {
//        val chip = Chip(this)
//        chip.text = text
//        chip.isCloseIconVisible = true  // 閉じるアイコンを表示
//        chip.setOnCloseIconClickListener {
//            // 閉じるアイコン押下でChipを削除
//            chipGroup.removeView(chip)
//        }
//        chipGroup.addView(chip)
//    }
//
//    // ヒントを表示するメソッド
//    private fun showTips(message: String) {
//        tipsText.text = message
//        tipsLayout.visibility = View.VISIBLE
//        // ヒント表示時にThipsボタンの位置を調整
//        adjustThipsButtonConstraint(showTips = true)
//    }
//
//    // ヒントを非表示にするメソッド
//    private fun hideTips() {
//        tipsLayout.visibility = View.GONE
//        // ヒント非表示時にThipsボタンの位置を調整
//        adjustThipsButtonConstraint(showTips = false)
//    }
//
//    // ThipsボタンのConstraintを動的に切り替えるメソッド
//    private fun adjustThipsButtonConstraint(showTips: Boolean) {
//        val set = ConstraintSet()
//        set.clone(constraintLayout)
//
//        if (showTips) {
//            // ヒント表示時はThipsボタンをtipsLayoutの上に配置（マージン8dp）
//            set.connect(
//                R.id.thipsbtn,
//                ConstraintSet.BOTTOM,
//                R.id.tipsLayout,
//                ConstraintSet.TOP,
//                8
//            )
//        } else {
//            // ヒント非表示時はThipsボタンをbottomMenuの上に配置（マージン16dp）
//            set.connect(
//                R.id.thipsbtn,
//                ConstraintSet.BOTTOM,
//                R.id.bottomMenu,
//                ConstraintSet.TOP,
//                16
//            )
//        }
//
//        // 他の制約は変更しないように注意
//        set.applyTo(constraintLayout)
//    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}
