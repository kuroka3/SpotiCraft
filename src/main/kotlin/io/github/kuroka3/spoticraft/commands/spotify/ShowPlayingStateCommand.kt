package io.github.kuroka3.spoticraft.commands.spotify

import io.github.kuroka3.spoticraft.commands.interfaces.CommandChildren
import io.github.kuroka3.spoticraft.manager.spotify.SpotifyManager
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ShowPlayingStateCommand : CommandChildren {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) { return false }
        if (args.size < 2) { return false }

        val showState = args[1].lowercase().toBooleanStrictOrNull()
        if (showState == null) { return false }
        else {
            if (showState) SpotifyManager.showPlayingState(sender)
            else SpotifyManager.stopShowPlayingState(sender)
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): MutableList<String>? {
        return if (args.size == 2) mutableListOf("true", "false") else null
    }
}