package com.android.example.hockeydj

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.example.hockeydj.com.android.example.hockeydj.MusicProvider
import com.android.example.hockeydj.databinding.FragmentMusicFilterBinding
import com.android.volley.VolleyError


class MusicFilterFragment : Fragment(), MusicFilterAdapter.OnNoteListener, View.OnClickListener, SpotifyDataInterface  {

    private val TAG = "JZ:MusicFilterFrag"
    private val spotify = SpotifyService

    private var mode = "playlist"
    private var hockeyPlaylist = ""

    //data binding
    lateinit var binding: FragmentMusicFilterBinding

    //RecyclerView
    lateinit var musicList: RecyclerView
    //var musicNames = mutableListOf<String>()

    //navigation
    lateinit var navController: NavController

    override fun onNoteClick(tracksUrl: String, holderImage:Bitmap?) {
        val bundle = bundleOf(
            "playlist" to hockeyPlaylist,
            "mode" to mode,
            "tracksUrl" to tracksUrl,
            "albumImage" to holderImage
        )
        navController.navigate(R.id.action_musicFilterFragment_to_musicSelectionFragment, bundle)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        spotify.dataInterface = this

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Handle the back button event
            navController.popBackStack()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView")

        arguments.let {args ->
            //Got arguments? Store them in the companion object
            Log.d(TAG,"Got arguments: setting...")
            val playlist = args?.getString("playlist")!!
            storedPlaylist = playlist
        }

        hockeyPlaylist = storedPlaylist

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_music_filter, container,false)

        binding.filterSelect.setOnClickListener(this)
        musicList = binding.musicRecyclerView
        musicList.addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        musicList.layoutManager = LinearLayoutManager(context)

        spotify.getPlaylists()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
    }

    fun updateMusicAdapter(items: List<MediaCollection>){

        val adapter = MusicFilterAdapter(this.context!!, items, this)
        musicList.adapter = adapter

    }

    override fun onClick(v: View?) {
        Log.d(TAG, "CLICK!")
        when (v) {
            binding.filterSelect -> {
                when(binding.filterSelect.text) {
                    "PLAYLIST" -> {
                        binding.filterSelect.text = "ALBUM"
                        spotify.getAlbums()
                        mode = "album"
                    }
                    "ALBUM" -> {
                        binding.filterSelect.text = "PLAYLIST"
                        spotify.getPlaylists()
                        mode = "playlist"
                    }
                }
            }
        }
        //navController.popBackStack()
    }

    override fun mediaCollectionHandler(mediaCollection: List<MediaCollection>?) {
        Log.d(TAG, "Now we need to show them playlists...")
        mediaCollection?.let{
            updateMusicAdapter(it)
        }
    }

    override fun dataRequestError(volleyError: VolleyError) {
        Log.e(TAG,"Something went wrong: ${volleyError.localizedMessage}")
    }

    companion object {

        var storedPlaylist: String = ""
    }

}
