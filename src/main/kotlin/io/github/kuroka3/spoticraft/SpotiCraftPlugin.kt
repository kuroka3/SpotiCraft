package io.github.kuroka3.spoticraft

import io.github.kuroka3.spoticraft.manager.TokenManager
import io.github.kuroka3.spoticraft.manager.spotify.SpotifyManager
import io.github.kuroka3.spoticraft.manager.utils.JSONFile
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class SpotiCraftPlugin : JavaPlugin() {
    override fun onEnable() {
        logger.info("SpotiCraft!")

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
        }
        return super.onCommand(sender, command, label, args)
    }
}