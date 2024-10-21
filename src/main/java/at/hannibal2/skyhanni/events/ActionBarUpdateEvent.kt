package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
//#if FORGE
import net.minecraft.util.ChatComponentText
//#endif
import net.minecraft.util.IChatComponent

class ActionBarUpdateEvent(var actionBar: String, var chatComponent: IChatComponent) : SkyHanniEvent() {
    //#if FORGE
    fun changeActionBar(newText: String) {
        chatComponent = ChatComponentText(newText)
    }
    //#endif
}
