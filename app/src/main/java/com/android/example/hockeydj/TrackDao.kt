package com.android.example.hockeydj

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.Deferred

@Dao
interface TrackDao {

    //Use with care!!
    @Query("DELETE FROM tracks")
    suspend fun deleteAll()

    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun update(track: Track)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(track: Track)

    //Used to initialize the playlist so that we can attach an observer to it...
    @Query("SELECT * FROM tracks ")
    fun getAllTracks() : LiveData<List<Track>>

    @Query("SELECT * FROM tracks WHERE homeGoal IS 1")
    fun homeGoalPlaylist() : LiveData<List<Track>>

    @Query("SELECT * FROM tracks WHERE homeFault IS 1")
    fun homeFaultPlaylist() : LiveData<List<Track>>

    @Query("SELECT * FROM tracks WHERE homeTimeout IS 1")
    fun homeTimeoutPlaylist() : LiveData<List<Track>>

    @Query("SELECT * FROM tracks WHERE guestGoal IS 1")
    fun guestGoalPlaylist() : LiveData<List<Track>>

    @Query("SELECT * FROM tracks WHERE guestFault IS 1")
    fun guestFaultPlaylist() : LiveData<List<Track>>

    @Query("SELECT * FROM tracks WHERE guestTimeout IS 1")
    fun guestTimeoutPlaylist() : LiveData<List<Track>>

    @Query("SELECT * FROM tracks WHERE genIntrpt IS 1")
    fun generalPlaylist() : LiveData<List<Track>>

    @Query("UPDATE tracks SET homeGoal = 0")
    suspend fun resetHomeGoalPlaylist()

    @Query("UPDATE tracks SET homeFault = 0")
    suspend fun resetHomeFaultPlaylist()

    @Query("UPDATE tracks SET homeTimeout = 0")
    suspend fun resetHomeTimeoutPlaylist()

    @Query("UPDATE tracks SET guestGoal = 0")
    suspend fun resetGuestGoalPlaylist()

    @Query("UPDATE tracks SET guestFault = 0")
    suspend fun resetGuestFaultPlaylist()

    @Query("UPDATE tracks SET guestTimeout = 0")
    suspend fun resetGuestTimeoutPlaylist()

    @Query("UPDATE tracks SET genIntrpt = 0")
    suspend fun resetGenIntrptPlaylist()

    @Query("SELECT * from tracks WHERE :trackUri LIKE track_uri")
    fun getTrackWithTrackUri(trackUri: String): Track?
}