package com.example.gomimi.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.gomimi.R
import com.example.gomimi.dataClass.GarbageResult
import com.example.gomimi.databinding.ActivityMainBinding
import com.example.gomimi.retrofit.NetworkResult
import com.example.gomimi.retrofit.TokenManager
import com.example.gomimi.retrofit.UserRepository
import com.example.gomimi.viewModel.MainViewModel
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
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

        // 通常の初期化処理
//        createNotificationChannel()
//        viewModel = ViewModelProvider(this@MainActivity).get(MainViewModel::class.java)
//        setupUI()
//        observeViewModel()
//        if (allPermissionsGranted()) {
//            startPreview()
//        } else {
//            ActivityCompat.requestPermissions(this@MainActivity, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
//        }
//        requestNotificationPermission()

        // トークン更新チェックも含めて初期化処理を行う

        // トークン未所持ならログイン画面へ
        val token = TokenManager.getToken()
        if (token.isNullOrEmpty()) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            return
        }

        lifecycleScope.launch {
            val result = UserRepository().refreshToken()
            if (result is NetworkResult.Error) {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                return@launch
            }
            // 通常の初期化処理
            createNotificationChannel()
            viewModel = ViewModelProvider(this@MainActivity).get(MainViewModel::class.java)
            setupUI()
            observeViewModel()
            if (allPermissionsGranted()) {
                startPreview()
            } else {
                ActivityCompat.requestPermissions(this@MainActivity, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
            }
            requestNotificationPermission()
        }

    }

    //権限チェック(1/3)
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        isGranted: Boolean ->
        if (isGranted) {
            // 通知権限が許可された時の処理
            Log.d("msg", "通知権限が許可されました。")
        } else {
            // 通知権限が拒否された時の処理
            Log.d("msg", "通知権限が許可されませんでした。")
        }
    }

    //権限チェック(2/3) - 通知権限をリクエストする関数
    private fun requestNotificationPermission() {
        // Android 13以上でのみ実行
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                // 権限がすでに許可されている場合
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("msg", "通知権限はすでに許可されています。")
                }

                // 権限が必要な理由を説明すべき場合 (一度拒否されたなど)
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    AlertDialog.Builder(this)
                        .setTitle("権限に関するお知らせ")
                        .setMessage("今後のゴミ収集日をお知らせするために、通知の権限が必要です。")
                        .setPositiveButton("OK") { _, _ ->
                            // 説明後、再度権限リクエストを行う
                            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        .setNegativeButton("キャンセル", null)
                        .show()
                }

                // それ以外の場合 (初回リクエストなど)
                else -> {
                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
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

    //通知チャンネル
    @SuppressLint("ObsoleteSdkInt")
    private fun createNotificationChannel(){
        // Build.VERSION.SDK_INTは、Android開発における定数で、
        // デバイスのオペレーティング・システムのAPIレベルを表す。
        // Build.VERSION_CODES.OはAndroid開発における定数で、
        // Android 8.0（APIレベル26）を表し、Oreoとしても知られている。
        // 通知チャネルという仕組みは、Android 8.0で初めて導入されました。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "ゴミミちゃん"
            val descriptionText = "ゴミミちゃんの通知"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID_GARBAGE, name, importance).apply {
                description = descriptionText
            }
            //　チャンネルをシステムに登録
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
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
                is NetworkResult.Success -> showResultList(result.data.query, result.data.results) // 画像認識結果を表示
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
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
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
        const val CHANNEL_ID_GARBAGE = "garbage_channel" //通知チャネルID（任意で可能）NotifySettingActivity.ktで使用
        const val NOTIFY_ID = 54304//通知ID（任意で可能）NotifySettingActivity.ktで使用
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
