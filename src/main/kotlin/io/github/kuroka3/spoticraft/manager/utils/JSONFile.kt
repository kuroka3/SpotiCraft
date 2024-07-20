package io.github.kuroka3.spoticraft.manager.utils

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException
import java.io.File
import java.io.IOException
import java.net.URI

class JSONFile : File {
    constructor(path: String) : super(path)
    constructor(parent: File, child: String) : super(parent, child)
    constructor(parent: String, child: String) : super(parent, child)
    constructor(uri: URI) : super(uri)

    @get:Throws(IOException::class, ParseException::class, ClassCastException::class)
    val jsonObject: JSONObject get() = JSONParser().parse(this.readText()) as JSONObject
    @get:Throws(IOException::class, ParseException::class, ClassCastException::class)
    val jsonArray: JSONArray get() = JSONParser().parse(this.readText()) as JSONArray

    fun saveJSON(obj: JSONObject) { this.writeText(obj.toJSONString()) }
    fun saveJSON(ary: JSONArray) { this.writeText(ary.toJSONString()) }
}