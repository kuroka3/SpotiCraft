package io.github.kuroka3.spoticraft.commands.spotify

import io.github.kuroka3.spoticraft.commands.interfaces.CommandChildren
import io.github.kuroka3.spoticraft.manager.spotify.SpotifyManager
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class StartCommand : CommandChildren {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) { return false }

        when (SpotifyManager.request(SpotifyManager.Requests.START, sender.uniqueId)) {
            SpotifyManager.Responses.OK -> sender.sendMessage("Start/Resumed")
            SpotifyManager.Responses.NEED_LOGIN -> SpotifyManager.pleaseLogin(sender)
            SpotifyManager.Responses.TOO_MANY -> sender.sendMessage("Too Many Requests")
            SpotifyManager.Responses.DENIED -> sender.sendMessage("Player is already playing")
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): MutableList<String>? {
        return null
    }
}