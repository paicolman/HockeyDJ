package com.android.example.hockeydj

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import com.android.example.hockeydj.BackgroundSet.applicationContext
import com.android.example.hockeydj.databinding.FragmentMusicSelectionListBinding
import com.android.example.hockeydj.databinding.FragmentSettingsBinding
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class SettingsFragment : Fragment(), View.OnClickListener, SeekBar.OnSeekBarChangeListener, ImageChanged {
    val TAG = "JZ:SettingsFragment"

    //data binding
    lateinit var binding: FragmentSettingsBinding

    //backgroundSet
    val backgroundSet = BackgroundSet

    //alpha
    var alpha: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val act = activity as MainActivity
        act.imageChanged = this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)

        binding.scaleType.setOnClickListener(this)
        binding.loadImage.setOnClickListener(this)
        binding.useDefault.setOnClickListener(this)
        alpha = (backgroundSet.alpha * 100).toInt()
        binding.transparency.progress = alpha
        binding.transparency.setOnSeekBarChangeListener(this)
        binding.imagePreview.setImageDrawable(backgroundSet.bitmap)
        binding.imagePreview.alpha = backgroundSet.alpha
        binding.imagePreview.scaleType = backgroundSet.scaleType
        binding.progressBar.visibility = View.GONE

        return binding.root
    }

    override fun onClick(v: View?) {
        when(v) {
            binding.scaleType -> {
                val prefs = applicationContext.getSharedPreferences("hockeyDJPrefs", Context.MODE_PRIVATE)?.edit()

                if (binding.imagePreview.scaleType == ImageView.ScaleType.FIT_CENTER) {
                    binding.imagePreview.scaleType = ImageView.ScaleType.CENTER
                    binding.scaleType.text = "FIT IMAGE"
                    prefs?.putInt("scale", 2)
                } else {
                    binding.imagePreview.scaleType = ImageView.ScaleType.FIT_CENTER
                    binding.scaleType.text = "FILL IMAGE"
                    prefs?.putInt("scale", 1)
                }
                prefs?.apply()
            }
            binding.loadImage -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.imagePreview.alpha = 0.1f
                val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, 3141)
            }
            binding.useDefault -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.imagePreview.alpha = 0.1f
                saveDefaultImageAsync()
            }
        }
    }

    fun saveDefaultImageAsync() = coroutineScope.launch(Dispatchers.IO) {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.background)
        val mainActivity = activity as MainActivity
        val scaledBitmap = mainActivity.scaleBitmap(bitmap)

        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir("images", Context.MODE_PRIVATE)
        file = File(file, "backgroundImage.png")

        try {
            Log.d(TAG, "Saving default image...")
            val stream: OutputStream = FileOutputStream(file)
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()
            imageReady()
        } catch (e: IOException){ // Catch the exception
            e.printStackTrace()
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        //Log.d(TAG, "Progress:$progress")
        binding.imagePreview.alpha = progress/100f
        alpha = progress
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        Log.d(TAG, "seekStart")
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        Log.d(TAG, "seekEnd")
        val prefs = applicationContext.getSharedPreferences("hockeyDJPrefs", Context.MODE_PRIVATE)?.edit()
        prefs.let {
            Log.d(TAG, "PREFS SEEMS TO BE OK?? aplha: $alpha")
        }
        prefs?.putInt("alpha", alpha)
        prefs?.apply()
    }

    override fun imageReady() {
        backgroundSet.loadImage()
        updatePreview()
    }

    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    fun updatePreview() = coroutineScope.launch(Dispatchers.Main) {
        binding.imagePreview.setImageDrawable(backgroundSet.bitmap)
        binding.progressBar.visibility = View.GONE
        binding.imagePreview.alpha = backgroundSet.alpha
    }
}
