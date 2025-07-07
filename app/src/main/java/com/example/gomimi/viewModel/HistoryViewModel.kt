package com.example.gomimi.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gomimi.dataClass.History
import com.example.gomimi.retrofit.NetworkResult
import com.example.gomimi.retrofit.UserRepository
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