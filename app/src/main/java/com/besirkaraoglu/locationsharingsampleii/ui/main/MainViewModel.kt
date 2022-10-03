package com.besirkaraoglu.locationsharingsampleii.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.besirkaraoglu.locationsharingsampleii.CloudDbWrapper
import com.besirkaraoglu.locationsharingsampleii.data.UsersRepository
import com.besirkaraoglu.locationsharingsampleii.model.Users
import com.huawei.hms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject constructor(
        usersRepository: UsersRepository
    ) : ViewModel() {

    val userData = liveData(Dispatchers.IO) {
        usersRepository.getUsers().collect {
            emit(it)
        }
    }

    fun upsertLocation(name: String, uid: String, latlng: LatLng, isActive: Boolean,  callback: (isSuccessful: Boolean) -> Unit){
        val user = Users()
        user.name = name
        user.latitude = latlng.latitude
        user.longitude = latlng.longitude
        user.uid = uid
        user.isActive = isActive
        CloudDbWrapper.upsertUser(user, callback)
    }

    fun stopLocationUpdates(name: String, uid: String,  callback: (isSuccessful: Boolean) -> Unit){
        val user = Users()
        user.name = name
        user.uid = uid
        user.isActive = false
        CloudDbWrapper.upsertUser(user, callback)
    }

    fun deleteLocation(uid: String, callback: (isSuccessful: Boolean) -> Unit){
        val user = Users()
        user.uid = uid
        CloudDbWrapper.deleteUser(user, callback)
    }
}