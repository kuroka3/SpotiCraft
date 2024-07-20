package io.github.kuroka3.spoticraft.manager.spotify

import io.github.kuroka3.spoticraft.manager.ConstVariables
import java.util.*

object SpotifyClient {
    val authorization: String
        get() {
            val encoded = Base64.getEncoder().encodeToString("${ConstVariables.CLIENT_ID}:${ConstVariables.CLIENT_SECRET}".toByteArray())
            return "Basic $encoded"
        }
}