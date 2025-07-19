package com.example.gomimi.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gomimi.dataClass.UserResponseBody
import com.example.gomimi.retrofit.NetworkResult
import com.example.gomimi.retrofit.UserRepository
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