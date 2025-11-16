// app/src/main/java/com/example/runeswipe/model/Spells.kt
package com.example.runeswipe.model

object SpellsRepo {
    // ─── Spells by Chapter ─────────────────────────────────────────────────────
    val Apprentice = listOf(
        Spell("Fehu", "Fireball", SpellType.ATTACK, damage = 6, status = StatusEffect.BURNED),
        Spell("Lefu", "Healing Light", SpellType.HEAL, heal = 5)
    )

    val Adept = listOf(
        Spell("Venhu", "Poison", SpellType.STATUS, status = StatusEffect.POISONED),
        Spell("Mute", "Mute", SpellType.STATUS, status = StatusEffect.SILENCED)
    )

    val AllChapters: Map<String, List<Spell>> = mapOf(
        "Apprentice Spells" to Apprentice,
        "Adept Spells" to Adept,
    )

    val All = Apprentice + Adept
}
