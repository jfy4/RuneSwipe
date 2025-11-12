package com.example.runeswipe.model

import kotlin.math.max
import kotlin.math.min

typealias StatusBehavior = (Player, Int) -> String

object StatusLibrary {

    val behaviors: Map<StatusEffect, StatusBehavior> = mapOf(

        // ─────────── DoT ───────────
        StatusEffect.POISONED to { c, tick ->
            val dmg = StatusEffect.POISONED.basePotency + tick
            c.stats.life = max(0, c.stats.life - dmg)
            "${c.name} suffers $dmg poison damage."
        },

        StatusEffect.BURNED to { c, _ ->
            val dmg = StatusEffect.BURNED.basePotency
            c.stats.life = max(0, c.stats.life - dmg)
            "${c.name} takes $dmg burn damage."
        },

        StatusEffect.BLEEDING to { c, _ ->
            val dmg = StatusEffect.BLEEDING.basePotency
            c.stats.life = max(0, c.stats.life - dmg)
            "${c.name} bleeds for $dmg HP."
        },

        StatusEffect.CURSED_FLAME to { c, _ ->
            val dmg = StatusEffect.CURSED_FLAME.basePotency
            c.stats.life = max(0, c.stats.life - dmg)
            "${c.name} is scorched by cursed fire for $dmg damage."
        },

        // ─────────── Buffs ───────────
        StatusEffect.REGENERATING to { c, _ ->
            val heal = StatusEffect.REGENERATING.basePotency
            c.stats.life = min(c.stats.maxLife, c.stats.life + heal)
            "${c.name} regenerates $heal HP."
        },

        StatusEffect.HASTE to { _, _ ->
            // handled elsewhere by turn order logic
            "Haste quickens the target’s movement."
        },

        StatusEffect.BARRIER to { _, _ ->
            "Barrier reduces incoming damage."
        },

        StatusEffect.REFLECTING to { _, _ ->
            "Reflect shimmers, ready to return spells."
        },

        // ─────────── Control ───────────
        StatusEffect.STUNNED to { c, _ ->
            "${c.name} is stunned and cannot act!"
        },

        StatusEffect.PARALYZED to { c, _ ->
            if ((0..1).random() == 0)
                "${c.name} is paralyzed and misses their turn!"
            else
                "${c.name} pushes through the paralysis!"
        },

        StatusEffect.SILENCED to { c, _ ->
            "${c.name} is silenced and cannot cast spells!"
        },

        StatusEffect.FROZEN to { c, tick ->
            if (tick == 0) "${c.name} is frozen solid!"
            else "${c.name} remains encased in ice."
        },

        // ─────────── Debuffs ───────────
        StatusEffect.WEAKENED to { _, _ -> "Attack power is reduced." },
        StatusEffect.CRIPPLED to { _, _ -> "Speed is reduced." },
        StatusEffect.SHATTERED_GUARD to { _, _ -> "Defense is reduced." },
        StatusEffect.CURSED_MIND to { _, _ -> "Accuracy or Magic Power reduced." },

        StatusEffect.DOOMED to { c, tick ->
            if (tick >= StatusEffect.DOOMED.baseDuration)
                "${c.name} succumbs to the doom!"
            else
                "${c.name}'s doom draws closer..."
        },
    )
}
