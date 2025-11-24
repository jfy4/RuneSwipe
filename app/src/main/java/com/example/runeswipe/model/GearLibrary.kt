package com.example.runeswipe.model

/**
 * Central library for all gear definitions.
 * This is the single source of truth for:
 * - IDs
 * - Names
 * - Descriptions
 * - Gear slots
 *
 * ONLY IDs appear in the save file. The runtime uses this library
 * to reconstruct full gear objects.
 */
object GearLibrary {

    // ─────────────────────────────────────────────
    // Base master list of gear
    // ─────────────────────────────────────────────
    private val gearList: List<Gear> = listOf(
        // ——— Head —
        Gear(
            id = "apprentice_hat",
            name = "Apprentice Hat",
            slot = GearSlot.HEAD,
            description = "A simple pointed hat worn by novice wizards."
        ),
        Gear(
            id = "wizard_cap",
            name = "Wizard Cap",
            slot = GearSlot.HEAD,
            description = "A sturdier cap offering slight magical protection."
        ),

        // ——— Body —
        Gear(
            id = "apprentice_robe",
            name = "Apprentice Robe",
            slot = GearSlot.BODY,
            description = "Light cloth robe favored by beginning casters."
        ),
        Gear(
            id = "novice_robe",
            name = "Novice Robe",
            slot = GearSlot.BODY,
            description = "A robe infused with faint protective wards."
        ),

        // ——— Hand —
        Gear(
            id = "wooden_wand",
            name = "Wooden Wand",
            slot = GearSlot.HAND,
            description = "A basic wand that helps channel simple spells."
        ),
        Gear(
            id = "oak_staff",
            name = "Oak Staff",
            slot = GearSlot.HAND,
            description = "A long staff carved with rune symbols."
        ),

        // ——— Accessory —
        Gear(
            id = "silver_ring",
            name = "Silver Ring",
            slot = GearSlot.ACCESSORY,
            description = "A ring engraved with tiny rune markings."
        )
    )

    // Fast lookup by ID
    private val gearMap: Map<String, Gear> = gearList.associateBy { it.id }

    // ─────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────

    /** Return a copy of the gear with the given ID (or null if missing). */
    fun get(id: String): Gear? = gearMap[id]?.copy()

    /** List all gear. Helpful for shops, dev testing, debugging. */
    fun getAll(): List<Gear> = gearList.map { it.copy() }

    /** Get all items belonging to a particular slot. */
    fun forSlot(slot: GearSlot): List<Gear> =
        gearList.filter { it.slot == slot }.map { it.copy() }
}
