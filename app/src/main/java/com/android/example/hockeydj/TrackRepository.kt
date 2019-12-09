package com.android.example.hockeydj

import android.util.Log
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Deferred

class TrackRepository(private val trackDao: TrackDao) {

    val TAG = "JZ:TrackRepository"

//    var allTracks = trackDao.getAllTracks()
//    var requestedTracks: LiveData<List<Track>>? = null

    var homeGoalTracks = trackDao.homeGoalPlaylist()
    var homeFaultTracks = trackDao.homeFaultPlaylist()
    var homeTimeoutTracks = trackDao.homeTimeoutPlaylist()
    var guestGoalTracks = trackDao.guestGoalPlaylist()
    var guestFaultTracks = trackDao.guestFaultPlaylist()
    var guestTimeoutTracks = trackDao.guestTimeoutPlaylist()
    var generalTracks = trackDao.generalPlaylist()

    suspend fun deleteAll() {
        trackDao.deleteAll()
    }

    suspend fun insertOrUpdate(track: Track) {
        trackDao.insertOrUpdate(track)
    }

    suspend fun resetHomeGoal(){
        trackDao.resetHomeGoalPlaylist()
    }

//    fun getTracksInPlaylist(hockeyPlaylist:String)  {
//        Log.d(TAG, "Getting tracks in playlist: $hockeyPlaylist")
//        requestedTracks = trackDao.getTracksInHockeyPlaylist(hockeyPlaylist)
//    }
}