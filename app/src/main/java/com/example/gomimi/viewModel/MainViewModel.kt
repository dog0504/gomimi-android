package com.example.gomimi.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gomimi.dataClass.GarbageIdentificationResponse
import com.example.gomimi.dataClass.Manual
import com.example.gomimi.retrofit.NetworkResult
import com.example.gomimi.retrofit.UserRepository
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class MainViewModel(private val repository: UserRepository = UserRepository()) : ViewModel() {

    // 画像認識結果のLiveDate
    private val _identificationResult = MutableLiveData<NetworkResult<GarbageIdentificationResponse>>()
    val identificationResult: LiveData<NetworkResult<GarbageIdentificationResponse>> = _identificationResult

    // ごみマニュアル詳細のLiveData
    private val _manualDetail = MutableLiveData<NetworkResult<Manual>>()
    val manualDetail: LiveData<NetworkResult<Manual>> = _manualDetail

    // 画像をアップロードしてごみを識別する
    // ★ メソッドの引数にphotoFileを追加
    fun identifyGarbage(photoFile: File) {
        viewModelScope.launch {
            _identificationResult.value = NetworkResult.Loading

            // ViewModel内でリクエストボディを作成するように変更
            val requestFile = photoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", photoFile.name, requestFile)

            val result = repository.uploadImage(body)
            Log.d("MainViewModel", "identifyGarbage result: $result")
            _identificationResult.value = result
        }
    }

    // メソッド名と呼び出すリポジトリのメソッドを修正
    fun fetchManualDetail(name: String) { // 引数名をkeywordからnameに変更
        viewModelScope.launch {
            _manualDetail.value = NetworkResult.Loading
            _manualDetail.value = repository.searchManualExact(name) // searchManualExactを呼び出す
        }
    }
}