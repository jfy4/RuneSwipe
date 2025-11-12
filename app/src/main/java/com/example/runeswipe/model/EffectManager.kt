package com.example.runeswipe.model

import kotlin.math.max
import kotlin.math.min

/**
 * Centralized tick + apply logic for all status, buff, and debuff effects.
 * Called periodically (e.g. every 2 seconds in BattleScreen).
 */
object EffectManager {

    // ─────────────────────────────────────────────
    // Entry point
    // ─────────────────────────────────────────────
    fun tickPlayer(player: Player): List<String> {
        val logs = mutableListOf<String>()

        // STATUS
        // val statusLog = tickStatus(player)
        // if (statusLog.isNotEmpty()) logs += statusLog
	val statusLogs = tickStatuses(player)
	if (statusLogs.isNotEmpty()) logs += statusLogs

        // BUFFS
        val buffLogs = tickBuffs(player)
        if (buffLogs.isNotEmpty()) logs += buffLogs

        // DEBUFFS
        val debuffLogs = tickDebuffs(player)
        if (debuffLogs.isNotEmpty()) logs += debuffLogs

        return logs
    }

    // ─────────────────────────────────────────────
    // STATUS EFFECTS
    // ─────────────────────────────────────────────
    private fun tickStatuses(player: Player): List<String> {
	val logs = mutableListOf<String>()
	val iterator = player.statuses.iterator()

	while (iterator.hasNext()) {
            val state = iterator.next()
            if (state.effect == StatusEffect.NONE) {
		iterator.remove()
		continue
            }

            val behavior = StatusLibrary.behaviors[state.effect]
            val effectiveTick = state.elapsed
            val stackedPotency = state.effect.basePotency + (state.potencyBonus * (state.stacks - 1))
            val log = behavior?.invoke(player, effectiveTick)?.replace(
		state.effect.basePotency.toString(),
		stackedPotency.toString()
            )

            if (!log.isNullOrEmpty()) logs += log

            state.elapsed += 1
            if (state.elapsed >= state.effect.baseDuration) {
		iterator.remove()
            }
	}
	return logs
    }
    // private fun tickStatus(player: Player): String {
    //     val state = player.status
    //     if (state.effect == StatusEffect.NONE) return ""

    //     val effect = state.effect
    //     val behavior = StatusLibrary.behaviors[effect]
    //     val log = behavior?.invoke(player, state.elapsed) ?: ""

    //     state.elapsed += 1
    //     if (state.elapsed >= effect.baseDuration) {
    //         player.status = StatusState() // clear status
    //     }
    //     return log
    // }

    // ─────────────────────────────────────────────
    // BUFFS
    // ─────────────────────────────────────────────
    private fun tickBuffs(player: Player): List<String> {
        val logs = mutableListOf<String>()
        val iterator = player.buffs.iterator()

        while (iterator.hasNext()) {
            val buff = iterator.next()
            if (buff.effect == BuffEffect.NONE) {
                iterator.remove()
                continue
            }

            val behavior = BuffLibrary.behaviors[buff.effect]
            val log = behavior?.invoke(player, buff.elapsed)
            if (!log.isNullOrEmpty()) logs += log

            buff.elapsed += 1
            if (buff.elapsed >= buff.effect.baseDuration) {
                revertBuff(player, buff.effect)
                iterator.remove()
            }
        }
        return logs
    }

    private fun revertBuff(player: Player, effect: BuffEffect) {
        when (effect) {
            BuffEffect.ENRAGED -> {
                player.stats.strength = max(0, player.stats.strength - effect.basePotency)
                player.stats.defense += 1
            }
            BuffEffect.FORTIFIED -> player.stats.defense = max(0, player.stats.defense - effect.basePotency)
            BuffEffect.HASTE -> player.stats.speed = max(0, player.stats.speed - effect.basePotency)
            BuffEffect.FOCUS -> player.stats.dexterity = max(0, player.stats.dexterity - effect.basePotency)
            else -> { /* handled elsewhere */ }
        }
    }

    // ─────────────────────────────────────────────
    // DEBUFFS
    // ─────────────────────────────────────────────
    private fun tickDebuffs(player: Player): List<String> {
        val logs = mutableListOf<String>()
        val iterator = player.debuffs.iterator()

        while (iterator.hasNext()) {
            val debuff = iterator.next()
            if (debuff.effect == DebuffEffect.NONE) {
                iterator.remove()
                continue
            }

            val behavior = DebuffLibrary.behaviors[debuff.effect]
            val log = behavior?.invoke(player, debuff.elapsed)
            if (!log.isNullOrEmpty()) logs += log

            debuff.elapsed += 1
            if (debuff.elapsed >= debuff.effect.baseDuration) {
                revertDebuff(player, debuff.effect)
                iterator.remove()
            }
        }
        return logs
    }

    private fun revertDebuff(player: Player, effect: DebuffEffect) {
        when (effect) {
            DebuffEffect.WEAKENED -> player.stats.strength += effect.basePotency
            DebuffEffect.CRIPPLED -> player.stats.speed += effect.basePotency
            DebuffEffect.SHATTERED_GUARD -> player.stats.defense += effect.basePotency
            DebuffEffect.CURSED_MIND -> player.stats.dexterity += effect.basePotency
            else -> { /* no-op */ }
        }
    }

    // ─────────────────────────────────────────────
    // APPLY HELPERS (for spells)
    // ─────────────────────────────────────────────
    // fun applyStatus(target: Player, effect: StatusEffect) {
    //     if (effect != StatusEffect.NONE) {
    //         target.status = StatusState(effect, 0)
    //     }
    // }
    fun applyStatus(target: Player, effect: StatusEffect) {
	if (effect == StatusEffect.NONE) return

	// Try to find an existing matching effect
	val existing = target.statuses.find { it.effect == effect }
	if (existing != null) {
            // Stack it: increment stacks and extend duration or potency
            existing.stacks++
            existing.potencyBonus += 1
            existing.elapsed = 0 // optional: reset duration on refresh
	} else {
            target.statuses += StatusState(effect = effect, elapsed = 0, stacks = 1)
	}
    }

    fun applyBuff(target: Player, effect: BuffEffect) {
        if (effect != BuffEffect.NONE) {
            target.buffs += BuffState(effect, 0)
        }
    }

    fun applyDebuff(target: Player, effect: DebuffEffect) {
        if (effect != DebuffEffect.NONE) {
            target.debuffs += DebuffState(effect, 0)
        }
    }
}
