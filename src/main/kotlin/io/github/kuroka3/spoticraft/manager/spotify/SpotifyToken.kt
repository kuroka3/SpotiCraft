package io.github.kuroka3.spoticraft.manager.spotify

import org.json.simple.JSONObject

data class SpotifyToken(val token: String,
                        val type: String,
                        val expires: Long,
                        val refreshToken: String,
                        val scope: String,
                        val time: Long) {

    constructor(obj: JSONObject) : this(
        obj["access_token"] as String,
        obj["token_type"] as String,
        obj["expires_in"] as Long,
        obj["refresh_token"] as String,
        obj["scope"] as String,
        obj["time"] as Long
        )

    override fun toString(): String {
        return "$type $token"
    }

    fun toJSON(): JSONObject {
        return JSONObject().apply {
            this["access_token"] = token
            this["token_type"] = type
            this["expires_in"] = expires
            this["refresh_token"] = refreshToken
            this["scope"] = scope
            this["time"] = time
        }
    }

    val isExpired: Boolean
        get() = System.currentTimeMillis() > time + (expires * 1000)
}