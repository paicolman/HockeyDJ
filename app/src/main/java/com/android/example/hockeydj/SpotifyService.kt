package com.android.example.hockeydj

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.sdk.android.authentication.AuthenticationClient
import com.spotify.sdk.android.authentication.AuthenticationRequest
import com.spotify.sdk.android.authentication.AuthenticationResponse
import org.json.JSONObject
import java.net.URL

object SpotifyService {
    private val TAG = "JZ:SpotifyService"

    lateinit var activity: Activity
    lateinit var context: Context

    var spotifyAvailable = false
    val REQUEST_CODE = 1445
    private var TOKEN = "No Token"

    lateinit private var connectionParams: ConnectionParams
    lateinit private var mSpotifyAppRemote: SpotifyAppRemote

    //interfaces
    var connectionInterface: SpotifyConnectionInterface? = null
    var dataInterface: SpotifyDataInterface? = null
    var trackInterface: SpotifyTrackInterface? = null
    var controlInterface: SpotifyControlInterface? = null

    fun authenticate() {
        Log.d(TAG, "authenticateSpotify")

        //TODO: Prevent hardcoded REDIRECT_URI and CLIENT_ID
        val REDIRECT_URI = "https://hockeydj-android/callback"
        val CLIENT_ID = "2596d9d73f8c46d0b803ce634f38f8a5"


        val builder = AuthenticationRequest.Builder(
            CLIENT_ID,
            AuthenticationResponse.Type.TOKEN,
            REDIRECT_URI
        )

        builder.setScopes(arrayOf("streaming","playlist-read-collaborative","playlist-read-private","user-library-read","user-follow-read"))
        val request = builder.build()

        connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(false)
            .build()

        AuthenticationClient.openLoginActivity(activity, REQUEST_CODE, request)
    }

    private fun connect() {
        Log.d(TAG, "connect")
        SpotifyAppRemote.connect(context, connectionParams, object : Connector.ConnectionListener {

                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                    Log.d(TAG, "Connected! Yay!")
                    mSpotifyAppRemote = spotifyAppRemote
                    // Now you can start interacting with App Remote
                    Log.d(TAG, "Now we are connected to Spotify...")
                    spotifyAvailable = true
                    connectionInterface?.spotifyAvailable()
                    //TODO: What when the token expires? Should we trigger a new authentication here with a timer??
                    //TODO: This playlist call should not be here...
                }

                override fun onFailure(throwable: Throwable) {
                    Log.w(TAG, throwable.message, throwable)
                    connectionInterface?.connectionError(throwable.message, throwable)
                }
            })
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {

        Log.d(TAG, "onActivityResult")

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            val response = AuthenticationClient.getResponse(resultCode, intent)
            when (response.type) {
                // Response was successful and contains auth token
                AuthenticationResponse.Type.TOKEN -> {
                    Log.d(TAG, "Got a Spotify TOKEN!! YAY!")
                    Log.d(TAG, "Your token: ${response.accessToken}")
                    Log.d(TAG, "Expires in: ${response.expiresIn}")
                    TOKEN = response.accessToken
                    connect()
                }

                // Auth flow returned an error
                AuthenticationResponse.Type.ERROR -> {
                    Log.e(TAG, "Authentication error!")
                    Log.e(TAG, response.error)
                    connectionInterface?.authenticationError(response)
                }
                AuthenticationResponse.Type.CODE -> {
                    //TODO: What to do here?
                }
                AuthenticationResponse.Type.EMPTY -> {
                    //TODO: What to do here?
                }
                AuthenticationResponse.Type.UNKNOWN -> {
                    //TODO: What to do here?
                }
                else -> {
                    //TODO: What to do here?
                }
            }
            // TODO: Handle successful response
            // TODO: Handle error response
            // TODO: Most likely auth flow was cancelled
            // TODO: Handle other cases
        } else {
            Log.d(TAG, "requestCode == Not from me: $requestCode")
        }
    }

    //TODO: This gets only the first twenty playlists - need to generalize somehow...
    fun getPlaylists(){
        if (spotifyAvailable) {
            Log.d(TAG, "getPlaylists")

            val queue = Volley.newRequestQueue(context)
            val url = "https://api.spotify.com/v1/me/playlists"

            val jsonRequest = SpotifyRequest(
                Request.Method.GET, url,null, Response.Listener { response ->
                    val playlistArray = response.getJSONArray("items")
                    val playlistData = mutableListOf<MediaCollection>()
                    for (i in 0 until playlistArray.length()) {
                        val playlistObject = playlistArray[i] as JSONObject
                        val name = playlistObject.getString("name")
                        val trackInfo = playlistObject.getJSONObject("tracks")
                        var imageUrl = ""
                        val tracksUrl = trackInfo.getString("href")
                        val tracks = trackInfo.getInt("total")
                        val images = playlistObject.getJSONArray("images")
                        if (images.length() > 0) {
                            val imageData = images[0] as JSONObject //Get just the first one you find...
                            imageUrl = imageData.getString("url")
                        }
                        playlistData.add(MediaCollection(name, tracksUrl, tracks, imageUrl))

                    }
                    dataInterface?.mediaCollectionHandler(playlistData)
                },
                Response.ErrorListener {
                    Log.e(TAG, "We had problems getting playlists")
                    Log.e(TAG, it.toString())
                    dataInterface?.dataRequestError(it)
                }, TOKEN)
            // Add the request to the RequestQueue.
            queue.add(jsonRequest)
        }
    }

    fun getAlbums(){
        if (spotifyAvailable) {
            Log.d(TAG, "getAlbums")

            val queue = Volley.newRequestQueue(context)
            val url = "https://api.spotify.com/v1/me/albums"

            val jsonRequest = SpotifyRequest(
                Request.Method.GET, url,null, Response.Listener { response ->
                    val albumsArray = response.getJSONArray("items")
                    val albumData = mutableListOf<MediaCollection>()
                    for (i in 0 until albumsArray.length()) {
                        val albumObject = (albumsArray[i] as JSONObject).getJSONObject("album")
                        val name = albumObject.getString("name")
                        val trackInfo = albumObject.getJSONObject("tracks")
                        val tracksUrl = trackInfo.getString("href")
                        val tracks = trackInfo.getInt("total")
                        var imageUrl = ""
                        val images = albumObject.getJSONArray("images")
                        if (images.length() > 0) {
                            val imageData = images[0] as JSONObject //Get just the first one you find...
                            imageUrl = imageData.getString("url")
                        }
                        albumData.add(MediaCollection(name, tracksUrl, tracks, imageUrl))
                    }
                    dataInterface?.mediaCollectionHandler(albumData)
                },
                Response.ErrorListener {
                    Log.e(TAG, "We had problems getting albums")
                    Log.e(TAG, it.toString())
                    dataInterface?.dataRequestError(it)
                }, TOKEN
            )
            // Add the request to the RequestQueue.
            queue.add(jsonRequest)
        }
    }

    fun getTracksFromPlaylist(tracksUrl: String){
        if (spotifyAvailable) {
            Log.d(TAG, "getTracksFromPlaylist")

            val queue = Volley.newRequestQueue(context)
            val jsonRequest = SpotifyRequest(
                Request.Method.GET, tracksUrl, null, Response.Listener { response ->
                    val tracksArray = response.getJSONArray("items")
                    val tracks = mutableListOf<Track>()
                    for (i in 0 until tracksArray.length()) {
                        val trackObject = (tracksArray[i] as JSONObject).getJSONObject("track")
                        val name = trackObject.getString("name")
                        val trackUri = trackObject.getString("uri")
                        val artists = trackObject.getJSONArray("artists")
                        var artistName = "unknown"
                        if (artists.length() > 0) {
                            artistName = (artists[0] as JSONObject).getString("name")
                            if (artists.length() > 1){
                                artistName = "$artistName, others"
                            }
                        }

                        var imageUrl = ""
                        val album = trackObject.getJSONObject("album")
                        val images = album.getJSONArray("images")
                        if (images.length() > 0) {
                            val imageData = images[0] as JSONObject //Get just the first one you find...
                            imageUrl = imageData.getString("url")
                        }
                        tracks.add(Track(name, trackUri, artistName, imageUrl,
                            false, false, false, false, false, false, false))
                    }
                    trackInterface?.tracksHandler(tracks)
                },
                Response.ErrorListener {
                    Log.e(TAG, "We had problems getting tracks!")
                    Log.e(TAG, it.toString())
                    trackInterface?.trackRequestError(it)
                }, TOKEN
            )
            // Add the request to the RequestQueue.
            queue.add(jsonRequest)
        }
    }

    fun getTracksFromAlbum(albumUrl: String){
        if (spotifyAvailable) {
            Log.d(TAG, "getTracksFromAlbum")

            val queue = Volley.newRequestQueue(context)
            val jsonRequest = SpotifyRequest(
                Request.Method.GET, albumUrl, null, Response.Listener { response ->
                    val tracksArray = response.getJSONArray("items")
                    val tracks = mutableListOf<Track>()
                    for (i in 0 until tracksArray.length()) {
                        val trackObject = (tracksArray[i] as JSONObject)
                        val name = trackObject.getString("name")
                        val trackUri = trackObject.getString("uri")
                        val artists = trackObject.getJSONArray("artists")
                        var artistName = "unknown"
                        if (artists.length() > 0) {
                            artistName = (artists[0] as JSONObject).getString("name")
                            if (artists.length() > 1){
                                artistName = "$artistName, others"
                            }
                        }

                        var imageUrl = ""
                        tracks.add(Track(name, trackUri, artistName, imageUrl,
                            false, false, false, false, false, false, false))
                    }
                    trackInterface?.tracksHandler(tracks)
                },
                Response.ErrorListener {
                    Log.e(TAG, "We had problems getting album tracks!")
                    Log.e(TAG, it.toString())
                    trackInterface?.trackRequestError(it)
                }, TOKEN
            )
            // Add the request to the RequestQueue.
            queue.add(jsonRequest)
        }
    }

    fun playTrack(trackUri: String){
        Log.d(TAG, "playTrack")

        // Play a track
        mSpotifyAppRemote.playerApi.play(trackUri).setResultCallback {
            controlInterface?.playing()
        }

        // Subscribe to PlayerState
        mSpotifyAppRemote.playerApi.subscribeToPlayerState()?.setEventCallback {
            Log.d(TAG,"playerStateCallback: paused:${it.isPaused}")

            val track = it.track
            if (track != null) {
                Log.d(TAG, "${track.name} by ${track.artist.name}")
                var nowPlaying = ""
                if (!it.isPaused) {
                    nowPlaying = "Now playing: ${track.name} by ${track.artist.name}"
                }
                controlInterface?.nowPlaying(nowPlaying)
            }
        }
    }

    fun pause(){
        mSpotifyAppRemote.playerApi.pause().setResultCallback {
            controlInterface?.paused()
        }
    }

    fun disconnect() {
        Log.d(TAG, "Disconnected from Spotify")
        SpotifyAppRemote.disconnect(mSpotifyAppRemote)
    }

    internal class SpotifyRequest(
        method: Int,
        url: String,
        json: JSONObject?,
        listener: Response.Listener<JSONObject>?,
        errListener: Response.ErrorListener?,
        private val Token: String): JsonObjectRequest(method, url, json, listener, errListener) {

        override fun getHeaders(): MutableMap<String, String> {
            val jsonParams = HashMap<String, String>()
            jsonParams.put("Accept", "application/json")
            jsonParams.put("Content-Type", "application/json; charset=UTF-8")
            jsonParams.put("Authorization", "Bearer $Token")

            return jsonParams
        }
    }
}

interface SpotifyConnectionInterface {
    fun spotifyAvailable()
    fun authenticationError(response: AuthenticationResponse)
    fun connectionError(msg: String?, throwable: Throwable)
}

interface SpotifyDataInterface {
    fun mediaCollectionHandler(mediaCollection: List<MediaCollection>?)
    fun dataRequestError(volleyError: VolleyError)
}

interface SpotifyTrackInterface {
    fun tracksHandler(tracks: List<Track>)
    fun trackRequestError(volleyError: VolleyError)
}

interface SpotifyControlInterface {
    fun playing()
    fun paused()
    fun nowPlaying(trackInfo: String)
}

data class MediaCollection(val name: String, val tracksUrl: String, val tracks: Int, val imageUrl:String)


//data class Track(val name: String, val trackUri: String, val artist: String, val imageUrl:String)