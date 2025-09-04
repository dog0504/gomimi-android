package com.example.gomimi.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gomimi.dataClass.Manual
import com.example.gomimi.retrofit.NetworkResult
import com.example.gomimi.retrofit.UserRepository
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

    fun fetchManualDetailById(id: Int) {
        viewModelScope.launch {
            _manualDetail.value = NetworkResult.Loading
            _manualDetail.value = repository.getManualById(id)
        }
    }
}