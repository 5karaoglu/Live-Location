package com.besirkaraoglu.locationsharingsampleii.data

import com.besirkaraoglu.locationsharingsampleii.CloudDbWrapper
import com.besirkaraoglu.locationsharingsampleii.model.Users
import com.besirkaraoglu.locationsharingsampleii.util.Resource
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery
import com.huawei.agconnect.cloud.database.OnSnapshotListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

class UsersRepository {


    suspend fun getUsers(): Flow<Resource<List<Users>>> = withContext(Dispatchers.IO){
        callbackFlow {
            trySend(Resource.Loading)
            val queryUsers = CloudDBZoneQuery.where(Users::class.java).equalTo("isActive",true )

            val subscription = CloudDbWrapper.cloudDBZone!!.subscribeSnapshot(
                queryUsers,CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
                , OnSnapshotListener<Users> { snapshot, exception ->
                    if (exception != null) {
                        trySend(Resource.Error(exception))
                        exception.printStackTrace()
                        return@OnSnapshotListener
                    }
                    try {
                        val snapshotObjects = snapshot?.snapshotObjects
                        val userList = mutableListOf<Users>()
                        // Get user data from db
                        if (snapshotObjects != null) {
                            while (snapshotObjects.hasNext()) {
                                userList.add(snapshotObjects.next())
                            }
                            trySend(Resource.Success(userList))
                        }else{
                            trySend(Resource.Empty)
                        }
                    }catch (e: Exception){
                        e.printStackTrace()
                        trySend(Resource.Error(e))
                    }finally {
                        snapshot.release()
                    }
                }
            )

            awaitClose {
                subscription.remove()
            }
        }
    }

    suspend fun upsertLocation(): Flow<Resource<String>> = withContext(Dispatchers.IO){
        callbackFlow {
            trySend(Resource.Loading)

        }
    }
}