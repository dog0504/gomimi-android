package com.example.test.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.retrofit.Language
import com.example.test.retrofit.NetworkResult
import com.example.test.retrofit.UpdateRequestBody
import com.example.test.retrofit.UserRepository
import com.example.test.retrofit.UserResponseBody
import kotlinx.coroutines.launch

class LanguageSettingsViewModel(private val repository: UserRepository = UserRepository()) : ViewModel() {

    // 利用可能な全言語リスト
    private val _languages = MutableLiveData<NetworkResult<List<Language>>>()
    val languages: LiveData<NetworkResult<List<Language>>> = _languages

    // 現在のユーザー情報
    private val _userProfile = MutableLiveData<NetworkResult<UserResponseBody>>()
    val userProfile: LiveData<NetworkResult<UserResponseBody>> = _userProfile

    // 更新結果
    private val _updateResult = MutableLiveData<NetworkResult<UserResponseBody>>()
    val updateResult: LiveData<NetworkResult<UserResponseBody>> = _updateResult

    // 画面表示に必要な情報をまとめて取得
    fun fetchInitialData() {
        viewModelScope.launch {
            _languages.value = NetworkResult.Loading
            _userProfile.value = NetworkResult.Loading

            _languages.value = repository.getLanguages()
            _userProfile.value = repository.getMyProfile()
        }
    }

    // 言語設定を更新
    fun updateLanguage(languageId: Int) {
        viewModelScope.launch {
            _updateResult.value = NetworkResult.Loading
            val requestBody = UpdateRequestBody(languageId = languageId)
            _updateResult.value = repository.updateUserProfile(requestBody)
        }
    }
}