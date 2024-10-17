package at.hannibal2.skyhanni.features.nether

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.GetFromSackAPI
import at.hannibal2.skyhanni.events.SkyhanniChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.PrimitiveItemStack.Companion.makePrimitiveStack
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatchers
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.api.event.HandleEvent
import kotlin.time.Duration.Companion.minutes

// https://wiki.hypixel.net/Pablo
@SkyHanniModule
object PabloHelper {

    private val config get() = SkyHanniMod.feature.crimsonIsle

    // TODO RepoPattern.list does not work, find out why

//     /**
//      * REGEX-TEST: §e[NPC] §5Pablo§f: §b✆ §f§rBring me that §aEnchanted Dandelion §fas soon as you can!
//      */
//     private val patterns by RepoPattern.list(
//         "crimson.pablo.helper",
//         "\\[NPC] Pablo: (?:✆ )?Could you bring me an (?<flower>[\\w ]+).*",
//         "\\[NPC] Pablo: (?:✆ )?Bring me that (?<flower>[\\w ]+) as soon as you can!",
//     )

    private val patterns = listOf(
        "\\[NPC] Pablo: (?:✆ )?Are you available\\? I desperately need an? (?<flower>[\\w ]+) today\\.".toPattern(),
        "\\[NPC] Pablo: (?:✆ )?Bring me that (?<flower>[\\w ]+) as soon as you can!".toPattern(),
        "\\[NPC] Pablo: (?:✆ )?Could you bring me an? (?<flower>[\\w ]+)\\?".toPattern(),
        "\\[NPC] Pablo: (?:✆ )?I really need an? (?<flower>[\\w ]+) today, do you have one you could spare\\?".toPattern(),
    )

    private var lastSentMessage = SimpleTimeMark.farPast()

    @HandleEvent
    fun onChat(event: SkyhanniChatEvent) {
        if (!isEnabled()) return
        if (lastSentMessage.passedSince() < 5.minutes) return
        val itemName = patterns.matchMatchers(event.message.removeColor()) {
            group("flower")
        } ?: return

        if (InventoryUtils.countItemsInLowerInventory { it.name.contains(itemName) } > 0) return

        DelayedRun.runNextTick {
            GetFromSackAPI.getFromChatMessageSackItems(
                itemName.asInternalName().makePrimitiveStack(),
                "Click here to grab an $itemName from sacks!",
            )
        }

        lastSentMessage = SimpleTimeMark.now()
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.pabloHelper
}
