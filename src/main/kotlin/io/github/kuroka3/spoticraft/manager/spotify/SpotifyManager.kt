package io.github.kuroka3.spoticraft.manager.spotify

import io.github.kuroka3.spoticraft.SpotiCraftPlugin
import io.github.kuroka3.spoticraft.manager.NamespacedKeys
import io.github.kuroka3.spoticraft.manager.pdc.TrackDataType
import io.github.kuroka3.spoticraft.manager.utils.SettingsManager
import io.github.kuroka3.spoticraft.manager.utils.TokenManager
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
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
        TOGGLE_SHUFFLE,
        SEEK_TO_POSITION
    }

    enum class Responses {
        NEED_LOGIN,
        OK,
        DENIED,
        TOO_MANY
    }

    private val urls: Map<Requests, String> = mapOf(
        Pair(Requests.START, "/play"),
        Pair(Requests.PAUSE, "/pause"),
        Pair(Requests.NEXT, "/next"),
        Pair(Requests.PREV, "/previous"),
        Pair(Requests.SET_REPEAT, "/repeat"),
        Pair(Requests.SET_VOLUME, "/volume"),
        Pair(Requests.TOGGLE_SHUFFLE, "/shuffle"),
        Pair(Requests.SEEK_TO_POSITION, "/seek")
    )

    private val methods: Map<Requests, String> = mapOf(
        Pair(Requests.START, "PUT"),
        Pair(Requests.PAUSE, "PUT"),
        Pair(Requests.NEXT, "POST"),
        Pair(Requests.PREV, "POST"),
        Pair(Requests.SET_REPEAT, "PUT"),
        Pair(Requests.SET_VOLUME, "PUT"),
        Pair(Requests.TOGGLE_SHUFFLE, "PUT"),
        Pair(Requests.SEEK_TO_POSITION, "PUT")
    )

    /**
     * Need to run with async (BukkitScheduler)
     */
    fun request(type: Requests, target: UUID, params: String = ""): Responses {
        var token = TokenManager[target] ?: return Responses.NEED_LOGIN
        if (token.isExpired) {
            TokenManager.refreshToken(target)
            token = TokenManager[target] ?: return Responses.NEED_LOGIN
        }

        val url = URI("$SPOTIFY_URL${urls[type]}?$params").toURL()
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = methods[type]
        connection.setRequestProperty("Authorization", token.toString())
        connection.doOutput = true
        connection.outputStream.use { it.write(ByteArray(0)) }

        val responseCode = connection.responseCode
        return if (responseCode == HttpURLConnection.HTTP_NO_CONTENT || responseCode == HttpURLConnection.HTTP_OK) Responses.OK else if (responseCode == 429) Responses.TOO_MANY else Responses.DENIED
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

        val notifyingBossBar = target.activeBossBars().filter { Regex(""".* - .* \[ .*? / .*? ]""").containsMatchIn((it.name() as TextComponent).content()) }

        if (notifyingBossBar.isNotEmpty()) {
            val element = notifyingBossBar[0]
            element.name(Component.text("${track.name} - ${track.artist} [ $currentms / $duration ]"))
            element.progress(track.currentms.toFloat()/track.length.toFloat())
        } else {
            target.showBossBar(BossBar.bossBar(Component.text("${track.name} - ${track.artist} [ $currentms / $duration ]"), track.currentms.toFloat()/track.length.toFloat(), BossBar.Color.GREEN, BossBar.Overlay.PROGRESS))
        }
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

    fun showPlayingState(target: Player) {
        val taskid = Bukkit.getScheduler().runTaskTimerAsynchronously(SpotiCraftPlugin.instance, Runnable {
            val state = getState(target.uniqueId)
            if (state == null) {
                pleaseLogin(target)

                val container = target.persistentDataContainer
                if (container.has(NamespacedKeys.SHOW_PLAYING_STATE_TASK_KEY)) {
                    Bukkit.getScheduler().cancelTask(container.get(NamespacedKeys.SHOW_PLAYING_STATE_TASK_KEY, PersistentDataType.INTEGER)!!)
                }
            } else if (state.isPlaying) {
                val track = state.track!!
                nowPlaying(target, track)
                for (i in 0..(SettingsManager.apiRequestDuration/SettingsManager.trackRefreshDuration)) {
                    Bukkit.getScheduler().runTaskLaterAsynchronously(SpotiCraftPlugin.instance, Runnable {
                        updateTrack(target)
                        notifyTrack(target)
                    }, i*SettingsManager.trackRefreshDuration)
                }
            }
        }, 0L, SettingsManager.apiRequestDuration).taskId
        target.persistentDataContainer.set(NamespacedKeys.SHOW_PLAYING_STATE_TASK_KEY, PersistentDataType.INTEGER, taskid)
    }

    fun stopShowPlayingState(target: Player) {
        val container = target.persistentDataContainer
        if (container.has(NamespacedKeys.SHOW_PLAYING_STATE_TASK_KEY)) {
            Bukkit.getScheduler().cancelTask(container.get(NamespacedKeys.SHOW_PLAYING_STATE_TASK_KEY, PersistentDataType.INTEGER)!!)
            val notifyingBossBar = target.activeBossBars().filter { Regex(""".* - .* \[ .*? / .*? ]""").containsMatchIn((it.name() as TextComponent).content()) }
            if (notifyingBossBar.isNotEmpty()) {
                Bukkit.getScheduler().runTaskLaterAsynchronously(SpotiCraftPlugin.instance, Runnable {
                    notifyingBossBar.forEach { target.hideBossBar(it) }
                }, SettingsManager.apiRequestDuration)
            }
        }
    }

    fun pleaseLogin(target: Player) {
        target.sendMessage(Component.text("Go Login: ${TokenManager.requestTokenURL(target.uniqueId)}").clickEvent(
            ClickEvent.openUrl(TokenManager.requestTokenURL(target.uniqueId))))
    }

    private fun formatMilliseconds(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
}