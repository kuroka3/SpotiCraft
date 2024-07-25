package io.github.kuroka3.spoticraft.manager.pdc

import com.google.gson.Gson
import io.github.kuroka3.spoticraft.manager.spotify.SpotifyTrack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

class TrackDataType : PersistentDataType<String, SpotifyTrack> {
    override fun getPrimitiveType(): Class<String> {
        return String::class.java
    }

    override fun getComplexType(): Class<SpotifyTrack> {
        return SpotifyTrack::class.java
    }

    override fun fromPrimitive(p0: String, p1: PersistentDataAdapterContext): SpotifyTrack {
        return Gson().fromJson(p0, SpotifyTrack::class.java)
    }

    override fun toPrimitive(p0: SpotifyTrack, p1: PersistentDataAdapterContext): String {
        return Gson().toJson(p0)
    }
}