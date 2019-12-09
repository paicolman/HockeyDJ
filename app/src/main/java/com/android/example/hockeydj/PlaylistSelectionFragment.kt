package com.android.example.hockeydj

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.android.example.hockeydj.databinding.FragmentPlaylistSelectionBinding
import kotlinx.android.synthetic.main.fragment_navigation.*

class PlaylistSelectionFragment : Fragment(), View.OnClickListener {

    val TAG = "JZ:PlaylistSelection"

    //Room DB
    private lateinit var trackViewModel: TrackViewModel

    //data binding
    lateinit var binding: FragmentPlaylistSelectionBinding

    //navigation
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        trackViewModel = ViewModelProvider(this).get(TrackViewModel::class.java)
        addPlaylistObserver()
        Log.d(TAG, "onCreate")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView")
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_playlist_selection, container,false)
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
    }

    override fun onResume() {
        super.onResume()
        trackViewModel = ViewModelProvider(this).get(TrackViewModel::class.java)
        addPlaylistObserver()
    }

    override fun onClick(v: View?) {

        when(v){
            binding.homeGoal, binding.homeFault, binding.homeTimeout,
            binding.guestGoal, binding.guestFault, binding.guestTimeout,
            binding.generalInterruption -> {
                v?.let {playlistView ->
                    Log.d(TAG,"PRESSED:${playlistView.tag.toString()}")
                    playlistView.setBackgroundColor(android.graphics.Color.parseColor("#e6e6e6"))

                    val bundle = bundleOf(
                        "playlist" to playlistView.tag.toString()
                    )

                    navController.navigate(R.id.action_playlistSelectionFragment_to_musicFilterFragment, bundle)
                }
            }
            binding.homeGoalDelete -> {
                val bundle = bundleOf(
                    "playlist" to "Home Goals" //IMPORTANT: Used also to select the DB column! Do not change!
                )
                val deleteDialog = PlaylistDeleteDialog()
                deleteDialog.arguments = bundle
                deleteDialog.show(parentFragmentManager,"delete_dialog")
            }
        }
    }

    private fun addPlaylistObserver(){
        trackViewModel.homeGoalPlaylist.observe (this, Observer {
            binding.homeGoalSongs.text = "Contains: ${it.size} songs"
        })
        trackViewModel.homeFaultPlaylist.observe(this, Observer {
            binding.homeFaultSongs.text = "Contains: ${it.size} songs"
        })
        trackViewModel.homeTimeoutPlaylist.observe(this, Observer {
            binding.homeTimeSongs.text = "Contains: ${it.size} songs"
        })
        trackViewModel.guestGoalPlaylist.observe(this, Observer {
            binding.guestGoalSongs.text = "Contains: ${it.size} songs"
        })
        trackViewModel.guestFaultPlaylist.observe(this, Observer {
            binding.guestFaultSongs.text = "Contains: ${it.size} songs"
        })
        trackViewModel.guestTimeoutPlaylist.observe(this, Observer {
            binding.guestTimeSongs.text = "Contains: ${it.size} songs"
        })
        trackViewModel.generalPlaylist.observe(this, Observer {
            binding.generalIntrptSongs.text = "Contains: ${it.size} songs"
        })

    }


}
