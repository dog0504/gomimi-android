package com.example.gomimi.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gomimi.dataClass.ManualInfo
import com.example.gomimi.retrofit.NetworkResult
import com.example.gomimi.retrofit.UserRepository
import kotlinx.coroutines.launch

class ManualViewModel(private val repository: UserRepository = UserRepository()) : ViewModel() {

    private val _manuals = MutableLiveData<NetworkResult<List<ManualInfo>>>()
    val manuals: LiveData<NetworkResult<List<ManualInfo>>> = _manuals

    // 画面表示時に全マニュアルを取得する
    fun fetchAllManuals() {
        viewModelScope.launch {
            _manuals.value = NetworkResult.Loading
            _manuals.value = repository.getAllManuals()
        }
    }

    // キーワードで検索
    fun searchByKeyword(keyword: String) {
        // 空白の場合は検索しない
        if (keyword.isBlank()) {
            // 必要であれば、全件表示に戻すなどの処理を追加
            // fetchAllManuals()
            return
        }
        viewModelScope.launch {
            _manuals.value = NetworkResult.Loading
            _manuals.value = repository.searchManualsByKeyword(keyword)
        }
    }

    // 頭文字で検索
    fun searchByInitial(initial: String) {
        viewModelScope.launch {
            _manuals.value = NetworkResult.Loading
            _manuals.value = repository.searchManualsByInitial(initial)
        }
    }
}