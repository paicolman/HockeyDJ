package com.android.example.hockeydj

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import com.android.example.hockeydj.databinding.FragmentMusicSelectionListBinding
import com.android.volley.VolleyError
import kotlinx.android.synthetic.main.fragment_playlist_selection.*


class MusicSelectionFragment : Fragment(), SpotifyTrackInterface, MusicSelectionAdapter.OnNoteCheckboxListener {

    private val TAG = "JZ:MusicSelectionFragment"

    var hockeyPlaylist = ""

    //Database Tracklist
    lateinit var activeHoekyPlaylist: List<Track>

    //navigation
    lateinit var navController: NavController

    //data binding
    lateinit var binding: FragmentMusicSelectionListBinding

    private lateinit var trackViewModel: TrackViewModel


    private val spotify = SpotifyService
    private var albumArtwork: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        trackViewModel = ViewModelProvider(this).get(TrackViewModel::class.java)

        //trackViewModel.deleteAll()

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Handle the back button event
            navController.popBackStack()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_music_selection_list, container, false)

        val mode = arguments?.getString("mode")
        val tracksUrl = arguments?.getString("tracksUrl")!! //TODO: This is ok-ish, but not really safe...
        albumArtwork = arguments?.getParcelable<Bitmap?>("albumImage")!! //TODO: This is ok-ish, but not really safe...
        hockeyPlaylist = arguments?.getString("playlist")!! //TODO: This is ok-ish, but not really safe...

        addPlaylistObserver()

        spotify.trackInterface = this
        when (mode) {
            "playlist" -> spotify.getTracksFromPlaylist(tracksUrl)
            "album" -> spotify.getTracksFromAlbum(tracksUrl)
        }


        return binding.root
    }

    private fun addPlaylistObserver(){
        when (hockeyPlaylist) {
            "homeGoal" -> {
                trackViewModel.homeGoalPlaylist.observe (this, Observer {
                    Log.d(TAG, "homeGoalPlaylist size: ${it.size}")
                    activeHoekyPlaylist = it
                })
            }
            "homeFault" -> {
                trackViewModel.homeFaultPlaylist.observe(this, Observer {
                    Log.d(TAG, "homeFaultPlaylist size: ${it.size}")
                    activeHoekyPlaylist = it
                })
            }
            "homeTimeout" -> {
                trackViewModel.homeTimeoutPlaylist.observe(this, Observer {
                    Log.d(TAG, "homeTimeoutPlaylist size: ${it.size}")
                    activeHoekyPlaylist = it
                })
            }
            "guestGoal" -> {
                trackViewModel.guestGoalPlaylist.observe(this, Observer {
                    Log.d(TAG, "guestGoalPlaylist size: ${it.size}")
                    activeHoekyPlaylist = it
                })
            }
            "guestFault" -> {
                trackViewModel.guestFaultPlaylist.observe(this, Observer {
                    Log.d(TAG, "guestFaultPlaylist size: ${it.size}")
                    activeHoekyPlaylist = it
                })
            }
            "guestTimeout" -> {
                trackViewModel.guestTimeoutPlaylist.observe(this, Observer {
                    Log.d(TAG, "guestTimeoutPlaylist size: ${it.size}")
                    activeHoekyPlaylist = it
                })
            }
            "genIntrpt" -> {
                trackViewModel.generalPlaylist.observe(this, Observer {
                    Log.d(TAG, "generalPlaylist size: ${it.size}")
                    activeHoekyPlaylist = it
                })
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
    }

    override fun tracksHandler(tracks: List<Track>) {
        Log.d(TAG, "We got the tracks...")
        //Update the ones already in the playlist...
        //FIXME: reimplement this

        val songList = binding.songRecyclerView
        songList.layoutManager = LinearLayoutManager(context)
        songList.addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        songList.adapter = MusicSelectionAdapter(this.context!!, tracks, activeHoekyPlaylist, albumArtwork, this)
    }

    override fun trackRequestError(volleyError: VolleyError) {
        Log.e(TAG, "Error getting tracks...")
    }

    override fun onCheckboxClicked(track: Track, isChecked: Boolean) {
        Log.d(TAG, "Checkbox clicked, check: $isChecked")

        var trackToSave = track.copy()

        when (hockeyPlaylist) {
            "homeGoal" -> { trackToSave = track.copy(inHomeGoal = isChecked) }
            "homeFault" -> { trackToSave = track.copy(inHomeFault = isChecked) }
            "homeTimeout" -> { trackToSave = track.copy(inHomeTimeout = isChecked) }
            "guestGoal" -> { trackToSave = track.copy(inGuestGoal = isChecked) }
            "guestFault" -> { trackToSave = track.copy(inGuestFault = isChecked) }
            "guestTimeout" -> { trackToSave = track.copy(inGuestTimeout = isChecked) }
            "genIntrpt" -> { trackToSave = track.copy(inGeneralInt = isChecked) }
        }

        trackViewModel.insertOrUpdate(trackToSave)
    }

//    companion object {
//        var mode: String? = null
//        var tracksUrl: String = ""
//        var artwork: Bitmap? = null
//        var playlist: String = ""
//    }
}