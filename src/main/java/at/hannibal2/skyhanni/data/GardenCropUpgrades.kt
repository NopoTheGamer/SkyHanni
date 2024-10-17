package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.SkyhanniChatEvent
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

@SkyHanniModule
object GardenCropUpgrades {

    private val patternGroup = RepoPattern.group("garden.cropupgrades")
    private val tierPattern by patternGroup.pattern(
        "tier",
        "§7Current Tier: §.(?<level>\\d)§7/§a9",
    )
    private val chatUpgradePattern by patternGroup.pattern(
        "chatupgrade",
        "\\s+§r§6§lCROP UPGRADE §e(?<crop>[\\w ]+)§7 #(?<tier>\\d)",
    )

    private val cropUpgrades: MutableMap<CropType, Int>? get() = GardenAPI.storage?.cropUpgrades

    @HandleEvent
    fun onChat(event: SkyhanniChatEvent) {
        if (!GardenAPI.inGarden()) return

        chatUpgradePattern.matchMatcher(event.message) {
            val crop = CropType.getByNameOrNull(group("crop"))
            val level = group("tier").formatInt()
            crop?.setUpgradeLevel(level)
        }
    }

    @HandleEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!GardenAPI.inGarden()) return
        if (event.inventoryName != "Crop Upgrades") return

        for (item in event.inventoryItems.values) {
            val crop = CropType.getByNameOrNull(item.name.removeColor()) ?: continue
            item.getLore().matchFirst(tierPattern) {
                val level = group("level").formatInt()
                crop.setUpgradeLevel(level)
            }
        }
    }

    fun CropType.getUpgradeLevel() = cropUpgrades?.get(this)

    private fun CropType.setUpgradeLevel(level: Int) {
        cropUpgrades?.put(this, level)
    }
}
