// model/SpellTree.kt
package com.example.runeswipe.model

/**
 * Compatibility facade over SpellsRepo.
 * Keeps old references alive (allSpells, prerequisites, canUnlock),
 * while exposing chapters for the tome.
 */
object SpellTree {
    // Old API — still works:
    // If other systems (e.g., status logic, unlock UI) read this, nothing breaks.
    val allSpells: List<Spell> get() = SpellsRepo.All

    // Keep your old prerequisites here so unlock logic stays intact
    val prerequisites: Map<String, List<String>> = mapOf(
        "Lefu" to listOf("Fehu")
    )

    fun canUnlock(player: Player, spellId: String): Boolean {
        val reqs = prerequisites[spellId] ?: return true
        return reqs.all { it in player.knownSpellIds }
    }

    // New API for the tome (chapters), but lives here to avoid churn elsewhere.
    // Uses LinkedHashMap so tab order is stable.
    val chapters: Map<String, List<Spell>> get() = SpellsRepo.AllChapters
}
