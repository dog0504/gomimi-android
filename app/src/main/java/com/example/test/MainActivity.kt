package com.example.yourapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.test.R
import com.example.test.databinding.ActivityMainBinding
import com.example.test.retrofit.GarbageResult
import com.example.test.retrofit.NetworkResult
import com.example.test.retrofit.UserRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : BaseActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private var garbageName: String? = "ダミー"
    private var garbageTips: String? = "中身を出して、さっと水洗いしてください。\n" +
            "できるだけつぶしてお出しください。\n" +
            "キャップやラベルは必ずはずして、\n" +
            "プラスチック資源にお出しください。\n" +
            "キャップをはずした後ペットボトルに残る\n" +
            "リング状簡単にはずすことができる場合は、\n" +
            "はずしてプラスチック資源に、\n" +
            "はずせない場合は、\n" +
            "そのまま資源ごみでお出しください。"

    private val userRepository = UserRepository()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        // レイアウトの設定
        setContentView(viewBinding.root)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomMenu)
        bottomNav.selectedItemId =R.id.navigation_camera
        setupBottomNav(bottomNav)

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

        // 閉じるボタンのクリックリスナー
        viewBinding.reshootBtn.setOnClickListener { hideTips();hideDescription() }

        cameraExecutor = Executors.newSingleThreadExecutor()

        // 初期状態ではヒントレイアウトを非表示に設定
        viewBinding.tipsLayout.visibility = View.GONE
        // Thipsボタンの位置をbottomMenu上に設定
//        adjustThipsButtonConstraint(showTips = false)

        // Thipsボタンのクリックリスナー設定
        viewBinding.tipsbtn.setOnClickListener {
            if (viewBinding.tipsLayout.isVisible) {
                // ヒントが表示されている場合は非表示にする
                hideTips()
            } else {
                // ヒントが非表示の場合は表示する
                showTips()
            }
        }

        com.example.test.retrofit.TokenManager.getToken()?.let { token ->
            Log.d(TAG, "トークン: $token")
        } ?: run {
            Log.d(TAG, "トークンが保存されていません。")
        }
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
        // 静止画を撮影し、APIサーバーに画像を送信
        val imageCapture = imageCapture ?: return

        // 一時ファイルの作成
        val photoFile = File(cacheDir, "temp_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "撮影に失敗しました: ${exc.message}", exc)
                    Toast.makeText(baseContext, "撮影に失敗しました", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Toast.makeText(baseContext, "画像を解析中...", Toast.LENGTH_SHORT).show()
                    // UIを解析中状態へ
                    showLoadingState()

                    // コルーチンで画像アップロードを実行
                    lifecycleScope.launch {
                        val requestFile = RequestBody.create(MediaType.parse("image/*"), photoFile)
                        val body = MultipartBody.Part.createFormData("image", photoFile.name, requestFile)
                        val result = withContext(Dispatchers.IO) {
                            userRepository.uploadImage(body)
                        }

                        // 結果に応じてUIを更新
                        when (result) {
                            is NetworkResult.Success -> {
                                Toast.makeText(this@MainActivity, "画像解析成功: ${result.data.queryText}", Toast.LENGTH_SHORT).show()
                                showResults(result.data.results)
                            }
                            is NetworkResult.Error -> {
                                Toast.makeText(this@MainActivity, "解析失敗: ${result.message}", Toast.LENGTH_LONG).show()
                                resetToInitialState() // エラー時は初期状態に戻す
                            }
                            is NetworkResult.Loading -> {
                                // ここでは何もしない
                            }
                        }
                        photoFile.delete() // 一時ファイルを削除
                    }
                }
            }
        )

//        imageCapture?.let { imageCapture ->
//            val photoFile = File.createTempFile("IMG_", ".jpg", cacheDir)
//            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
//            imageCapture.takePicture(
//                outputOptions,
//                ContextCompat.getMainExecutor(this),
//                object : ImageCapture.OnImageSavedCallback {
//                    override fun onError(exception: ImageCaptureException) {
//                        Toast.makeText(this@MainActivity, "撮影に失敗しました", Toast.LENGTH_SHORT).show()
//                    }
//                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
//                        // 画像ファイルをAPIサーバーに送信
//                        lifecycleScope.launch {
//                            val requestFile = RequestBody.create(MediaType.parse("image/jpeg"), photoFile)
//                            val body = MultipartBody.Part.createFormData("image", photoFile.name, requestFile)
//                            val result = withContext(Dispatchers.IO) {
//                                userRepository.uploadImage(body)
//                            }
//                            when (result) {
//                                is com.example.test.retrofit.NetworkResult.Success -> {
//                                    val recognition = result.data
//                                    garbageName = recognition.name
//                                    garbageTips = recognition.description
//                                    showDescription()
//                                }
//                                is com.example.test.retrofit.NetworkResult.Error -> {
//                                    Toast.makeText(this@MainActivity, "認識失敗: ${result.message}", Toast.LENGTH_SHORT).show()
//                                    showDescription() // ダミー表示
//                                }
//                                else -> {}
//                            }
//                            photoFile.delete()
//                        }
//                    }
//                }
//            )
//        }
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

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
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

    private fun showDescription() {
        viewBinding.descText.text = garbageName // テキストを設定
        viewBinding.descLayout.visibility = View.VISIBLE // レイアウトを表示
        viewBinding.btnLayout.visibility = View.VISIBLE // ボタンを表示
        viewBinding.takePhotoButton.visibility = View.GONE // 撮影ボタンを非表示にする
    }

    private fun hideDescription() {
        viewBinding.descText.text = "" // テキストをクリア
        viewBinding.descLayout.visibility = View.GONE // レイアウトを非表示
        viewBinding.btnLayout.visibility = View.GONE // ボタンを非表示
        viewBinding.takePhotoButton.visibility = View.VISIBLE // 撮影ボタンを表示
    }

    private fun showTips() {
        viewBinding.garbageNameText.text = garbageName // ごみの名前を設定
        viewBinding.tipsText.text = garbageTips // ヒントのテキストを設定
        viewBinding.descLayout.visibility = View.GONE // レイアウトを非表示
        viewBinding.tipsLayout.visibility = View.VISIBLE // ヒントレイアウトを表示
    }

    private fun hideTips() {
        viewBinding.garbageNameText.text = "" // ごみの名前をクリア
        viewBinding.tipsText.text = "" // ヒントのテキストをクリア
        viewBinding.tipsLayout.visibility = View.GONE // ヒントレイアウトを非表示
        viewBinding.descLayout.visibility = View.VISIBLE // 元のレイアウトを表示
    }

    // 解析結果をChipとして表示する
    private fun showResults(results: List<GarbageResult>) {
//        viewBinding.thipGroup.removeAllViews() // 以前の結果をクリア
//        viewBinding.thipGroup.visibility = View.VISIBLE
//
//        results.forEach { garbageItem ->
//            val chip = Chip(this).apply {
//                text = garbageItem.name
//                isClickable = true
//                isCheckable = true
//                setOnClickListener {
//                    // チップクリック時の動作（例：詳細表示など）をここに追加
//                    Toast.makeText(this@MainActivity, "${garbageItem.name} の情報を表示します", Toast.LENGTH_SHORT).show()
//                }
//            }
//            viewBinding.thipGroup.addView(chip)
//        }

        // UIの状態を結果表示用に変更
        viewBinding.takePhotoButton.visibility = View.VISIBLE
        viewBinding.progressBar.visibility = View.GONE // プログレスバーを非表示
//        viewBinding.btnLayout.visibility = View.VISIBLE
    }

    // UIを初期のカメラプレビュー状態に戻す
    private fun resetToInitialState() {
        hideDescription()
        viewBinding.progressBar.visibility = View.GONE // プログレスバーを非表示
    }

    // UIをロード中（解析中）の状態にする
    private fun showLoadingState() {
        viewBinding.takePhotoButton.visibility = View.GONE
        viewBinding.progressBar.visibility = View.VISIBLE
    }


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
