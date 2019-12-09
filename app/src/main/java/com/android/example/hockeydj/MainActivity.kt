package com.android.example.hockeydj

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import com.android.example.hockeydj.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout
import com.spotify.sdk.android.authentication.AuthenticationResponse


class MainActivity : AppCompatActivity(), SpotifyConnectionInterface {

    val TAG = "JZ:MainActivity"
    val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: Int = 180569

    //Spotify
    val spotify = SpotifyService
    var spotifyReady: SpotifyReady? = null

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
        tabLayout = findViewById<TabLayout>(R.id.tablayout)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getPermission()
        }

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
    }

    override fun connectionError(msg: String?, throwable: Throwable) {
        Log.e(TAG, "Spotify Connection Error: $msg")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        spotify.onActivityResult(requestCode, resultCode, intent)
    }
}

interface SpotifyReady {
    fun spotifyReady()
}
