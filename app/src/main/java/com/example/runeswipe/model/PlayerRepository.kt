package com.example.runeswipe.model

import android.content.Context
import kotlinx.serialization.json.Json
import java.io.File
import android.util.Log


object PlayerRepository {
    private const val FILE_NAME = "player_save.json"
    private val json = Json {
	prettyPrint = true
	encodeDefaults = true
    }

    fun save(context: Context, player: Player) {
        val data = player.toData()
        val jsonText = json.encodeToString(PlayerData.serializer(), data)
	Log.d("RuneSwipe", "Saving player JSON:\n$jsonText")
	File(context.filesDir, FILE_NAME).writeText(jsonText)
    }

    fun load(context: Context): Player? {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return null
        return try {
            val text = file.readText()
            val data = json.decodeFromString(PlayerData.serializer(), text)
            Player.fromData(data)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun delete(context: Context) {
        File(context.filesDir, FILE_NAME).delete()
    }
}
