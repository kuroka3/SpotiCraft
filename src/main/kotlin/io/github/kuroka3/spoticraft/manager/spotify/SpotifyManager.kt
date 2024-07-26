package io.github.kuroka3.spoticraft.manager.spotify

import io.github.kuroka3.spoticraft.manager.NamespacedKeys
import io.github.kuroka3.spoticraft.manager.pdc.TrackDataType
import io.github.kuroka3.spoticraft.manager.utils.TokenManager
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.net.HttpURLConnection
import java.net.URI
import java.util.UUID
import kotlin.math.roundToInt

object SpotifyManager {

    private const val SPOTIFY_URL = "https://api.spotify.com/v1/me/player"

    enum class Requests {
        START,
        PAUSE,
        NEXT,
        PREV,
        SET_REPEAT,
        SET_VOLUME,
        TOGGLE_SHUFFLE
    }

    private val urls: Map<Requests, String> = mapOf(
        Pair(Requests.START, "/play"),
        Pair(Requests.PAUSE, "/pause"),
        Pair(Requests.NEXT, "/next"),
        Pair(Requests.PREV, "/previous"),
        Pair(Requests.SET_REPEAT, "/repeat"),
        Pair(Requests.SET_VOLUME, "/volume"),
        Pair(Requests.TOGGLE_SHUFFLE, "/shuffle")
    )

    private val methods: Map<Requests, String> = mapOf(
        Pair(Requests.START, "PUT"),
        Pair(Requests.PAUSE, "PUT"),
        Pair(Requests.NEXT, "POST"),
        Pair(Requests.PREV, "POST"),
        Pair(Requests.SET_REPEAT, "PUT"),
        Pair(Requests.SET_VOLUME, "PUT"),
        Pair(Requests.TOGGLE_SHUFFLE, "PUT")
    )

    /**
     * Need to run with async (BukkitScheduler)
     */
    fun request(type: Requests, target: UUID): Boolean {
        var token = TokenManager[target] ?: return false
        if (token.isExpired) {
            TokenManager.refreshToken(target)
            token = TokenManager[target] ?: return false
        }

        val url = URI("$SPOTIFY_URL${urls[type]}").toURL()
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = methods[type]
        connection.setRequestProperty("Authorization", token.toString())
        connection.doOutput = true
        connection.outputStream.use { it.write(ByteArray(0)) }

        val responseCode = connection.responseCode
        return responseCode == HttpURLConnection.HTTP_NO_CONTENT || responseCode == HttpURLConnection.HTTP_OK
    }

    /**
     * Need to run with async (BukkitScheduler)
     */
    fun getState(target: UUID): SpotifyState? {
        var token = TokenManager[target] ?: return null
        if (token.isExpired) {
            TokenManager.refreshToken(target)
            token = TokenManager[target] ?: return null
        }

        val url = URI(SPOTIFY_URL).toURL()
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Authorization", token.toString())

        val responseCode = connection.responseCode
        when (responseCode) {
            HttpURLConnection.HTTP_OK -> {
                val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                val obj = JSONParser().parse(responseBody) as JSONObject

                return SpotifyState(obj)
            }
            HttpURLConnection.HTTP_NO_CONTENT -> {
                return SpotifyState(false)
            }
            else -> {
                return null
            }
        }
    }

    fun notifyTrack(target: Player) {
        val container = target.persistentDataContainer
        if (container.has(NamespacedKeys.CURRENT_TRACK_KEY)) {
            val track = container.get(NamespacedKeys.CURRENT_TRACK_KEY, TrackDataType())!!
            notifyTrack(target, track)
        }
    }

    fun notifyTrack(target: Player, track: SpotifyTrack) {
        val currentms = formatMilliseconds(track.currentms)
        val duration = formatMilliseconds(track.length)
        val progressBar = getProgressBar(track.currentms.toFloat()/track.length.toFloat())

        target.sendActionBar(Component.text("$currentms $progressBar $duration"))
    }

    fun updateTrack(target: Player) {
        val container = target.persistentDataContainer
        if (container.has(NamespacedKeys.CURRENT_TRACK_KEY)) {
            val track = container.get(NamespacedKeys.CURRENT_TRACK_KEY, TrackDataType())!!
            val updated = SpotifyTrack(track.id, track.name, track.artist, track.length, track.currentms+(System.currentTimeMillis()-track.lastUpdate))
            if (updated.currentms <= updated.length) {
                container.set(NamespacedKeys.CURRENT_TRACK_KEY, TrackDataType(), updated)
            }
        }
    }

    fun nowPlaying(target: Player, track: SpotifyTrack) {
        val container = target.persistentDataContainer
        if (container.has(NamespacedKeys.CURRENT_TRACK_KEY)) {
            val before = container.get(NamespacedKeys.CURRENT_TRACK_KEY, TrackDataType())!!
            if (before.id != track.id) {
                target.sendMessage(Component.text("♫ Now Playing: ").append(Component.text("${track.name} - ${track.artist}")))
                target.persistentDataContainer.set(NamespacedKeys.CURRENT_TRACK_KEY, TrackDataType(), track)
            }
        }
        target.persistentDataContainer.set(NamespacedKeys.CURRENT_TRACK_KEY, TrackDataType(), track)
    }

    private fun formatMilliseconds(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    private fun getProgressBar(progress: Float): String {
        val totalLength = 60 // 진행바의 총 길이
        val filledLength = (progress * totalLength).roundToInt() // 채워진 부분의 길이
        val emptyLength = totalLength - filledLength // 비어있는 부분의 길이

        val filledBar = "⣿".repeat(filledLength)
        val emptyBar = "⣀".repeat(emptyLength)
        val progressBar = "${filledBar}$emptyBar"

        return progressBar
    }
}