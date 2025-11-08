package com.example.runeswipe.model

import kotlin.math.min

typealias BuffBehavior = (Player, Int) -> String

object BuffLibrary {

    val behaviors: Map<BuffEffect, BuffBehavior> = mapOf(

        BuffEffect.ENRAGED to { c, _ ->
            c.stats.strength += BuffEffect.ENRAGED.basePotency
            c.stats.defense = maxOf(1, c.stats.defense - 1)
            "${c.name} is enraged! Attack up, defense down."
        },

        BuffEffect.FORTIFIED to { c, _ ->
            c.stats.defense += BuffEffect.FORTIFIED.basePotency
            "${c.name}'s defenses strengthen."
        },

        BuffEffect.HASTE to { c, _ ->
            c.stats.speed += BuffEffect.HASTE.basePotency
            "${c.name} moves with haste!"
        },

        BuffEffect.BARRIER to { _, _ ->
            // Should be implemented in damage reduction code
            "A barrier shimmers around the target."
        },

        BuffEffect.REGENERATION to { c, _ ->
            val heal = BuffEffect.REGENERATION.basePotency
            c.stats.life = min(c.stats.maxLife, c.stats.life + heal)
            "${c.name} regenerates $heal HP."
        },

        BuffEffect.FOCUS to { c, _ ->
            c.stats.dexterity += BuffEffect.FOCUS.basePotency
            "${c.name} focuses intensely."
        },

        BuffEffect.REFLECT to { _, _ ->
            // Will be handled in spell reflection check
            "Magic energy swirls defensively around ${'$'}{c.name}."
        },

        BuffEffect.INVISIBILITY to { _, _ ->
            // Should reduce hit chance in battle loop
            "${'$'}{c.name} fades from sight."
        },

        BuffEffect.RADIANT_BLESSING to { _, _ ->
            // Immunity flag can be handled elsewhere
            "${'$'}{c.name} is blessed with radiant protection."
        },
    )
}
