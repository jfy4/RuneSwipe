package com.example.runeswipe.model

object ItemEffectManager {

    fun applyEffect(player: Player, item: Item): String {
        return when (item.effect) {

            ItemEffect.HEAL_SMALL -> {
                val amount = 10
                val healed = minOf(amount, player.stats.maxLife - player.stats.life)
                player.stats.life += healed
                "You used ${item.name} and recovered $healed HP."
            }

            ItemEffect.HEAL_MEDIUM -> {
                val amount = 25
                val healed = minOf(amount, player.stats.maxLife - player.stats.life)
                player.stats.life += healed
                "You used ${item.name} and recovered $healed HP."
            }

            ItemEffect.RESTORE_MANA_SMALL -> {
                // Add mana system later
                "You used ${item.name}. Your mana stirs faintly... (not implemented)"
            }

            // ItemEffect.LEARN_FIREBOLT -> {
            //     val spellId = "Firebolt"

            //     if (player.knowsSpell(spellId)) {
            //         "You already know Firebolt."
            //     } else {
            //         val spell = SpellTree.getSpell(spellId)
            //         if (spell != null) {
            //             player.learnSpell(spell)
            //             "You learned the spell Firebolt!"
            //         } else {
            //             "Nothing happens... the scroll crumbles."
            //         }
            //     }
            // }

            ItemEffect.NONE -> "Nothing happens."

            else -> "Nothing happens."
        }
    }
}
