package io.github.kuroka3.spoticraft

import io.github.kuroka3.spoticraft.commands.SpoticraftCommand
import io.github.kuroka3.spoticraft.commands.SpotifyCommand
import io.github.kuroka3.spoticraft.manager.NamespacedKeys
import io.github.kuroka3.spoticraft.manager.utils.TokenManager
import io.github.kuroka3.spoticraft.manager.auther.WebAuther
import io.github.kuroka3.spoticraft.manager.spotify.SpotifyManager
import io.github.kuroka3.spoticraft.manager.utils.SettingsManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

class SpotiCraftPlugin : JavaPlugin() {

    companion object {
        lateinit var instance: SpotiCraftPlugin
    }

    override fun onEnable() {
        logger.info("SpotiCraft v${pluginMeta.version} enabled")

        instance = this

        if (!dataFolder.exists()) { dataFolder.mkdir() }
        TokenManager.init()
        SettingsManager.load()

        Bukkit.getScheduler().runTaskAsynchronously(this, Runnable { WebAuther.run() })

        registerCommands()
    }

    private fun registerCommands() {
        getCommand("spoticraft")!!.run {
            this.setExecutor(SpoticraftCommand())
            this.tabCompleter = SpoticraftCommand()
        }
        getCommand("spotify")!!.run {
            this.setExecutor(SpotifyCommand())
            this.tabCompleter = SpotifyCommand()
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (command.name.equals("gettoken", ignoreCase = true) && sender is Player) {
            sender.sendMessage("${TokenManager[sender.uniqueId]?.token}")
            sender.sendMessage("isExpired: ${TokenManager[sender.uniqueId]?.isExpired}")
            return true
        } else if (command.name.equals("refreshtoken", ignoreCase = true) && sender is Player) {
            Bukkit.getScheduler().runTaskAsynchronously(this, Runnable {
                if(!TokenManager.refreshToken(sender.uniqueId)) {
                    sender.sendMessage(Component.text("Go Login: ${TokenManager.requestTokenURL(sender.uniqueId)}").clickEvent(
                        ClickEvent.openUrl(TokenManager.requestTokenURL(sender.uniqueId))))
                }
                sender.sendMessage("${TokenManager[sender.uniqueId]?.token}")
                sender.sendMessage("isExpired: ${TokenManager[sender.uniqueId]?.isExpired}")
            })
            return true
        } else if (command.name.equals("getstate", ignoreCase = true) && sender is Player) {
            Bukkit.getScheduler().runTaskAsynchronously(this, Runnable {
                val state = SpotifyManager.getState(sender.uniqueId)
                if (state == null) {
                    sender.sendMessage(Component.text("Go Login: ${TokenManager.requestTokenURL(sender.uniqueId)}").clickEvent(
                        ClickEvent.openUrl(TokenManager.requestTokenURL(sender.uniqueId))))
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
                    sender.sendMessage(Component.text("Go Login: ${TokenManager.requestTokenURL(sender.uniqueId)}").clickEvent(
                        ClickEvent.openUrl(TokenManager.requestTokenURL(sender.uniqueId))))

                    val container = sender.persistentDataContainer
                    if (container.has(NamespacedKeys.MONITOR_TASK_KEY)) {
                        Bukkit.getScheduler().cancelTask(container.get(NamespacedKeys.MONITOR_TASK_KEY, PersistentDataType.INTEGER)!!)
                    }

                } else {
                    if (state.isPlaying) {
                        val track = state.track!!
                        SpotifyManager.nowPlaying(sender, track)
                        for (i in 0..(SettingsManager.apiRequestDuration/SettingsManager.trackRefreshDuration)) {
                            Bukkit.getScheduler().runTaskLaterAsynchronously(this, Runnable {
                                SpotifyManager.updateTrack(sender)
                                SpotifyManager.notifyTrack(sender)
                            }, i*SettingsManager.trackRefreshDuration)
                        }
                    } else {
                        sender.sendMessage("notplaying")

                        val container = sender.persistentDataContainer
                        if (container.has(NamespacedKeys.MONITOR_TASK_KEY)) {
                            Bukkit.getScheduler().cancelTask(container.get(NamespacedKeys.MONITOR_TASK_KEY, PersistentDataType.INTEGER)!!)
                        }
                    }
                }
            }, 0L, SettingsManager.apiRequestDuration).taskId

            sender.persistentDataContainer.set(NamespacedKeys.MONITOR_TASK_KEY, PersistentDataType.INTEGER, taskid)
            return true
        }
        return super.onCommand(sender, command, label, args)
    }

    override fun onDisable() {
        WebAuther.stop()
    }
}