package com.besirkaraoglu.locationsharingsampleii.util

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.besirkaraoglu.locationsharingsampleii.R
import com.bumptech.glide.Glide

fun showToastLong(context: Context, message: String) =
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()

fun showToastShort(context: Context, message: String) =
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

fun loadImage(view: ImageView, url: String?) =
    Glide.with(view.context)
        .load(url)
        .error(R.drawable.ic_launcher_foreground)
        .centerCrop()
        .into(view)