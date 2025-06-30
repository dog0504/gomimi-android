package com.example.test

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.retrofit.Manual
import com.example.test.retrofit.NetworkResult
import com.example.test.retrofit.UserRepository
import kotlinx.coroutines.launch

class HistoryDetailViewModel(private val repository: UserRepository = UserRepository()) : ViewModel() {

    private val _manualDetail = MutableLiveData<NetworkResult<Manual>>()
    val manualDetail: LiveData<NetworkResult<Manual>> = _manualDetail

    fun fetchManualDetail(name: String) {
        viewModelScope.launch {
            _manualDetail.value = NetworkResult.Loading
            _manualDetail.value = repository.searchManualExact(name)
        }
    }
}