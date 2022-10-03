package com.besirkaraoglu.locationsharingsampleii.util

import android.util.Log

class LocationLog {

    companion object {
        const val DEBUG = Log.DEBUG
        const val INFO = Log.INFO
        const val WARN = Log.WARN
        const val ERROR = Log.ERROR

        fun d(tag: String?, msg: String?, tr: Throwable?) {
            println(DEBUG, tag, msg, tr)
        }

        fun d(tag: String?, msg: String?) {
            d(tag, msg, null)
        }

        fun i(tag: String?, msg: String?, tr: Throwable?) {
            println(INFO, tag, msg, tr)
        }

        fun i(tag: String?, msg: String?) {
            i(tag, msg, null)
        }

        fun w(tag: String?, msg: String?, tr: Throwable?) {
            println(WARN, tag, msg, tr)
        }

        fun w(tag: String?, msg: String?) {
            w(tag, msg, null)
        }

        fun e(tag: String?, msg: String?, tr: Throwable?) {
            println(ERROR, tag, msg, tr)
        }

        fun e(tag: String?, msg: String?) {
            e(tag, msg, null)
        }

        private fun println(
            priority: Int,
            tag: String?,
            msg: String?,
            tr: Throwable?
        ) {
            Log.e(tag, "println: $msg,$tr")
        }


    }
}