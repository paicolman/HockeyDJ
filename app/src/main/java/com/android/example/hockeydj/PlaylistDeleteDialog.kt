package com.android.example.hockeydj

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

class PlaylistDeleteDialog : DialogFragment() {

    val TAG = "JZ:PlaylistDeleteDialog"

    lateinit var cancelBUtton: Button
    lateinit var deleteButton: Button
    lateinit var playlistName: TextView

    //Room DB
    private lateinit var trackViewModel: TrackViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        trackViewModel = ViewModelProvider(this).get(TrackViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_delete_dialog, container, false)
        cancelBUtton = view.findViewById(R.id.cancelButton)
        deleteButton = view.findViewById(R.id.deleteButton)
        playlistName = view.findViewById(R.id.playlistToDelete)

        val playlistToDelete = arguments?.getString("playlist")

        playlistName.text = playlistToDelete

        cancelBUtton.setOnClickListener(View.OnClickListener {
            dialog?.dismiss()
        })

        deleteButton.setOnClickListener(View.OnClickListener {
            trackViewModel.resetPlaylist(playlistToDelete!!)
            dialog?.dismiss()
        })

        return view
    }
}