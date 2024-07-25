package io.github.kuroka3.spoticraft.manager.utils

import io.github.kuroka3.spoticraft.SpotiCraftPlugin
import io.github.kuroka3.spoticraft.manager.spotify.SpotifyClient
import io.github.kuroka3.spoticraft.manager.spotify.SpotifyToken
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.*

object TokenManager {
    private lateinit var TOKENFILE: JSONFile

    fun init() {
        TOKENFILE = JSONFile(SpotiCraftPlugin.instance.dataFolder, "spotify.token")
        if (!TOKENFILE.exists()) { TOKENFILE.saveJSON(JSONObject().apply { this["players"] = JSONObject() }) }
    }

    operator fun get(uuid: UUID): SpotifyToken? {
        val key = uuid.toString().replace("-", "")
        val obj = TOKENFILE.jsonObject["players"] as JSONObject
        if (!obj.containsKey(key)) { return null }

        val user = obj[key] as JSONObject
        return SpotifyToken(user)
    }

    operator fun set(uuid: UUID, token: SpotifyToken) {
        val key = uuid.toString().replace("-", "")
        val obj = TOKENFILE.jsonObject
        val players = obj["players"] as JSONObject
        players[key] = token.toJSON()
        obj["players"] = players
        TOKENFILE.saveJSON(obj)
    }

    fun requestTokenURL(uuid: UUID): URL {
        val key = uuid.toString().replace("-", "")
        return URI("${SettingsManager.serverDomain}:${SettingsManager.serverPort}/login?uuid=$key").toURL()
    }

    /**
     * Need to run with async (BukkitScheduler)
     */
    fun refreshToken(uuid: UUID): Boolean {
        val token = this[uuid] ?: return false
        val refreshToken = token.refreshToken
        val url = URI("https://accounts.spotify.com/api/token").toURL()
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        connection.setRequestProperty("Authorization", SpotifyClient.authorization)

        val body = "grant_type=refresh_token&refresh_token=$refreshToken"
        connection.doOutput = true
        connection.outputStream.use { it.write(body.toByteArray()) }

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
            val obj = JSONParser().parse(responseBody) as JSONObject
            this[uuid] = SpotifyToken(
                obj["access_token"] as String,
                obj["token_type"] as String,
                obj["expires_in"] as Long,
                refreshToken,
                obj["scope"] as String,
                System.currentTimeMillis()
            )
            return true
        } else {
            return false
        }
    }
}