package com.example.test.retrofit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.retrofit.BinDay
import com.example.test.retrofit.NetworkResult
import com.example.test.retrofit.UserRepository
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