package com.android.example.hockeydj.com.android.example.hockeydj

import android.content.ContentUris
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.annotation.RequiresApi


class MusicProvider(val context: Context) {

    val TAG = "JZ: MusicProvider"


    data class MediaCollection(
        val id: Int,
        var name: String,
        val extraInfo: String?,
        val artworkUrl: String?
    )
    data class Media(
        var title: String,
        val track: AssetFileDescriptor?
    )

    fun getPlaylists(): List<MediaCollection> {
        Log.d(TAG, "getPlaylists")
        val mediaType = Uri.parse("content://com.google.android.music.MusicContent/playlists")
        val projection = arrayOf<String>("_id", "playlist_name", "playlist_description", "playlist_art_url")
        return getMediaCollection(mediaType, projection, true)
    }

    fun getGenres(): List<MediaCollection> {
        Log.d(TAG, "getGenres")
        val mediaType = Uri.parse("content://com.google.android.music.MusicContent/genres")
        val projection = arrayOf<String>("_id", "name", "name", "name")
        return getMediaCollection(mediaType, projection, true)
    }

    fun getArtists(): List<MediaCollection> {
        Log.d(TAG, "getArtists")
        val projection = arrayOf<String>("_id", "artist", "artist", "artist")
        return getAlbumsOrArtists(projection, false)
    }

    fun getAlbums(): List<MediaCollection> {
        Log.d(TAG, "getAlbums")
        val projection = arrayOf<String>("_id", "album", "album", "album")
        return getAlbumsOrArtists(projection, true)
    }

//    fun trialGetArtwork(albumId: Int) {
//        val media = Uri.parse("content://com.google.android.music.MusicContent/audio/media")
//        val uri = ContentUris.appendId(media.buildUpon(), albumId.toLong()).build()
//        val musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
//
//        val albumArt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            context.contentResolver.loadThumbnail(uri, Size(60, 60), null)
//        } else {
//            TODO("VERSION.SDK_INT < LOLLIPOP")
//        }
//
//    }

    private fun getAlbumsOrArtists(projection: Array<String>, addUnknown:Boolean): List<MediaCollection> {
        Log.d(TAG, "getAlbumsOrArtists")
        val mediaType = Uri.parse("content://com.google.android.music.MusicContent/audio")

        val collection = mutableListOf<MediaCollection>()

        val mediaCursor = context.contentResolver.query(mediaType, projection, null, null, null)
        if (mediaCursor != null && mediaCursor.moveToFirst()) {
            do {
                var thisMediaItem = MediaCollection(
                    mediaCursor.getInt(0),
                    mediaCursor.getString(1),
                    mediaCursor.getString(2),
                    mediaCursor.getString(3)
                )
                if (thisMediaItem.name == "") {
                    thisMediaItem.name = "<unknown>"
                }
                //trialGetArtwork(thisMediaItem.id)

                if (isNotDuplicate(collection, thisMediaItem.name))  collection.add(thisMediaItem)

            } while (mediaCursor.moveToNext())
        }
        if (addUnknown) {
            collection.add(MediaCollection(-1, "<unknown>", null, null))
        }
        collection.sortBy { it.name }
        return collection
    }

    private fun isNotDuplicate(list: List<MediaCollection>, searchItem: String): Boolean {
        for (item in list) {
            if (item.name == searchItem){
                return false
            }
        }
        return true
    }

    private fun getMediaCollection(mediaType: Uri, projection: Array<String>, addUnknown: Boolean): List<MediaCollection> {
        val collection = mutableListOf<MediaCollection>()
        val mediaCursor = context.contentResolver.query(mediaType, projection, null, null, null)

        if (mediaCursor != null && mediaCursor.moveToFirst()) {
            do {
                collection.add(MediaCollection(
                        mediaCursor.getInt(0),
                        mediaCursor.getString(1),
                        mediaCursor.getString(2),
                        mediaCursor.getString(3)
                    )
                )
            } while (mediaCursor.moveToNext())
            if (addUnknown) {
                collection.add(MediaCollection(-1, "<unknown>", null, null))
            }
        }
        collection.sortBy { it.name }
        return collection
    }

    //TODO: this should return complete stuff later, not only title/artist:

    fun getPlaylistMembers(playlistId: Int): List<Media>? {
        Log.d(TAG, "getPlaylistMembers")
        val mediaType = Uri.parse("content://com.google.android.music.MusicContent/playlists/$playlistId/members")
        val projection = arrayOf<String>("_id","title")
        val media = mutableListOf<Media>()
        val mediaCursor = context.contentResolver.query(mediaType, projection, null, null, null)
        if (mediaCursor != null && mediaCursor.moveToFirst()) {
            do {
                val songUri = Uri.withAppendedPath(mediaType, mediaCursor.getString(0))
                val thisTrack = context.contentResolver.openAssetFileDescriptor(songUri, "r")
                media.add(Media(mediaCursor.getString(1), thisTrack))
            } while (mediaCursor.moveToNext())
        }
        return media
    }

    fun getCollectionMembers(type:String, name: String): List<Media> {
        Log.d(TAG, "getAlbumMembers")
        val mediaType = Uri.parse("content://com.google.android.music.MusicContent/audio")
        val projection = arrayOf<String>("_id","title")
        val media = mutableListOf<Media>()
        val selection = "$type=?"
        val selArgs = arrayOf<String>(name)
        val mediaCursor = context.contentResolver.query(mediaType, projection, selection, selArgs, null)
        if (mediaCursor != null && mediaCursor.moveToFirst()) {
            do {
                val musicUri1 = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI
                val musicUri2 = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                val songUri = Uri.withAppendedPath(mediaType, mediaCursor.getString(0))
                val thisTrack = context.contentResolver.openAssetFileDescriptor(songUri, "r")
                media.add(Media(mediaCursor.getString(0), thisTrack))
            } while (mediaCursor.moveToNext())
        }
        return media


    }

}