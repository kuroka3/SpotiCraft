package io.github.kuroka3.spoticraft.commands

import io.github.kuroka3.spoticraft.commands.interfaces.CommandChildren
import io.github.kuroka3.spoticraft.commands.spotify.*
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class SpotifyCommand : CommandExecutor, TabCompleter {
    private val children = mapOf<String, CommandChildren>(
        Pair("next", NextCommand()),
        Pair("pause", PauseCommand()),
        Pair("prev", PrevCommand()),
        Pair("position", SeekToPositionCommand()),
        Pair("repeat", SetRepeatCommand()),
        Pair("volume", SetVolumeCommand()),
        Pair("playing_state", ShowPlayingStateCommand()),
        Pair("start", StartCommand()),
        Pair("shuffle", ToggleShuffleCommand())
    )

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        return if (args.isEmpty()) false
        else {
            if (children.containsKey(args[0])) children[args[0]]!!.onCommand(sender, command, label, args) else false
        }
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): MutableList<String>? {
        if (args.size == 1) {
            val completions = mutableListOf<String>()
            children.forEach { completions.add(it.key) }
            return completions
        } else {
            return if (children.containsKey(args[0])) children[args[0]]!!.onTabComplete(sender, command, label, args) else null
        }
    }
}