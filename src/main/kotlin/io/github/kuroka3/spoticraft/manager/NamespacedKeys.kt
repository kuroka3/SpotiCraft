package io.github.kuroka3.spoticraft.manager

import io.github.kuroka3.spoticraft.SpotiCraftPlugin
import org.bukkit.NamespacedKey

object NamespacedKeys {
    val MONITOR_TASK_KEY = NamespacedKey(SpotiCraftPlugin.instance, "spoticraft.monitor_task_key")
    val CURRENT_TRACK_KEY = NamespacedKey(SpotiCraftPlugin.instance, "spoticraft.current_track_key")
}