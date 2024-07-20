package io.github.kuroka3.spoticraft.manager.spotify

import io.github.kuroka3.spoticraft.manager.TokenManager
import java.net.HttpURLConnection
import java.net.URI
import java.util.UUID

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
        println("responseCode: $responseCode")
        println("responseBody: ${connection.inputStream.bufferedReader().use { it.readText() }}")
        return responseCode == HttpURLConnection.HTTP_NO_CONTENT || responseCode == HttpURLConnection.HTTP_OK
    }
}