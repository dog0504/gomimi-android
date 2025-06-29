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
//    private val _loginResult = MutableLiveData<NetworkResult<User>>()
//    val loginResult: LiveData<NetworkResult<User>> = _loginResult
//
//    fun performLogin(email: String, password: String) {
//        viewModelScope.launch {
//            _loginResult.value = NetworkResult.Loading
//            _loginResult.value = userRepository.login(email, password)
//        }
//    }

    // 本番仕様
    private val _loginResult = MutableLiveData<NetworkResult<AuthResponse>>()
    val loginResult: LiveData<NetworkResult<AuthResponse>> = _loginResult
    fun performLogin(email: String, password: String) {
        viewModelScope.launch {
            _loginResult.value = NetworkResult.Loading
            _loginResult.value = userRepository.login(email, password)
        }
    }

    // ★登録処理を追加★
    // 今回はモックのため、引数をすべて使用する必要はありませんが、実際のAPIに合わせて定義
    fun register(email: String, password: String, language: String, zipCode: String, prefecture: String, city: String) {
        viewModelScope.launch {
//            _loginResult.value = NetworkResult.Loading // 登録もローディング状態から開始
//            // UserRepositoryのregisterメソッドを呼び出す（次のステップで実装）
//            _loginResult.value = userRepository.register(email, password) // 仮にメールとパスワードのみ渡す
        }
    }
}