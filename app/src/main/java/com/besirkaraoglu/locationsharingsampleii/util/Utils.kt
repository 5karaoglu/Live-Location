package com.besirkaraoglu.locationsharingsampleii.util

object Utils {

    const val ACTION_PROCESS_LOCATION =
        "com.hms.locationkit.activity.location.ACTION_PROCESS_LOCATION"

    const val TAG = "LocationReceiver"

    var isListenActivityIdentification = false

    var isListenActivityConversion = false

    fun addConversionListener() {
        this.isListenActivityConversion = true
    }
    fun removeConversionListener() {
        this.isListenActivityConversion = false
    }

    fun addIdentificationListener() {
        this.isListenActivityIdentification = true
    }

    fun removeIdentificationListener() {
        this.isListenActivityIdentification = false
    }
}