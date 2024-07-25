package io.github.kuroka3.spoticraft.manager.spotify

import org.json.simple.JSONArray
import org.json.simple.JSONObject

class SpotifyTrack {

    val id: String
    val name: String
    val artist: String
    val length: Long
    val currentms: Long

    constructor(id: String, name: String, artist: String, length: Long, currentms: Long) {
        this.id = id
        this.name = name
        this.artist = artist
        this.length = length
        this.currentms = currentms
    }

    constructor(obj: JSONObject) {
        val item = obj["item"] as JSONObject

        this.id = item["id"] as String
        this.name = item["name"] as String
        this.artist = (item["artists"] as JSONArray).map { it as JSONObject }.joinToString(", ") { it["name"] as String }
        this.length = item["duration_ms"] as Long
        this.currentms = obj["progress_ms"] as Long
    }
}