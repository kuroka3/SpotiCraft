package io.github.kuroka3.spoticraft.manager.auther

import io.github.kuroka3.spoticraft.SpotiCraftPlugin
import io.github.kuroka3.spoticraft.manager.spotify.SpotifyClient
import io.github.kuroka3.spoticraft.manager.utils.JSONFile
import io.github.kuroka3.spoticraft.manager.utils.SettingsManager
import io.javalin.Javalin
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import java.util.*

object WebAuther {

    private lateinit var app: Javalin
    private val redirect = "${SettingsManager.serverDomain}:${SettingsManager.serverPort}/callback"

    fun run() {
        app = Javalin.create()
            .get("/") { ctx: Context -> ctx.result("SpotiCraft Auth Server is Running") }
            .start(8000)

        app.get("/message") { ctx: Context ->
            ctx.result(ctx.queryParam("p").toString())
        }

        app.get("/login") { ctx: Context ->
            val uuid: String = ctx.queryParam("uuid") ?: throw BadRequestResponse("player's UUID is needed")

            val scope = URLEncoder.encode("user-read-private user-read-playback-state user-modify-playback-state", "UTF-8")
            val clientID = URLEncoder.encode(SettingsManager.clientID, "UTF-8")

            ctx.redirect("https://accounts.spotify.com/authorize?" +
                    "response_type=code&" +
                    "client_id=${clientID}&" +
                    "scope=${scope}&" +
                    "redirect_uri=${redirect}&" +
                    "state=$uuid")
        }

        app.get("/callback") { ctx: Context ->
            val code: String? = ctx.queryParam("code")
            val state: String = ctx.queryParam("state") ?: throw BadRequestResponse("state mismatch")

//        val authOptions = JSONObject().apply {
//            this["code"] = code
//            this["redirect_uri"] = redirect
//            this["grant_type"] = "authorization_code"
//        }
//        val authOptions = mapOf(
//            "code" to code,
//            "redirect_uri" to redirect,
//            "grant_type" to "authorization_code"
//        )
            val authOptions = "code=$code&redirect_uri=$redirect&grant_type=authorization_code"

            try {
                val url = URI("https://accounts.spotify.com/api/token").toURL()
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.setRequestProperty("Authorization", SpotifyClient.authorization)

                connection.doOutput = true
                connection.outputStream.use { it.write(authOptions.toByteArray()) }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseBody = connection.inputStream.bufferedReader().use { it.readText() }

                    val obj = JSONParser().parse(responseBody) as JSONObject
                    obj["time"] = System.currentTimeMillis()

                    val jFile = JSONFile(SpotiCraftPlugin.instance.dataFolder,"token.json")

                    val beforeObj = jFile.jsonObject
                    (beforeObj["players"] as JSONObject)[state] = obj
                    jFile.saveJSON(beforeObj)
                    ctx.result("LOGIN COMPLETE")
                } else {
                    throw Exception("Unexpected response code: $responseCode ${connection.responseMessage}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ctx.status(HttpStatus.BAD_REQUEST)
            }
        }
    }

    fun stop() {
        app.stop()
    }
}