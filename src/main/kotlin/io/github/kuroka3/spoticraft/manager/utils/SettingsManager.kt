package io.github.kuroka3.spoticraft.manager.utils

import com.google.gson.GsonBuilder
import io.github.kuroka3.spoticraft.SpotiCraftPlugin
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileWriter

object SettingsManager {

    private lateinit var configFile: File

    var serverDomain: String = "localhost"; private set
    var serverPort: Int = 8000; private set
    var apiRequestDuration: Long = 20L; private set
    var clientID: String = "YOUR CLIENT ID"; private set
    var clientSecret: String = "YOUR CLIENT SECRET"; private set

    fun load() {
        configFile = File(SpotiCraftPlugin.instance.dataFolder, "settings.config")

        val gson = GsonBuilder().setPrettyPrinting().create()

        if (!configFile.exists()) {
            configFile.createNewFile()
            configFile.writeText(gson.toJson(Settings(serverDomain, serverPort, apiRequestDuration, clientID, clientSecret)))
        }

        val settings = gson.fromJson(configFile.readText(), Settings::class.java)
        serverDomain = settings.serverDomain
        serverPort = settings.serverPort
        apiRequestDuration = settings.apiRequestDuration
        clientID = settings.clientID
        clientSecret = settings.clientSecret
    }


    private data class Settings(
        val serverDomain: String,
        val serverPort: Int,
        val apiRequestDuration: Long,
        val clientID: String,
        val clientSecret: String
    )
}