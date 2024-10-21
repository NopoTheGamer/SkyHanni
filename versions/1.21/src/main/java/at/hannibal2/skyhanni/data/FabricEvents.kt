package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.ActionBarUpdateEvent
import at.hannibal2.skyhanni.events.KeyPressEvent
import at.hannibal2.skyhanni.events.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.SkyHanniTickEvent
import at.hannibal2.skyhanni.events.WorldChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.DelayedRun
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.text.MutableText
import net.minecraft.util.Formatting

@SkyHanniModule
object FabricEvents {

    var totalTicks = 0

    init {
        ClientTickEvents.START_WORLD_TICK.register(
            ClientTickEvents.StartWorldTick {
                MinecraftClient.getInstance().player ?: return@StartWorldTick

                DelayedRun.checkRuns()
                totalTicks++
                SkyHanniTickEvent(totalTicks).post()
            },
        )

        ClientPlayConnectionEvents.JOIN.register(
            ClientPlayConnectionEvents.Join { handler, sender, server ->
                WorldChangeEvent.post()
            },
        )

        ClientReceiveMessageEvents.ALLOW_GAME.register { message, overlay ->
            if (!overlay) {
                val rawMessage = Formatting.strip(message.string) ?: return@register true
                val skyHanniChatEvent = SkyHanniChatEvent(rawMessage, MutableText.of(message.content))
                skyHanniChatEvent.post()
                if (skyHanniChatEvent.blockedReason != "") {
                    return@register false
                }
                true
            } else {
                val rawMessage = Formatting.strip(message.string) ?: return@register true
                ActionBarUpdateEvent(rawMessage, MutableText.of(message.content)).post()
                true
            }
        }
    }
}
