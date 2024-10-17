package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.SkyhanniToolTipEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getItemUuid
import at.hannibal2.skyhanni.api.event.HandleEvent

@SkyHanniModule
object ShowItemUuid {

    @HandleEvent
    fun onTooltip(event: SkyhanniToolTipEvent) {
        if (!SkyHanniMod.feature.dev.debug.showItemUuid) return
        event.itemStack.getItemUuid()?.let {
            event.toolTip.add("§7Item UUID: '$it'")
        }
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "dev.showItemUuid", "dev.debug.showItemUuid")
    }
}
