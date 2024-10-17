package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.events.ActionBarUpdateEvent
import at.hannibal2.skyhanni.events.WorldChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object ActionBarData {
    private var actionBar = ""

    fun getActionBar() = actionBar

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        actionBar = ""
    }

    @SubscribeEvent(receiveCanceled = true)
    fun onChatReceive(event: ClientChatReceivedEvent) {
        if (event.type.toInt() != 2) return

        val original = event.message
        val message = LorenzUtils.stripVanillaMessage(original.formattedText)
        actionBar = message
        val actionBarEvent = ActionBarUpdateEvent(actionBar, event.message)
        actionBarEvent.postAndCatch()
        if (event.message.formattedText != actionBarEvent.chatComponent.formattedText) {
            event.message = actionBarEvent.chatComponent
        }
    }
}
