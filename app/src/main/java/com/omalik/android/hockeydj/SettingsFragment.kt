package com.omalik.android.hockeydj

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.omalik.android.hockeydj.BackgroundSet.applicationContext
import com.omalik.android.hockeydj.databinding.FragmentSettingsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class SettingsFragment : Fragment(), View.OnClickListener, SeekBar.OnSeekBarChangeListener,
    ImageChanged {
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
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_settings, container, false)

        binding.scaleType.setOnClickListener(this)
        binding.loadImage.setOnClickListener(this)
        binding.useDefault.setOnClickListener(this)
        binding.quickhelp.setOnClickListener(this)
        binding.acknowledgements.setOnClickListener(this)
        alpha = (BackgroundSet.alpha * 100).toInt()
        binding.transparency.progress = alpha
        binding.transparency.setOnSeekBarChangeListener(this)
        binding.imagePreview.setImageDrawable(BackgroundSet.bitmap)
        binding.imagePreview.alpha =
            BackgroundSet.alpha
        binding.imagePreview.scaleType =
            BackgroundSet.scaleType
        binding.progressBar.visibility = View.GONE

        val prefs = applicationContext.getSharedPreferences("hockeyDJPrefs", Context.MODE_PRIVATE)

        val scale = prefs.getInt("scale", 1)

        if (scale == 1) {
            binding.scaleType.text = "FILL IMAGE"
        } else {
            binding.scaleType.text = "FIT IMAGE"
        }

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
            binding.quickhelp -> {
                val quickHelp = QuickHelp()
                quickHelp.show(this.activity!!.supportFragmentManager,"help")
            }
            binding.acknowledgements -> {
                val quickHelp = QuickHelp()
                quickHelp.show(this.activity!!.supportFragmentManager,"ack")
            }
        }
    }

    fun saveDefaultImageAsync() = coroutineScope.launch(Dispatchers.IO) {
        val bitmap = BitmapFactory.decodeResource(resources,
            R.drawable.background
        )
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
        BackgroundSet.loadImage()
        updatePreview()
    }

    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    fun updatePreview() = coroutineScope.launch(Dispatchers.Main) {
        binding.imagePreview.setImageDrawable(BackgroundSet.bitmap)
        binding.progressBar.visibility = View.GONE
        binding.imagePreview.alpha =
            BackgroundSet.alpha
    }
}
