package com.besirkaraoglu.locationsharingsampleii.util

import android.content.Context
import android.widget.Toast

fun showToastLong(context: Context, message: String) =
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()

fun showToastShort(context: Context, message: String) =
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()