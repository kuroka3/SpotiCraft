package io.github.kuroka3.spoticraft.commands.spotify

import io.github.kuroka3.spoticraft.commands.interfaces.CommandChildren
import io.github.kuroka3.spoticraft.manager.spotify.SpotifyManager
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SetVolumeCommand : CommandChildren {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) { return false }
        if (args.size < 2) { return false }

        when (SpotifyManager.request(SpotifyManager.Requests.SET_VOLUME, sender.uniqueId, "volume_percent=${args[1]}")) {
            SpotifyManager.Responses.OK -> sender.sendMessage("Set Volume to ${args[1]}")
            SpotifyManager.Responses.NEED_LOGIN -> SpotifyManager.pleaseLogin(sender)
            SpotifyManager.Responses.TOO_MANY -> sender.sendMessage("Too Many Requests")
            SpotifyManager.Responses.DENIED -> sender.sendMessage("Something went wrong")
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): MutableList<String>? {
        return if (args.size == 2) {
            val completions = mutableListOf<String>()
            for (i in 1..100) { completions.add(i.toString()) }
            completions
        } else null
    }
}