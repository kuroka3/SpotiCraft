package io.github.kuroka3.spoticraft.commands.spoticraft

import io.github.kuroka3.spoticraft.SpotiCraftPlugin
import io.github.kuroka3.spoticraft.commands.interfaces.CommandChildren
import io.github.kuroka3.spoticraft.manager.auther.WebAuther
import io.github.kuroka3.spoticraft.manager.utils.SettingsManager
import io.github.kuroka3.spoticraft.manager.utils.TokenManager
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class ReloadCommand : CommandChildren {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val start = System.currentTimeMillis()

        WebAuther.stop()

        val dataFolder = SpotiCraftPlugin.instance.dataFolder
        if (!dataFolder.exists()) { dataFolder.mkdir() }
        TokenManager.clear()
        TokenManager.init()
        SettingsManager.load()

        Bukkit.getScheduler().runTaskAsynchronously(SpotiCraftPlugin.instance, Runnable {
            WebAuther.run()

            val end = System.currentTimeMillis()
            sender.sendMessage(Component.text("Reload Complete (${end-start}ms)"))
        })

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): MutableList<String>? {
        return null
    }
}