package com.besirkaraoglu.locationsharingsampleii

import android.content.Context
import android.util.Log
import com.besirkaraoglu.locationsharingsampleii.model.ObjectTypeInfoHelper

import com.besirkaraoglu.locationsharingsampleii.model.Users
import com.huawei.agconnect.AGCRoutePolicy
import com.huawei.agconnect.AGConnectInstance
import com.huawei.agconnect.AGConnectOptionsBuilder
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.cloud.database.*
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import com.huawei.hmf.tasks.Task

class CloudDbWrapper {

    companion object {
        private const val TAG = "CloudDBWrapper"
        private const val ClodDBLog = "Cloud DB Zone is null, try re-open it"

        private var cloudDB: AGConnectCloudDB? = null
        private var config: CloudDBZoneConfig? = null
        var cloudDBZone: CloudDBZone? = null
        var instance: AGConnectInstance? = null

        fun initialize(
            context: Context,
            cloudDbInitializeResponse: (Boolean) -> Unit
        ) {
            if (cloudDBZone != null) {
                cloudDbInitializeResponse(true)
                return
            }

            AGConnectCloudDB.initialize(context)

            instance = AGConnectInstance.buildInstance(
                AGConnectOptionsBuilder().setRoutePolicy(AGCRoutePolicy.GERMANY).build(context)
            )

            cloudDB = AGConnectCloudDB.getInstance(
                AGConnectInstance.getInstance(),
                AGConnectAuth.getInstance()
            )

            cloudDB?.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo())

            config = CloudDBZoneConfig(
                "lsszone",
                CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC
            )

            config?.persistenceEnabled = true
            val task = cloudDB?.openCloudDBZone2(config!!, true)
            task?.addOnSuccessListener {
                Log.i(TAG, "Open cloudDBZone success")
                cloudDBZone = it
                cloudDbInitializeResponse(true)
            }?.addOnFailureListener {
                Log.e(TAG, "Open cloudDBZone failed for " + it.message)
                cloudDbInitializeResponse(false)
            }

        }

        fun queryUsers(userList: (ArrayList<Users>) -> Unit) {

            val queryUsers = CloudDBZoneQuery.where(Users::class.java)

            val queryTask = cloudDBZone?.executeQuery(
                queryUsers,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
            )

            queryTask?.addOnSuccessListener { snapshot ->
                val usersList = arrayListOf<Users>()
                try {
                    while (snapshot.snapshotObjects.hasNext()) {
                        val user = snapshot.snapshotObjects.next()
                        usersList.add(user)
                    }
                } catch (e: AGConnectCloudDBException) {
                    Log.e(TAG, "processQueryResultExc: " + e.message)
                } finally {
                    userList(usersList)
                    snapshot.release()
                }
            }?.addOnFailureListener {
                Log.e(TAG, "Fail processQueryResult: " + it.message)
            }
        }

        fun upsertUser(user: Users, callback: (isSuccessful: Boolean) -> Unit) {
            if (cloudDBZone == null) {
                Log.w(TAG, "CloudDBZone is null, try re-open it")
                return
            }
            val upsertTask = cloudDBZone!!.executeUpsert(user)
            upsertTask.addOnSuccessListener { cloudDBZoneResult ->
                Log.i(TAG, "Upsert $cloudDBZoneResult records")
                callback(true)
            }.addOnFailureListener {
                Log.e(TAG, "Fail processUpsertResult: " + it.message)
                callback(false)
            }
        }

        fun deleteUser(user: Users, callback: (isSuccessful: Boolean) -> Unit){
            if (cloudDBZone == null) {
                Log.w(TAG, "CloudDBZone is null, try re-open it")
                return
            }

            val deleteTask = cloudDBZone!!.executeDelete(user)
            deleteTask.addOnSuccessListener{
                Log.i(TAG, "deleteUser: Deleted $it user(s).")
                callback(true)
            }.addOnFailureListener {
                Log.i(TAG, "deleteUser: Delete failed! ${it.message}")
                callback(false)
            }
        }
    }

}