package com.example.runeswipe.model

import kotlin.math.max

typealias DebuffBehavior = (Player, Int) -> String

object DebuffLibrary {

    val behaviors: Map<DebuffEffect, DebuffBehavior> = mapOf(

        DebuffEffect.WEAKENED to { c, _ ->
            c.stats.strength = max(0, c.stats.strength - DebuffEffect.WEAKENED.basePotency)
            "${c.name}'s strength falters!"
        },

        DebuffEffect.CRIPPLED to { c, _ ->
            c.stats.speed = max(0, c.stats.speed - DebuffEffect.CRIPPLED.basePotency)
            "${c.name}'s movement slows."
        },

        DebuffEffect.SHATTERED_GUARD to { c, _ ->
            c.stats.defense = max(0, c.stats.defense - DebuffEffect.SHATTERED_GUARD.basePotency)
            "${c.name}'s guard is shattered!"
        },

        DebuffEffect.CURSED_MIND to { c, _ ->
            c.stats.dexterity = max(0, c.stats.dexterity - DebuffEffect.CURSED_MIND.basePotency)
            "${c.name}'s focus wavers under the curse."
        },

        DebuffEffect.DOOMED to { c, tick ->
            if (tick >= DebuffEffect.DOOMED.baseDuration)
                "${c.name} succumbs to their doom!"
            else
                "${c.name}'s doom draws nearer..."
        },

        DebuffEffect.HEXED to { c, _ ->
            // Could apply a random stat debuff
            val statHit = listOf("strength", "defense", "speed").random()
            "${c.name} is hexed, ${statHit} is weakened!"
        },

        DebuffEffect.MANA_LEAK to { c, _ ->
            // Assuming you have MP implemented
            "${c.name}'s mana leaks away..."
        },
    )
}
