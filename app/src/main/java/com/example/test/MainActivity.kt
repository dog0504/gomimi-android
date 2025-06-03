package com.example.yourapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.test.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class MainActivity : AppCompatActivity() {

    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var imageView: ImageView
    private lateinit var takePhotoButton: ImageView
    private lateinit var chipGroup: ChipGroup
    private lateinit var tipsLayout: View
    private lateinit var tipsText: TextView
    private lateinit var characterImage: ImageView
    private lateinit var showTipsButton: Button

    private val CAMERA_PERMISSION_CODE = 100

    // カメラ撮影結果のコールバック
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // 撮影が成功した場合
        if (result.resultCode == Activity.RESULT_OK) {
            // サムネイル画像を取得
            val photo = result.data?.extras?.get("data") as? Bitmap
            photo?.let {
                // ImageViewに写真を表示
                imageView.setImageBitmap(it)
                // ヒントを表示
                showTips("これはペットボトルです。")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // レイアウトの設定
        setContentView(R.layout.activity_main)

        // Viewの参照を取得
        constraintLayout = findViewById(R.id.constraintLayout)
        imageView = findViewById(R.id.imageView)
        takePhotoButton = findViewById(R.id.takePhotoButton)
        chipGroup = findViewById(R.id.thipGroup)
        tipsLayout = findViewById(R.id.tipsLayout)
        tipsText = findViewById(R.id.tipsText)
        characterImage = findViewById(R.id.characterImage)
        showTipsButton = findViewById(R.id.thipsbtn)

        // 初期状態ではヒントレイアウトを非表示に設定
        tipsLayout.visibility = View.GONE
        // Thipsボタンの位置をbottomMenu上に設定
        adjustThipsButtonConstraint(showTips = false)

        // Thipsボタンのクリックリスナー設定
        showTipsButton.setOnClickListener {
            if (tipsLayout.visibility == View.VISIBLE) {
                // ヒントが表示されている場合は非表示にする
                hideTips()
            } else {
                // ヒントが非表示の場合は表示する
                showTips("中身を出して、さっと水洗いしてください。\n" +
                        "できるだけつぶしてお出しください。\n" +
                        "キャップやラベルは必ずはずして、\n" +
                        "プラスチック資源にお出しください。\n" +
                        "キャップをはずした後ペットボトルに残る\n" +
                        "リング状簡単にはずすことができる場合は、\n" +
                        "はずしてプラスチック資源に、\n" +
                        "はずせない場合は、\n" +
                        "そのまま資源ごみでお出しください。")
            }
        }

        // 撮影ボタンのクリックリスナー設定
        takePhotoButton.setOnClickListener {
            // カメラ権限を確認
            if (checkCameraPermission()) {
                // 権限があればカメラを起動
                openCamera()
            } else {
                // 権限がなければ要求
                requestCameraPermission()
            }
        }
    }

    // カメラ権限の確認メソッド
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // カメラ権限の要求メソッド
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }

    // 権限要求の結果を受け取るメソッド
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // 権限が許可された場合カメラを起動
                openCamera()
            } else {
                // 権限拒否時にヒント表示
                showTips("カメラの権限が必要です。")
            }
        }
    }

    // カメラ起動メソッド
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureLauncher.launch(intent)
    }

    // ChipをChipGroupに追加するメソッド
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

    // ヒントを表示するメソッド
    private fun showTips(message: String) {
        tipsText.text = message
        tipsLayout.visibility = View.VISIBLE
        // ヒント表示時にThipsボタンの位置を調整
        adjustThipsButtonConstraint(showTips = true)
    }

    // ヒントを非表示にするメソッド
    private fun hideTips() {
        tipsLayout.visibility = View.GONE
        // ヒント非表示時にThipsボタンの位置を調整
        adjustThipsButtonConstraint(showTips = false)
    }

    // ThipsボタンのConstraintを動的に切り替えるメソッド
    private fun adjustThipsButtonConstraint(showTips: Boolean) {
        val set = ConstraintSet()
        set.clone(constraintLayout)

        if (showTips) {
            // ヒント表示時はThipsボタンをtipsLayoutの上に配置（マージン8dp）
            set.connect(
                R.id.thipsbtn,
                ConstraintSet.BOTTOM,
                R.id.tipsLayout,
                ConstraintSet.TOP,
                8
            )
        } else {
            // ヒント非表示時はThipsボタンをbottomMenuの上に配置（マージン16dp）
            set.connect(
                R.id.thipsbtn,
                ConstraintSet.BOTTOM,
                R.id.bottomMenu,
                ConstraintSet.TOP,
                16
            )
        }

        // 他の制約は変更しないように注意
        set.applyTo(constraintLayout)
    }
}
