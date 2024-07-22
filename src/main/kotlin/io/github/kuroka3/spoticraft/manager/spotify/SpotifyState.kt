package io.github.kuroka3.spoticraft.manager.spotify

import org.json.simple.JSONObject

data class SpotifyState(val isPlaying: Boolean, val track: SpotifyTrack? = null) {
    constructor(obj: JSONObject) : this(obj["is_playing"] as Boolean, SpotifyTrack(obj))
}
