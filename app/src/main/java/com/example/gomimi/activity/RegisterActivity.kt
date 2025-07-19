package com.example.gomimi.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.gomimi.dataClass.Address
import com.example.gomimi.dataClass.AuthResponse
import com.example.gomimi.dataClass.Language
import com.example.gomimi.databinding.ActivityRegisterBinding
import com.example.gomimi.retrofit.NetworkResult
import com.example.gomimi.retrofit.TokenManager
import com.example.gomimi.viewModel.RegisterViewModel

class RegisterActivity: BaseActivity() {
    private lateinit var viewBinding: ActivityRegisterBinding
//    private lateinit var userViewModel: UserViewModel // テスト ViewModelを追加
    private lateinit var viewModel: RegisterViewModel // ★ 正しいViewModelの型に変更

    // 取得したデータを保持する変数
    private var languageList: List<Language> = emptyList()
    private var addressList: List<Address> = emptyList()
    private var selectedAddressId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewModel = ViewModelProvider(this)[RegisterViewModel::class.java]

        // 言語リストの取得を開始
        viewModel.fetchLanguages()

        // 初期UI設定
        setAddressFieldsVisibility(View.GONE)

        // LiveDataとUIイベントリスナーを設定
        observeAllLiveData()
        setupUIEventListeners()
    }

    private fun setupUIEventListeners() {
        // XMLレイアウトのボタンIDを `searchAddressButton` に合わせてください
        viewBinding.zipBtn.setOnClickListener {
            val postalCode = viewBinding.zipCodeInput.text.toString().trim()
            if (postalCode.length == 7) {
                viewModel.searchAddress(postalCode)
            } else {
                Toast.makeText(this, "郵便番号を7桁で入力してください", Toast.LENGTH_SHORT).show()
            }
        }

        viewBinding.registerBtn.setOnClickListener {
            performRegistration()
        }
    }

    // 全てのLiveDataの監視をこのメソッドに集約
    private fun observeAllLiveData() {
        viewModel.languages.observe(this) { result ->
            if (result is NetworkResult.Success) {
                languageList = result.data
                setupSpinner(viewBinding.languageSpinner, languageList.map { it.name })
            } else if (result is NetworkResult.Error) {
                Toast.makeText(this, "言語の取得に失敗", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.addresses.observe(this) { result ->
//            viewBinding.addressSearchProgress.visibility = if (result is NetworkResult.Loading) View.VISIBLE else View.GONE

            if (result is NetworkResult.Success) {
                addressList = result.data
                if (addressList.isNotEmpty()) {
                    // ★ 住所特定後のUIセットアップを呼び出す
                    setupAddressSelection()
                } else {
                    Toast.makeText(this, "該当する住所が見つかりませんでした", Toast.LENGTH_SHORT).show()
                    setAddressFieldsVisibility(View.GONE)
                }
            } else if (result is NetworkResult.Error) {
                Toast.makeText(this, "該当する住所が見つかりませんでした", Toast.LENGTH_LONG).show()
                setAddressFieldsVisibility(View.GONE)
            } else if (result is NetworkResult.Loading) {
                // 検索中は住所フィールドを隠す
                setAddressFieldsVisibility(View.GONE)
            }
        }

        viewModel.registrationResult.observe(this) { result ->
//            viewModel.progressBar.visibility = if (result is NetworkResult.Loading) View.VISIBLE else View.GONE
            viewBinding.registerBtn.isEnabled = result !is NetworkResult.Loading

            if (result is NetworkResult.Success) {
                Toast.makeText(this, "登録成功！", Toast.LENGTH_LONG).show()
                val token = (result.data as? AuthResponse)?.accessToken
                if (token != null) TokenManager.saveToken(token)

                startActivity(Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            } else if (result is NetworkResult.Error) {
                Toast.makeText(this, "登録エラー", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * 郵便番号検索後、特定された住所を表示し、それ以降の絞り込みスピナーをセットアップする
     */
    private fun setupAddressSelection() {
        setAddressFieldsVisibility(View.VISIBLE)
        selectedAddressId = null

        // APIレスポンスから特定された共通の住所情報をTextViewに表示
        val firstAddress = addressList.first()
        viewBinding.cityTextView.text = firstAddress.city
        viewBinding.wardTextView.text = firstAddress.ward
        viewBinding.townTextView.text = firstAddress.town

        // 1.「丁目」スピナーのセットアップ
        val choms = addressList.mapNotNull { it.chom }.distinct()
        setupSpinner(viewBinding.chomSpinner, listOf("丁目を選択") + choms) { chomPos ->
            val selectedChom = choms.getOrNull(chomPos - 1)
            // 丁目が選択されたら、それに基づいて番地の選択肢を更新
            updateStreetSpinner(selectedChom)
        }
        // 初期状態では下位のスピナーは空にしておく
        updateStreetSpinner(null)
    }

    /** 丁目の選択に応じて「番地」スピナーを更新する */
    private fun updateStreetSpinner(selectedChom: String?) {
        val filteredByChom = addressList.filter { selectedChom == null || it.chom == selectedChom }
        val streets = filteredByChom.mapNotNull { it.street }.distinct()

        setupSpinner(viewBinding.streetSpinner, listOf("番地を選択") + streets) { streetPos ->
            val selectedStreet = streets.getOrNull(streetPos - 1)
            // 番地が選択されたら、それに基づいて詳細の選択肢を更新
            updateInfSpinner(selectedChom, selectedStreet)
        }
        // 初期状態では下位のスピナーは空にしておく
        updateInfSpinner(selectedChom, null)
    }

    /** 丁目・番地の選択に応じて「詳細」スピナーを更新する */
    private fun updateInfSpinner(selectedChom: String?, selectedStreet: String?) {
        val filtered = addressList.filter { (selectedChom == null || it.chom == selectedChom) && (selectedStreet == null || it.street == selectedStreet) }
        val infs = filtered.mapNotNull { it.inf }.distinct()

        setupSpinner(viewBinding.infSpinner, listOf("詳細を選択") + infs) { infPos ->
            val selectedInf = infs.getOrNull(infPos - 1)
            // 全ての条件で絞り込んだ結果、候補が1つに確定すればIDを保存
            val finalAddress = filtered.find { it.inf == selectedInf }
            selectedAddressId = finalAddress?.id
        }
    }

    // 登録処理の実行
    private fun performRegistration() {
        // ... (このメソッドの中身は前回のままでOK)
        val email = viewBinding.emailInput.text.toString().trim()
        val password = viewBinding.passwordInput.text.toString().trim()
        val confirmPassword = viewBinding.confirmPasswordInput.text.toString().trim()
        val selectedLangPosition = viewBinding.languageSpinner.selectedItemPosition
        val selectedLanguageId = if (languageList.isNotEmpty()) languageList.getOrNull(selectedLangPosition)?.id else null

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "有効なメールアドレスを入力してください", Toast.LENGTH_SHORT).show(); return
        }
        if (password.length < 6 || password != confirmPassword) {
            Toast.makeText(this, "6文字以上のパスワードを入力し、確認用と一致させてください", Toast.LENGTH_SHORT).show(); return
        }
        if (selectedLanguageId == null) {
            Toast.makeText(this, "言語を選択してください", Toast.LENGTH_SHORT).show(); return
        }
        if (selectedAddressId == null) {
            Toast.makeText(this, "住所を最後まで選択してください", Toast.LENGTH_SHORT).show(); return
        }
        viewModel.registerUser(email, password, selectedLanguageId, selectedAddressId!!)
    }

    private fun setAddressFieldsVisibility(visibility: Int) {
        viewBinding.addressContaint.visibility = visibility
        viewBinding.chomSpinner.visibility = visibility
        viewBinding.streetSpinner.visibility = visibility
        viewBinding.infSpinner.visibility = visibility
    }

    private fun setupSpinner(spinner: Spinner, items: List<String>, onItemSelectedListener: ((Int) -> Unit)? = null) {
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinner.onItemSelectedListener = if (onItemSelectedListener != null) {
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    onItemSelectedListener(position)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // 何も選択されなかった場合（例：スピナーがリセットされた時）も通知
                    onItemSelectedListener(0)
                }
            }
        } else {
            null
        }
    }
}
