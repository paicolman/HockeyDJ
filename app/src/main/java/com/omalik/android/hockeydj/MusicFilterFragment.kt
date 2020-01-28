package com.omalik.android.hockeydj

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.omalik.android.hockeydj.databinding.FragmentMusicFilterBinding
import com.android.volley.VolleyError
import com.bumptech.glide.Glide


class MusicFilterFragment : Fragment(),
    MusicFilterAdapter.OnNoteListener,
    SpotifyDataInterface {

    private val TAG = "JZ:MusicFilterFrag"
    private val spotify = SpotifyService

    private var mode = "playlist"
    private var hockeyPlaylist = ""

    //data binding
    lateinit var binding: FragmentMusicFilterBinding

    //RecyclerView
    lateinit var musicList: RecyclerView

    //Playlist Info
    var iconId = 0
    var name = ""
    var info = ""

    //navigation
    lateinit var navController: NavController

    override fun onNoteClick(tracksUrl: String, holderImage:Bitmap?) {
        val bundle = bundleOf(
            "playlist" to hockeyPlaylist,
            "mode" to mode,
            "tracksUrl" to tracksUrl,
            "albumImage" to holderImage,
            "icon" to iconId,
            "name" to name,
            "info" to info
        )
        navController.navigate(R.id.action_musicFilterFragment_to_musicSelectionFragment, bundle)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SpotifyService.dataInterface = this

        requireActivity().onBackPressedDispatcher.addCallback(this) {
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
            iconId = args.getInt("icon")
            name = args.getString("name","")
            info = args.getString("info","")
            storedPlaylist = playlist
        }

        hockeyPlaylist =
            storedPlaylist

        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_music_filter, container,false)

        //binding.filterSelect.setOnClickListener(this)
        musicList = binding.musicRecyclerView
        musicList.addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        musicList.layoutManager = LinearLayoutManager(context)

        binding.playlistIcon.setImageBitmap(BitmapFactory.decodeResource(resources, iconId))
        binding.playlistTitle.text = name
        binding.playlistSize.text = info

        val backgroundSet = BackgroundSet
        backgroundSet.applicationContext = requireContext()
        backgroundSet.loadImage()
        backgroundSet.loadSettings()

        Glide.with(this).load(BackgroundSet.bitmap).into(binding.backPane)

        //binding.backPane.setImageDrawable(BackgroundSet.bitmap)
        binding.backPane.alpha = BackgroundSet.alpha
        binding.backPane.scaleType =
            BackgroundSet.scaleType

        SpotifyService.getPlaylists()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
    }

    override fun onResume() {
        super.onResume()

        val backgroundSet = BackgroundSet
        backgroundSet.applicationContext = requireContext()
        backgroundSet.loadImage()
        backgroundSet.loadSettings()

        binding.backPane.setImageDrawable(BackgroundSet.bitmap)
        binding.backPane.alpha = BackgroundSet.alpha
        binding.backPane.scaleType =
            BackgroundSet.scaleType
    }

    fun updateMusicAdapter(items: List<MediaCollection>){

        val adapter = MusicFilterAdapter(
            this.context!!,
            items,
            this
        )
        musicList.adapter = adapter

    }

    override fun mediaCollectionHandler(mediaCollection: List<MediaCollection>?) {
        Log.d(TAG, "Now we need to show those playlists...")
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
