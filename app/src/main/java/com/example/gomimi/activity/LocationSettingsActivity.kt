package com.example.gomimi.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.gomimi.dataClass.Address
import com.example.gomimi.databinding.LocationSettingsBinding
import com.example.gomimi.retrofit.NetworkResult
import com.example.gomimi.viewModel.LocationSettingsViewModel

class LocationSettingsActivity : BaseActivity() {

    private lateinit var binding: LocationSettingsBinding
    private lateinit var viewModel: LocationSettingsViewModel

    private var searchedAddressList: List<Address> = emptyList()
    private var selectedAddressId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LocationSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(LocationSettingsViewModel::class.java)

        // UIの初期設定とイベントリスナーの設定
        setupUI()
        // ViewModelからのデータ変更を監視
        observeViewModel()

        // 画面表示時に現在のユーザー情報を取得
        viewModel.fetchCurrentUser()
    }

    private fun setupUI() {
        binding.backButton.setOnClickListener { finish() }

        // 郵便番号検索ボタン
        binding.zipBtn.setOnClickListener {
            val postalCode = binding.postalCodeInput.text.toString().trim()
            if (postalCode.length == 7) {
                viewModel.searchAddress(postalCode)
            } else {
                Toast.makeText(this, "郵便番号を7桁で入力してください", Toast.LENGTH_SHORT).show()
            }
        }

        // 適用ボタン
        binding.locationApplyBtn.setOnClickListener {
            selectedAddressId?.let {
                viewModel.updateAddress(it)
            } ?: Toast.makeText(this, "住所を最後まで選択してください", Toast.LENGTH_SHORT).show()
        }

        // 初期状態ではスピナー群を非表示にする
        setAddressFieldsVisibility(View.GONE)
    }

    private fun observeViewModel() {
        // 現在のユーザー情報の監視
        viewModel.userProfile.observe(this) { result ->
            if (result is NetworkResult.Success) {
                val addr = result.data.address
                binding.currentAddressTextView.text = "${addr.zip}\n${addr.city}${addr.ward}${addr.town ?: ""}${addr.chom ?: ""}${addr.street ?: ""}${addr.inf ?: ""}"
            }
        }

        // 住所検索結果の監視
        viewModel.searchedAddresses.observe(this) { result ->
            binding.progressBar.visibility = if (result is NetworkResult.Loading) View.VISIBLE else View.GONE

            if (result is NetworkResult.Success) {
                searchedAddressList = result.data
                if (searchedAddressList.isNotEmpty()) {
                    setupAddressSelection()
                } else {
                    Toast.makeText(this, "該当する住所が見つかりませんでした", Toast.LENGTH_SHORT).show()
                    setAddressFieldsVisibility(View.GONE)
                }
            } else if (result is NetworkResult.Error) {
                Toast.makeText(this, "住所の検索に失敗: ${result.message}", Toast.LENGTH_LONG).show()
                setAddressFieldsVisibility(View.GONE)
            }
        }

        // 所在地更新結果の監視
        viewModel.updateResult.observe(this) { result ->
            binding.progressBar.visibility = if (result is NetworkResult.Loading) View.VISIBLE else View.GONE

            if (result is NetworkResult.Success) {
//                Toast.makeText(this, "所在地を更新しました", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish() // 更新成功したら画面を閉じる
            } else if (result is NetworkResult.Error) {
                Toast.makeText(this, "更新に失敗しました: ${result.message}", Toast.LENGTH_LONG).show()
            }
        }

    }

    /**
     * 郵便番号検索後、特定された住所を表示し、それ以降の絞り込みスピナーをセットアップする
     */
    private fun setupAddressSelection() {
        setAddressFieldsVisibility(View.VISIBLE)
        selectedAddressId = null

        val firstAddress = searchedAddressList.first()
        binding.cityTextView.text = firstAddress.city
        binding.wardTextView.text = firstAddress.ward
        binding.townTextView.text = firstAddress.town

        // --- 登録画面からコピーした段階的絞り込みロジック ---
        val choms = searchedAddressList.mapNotNull { it.chom }.distinct()
        setupSpinner(binding.chomSpinner, listOf("丁目を選択") + choms) { chomPos ->
            val selectedChom = choms.getOrNull(chomPos - 1)
            updateStreetSpinner(selectedChom)
        }
        updateStreetSpinner(null)
    }

    private fun updateStreetSpinner(selectedChom: String?) {
        val filtered = searchedAddressList.filter { selectedChom == null || it.chom == selectedChom }
        val streets = filtered.mapNotNull { it.street }.distinct()
        setupSpinner(binding.streetSpinner, listOf("番地を選択") + streets) { streetPos ->
            val selectedStreet = streets.getOrNull(streetPos - 1)
            updateInfSpinner(selectedChom, selectedStreet)
        }
        updateInfSpinner(selectedChom, null)
    }

    private fun updateInfSpinner(selectedChom: String?, selectedStreet: String?) {
        val filtered = searchedAddressList.filter {
            (selectedChom == null || it.chom == selectedChom) &&
                    (selectedStreet == null || it.street == selectedStreet)
        }
        val infs = filtered.mapNotNull { it.inf }.distinct()
        setupSpinner(binding.infSpinner, listOf("詳細を選択") + infs) { infPos ->
            val selectedInf = infs.getOrNull(infPos - 1)
            val finalAddress = filtered.find { it.inf == selectedInf }
            selectedAddressId = finalAddress?.id
        }
    }

    private fun setAddressFieldsVisibility(visibility: Int) {
        binding.addressContaint.visibility = visibility
        binding.chomSpinner.visibility = visibility
        binding.streetSpinner.visibility = visibility
        binding.infSpinner.visibility = visibility
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
                    onItemSelectedListener(0)
                }
            }
        } else {
            null
        }
    }
}