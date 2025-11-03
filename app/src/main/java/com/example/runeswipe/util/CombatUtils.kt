package com.example.runeswipe.util

import com.example.runeswipe.model.*

fun applyStatusEffects(character: Player): String {
    val s = character.status
    val effect = s.effect
    if (effect == StatusEffect.NONE) return ""

    val behavior = StatusLibrary.behaviors[effect]
    var log = ""

    if (behavior != null) {
        log = behavior(character, s.elapsed)
        s.elapsed++
    }

    // compare elapsed to baseDuration in the effect definition
    if (s.elapsed >= effect.baseDuration) {
        character.status = StatusState(StatusEffect.NONE)
        log += " ${character.name} is no longer affected."
    }

    return log
}
