package com.example.test.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.retrofit.Address
import com.example.test.retrofit.NetworkResult
import com.example.test.retrofit.UpdateRequestBody
import com.example.test.retrofit.UserRepository
import com.example.test.retrofit.UserResponseBody
import kotlinx.coroutines.launch

class LocationSettingsViewModel(private val repository: UserRepository = UserRepository()) : ViewModel() {

    // 現在のユーザー情報
    private val _userProfile = MutableLiveData<NetworkResult<UserResponseBody>>()
    val userProfile: LiveData<NetworkResult<UserResponseBody>> = _userProfile

    // 郵便番号での住所検索結果
    private val _searchedAddresses = MutableLiveData<NetworkResult<List<Address>>>()
    val searchedAddresses: LiveData<NetworkResult<List<Address>>> = _searchedAddresses

    // プロフィール更新結果
    private val _updateResult = MutableLiveData<NetworkResult<UserResponseBody>>()
    val updateResult: LiveData<NetworkResult<UserResponseBody>> = _updateResult

    // 現在のプロフィールを取得
    fun fetchCurrentUser() {
        viewModelScope.launch {
            _userProfile.value = NetworkResult.Loading
            _userProfile.value = repository.getMyProfile()
        }
    }

    // 郵便番号で住所を検索
    fun searchAddress(postalCode: String) {
        viewModelScope.launch {
            _searchedAddresses.value = NetworkResult.Loading
            _searchedAddresses.value = repository.searchAddress(postalCode)
        }
    }

    // 所在地を更新
    fun updateAddress(addressId: Int) {
        viewModelScope.launch {
            _updateResult.value = NetworkResult.Loading
            val requestBody = UpdateRequestBody(addressId = addressId)
            _updateResult.value = repository.updateUserProfile(requestBody)
        }
    }
}