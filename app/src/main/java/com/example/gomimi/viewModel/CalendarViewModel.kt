package com.example.gomimi.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gomimi.dataClass.BinDay
import com.example.gomimi.retrofit.NetworkResult
import com.example.gomimi.retrofit.UserRepository
import kotlinx.coroutines.launch

class CalendarViewModel(private val repository: UserRepository = UserRepository()) : ViewModel() {
    private val _binDays = MutableLiveData<NetworkResult<List<BinDay>>>()
    val binDays: LiveData<NetworkResult<List<BinDay>>> = _binDays

    fun fetchBinDays() {
        viewModelScope.launch {
            _binDays.value = NetworkResult.Loading
            _binDays.value = repository.getBinDays()
        }
    }
}