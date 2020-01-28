package com.omalik.android.hockeydj

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.omalik.android.hockeydj.databinding.FragmentPlaylistSelectionBinding

class PlaylistSelectionFragment : Fragment(), View.OnClickListener {

    val TAG = "JZ:PlaylistSelection"

    //Room DB
    private lateinit var trackViewModel: TrackViewModel

    //data binding
    lateinit var binding: FragmentPlaylistSelectionBinding

    //navigation
    lateinit var navController: NavController

    //Playlist info
    var playlistInfo = arrayOf("","","","","","","")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        trackViewModel = ViewModelProvider(this).get(TrackViewModel::class.java)
        addPlaylistObserver()

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Handle the back button event
            Log.d(TAG, "Back button thing disabled...")
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView")
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_playlist_selection, container,false)

        val backgroundSet = BackgroundSet
        backgroundSet.applicationContext = requireContext()
        backgroundSet.loadImage()
        backgroundSet.loadSettings()

        Glide.with(this).load(BackgroundSet.bitmap).into(binding.backPane)

        //binding.backPane.setImageDrawable(BackgroundSet.bitmap)
        binding.backPane.alpha = BackgroundSet.alpha
        binding.backPane.scaleType =
            BackgroundSet.scaleType

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        navController = Navigation.findNavController(view)

        binding.homeGoal.setOnClickListener(this)
        binding.homeFault.setOnClickListener(this)
        binding.homeTimeout.setOnClickListener(this)
        binding.guestGoal.setOnClickListener(this)
        binding.guestFault.setOnClickListener(this)
        binding.guestTimeout.setOnClickListener(this)
        binding.generalInterruption.setOnClickListener(this)

        binding.homeGoalDelete.setOnClickListener(this)
        binding.homeFaultDelete.setOnClickListener(this)
        binding.homeTimeDelete.setOnClickListener(this)
        binding.guestGoalDelete.setOnClickListener(this)
        binding.guestFaultDelete.setOnClickListener(this)
        binding.guestTimeDelete.setOnClickListener(this)
        binding.generalIntrptDelete.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        trackViewModel = ViewModelProvider(this).get(TrackViewModel::class.java)
        addPlaylistObserver()

        val backgroundSet = BackgroundSet
        backgroundSet.applicationContext = requireContext()
        backgroundSet.loadImage()
        backgroundSet.loadSettings()

        binding.backPane.setImageDrawable(BackgroundSet.bitmap)
        binding.backPane.alpha = BackgroundSet.alpha
        binding.backPane.scaleType =
            BackgroundSet.scaleType

    }

    override fun onClick(v: View?) {

        when(v){
            binding.homeGoal, binding.homeFault, binding.homeTimeout,
            binding.guestGoal, binding.guestFault, binding.guestTimeout,
            binding.generalInterruption -> {
                v.let {playlistView ->
                    playlistView.setBackgroundColor(android.graphics.Color.parseColor("#e6e6e6"))

                    var name = ""
                    var icon = 0
                    var info = ""

                    when (playlistView.tag.toString()) {
                        "homeGoal" -> {
                            name = "Home Goals playlist"
                            icon =
                                R.drawable.goal_list
                            info = playlistInfo[0]
                        }
                        "homeFault" -> {
                            name = "Home Faults playlist"
                            icon =
                                R.drawable.fault_list
                            info = playlistInfo[1]
                        }
                        "homeTimeout" -> {
                            name = "Home Timeouts playlist"
                            icon =
                                R.drawable.time_list
                            info = playlistInfo[2]
                        }
                        "guestGoal" -> {
                            name = "Guest Goals playlist"
                            icon =
                                R.drawable.goal_list
                            info = playlistInfo[3]
                        }
                        "guestFault" -> {
                            name = "Guest Faults playlist"
                            icon =
                                R.drawable.fault_list
                            info = playlistInfo[4]
                        }
                        "guestTimeout" -> {
                            name = "Guest Timeouts playlist"
                            icon =
                                R.drawable.time_list
                            info = playlistInfo[5]
                        }
                        "genIntrpt" -> {
                            name = "Game Int. playlist"
                            icon =
                                R.drawable.gen_list
                            info = playlistInfo[6]
                        }
                    }

                    val bundle = bundleOf(
                        "playlist" to playlistView.tag.toString(),
                        "name" to name,
                        "icon" to icon,
                        "info" to info
                    )

                    navController.navigate(R.id.action_playlistSelectionFragment_to_musicFilterFragment, bundle)
                }
            }
            binding.homeGoalDelete, binding.homeFaultDelete, binding.homeTimeDelete,
            binding.guestGoalDelete, binding.guestFaultDelete, binding.guestTimeDelete,
            binding.generalIntrptDelete -> {
                val bundle = bundleOf(
                    "playlist" to v.tag.toString()
                )
                val deleteDialog =
                    PlaylistDeleteDialog()
                deleteDialog.arguments = bundle
                deleteDialog.show(parentFragmentManager,"delete_dialog")
            }
        }
    }

    private fun addPlaylistObserver(){
        trackViewModel.homeGoalPlaylist.observe (this, Observer {
            playlistInfo[0] = "Contains: ${it.size} songs"
            binding.homeGoalSongs.text = playlistInfo[0]
        })
        trackViewModel.homeFaultPlaylist.observe(this, Observer {
            playlistInfo[1] = "Contains: ${it.size} songs"
            binding.homeFaultSongs.text = playlistInfo[1]
        })
        trackViewModel.homeTimeoutPlaylist.observe(this, Observer {
            playlistInfo[2] = "Contains: ${it.size} songs"
            binding.homeTimeSongs.text = playlistInfo[2]
        })
        trackViewModel.guestGoalPlaylist.observe(this, Observer {
            playlistInfo[3] = "Contains: ${it.size} songs"
            binding.guestGoalSongs.text = playlistInfo[3]
        })
        trackViewModel.guestFaultPlaylist.observe(this, Observer {
            playlistInfo[4] = "Contains: ${it.size} songs"
            binding.guestFaultSongs.text = playlistInfo[4]
        })
        trackViewModel.guestTimeoutPlaylist.observe(this, Observer {
            playlistInfo[5] = "Contains: ${it.size} songs"
            binding.guestTimeSongs.text = playlistInfo[5]
        })
        trackViewModel.generalPlaylist.observe(this, Observer {
            playlistInfo[6] = "Contains: ${it.size} songs"
            binding.generalIntrptSongs.text = playlistInfo[6]
        })

    }


}
