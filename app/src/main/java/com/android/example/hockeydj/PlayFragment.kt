package com.android.example.hockeydj

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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
        activateButton(trackViewModel.homeTimeoutPlaylist, binding.homeTimeoutButton,R.drawable.time_list)
        activateButton(trackViewModel.guestTimeoutPlaylist, binding.guestTimeoutButton,"#77fa6166")
        activateButton(trackViewModel.generalPlaylist, binding.generalInterruptButton,"#77157bfe")
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
        binding.stopButton.setBackgroundColor(Color.parseColor("#77dd0000"))
        if (v != binding.stopButton) {
            var bm = BitmapFactory.decodeResource(resources, R.drawable.sound)
            binding.soundOnOff.setImageBitmap(Bitmap.createBitmap(bm))

        }
        when(v){
            binding.homeGoalButton -> {
                activeButton?.background = context?.getDrawable(R.drawable.goal_off)
                activeColor = R.drawable.goal_off
                binding.homeGoalButton.background = context?.getDrawable(R.drawable.goal_on)
                playRandom(trackViewModel.homeGoalPlaylist.value)
            }
            binding.guestGoalButton -> {
                activeButton?.context?.getDrawable(R.drawable.goal_off)
                activeColor = R.drawable.goal_off
                binding.homeGoalButton.background = context?.getDrawable(R.drawable.goal_on)
                //binding.guestGoalButton.setBackgroundColor(Color.parseColor("#ff31ff7f"))
                playRandom(trackViewModel.guestGoalPlaylist.value)
            }
            binding.homeFaultButton -> {
                activeButton?.context?.getDrawable(R.drawable.fault_off)
                activeColor = R.drawable.fault_off
                binding.homeGoalButton.background = context?.getDrawable(R.drawable.fault_on)
                //binding.homeFaultButton.setBackgroundColor(Color.parseColor("#fffeff67"))
                playRandom(trackViewModel.homeFaultPlaylist.value)
            }
            binding.guestFaultButton -> {
                activeButton?.context?.getDrawable(R.drawable.fault_off)
                activeColor = R.drawable.goal_off
                binding.homeGoalButton.background = context?.getDrawable(R.drawable.fault_on)
                //binding.guestFaultButton.setBackgroundColor(Color.parseColor("#fffeff67"))
                playRandom(trackViewModel.guestFaultPlaylist.value)
            }
            binding.homeTimeoutButton -> {
                activeButton?.context?.getDrawable(R.drawable.goal_off)
                activeColor = R.drawable.goal_off
                binding.homeGoalButton.background = context?.getDrawable(R.drawable.goal_on)
                //binding.homeTimeoutButton.setBackgroundColor(Color.parseColor("#fffa6166"))
                playRandom(trackViewModel.homeTimeoutPlaylist.value)
            }
            binding.guestTimeoutButton -> {
                activeButton?.context?.getDrawable(R.drawable.goal_off)
                activeColor = R.drawable.goal_off
                binding.homeGoalButton.background = context?.getDrawable(R.drawable.goal_on)
                //binding.guestTimeoutButton.setBackgroundColor(Color.parseColor("#fffa6166"))
                playRandom(trackViewModel.guestTimeoutPlaylist.value)
            }
            binding.generalInterruptButton -> {
                activeButton?.context?.getDrawable(R.drawable.goal_off)
                activeColor = R.drawable.goal_off
                binding.homeGoalButton.background = context?.getDrawable(R.drawable.goal_on)
                //binding.generalInterruptButton.setBackgroundColor(Color.parseColor("#ff157bfe"))
                playRandom(trackViewModel.generalPlaylist.value)
            }
            binding.stopButton -> {
                activeButton?.context?.getDrawable(R.drawable.goal_off)
                activeColor = R.drawable.goal_off
                binding.stopButton.setBackgroundColor(Color.parseColor("#ffff0000"))
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
