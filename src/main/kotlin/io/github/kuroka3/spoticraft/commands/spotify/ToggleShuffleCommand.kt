package io.github.kuroka3.spoticraft.commands.spotify

import io.github.kuroka3.spoticraft.commands.interfaces.CommandChildren
import io.github.kuroka3.spoticraft.manager.spotify.SpotifyManager
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ToggleShuffleCommand : CommandChildren {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) { return false }
        if (args.size < 2) { return false }

        when (SpotifyManager.request(SpotifyManager.Requests.TOGGLE_SHUFFLE, sender.uniqueId, "state=${args[1]}")) {
            SpotifyManager.Responses.OK -> sender.sendMessage("Set Shuffle to ${args[1]}")
            SpotifyManager.Responses.NEED_LOGIN -> SpotifyManager.pleaseLogin(sender)
            SpotifyManager.Responses.TOO_MANY -> sender.sendMessage("Too Many Requests")
            SpotifyManager.Responses.DENIED -> sender.sendMessage("Something went wrong")
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): MutableList<String>? {
        return if (args.size == 2) mutableListOf("true", "false") else null
    }
}