package io.github.kuroka3.spoticraft.manager.spotify

import io.github.kuroka3.spoticraft.manager.utils.SettingsManager
import java.util.*

object SpotifyClient {
    val authorization: String
        get() {
            val encoded = Base64.getEncoder().encodeToString("${SettingsManager.clientID}:${SettingsManager.clientSecret}".toByteArray())
            return "Basic $encoded"
        }
}