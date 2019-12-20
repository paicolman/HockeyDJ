package com.android.example.hockeydj

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.example.hockeydj.BackgroundSet.applicationContext
import com.android.example.hockeydj.databinding.FragmentPlayBinding
import com.spotify.sdk.android.authentication.AuthenticationResponse
import kotlin.random.Random


class PlayFragment : Fragment(), View.OnClickListener, SpotifyReady, SpotifyControlInterface {

    val TAG = "JZ:PlayFragment"

    //Room DB
    private lateinit var trackViewModel: TrackViewModel

    //data binding
    lateinit var binding: FragmentPlayBinding

    //Spotify
    private var spotify: SpotifyService? = null// = SpotifyService

    //Active button
    private var activeButton: View? = null
    private var activeColor: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)

        val act = this.activity as MainActivity
        act.spotifyReady = this
        trackViewModel = ViewModelProvider(this).get(TrackViewModel::class.java)
        addPlaylistObserver()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView")
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_play, container, false)

        binding.homeGoalButton.setOnClickListener(this)
        binding.guestGoalButton.setOnClickListener(this)
        binding.homeFaultButton.setOnClickListener(this)
        binding.guestFaultButton.setOnClickListener(this)
        binding.homeTimeoutButton.setOnClickListener(this)
        binding.guestTimeoutButton.setOnClickListener(this)
        binding.generalInterruptButton.setOnClickListener(this)
        binding.stopButton.setOnClickListener(this)
        binding.nowPlaying.setSelected(true)

        val backgroundSet = BackgroundSet
        backgroundSet.applicationContext = requireContext()
        backgroundSet.loadImage()
        backgroundSet.loadSettings()

        binding.backPane.setImageDrawable(backgroundSet.bitmap)
        binding.backPane.alpha = backgroundSet.alpha
        binding.backPane.scaleType = backgroundSet.scaleType

//        binding.playFrame.background = backgroundSet.bitmap
//        binding.playFrame.background.alpha = backgroundSet.alpha
//        binding.playFrame.background
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        activateButton(trackViewModel.homeGoalPlaylist, binding.homeGoalButton, R.drawable.goal_off)
        activateButton(trackViewModel.guestGoalPlaylist, binding.guestGoalButton, R.drawable.goal_off)
        activateButton(trackViewModel.homeFaultPlaylist, binding.homeFaultButton, R.drawable.fault_off)
        activateButton(trackViewModel.guestFaultPlaylist, binding.guestFaultButton,R.drawable.fault_off)
        activateButton(trackViewModel.homeTimeoutPlaylist, binding.homeTimeoutButton,R.drawable.time_off)
        activateButton(trackViewModel.guestTimeoutPlaylist, binding.guestTimeoutButton,R.drawable.time_off)
        activateButton(trackViewModel.generalPlaylist, binding.generalInterruptButton,R.drawable.general_off)

        val backgroundSet = BackgroundSet
        backgroundSet.applicationContext = requireContext()
        backgroundSet.loadImage()
        backgroundSet.loadSettings()

        binding.backPane.setImageDrawable(backgroundSet.bitmap)
        binding.backPane.alpha = backgroundSet.alpha
        binding.backPane.scaleType = backgroundSet.scaleType
    }

    private fun activateButton(livePlaylist: LiveData<List<Track>>, v:View, bgnd: Int) {
        if (!livePlaylist.value.isNullOrEmpty()) {
            v.isEnabled = true
            //v.setBackgroundColor(Color.parseColor(colorString))
            v.background = context?.getDrawable(bgnd)
        } else {
            v.isEnabled = false
            v.background = context?.getDrawable(R.drawable.disabled)
        }
    }

    private fun addPlaylistObserver(){
        trackViewModel.homeGoalPlaylist.observe (this, Observer {
            Log.d(TAG, "homeGoalPlaylist size: ${it.size}")
        })
        trackViewModel.homeFaultPlaylist.observe(this, Observer {
            Log.d(TAG, "homeFaultPlaylist size: ${it.size}")
        })
        trackViewModel.homeTimeoutPlaylist.observe(this, Observer {
            Log.d(TAG, "homeTimeoutPlaylist size: ${it.size}")
        })
        trackViewModel.guestGoalPlaylist.observe(this, Observer {
            Log.d(TAG, "guestGoalPlaylist size: ${it.size}")
        })
        trackViewModel.guestFaultPlaylist.observe(this, Observer {
            Log.d(TAG, "guestFaultPlaylist size: ${it.size}")
        })
        trackViewModel.guestTimeoutPlaylist.observe(this, Observer {
            Log.d(TAG, "guestTimeoutPlaylist size: ${it.size}")
        })
        trackViewModel.generalPlaylist.observe(this, Observer {
            Log.d(TAG, "generalPlaylist size: ${it.size}")
        })

    }


    override fun onClick(v: View?) {
        binding.stopButton.background = context?.getDrawable(R.drawable.stop_off)
        if (v != binding.stopButton) {
            var bm = BitmapFactory.decodeResource(resources, R.drawable.sound)
            binding.soundOnOff.setImageBitmap(Bitmap.createBitmap(bm))

        }
        activeButton?.background = context?.getDrawable(activeColor)
        when(v){
            binding.homeGoalButton -> {
                activeColor = R.drawable.goal_off
                binding.homeGoalButton.background = context?.getDrawable(R.drawable.goal_on)
                playRandom(trackViewModel.homeGoalPlaylist.value)
            }
            binding.guestGoalButton -> {
                activeColor = R.drawable.goal_off
                binding.guestGoalButton.background = context?.getDrawable(R.drawable.goal_on)
                //binding.guestGoalButton.setBackgroundColor(Color.parseColor("#ff31ff7f"))
                playRandom(trackViewModel.guestGoalPlaylist.value)
            }
            binding.homeFaultButton -> {
                activeColor = R.drawable.fault_off
                binding.homeFaultButton.background = context?.getDrawable(R.drawable.fault_on)
                //binding.homeFaultButton.setBackgroundColor(Color.parseColor("#fffeff67"))
                playRandom(trackViewModel.homeFaultPlaylist.value)
            }
            binding.guestFaultButton -> {
                activeColor = R.drawable.fault_off
                binding.guestFaultButton.background = context?.getDrawable(R.drawable.fault_on)
                playRandom(trackViewModel.guestFaultPlaylist.value)
            }
            binding.homeTimeoutButton -> {
                activeColor = R.drawable.time_off
                binding.homeTimeoutButton.background = context?.getDrawable(R.drawable.time_on)
                playRandom(trackViewModel.homeTimeoutPlaylist.value)
            }
            binding.guestTimeoutButton -> {
                activeColor = R.drawable.time_off
                binding.guestTimeoutButton.background = context?.getDrawable(R.drawable.time_on)
                playRandom(trackViewModel.guestTimeoutPlaylist.value)
            }
            binding.generalInterruptButton -> {
                activeColor = R.drawable.general_off
                binding.generalInterruptButton.background = context?.getDrawable(R.drawable.general_on)
                playRandom(trackViewModel.generalPlaylist.value)
            }
            binding.stopButton -> {
                activeColor = R.drawable.stop_off
                binding.stopButton.background = context?.getDrawable(R.drawable.stop_on)
                stop()
                var bm = BitmapFactory.decodeResource(resources, R.drawable.nosound)
                binding.soundOnOff.setImageBitmap(Bitmap.createBitmap(bm))
                binding.nowPlaying.text = ""
            }
        }
        activeButton = v
    }

    private fun playRandom(playlist: List<Track>?){
        if (playlist != null) {
            val randTrack = Random.nextInt(playlist.size)
            val trackToPlay = playlist[randTrack]
            Log.d(TAG, "Playing track: ${trackToPlay.trackUri}")
            spotify?.playTrack(trackToPlay.trackUri)
        } else {
            Log.e(TAG, "playlist is NULL!!")
        }
    }

    private fun stop(){
        spotify?.pause()
    }

    override fun spotifyReady() {
        Log.d(TAG, "Instantiating spotify in the fragment")
        spotify = SpotifyService
        spotify?.controlInterface = this
    }

    override fun playing() {
        Log.d(TAG, "We playing")

    }

    override fun paused() {
        Log.d(TAG, "We paused")
    }

    override fun nowPlaying(trackInfo: String) {
        binding.nowPlaying.text = trackInfo
    }
}
