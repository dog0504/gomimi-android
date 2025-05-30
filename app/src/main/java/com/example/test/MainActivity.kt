package com.example.yourapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.test.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var takePhotoButton: ImageView
    private lateinit var chipGroup: ChipGroup
    private lateinit var tipsLayout: View
    private lateinit var tipsText: TextView
    private lateinit var characterImage: ImageView

    private val CAMERA_PERMISSION_CODE = 100

    // カメラ撮影結果のコールバック
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // 撮影成功した場合
        if (result.resultCode == Activity.RESULT_OK) {
            // サムネイル画像を取得
            val photo = result.data?.extras?.get("data") as? Bitmap
            photo?.let {
                // ImageViewに写真を表示
                imageView.setImageBitmap(it)
                // ヒントを表示
                showTips("撮影が完了しました！")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Viewの参照を取得
        imageView = findViewById(R.id.imageView)
        takePhotoButton = findViewById(R.id.takePhotoButton)
        chipGroup = findViewById(R.id.chipGroup)
        tipsLayout = findViewById(R.id.tipsLayout)
        tipsText = findViewById(R.id.tipsText)
        characterImage = findViewById(R.id.characterImage)

        // 初期状態ではヒントレイアウトを非表示にする
        tipsLayout.visibility = View.GONE

        // 撮影ボタンのクリックリスナー設定
        takePhotoButton.setOnClickListener {
            // カメラの権限をチェック
            if (checkCameraPermission()) {
                // 権限あればカメラを起動
                openCamera()
            } else {
                // 権限がなければ要求
                requestCameraPermission()
            }
        }

        // ChipGroupにテスト用のチップを追加
        addChip("テストチップ1")
        addChip("テストチップ2")
    }

    // カメラ権限の確認
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // カメラ権限の要求
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }

    // 権限要求の結果を受け取る
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // 権限が許可されたらカメラを起動
                openCamera()
            } else {
                // 権限拒否時のメッセージ表示
                showTips("カメラの権限が必要です。")
            }
        }
    }

    // カメラ起動処理
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureLauncher.launch(intent)
    }

    // ChipをChipGroupに追加
    private fun addChip(text: String) {
        val chip = Chip(this)
        chip.text = text
        chip.isCloseIconVisible = true  // 閉じるアイコンを表示
        chip.setOnCloseIconClickListener {
            // 閉じるアイコン押下でChipを削除
            chipGroup.removeView(chip)
        }
        chipGroup.addView(chip)
    }

    // ヒント表示用メソッド
    private fun showTips(message: String) {
        tipsText.text = message
        tipsLayout.visibility = View.VISIBLE
    }

    // ヒント非表示用メソッド
    private fun hideTips() {
        tipsLayout.visibility = View.GONE
    }
}
