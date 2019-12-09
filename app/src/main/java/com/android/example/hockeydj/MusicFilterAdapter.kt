package com.android.example.hockeydj

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.Volley

class MusicFilterAdapter(private val context: Context, private val media: List<MediaCollection>, private val noteListener: OnNoteListener): RecyclerView.Adapter<MusicFilterAdapter.ViewHolder>() {

    val TAG = "JZ:MusicFilterAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.music_item, parent, false)
        return  ViewHolder(view, noteListener)
    }

    override fun getItemCount(): Int {
        return media.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mediaName.text = media.get(position).name
        holder.mediaInfo.text = "Contains ${media.get(position).tracks} songs"
        requestBitmap(media.get(position).imageUrl, holder)
        holder.itemView.setBackgroundColor(Color.WHITE)
    }

    fun requestBitmap(url: String, holder: ViewHolder) {
        val queue = Volley.newRequestQueue(context)
        val request = ImageRequest(url,
            Response.Listener<Bitmap> {
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
    }

    fun imageHandler(image: Bitmap, holder: ViewHolder){
        val w = holder.mediaArtwork.width
        val h = holder.mediaArtwork.height
        val resized = Bitmap.createScaledBitmap(image, w, h, true)
        holder.mediaArtwork.setImageBitmap(resized)
        holder.holderImage = resized
    }

    inner class ViewHolder(itemView: View, private val noteListener: OnNoteListener): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val TAG = "JZ:.ViewHolder"
        var holderImage: Bitmap? = null

        override fun onClick(v: View?) {
            val tracskUrl = media.get(adapterPosition).tracksUrl
            noteListener.onNoteClick(tracskUrl, holderImage)
            //v?.setBackgroundColor(Color.GREEN)
        }

        val mediaName: TextView = itemView.findViewById(R.id.collection_name)
        val mediaInfo: TextView = itemView.findViewById(R.id.extra_info)
        val mediaArtwork: ImageView = itemView.findViewById(R.id.artwork)

        //val  parentLayout: RelativeLayout = itemView.findViewById(R.id.parent_layout)

        init {
            itemView.setOnClickListener(this)
        }
    }

    interface OnNoteListener {
        fun onNoteClick(tracksUrl: String, holderImage: Bitmap?)
    }
}