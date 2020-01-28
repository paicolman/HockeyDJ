package com.omalik.android.hockeydj

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class TrackViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "JZ:TrackViewModel"
    private val repository: TrackRepository

    var homeGoalPlaylist:LiveData<List<Track>>
    var homeFaultPlaylist: LiveData<List<Track>>
    var homeTimeoutPlaylist: LiveData<List<Track>>
    var guestGoalPlaylist: LiveData<List<Track>>
    var guestFaultPlaylist: LiveData<List<Track>>
    var guestTimeoutPlaylist: LiveData<List<Track>>
    var generalPlaylist: LiveData<List<Track>>

    //These list track already played tracks to minimize repetitions.
    var homeGoalPlayed:MutableList<Int>
    var homeFaultPlayed: MutableList<Int>
    var homeTimeoutPlayed: MutableList<Int>
    var guestGoalPlayed: MutableList<Int>
    var guestFaultPlayed: MutableList<Int>
    var guestTimeoutPlayed: MutableList<Int>
    var generalPlayed: MutableList<Int>

    init{
        val trackDao = TracksDatabase.getDatabase(application).trackDao()
        repository = TrackRepository(trackDao)
        //Log.d(TAG, "Initiating database playlists")
        homeGoalPlaylist = repository.homeGoalTracks
        homeFaultPlaylist = repository.homeFaultTracks
        homeTimeoutPlaylist = repository.homeTimeoutTracks
        guestGoalPlaylist = repository.guestGoalTracks
        guestFaultPlaylist = repository.guestFaultTracks
        guestTimeoutPlaylist = repository.guestTimeoutTracks
        generalPlaylist = repository.generalTracks
        homeGoalPlayed = mutableListOf()
        homeFaultPlayed = mutableListOf()
        homeTimeoutPlayed = mutableListOf()
        guestGoalPlayed = mutableListOf()
        guestFaultPlayed = mutableListOf()
        guestTimeoutPlayed = mutableListOf()
        generalPlayed = mutableListOf()
    }

    fun deleteAll() = viewModelScope.launch {
        Log.e(TAG, "*****************************")
        Log.e(TAG, " DELETED THE WHOLE DATABASE!")
        Log.e(TAG, "*****************************")
        repository.deleteAll()
    }

    fun insertOrUpdate(track: Track) = viewModelScope.async{
        Log.d(TAG, "inserting stuff in the DB")
        repository.insertOrUpdate(track)
    }

    fun resetPlaylist(playlist: String) = viewModelScope.async {
        Log.d(TAG, "Deleting playlist...")
        repository.resetPlaylist(playlist)
        Log.d(TAG,"Purging unused tracks...")
        repository.purgeDB()
    }
}