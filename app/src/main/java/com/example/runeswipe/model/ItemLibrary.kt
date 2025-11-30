package com.example.runeswipe.model

/**
 * Central library for all item definitions.
 * These are "templates" — never modify them directly.
 * Player inventories contain copies of these.
 */
object ItemLibrary {

    private val itemList = listOf(

        Item(
            id = "potion_small",
            name = "Small Potion",
            description = "Restores a small amount of health.",
            effect = ItemEffect.HEAL_SMALL
        ),

        Item(
            id = "potion_medium",
            name = "Medium Potion",
            description = "Restores a moderate amount of health.",
            effect = ItemEffect.HEAL_MEDIUM
        ),

        Item(
            id = "mana_dust",
            name = "Mana Dust",
            description = "A pinch of sparkly arcane powder.",
            effect = ItemEffect.RESTORE_MANA_SMALL
        ),

        Item(
            id = "scroll_firebolt",
            name = "Scroll of Firebolt",
            description = "Teaches the Firebolt spell.",
            effect = ItemEffect.LEARN_FIREBOLT
        ),

        Item(
            id = "ancient_relic",
            name = "Ancient Relic",
            description = "A relic humming with forgotten energy.",
            effect = ItemEffect.NONE
        )
    )

    // Quick lookup by ID
    private val itemMap = itemList.associateBy { it.id }

    /** Return a fresh copy of an item identified by its ID. */
    fun get(id: String): Item? = itemMap[id]?.copy()

    /** Get all items (used for shops, debugging, etc.) */
    fun getAll(): List<Item> = itemList.map { it.copy() }

    /** Get all potions, key items, relics — future extensibility */
    fun filter(predicate: (Item) -> Boolean): List<Item> =
        itemList.filter(predicate).map { it.copy() }

}
