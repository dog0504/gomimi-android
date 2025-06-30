package com.example.test.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.retrofit.NetworkResult
import com.example.test.retrofit.UserRepository
import com.example.test.retrofit.UserResponseBody
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: UserRepository = UserRepository()) : ViewModel() {

    private val _userProfile = MutableLiveData<NetworkResult<UserResponseBody>>()
    val userProfile: LiveData<NetworkResult<UserResponseBody>> = _userProfile

    fun fetchUserProfile() {
        viewModelScope.launch {
            _userProfile.value = NetworkResult.Loading
            _userProfile.value = repository.getMyProfile()
        }
    }
}