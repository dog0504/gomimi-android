package com.example.test

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.retrofit.History
import com.example.test.retrofit.NetworkResult
import com.example.test.retrofit.UserRepository
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: UserRepository = UserRepository()) : ViewModel() {

    private val _histories = MutableLiveData<NetworkResult<List<History>>>()
    val histories: LiveData<NetworkResult<List<History>>> = _histories

    fun fetchHistories() {
        viewModelScope.launch {
            _histories.value = NetworkResult.Loading
            _histories.value = repository.getHistories(limit = 100)
        }
    }
}