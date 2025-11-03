package com.example.runeswipe.model

import kotlin.math.max
import kotlin.math.min

typealias StatusBehavior = (Player, Int) -> String

object StatusLibrary {
    val behaviors: Map<StatusEffect, StatusBehavior> = mapOf(
        StatusEffect.BURNED to { c, tick ->
            val dmg = StatusEffect.BURNED.basePotency
            c.stats.life = max(0, c.stats.life - dmg)
            "${c.name} takes $dmg burn damage."
        },

        StatusEffect.POISONED to { c, tick ->
            val dmg = StatusEffect.POISONED.basePotency + tick // intensifies each tick
            c.stats.life = max(0, c.stats.life - dmg)
            "${c.name} suffers $dmg poison damage."
        },

        // StatusEffect.REGENERATING to { c, tick ->
        //     val heal = StatusEffect.
        //     c.life = min(c.maxLife, c.life + heal)
        //     "${c.name} regenerates $heal HP."
        // },

        // StatusEffect.FROZEN to { c, tick ->
        //     if (tick == 0) "${c.name} is frozen solid!" else ""
        // }
    )
}
