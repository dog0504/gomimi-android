package com.example.test.retrofit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository = UserRepository()) : ViewModel() {

    private val _user = MutableLiveData<NetworkResult<User>>()
    val user: LiveData<NetworkResult<User>> = _user

    private val _users = MutableLiveData<NetworkResult<List<User>>>()
    val users: LiveData<NetworkResult<List<User>>> = _users

    fun fetchUser(id: String) {
        viewModelScope.launch {
            _user.value = NetworkResult.Loading
            _user.value = userRepository.getUser(id)
        }
    }

    fun fetchUsers() {
        viewModelScope.launch {
            _users.value = NetworkResult.Loading
            _users.value = userRepository.getUsers()
        }
    }

    // loginのシミュレーション
    private val _loginResult = MutableLiveData<NetworkResult<User>>()
    val loginResult: LiveData<NetworkResult<User>> = _loginResult

    fun performLogin(email: String, password: String) {
        viewModelScope.launch {
            _loginResult.value = NetworkResult.Loading
            _loginResult.value = userRepository.login(email, password)
        }
    }
}