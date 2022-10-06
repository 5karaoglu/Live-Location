package com.besirkaraoglu.locationsharingsampleii.util

import com.huawei.agconnect.auth.AGConnectUser

sealed class HuaweiAuthResult {
    data class UserSuccessful(val user: AGConnectUser) : HuaweiAuthResult()
    data class UserFailure(val errorMessage: String?) : HuaweiAuthResult()
}