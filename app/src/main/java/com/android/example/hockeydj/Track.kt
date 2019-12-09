package com.android.example.hockeydj

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class Track(
    val name: String,
    @PrimaryKey @ColumnInfo(name = "track_uri") val trackUri: String, //Should be unique
    @ColumnInfo(name = "artist_name") val artist: String,
    @ColumnInfo(name = "image_url") val imageUrl:String,
    @ColumnInfo(name = "homeGoal") val inHomeGoal: Boolean,
    @ColumnInfo(name = "homeFault") val inHomeFault: Boolean,
    @ColumnInfo(name = "homeTimeout") val inHomeTimeout: Boolean,
    @ColumnInfo(name = "guestGoal") val inGuestGoal: Boolean,
    @ColumnInfo(name = "guestFault") val inGuestFault: Boolean,
    @ColumnInfo(name = "guestTimeout") val inGuestTimeout: Boolean,
    @ColumnInfo(name = "genIntrpt") val inGeneralInt: Boolean) {
}