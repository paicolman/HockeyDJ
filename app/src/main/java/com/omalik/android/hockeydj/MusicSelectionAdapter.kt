package com.omalik.android.hockeydj

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.Volley

import kotlinx.android.synthetic.main.fragment_music_selection.view.*


class MusicSelectionAdapter(private val context: Context, private val songs: List<Track>, private val hockeyTracks: List<Track>, private val albumArtwork: Bitmap?, val noteCheckboxListener: OnNoteCheckboxListener) : RecyclerView.Adapter<MusicSelectionAdapter.ViewHolder>() {

    val TAG = "JZ:MusicSelectionAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_music_selection, parent, false)
        return ViewHolder(view, noteCheckboxListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songs[position]
        holder.song = song
        holder.trackName.text = song.name
        holder.track = song.trackUri
        holder.checkbox.isChecked = checkTrackIsSelected(song)
        holder.pos = position
        holder.playButton.setImageResource(android.R.drawable.ic_media_play)
        requestBitmap(song.imageUrl, holder)
    }

    private fun checkTrackIsSelected(track: Track): Boolean {
        for (hockeyTrack in hockeyTracks) {

            if (track.trackUri == hockeyTrack.trackUri) {
                Log.d(TAG, "${track.trackUri} = ${hockeyTrack.trackUri} !")
                return true
            }
        }
        return false
    }

    fun requestBitmap(url: String, holder: ViewHolder) {

        if (url != "") {
            //Log.d(TAG, "Requesting bitmap: $url holder width:${holder.trackArtwork.width}")
            val queue = Volley.newRequestQueue(context)
            val request = ImageRequest(url,
                Response.Listener<Bitmap> {
                    //Log.d(TAG, "Got image - holder width:${holder.trackArtwork.width}")
                    imageHandler(it, holder)
                },
                2000,
                2000,
                ImageView.ScaleType.FIT_CENTER,
                Bitmap.Config.HARDWARE,
                Response.ErrorListener {
                    Log.e(TAG, "Error while requesting the Bitmap!")
                })
            queue.add(request)
        } else{
            Log.d(TAG, "Using album bitmap...")

            holder.observer.addOnPreDrawListener{
                albumArtwork?.let{
                    imageHandler(it, holder)
                }
                true
            }
        }
    }

    fun imageHandler(image: Bitmap, holder: ViewHolder){
        val w = holder.trackArtwork.width
        val h = holder.trackArtwork.height
        val resized = Bitmap.createScaledBitmap(image, w, h, true)
        holder.trackArtwork.setImageBitmap(resized)
    }

    override fun getItemCount(): Int {
        return songs.count()
    }

    inner class ViewHolder(mView: View, val noteCheckboxListener: OnNoteCheckboxListener) : RecyclerView.ViewHolder(mView), View.OnClickListener {

        val spotify = SpotifyService
        lateinit var song: Track

        val trackName: TextView = mView.song_title
        val trackArtwork: ImageView = mView.artwork
        val playButton: ImageView = mView.play_button
        val checkbox = mView.checkBox
        var track = ""
        var playing = false
        val observer = trackArtwork.viewTreeObserver
        var pos = -1

        init {
            playButton.setOnClickListener(this)
            checkbox.setOnClickListener(this)
        }

        fun delayedStop() {

        }

        val handler = Handler()
        override fun onClick(v: View?) {
            when (v) {
                playButton -> {
                    noteCheckboxListener.onPlaybuttonclicked(adapterPosition)
                    if (!playing) {
                        playButton.setImageResource(android.R.drawable.ic_media_pause)
                        SpotifyService.playTrack(
                            track
                        )
                        //stop preview after 30 sec
                        handler.postDelayed({
                            playButton.setImageResource(android.R.drawable.ic_media_play)
                            SpotifyService.pause()
                            playing = false
                        }, 30000)

                        playing = true
                    } else {
                        playButton.setImageResource(android.R.drawable.ic_media_play)
                        SpotifyService.pause()
                        playing = false
                        handler.removeCallbacksAndMessages(null);
                    }
                }
                checkbox -> {

                    noteCheckboxListener.onCheckboxClicked(song, checkbox.isChecked)
                }

            }
        }



    }

    interface OnNoteCheckboxListener {
        fun onCheckboxClicked(track: Track, isChecked: Boolean)
        fun onPlaybuttonclicked(pos: Int)
    }
}
