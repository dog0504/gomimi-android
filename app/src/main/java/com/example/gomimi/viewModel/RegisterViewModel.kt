package com.example.gomimi.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gomimi.dataClass.Address
import com.example.gomimi.dataClass.AuthResponse
import com.example.gomimi.dataClass.RegisterRequestBody
import com.example.gomimi.retrofit.NetworkResult
import com.example.gomimi.retrofit.UserRepository
import kotlinx.coroutines.launch

class RegisterViewModel(private val userRepository: UserRepository = UserRepository()) : ViewModel() {

    // --- LiveDataの定義 ---

    // 言語リストの状態
    private val _languages = MutableLiveData<NetworkResult<List<Language>>>()
    val languages: LiveData<NetworkResult<List<Language>>> = _languages

    // 住所検索結果の状態
    private val _addresses = MutableLiveData<NetworkResult<List<Address>>>()
    val addresses: LiveData<NetworkResult<List<Address>>> = _addresses

    // 登録処理の結果の状態
    private val _registrationResult = MutableLiveData<NetworkResult<AuthResponse>>()
    val registrationResult: LiveData<NetworkResult<AuthResponse>> = _registrationResult


    // --- API呼び出しメソッド ---

    fun fetchLanguages() {
        viewModelScope.launch {
            _languages.value = NetworkResult.Loading
            _languages.value = userRepository.getLanguages()
        }
    }

    fun searchAddress(postalCode: String) {
        viewModelScope.launch {
            _addresses.value = NetworkResult.Loading
            _addresses.value = userRepository.searchAddress(postalCode)
        }
    }

    fun registerUser(email: String, password: String, languageId: Int, addressId: Int) {
        viewModelScope.launch {
            val requestBody = RegisterRequestBody(email, password, languageId, addressId)
            _registrationResult.value = NetworkResult.Loading
            _registrationResult.value = userRepository.registerUser(requestBody)
        }
    }
}