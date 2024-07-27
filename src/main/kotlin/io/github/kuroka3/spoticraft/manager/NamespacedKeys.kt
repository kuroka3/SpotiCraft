package io.github.kuroka3.spoticraft.manager

import io.github.kuroka3.spoticraft.SpotiCraftPlugin
import org.bukkit.NamespacedKey

object NamespacedKeys {
    val SHOW_PLAYING_STATE_TASK_KEY = NamespacedKey(SpotiCraftPlugin.instance, "spoticraft.show_playing_state_task_key")
    val CURRENT_TRACK_KEY = NamespacedKey(SpotiCraftPlugin.instance, "spoticraft.current_track_key")
}