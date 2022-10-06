package com.besirkaraoglu.locationsharingsampleii.ui.login

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.besirkaraoglu.locationsharingsampleii.domain.LoginUseCase
import com.besirkaraoglu.locationsharingsampleii.util.HuaweiAuthResult
import com.besirkaraoglu.locationsharingsampleii.util.Resource
import com.huawei.agconnect.auth.AGConnectUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
): ViewModel() {

    private val signInHuaweiIdLiveData: MutableLiveData<Resource<AGConnectUser?>> =
        MutableLiveData()

    fun getSignInHuaweiIdLiveData(): LiveData<Resource<AGConnectUser?>> = signInHuaweiIdLiveData

    fun signInWithHuaweiId(requestCode: Int, data: Intent?) {
        viewModelScope.launch {
            signInHuaweiIdLiveData.value = Resource.Loading

            when (val result = loginUseCase.signInWithHuaweiId(requestCode, data)) {
                is HuaweiAuthResult.UserSuccessful -> {
                    signInHuaweiIdLiveData.value = Resource.Success(result.user)
                }
                is HuaweiAuthResult.UserFailure -> {
                    signInHuaweiIdLiveData.value = result.errorMessage?.let { Resource.Error(Exception(it)) }
                }
            }
        }
    }
}