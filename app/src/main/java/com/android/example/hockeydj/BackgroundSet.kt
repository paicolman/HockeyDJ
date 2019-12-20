package com.android.example.hockeydj

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

object BackgroundSet {
    val TAG = "JZ:BackgroundSet"

    var bitmap: Drawable? = null
    lateinit var applicationContext: Context
    var alpha: Float = 0.5f
    var scaleType = ImageView.ScaleType.FIT_CENTER

    init {
    }

    fun loadImage() {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir("images", Context.MODE_PRIVATE)
        file = File(file, "backgroundImage.png")

        try {
            val stream: InputStream = FileInputStream(file)
            bitmap = Drawable.createFromStream(stream, "background_set")
            //bitmap = BitmapFactory.decodeStream(stream)
            Log.d(TAG, "Image loaded successfully")
        } catch (e: IOException) {
            Log.e(TAG, "Image could not be loaded")
            bitmap = null
        }
    }

    fun loadSettings() {
        val prefs = applicationContext.getSharedPreferences("hockeyDJPrefs", Context.MODE_PRIVATE)
        val alpha100 = prefs.getInt("alpha", 50)
        alpha = alpha100 / 100f
        val scale = prefs.getInt("scale", 1)
        when(scale) {
            1 -> {
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
            2 -> {
                scaleType = ImageView.ScaleType.CENTER
            }
        }

    }
}