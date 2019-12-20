package com.android.example.hockeydj

import android.Manifest
import android.R.attr.data
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import com.android.example.hockeydj.SpotifyService.context
import com.android.example.hockeydj.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout
import com.spotify.sdk.android.authentication.AuthenticationResponse
import kotlinx.coroutines.*
import java.io.*
import java.lang.Double.max
import java.lang.Double.min


class MainActivity : AppCompatActivity(), SpotifyConnectionInterface {

    val TAG = "JZ:MainActivity"
    val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: Int = 180569

    //Spotify
    val spotify = SpotifyService
    var spotifyReady: SpotifyReady? = null
    var imageChanged: ImageChanged? = null

    //data binding
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Starting the app...")
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val viewPager: ViewPager
        val tabLayout: TabLayout

        viewPager = findViewById(R.id.container)
        tabLayout = findViewById(R.id.tablayout)

        getPermission()

        window.setBackgroundDrawableResource(R.color.white)

        spotify.connectionInterface = this
        spotify.activity = this
        spotify.context = this
        Log.d(TAG, "authentication Spotify...")
        spotify.authenticate()

        setupViewPager(viewPager)
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))
    }



    fun setupViewPager(viewPager: ViewPager) {
        val pageAdapter = PagerAdapter(supportFragmentManager)

        pageAdapter.addFragment(PlayFragment())
        pageAdapter.addFragment(NavigationFragment())
        pageAdapter.addFragment(SettingsFragment())

        viewPager.adapter = pageAdapter
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getPermission() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // TODO: Explain to the user why we need to access external content
            }

            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

            //  is an
            // app-defined int constant that should be quite unique

            return;
        }
    }

    override fun spotifyAvailable() {
        Log.d(TAG, "Spotify is now available")
        spotifyReady?.spotifyReady()
    }

    override fun authenticationError(response: AuthenticationResponse) {
        Log.e(TAG, "Spotify Authentication Error: ${response.error}")
        val errorDialog = ConnectionErrorDialog()
        val bundle = bundleOf(
            "error" to "ERROR: ${response.error}"
        )
        errorDialog.arguments = bundle
        errorDialog.show(supportFragmentManager, "")
    }

    override fun connectionError(msg: String?, throwable: Throwable) {
        Log.e(TAG, "Spotify Connection Error: $msg")
        val errorDialog = ConnectionErrorDialog()
        val bundle = bundleOf(
            "error" to "ERROR: $msg"
        )
        errorDialog.arguments = bundle
        errorDialog.show(supportFragmentManager, "")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        when(requestCode) {

            spotify.REQUEST_CODE -> {
                spotify.onActivityResult(requestCode, resultCode, intent)
            }

            else -> {
                if(resultCode == Activity.RESULT_OK) {
                    Log.d(TAG, "Pic requested. Uri: ${intent?.data}")
                    saveImageAsync(intent?.data)
                }
            }

        }
    }

    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    fun saveImageAsync(imageUri: Uri?) = coroutineScope.launch(Dispatchers.IO) {
        if (imageUri != null) {
            val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
            val scaledBitmap = scaleBitmap(bitmap)
            val wrapper = ContextWrapper(applicationContext)
            var file = wrapper.getDir("images", Context.MODE_PRIVATE)
            file = File(file, "backgroundImage.png")

            try {
                Log.d(TAG, "Saving image from gallery...")
                val stream: OutputStream = FileOutputStream(file)
                scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.flush()
                stream.close()
                imageChanged?.imageReady()
            } catch (e: IOException){ // Catch the exception
                e.printStackTrace()
            }
        }
    }

    fun scaleBitmap(bitmap: Bitmap) : Bitmap {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        if (bitmap.width > 2 * displayMetrics.widthPixels) {
            val widthRatio: Double =  displayMetrics.widthPixels.toDouble() / bitmap.width.toDouble()
            val heightRatio: Double = displayMetrics.heightPixels.toDouble() / bitmap.height.toDouble()
            val minRatio = min(widthRatio, heightRatio) * 2
            val scaleWidth = (bitmap.width * minRatio).toInt()
            val scaleHeight = (bitmap.height * minRatio).toInt()
            Log.d(TAG, "Display: ${displayMetrics.widthPixels}x${displayMetrics.heightPixels}")
            Log.d(TAG, "Bitmap: ${bitmap.width}x${bitmap.height}")
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaleWidth, scaleHeight, true)
            return scaledBitmap
        } else {
            return bitmap
        }
    }
}

interface SpotifyReady {
    fun spotifyReady()
}

interface ImageChanged {
    fun imageReady()
}