package com.example.gomimi.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import androidx.lifecycle.ViewModelProvider
import com.example.gomimi.R
import com.example.gomimi.dataClass.GarbageResult
import com.example.gomimi.databinding.ActivityMainBinding
import com.example.gomimi.retrofit.NetworkResult
import com.example.gomimi.viewModel.MainViewModel
import com.google.android.material.chip.Chip
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : BaseActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        // レイアウトの設定
        setContentView(viewBinding.root)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        setupUI()
        observeViewModel()

        if (allPermissionsGranted()) {
            startPreview()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
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

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    viewModel.identifyGarbage(photoFile)
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "撮影に失敗しました: ${exc.message}", exc)
                    Toast.makeText(baseContext, "撮影に失敗しました", Toast.LENGTH_SHORT).show()
                    photoFile.delete() // エラー発生時もファイルを削除
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

    // UIの初期設定とイベントリスナー
    private fun setupUI() {
        // BottomNav
        setupBottomNav(viewBinding.bottomMenu)
        viewBinding.bottomMenu.selectedItemId = R.id.navigation_camera

        // Click Listeners
        viewBinding.takePhotoButton.setOnClickListener { takePhoto() }
        viewBinding.reshootBtn.setOnClickListener { resetToInitialState() }
        viewBinding.backToListBtn.setOnClickListener { showResultList() }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    // ViewModelのLiveDataを監視
    private fun observeViewModel() {
        // 画像認識APIの結果を監視
        viewModel.identificationResult.observe(this) { result ->
            viewBinding.progressBar.visibility = if (result is NetworkResult.Loading) View.VISIBLE else View.GONE

            when (result) {
                is NetworkResult.Success -> showResultList(result.data.query_text, result.data.results) // 画像認識結果を表示
                is NetworkResult.Error -> {
                    Toast.makeText(this, "解析失敗: ${result.message}", Toast.LENGTH_LONG).show()
                    resetToInitialState()
                }
                else -> {}
            }
        }

        // ごみマニュアル検索APIの結果を監視
        viewModel.manualDetail.observe(this) { result ->
            if (result is NetworkResult.Success) {
                val manual = result.data
                if (manual != null) {
                    showTips(manual.name, manual.remarks ?: getString(R.string.noinformation), manual.category)
                } else {
                    Toast.makeText(this, getString(R.string.intelligence_not_found), Toast.LENGTH_SHORT).show()
                }
            } else if (result is NetworkResult.Error) {
                Toast.makeText(this, "${getString(R.string.date_not_found)} ${result.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun resetToInitialState() {
        viewBinding.takePhotoButton.visibility = View.VISIBLE
        viewBinding.descLayout.visibility = View.GONE
        viewBinding.tipsLayout.visibility = View.GONE
        viewBinding.btnLayout.visibility = View.GONE
        viewBinding.queryTextView.visibility = View.GONE
    }

    private fun showResultList(query: String = null.toString(), results: List<GarbageResult>? = null) {
        viewBinding.takePhotoButton.visibility = View.GONE
        viewBinding.descLayout.visibility = View.VISIBLE
        viewBinding.tipsLayout.visibility = View.GONE
        viewBinding.btnLayout.visibility = View.VISIBLE
        viewBinding.queryTextView.visibility = View.VISIBLE

        if (results != null) {
            viewBinding.queryTextView.text = query
            viewBinding.descChipGroup.removeAllViews()
            results.forEach { garbageItem ->
                val chip = Chip(this).apply {
                    text = garbageItem.name
                    isClickable = true
                    setOnClickListener {
                        // チップがクリックされたら、その名前で詳細情報を検索
                        viewModel.fetchManualDetail(garbageItem.name)
                    }
                }
                viewBinding.descChipGroup.addView(chip)
            }
        }
    }

    private fun showTips(name: String, description: String, category: String) {
        viewBinding.descLayout.visibility = View.GONE
        viewBinding.queryTextView.visibility = View.GONE
        viewBinding.tipsLayout.visibility = View.VISIBLE
        viewBinding.btnLayout.visibility = View.VISIBLE // 再撮影ボタンは表示したまま

        viewBinding.garbageNameText.text = name
        viewBinding.tipsText.text = description
        viewBinding.categoryTextView.text = category
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
