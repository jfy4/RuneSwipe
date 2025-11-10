// ─────────────────────────────────────────────────────────────────────────────
// app/src/main/java/com/example/runeswipe/model/SpellTree.kt
// ─────────────────────────────────────────────────────────────────────────────
package com.example.runeswipe.model

/**
 * Represents the full spell tome — all spells in the game and their dependencies.
 * This can later evolve into a full skill tree with branching unlocks.
 */
object SpellTree {
    // The complete list of all spells in the game
    val allSpells: List<Spell> = listOf(
        SpellsRepo.Fireball,
        SpellsRepo.Heal,
	SpellsRepo.Poison,
	SpellsRepo.Mute
        // add others later...
    )

    // For a simple unlock tree structure, define dependencies:
    // childSpellId → list of prerequisite spell IDs
    val prerequisites: Map<String, List<String>> = mapOf(
        "Lefu" to listOf("Fehu") // Heal requires Fireball, for example
    )

    fun canUnlock(player: Player, spellId: String): Boolean {
        val reqs = prerequisites[spellId] ?: return true // no requirements
        return reqs.all { it in player.knownSpellIds }
    }
}
