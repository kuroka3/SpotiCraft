package io.github.kuroka3.spoticraft

import io.github.kuroka3.spoticraft.manager.TokenManager
import io.github.kuroka3.spoticraft.manager.spotify.SpotifyManager
import io.github.kuroka3.spoticraft.manager.utils.JSONFile
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

class SpotiCraftPlugin : JavaPlugin() {

    companion object {
        lateinit var instance: SpotiCraftPlugin
    }

    val testkey = NamespacedKey(this, "monitortask")

    override fun onEnable() {
        logger.info("SpotiCraft!")

        instance = this

        if(!dataFolder.exists()) { dataFolder.mkdir() }
        TokenManager.TOKENFILE = JSONFile(dataFolder, "token.json")
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (command.name.equals("gettoken", ignoreCase = true) && sender is Player) {
            sender.sendMessage("${TokenManager[sender.uniqueId]?.token}")
            sender.sendMessage("isExpired: ${TokenManager[sender.uniqueId]?.isExpired}")
            return true
        } else if (command.name.equals("refreshtoken", ignoreCase = true) && sender is Player) {
            Bukkit.getScheduler().runTaskAsynchronously(this, Runnable {
                if(!TokenManager.refreshToken(sender.uniqueId)) {
                    sender.sendMessage("Go Login: ${TokenManager.requestTokenURL(sender.uniqueId)}")
                }
                sender.sendMessage("${TokenManager[sender.uniqueId]?.token}")
                sender.sendMessage("isExpired: ${TokenManager[sender.uniqueId]?.isExpired}")
            })
            return true
        } else if (command.name.equals("skip", ignoreCase = true) && sender is Player) {
            Bukkit.getScheduler().runTaskAsynchronously(this, Runnable {
                if(!SpotifyManager.request(SpotifyManager.Requests.NEXT, sender.uniqueId)) {
                    sender.sendMessage("Go Login: ${TokenManager.requestTokenURL(sender.uniqueId)}")
                }
                sender.sendMessage("Skipped")
            })
            return true
        } else if (command.name.equals("pause", ignoreCase = true) && sender is Player) {
            Bukkit.getScheduler().runTaskAsynchronously(this, Runnable {
                if(!SpotifyManager.request(SpotifyManager.Requests.PAUSE, sender.uniqueId)) {
                    sender.sendMessage("Go Login: ${TokenManager.requestTokenURL(sender.uniqueId)}")
                }
                sender.sendMessage("Paused")
            })
            return true
        } else if (command.name.equals("getstate", ignoreCase = true) && sender is Player) {
            Bukkit.getScheduler().runTaskAsynchronously(this, Runnable {
                val state = SpotifyManager.getState(sender.uniqueId)
                if (state == null) {
                    sender.sendMessage("Go Login: ${TokenManager.requestTokenURL(sender.uniqueId)}")
                } else {
                    if (state.isPlaying) {
                        val track = state.track!!
                        sender.sendMessage("${track.name} - ${track.artist}")
                        SpotifyManager.notifyTrack(sender, track)
                    } else {
                        sender.sendMessage("notplaying")
                    }
                }
            })
            return true
        } else if (command.name.equals("monitoring", ignoreCase = true) && sender is Player) {
            val taskid = Bukkit.getScheduler().runTaskTimerAsynchronously(this, Runnable {
                val state = SpotifyManager.getState(sender.uniqueId)
                if (state == null) {
                    sender.sendMessage("Go Login: ${TokenManager.requestTokenURL(sender.uniqueId)}")

                    val container = sender.persistentDataContainer
                    if (container.has(testkey)) {
                        Bukkit.getScheduler().cancelTask(container.get(testkey, PersistentDataType.INTEGER)!!)
                    }

                } else {
                    if (state.isPlaying) {
                        val track = state.track!!
                        SpotifyManager.notifyTrack(sender, track)
                    } else {
                        sender.sendMessage("notplaying")

                        val container = sender.persistentDataContainer
                        if (container.has(testkey)) {
                            Bukkit.getScheduler().cancelTask(container.get(testkey, PersistentDataType.INTEGER)!!)
                        }
                    }
                }
            }, 0L, 20L).taskId

            sender.persistentDataContainer.set(testkey, PersistentDataType.INTEGER, taskid)
            return true
        }
        return super.onCommand(sender, command, label, args)
    }
}