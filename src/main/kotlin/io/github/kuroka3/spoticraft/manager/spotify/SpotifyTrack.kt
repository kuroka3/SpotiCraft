package io.github.kuroka3.spoticraft.manager.spotify

import org.json.simple.JSONArray
import org.json.simple.JSONObject

class SpotifyTrack {

    val name: String
    val artist: String
    val length: Long
    val currentms: Long

    constructor(name: String, artist: String, length: Long, currentms: Long) {
        this.name = name
        this.artist = artist
        this.length = length
        this.currentms = currentms
    }

    constructor(obj: JSONObject) {
        val item = obj["item"] as JSONObject

        this.name = item["name"] as String
        this.artist = (item["artists"] as JSONArray).map { it as JSONObject }.joinToString(", ") { it["name"] as String }
        this.length = item["duration_ms"] as Long
        this.currentms = obj["progress_ms"] as Long
    }
}