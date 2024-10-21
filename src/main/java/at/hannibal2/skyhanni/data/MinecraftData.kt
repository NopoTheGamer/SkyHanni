package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.SkyHanniTickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.events.WorldChangeEvent
import at.hannibal2.skyhanni.events.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.ActionBarUpdateEvent
//#if FORGE
import net.minecraft.client.Minecraft
import at.hannibal2.skyhanni.events.ItemInHandChangeEvent
import at.hannibal2.skyhanni.events.PlaySoundEvent
import at.hannibal2.skyhanni.events.ReceiveParticleEvent
import at.hannibal2.skyhanni.events.minecraft.packet.PacketReceivedEvent
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NEUInternalName
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
//#elseif FABRIC
//$$ import net.minecraft.client.MinecraftClient
//$$ import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
//$$ import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
//$$ import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
//$$ import net.minecraft.text.MutableText
//#endif

@SkyHanniModule
object MinecraftData {
    var totalTicks = 0
    //#if FORGE
    @HandleEvent(receiveCancelled = true, onlyOnSkyblock = true)
    fun onSoundPacket(event: PacketReceivedEvent) {
        val packet = event.packet
        if (packet !is S29PacketSoundEffect) return

        if (PlaySoundEvent(
                packet.soundName,
                LorenzVec(packet.x, packet.y, packet.z),
                packet.pitch,
                packet.volume
            ).post()
        ) {
            event.cancel()
        }
    }

    @HandleEvent(receiveCancelled = true, onlyOnSkyblock = true)
    fun onParticlePacketReceive(event: PacketReceivedEvent) {
        val packet = event.packet
        if (packet !is S2APacketParticles) return

        if (ReceiveParticleEvent(
                packet.particleType!!,
                LorenzVec(packet.xCoordinate, packet.yCoordinate, packet.zCoordinate),
                packet.particleCount,
                packet.particleSpeed,
                LorenzVec(packet.xOffset, packet.yOffset, packet.zOffset),
                packet.isLongDistance,
                packet.particleArgs,
            ).post()
        ) {
            event.cancel()
        }
    }

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!LorenzUtils.inSkyBlock) return
        val hand = InventoryUtils.getItemInHand()
        val newItem = hand?.getInternalName() ?: NEUInternalName.NONE
        val oldItem = InventoryUtils.itemInHandId
        if (newItem != oldItem) {

            InventoryUtils.recentItemsInHand.keys.removeIf { it + 30_000 > System.currentTimeMillis() }
            if (newItem != NEUInternalName.NONE) {
                InventoryUtils.recentItemsInHand[System.currentTimeMillis()] = newItem
            }
            InventoryUtils.itemInHandId = newItem
            InventoryUtils.latestItemInHand = hand
            ItemInHandChangeEvent(newItem, oldItem).post()
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        InventoryUtils.itemInHandId = NEUInternalName.NONE
        InventoryUtils.recentItemsInHand.clear()
    }


    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        WorldChangeEvent.post()
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.START) return
        Minecraft.getMinecraft().thePlayer ?: return

        DelayedRun.checkRuns()
        totalTicks++
        SkyHanniTickEvent(totalTicks).post()
    }

    //#elseif FABRIC
    //$$ init {
    //$$     ClientTickEvents.START_WORLD_TICK.register(ClientTickEvents.StartWorldTick {
    //$$         MinecraftClient.getInstance().player ?: return@StartWorldTick
    //$$
    //$$         DelayedRun.checkRuns()
    //$$         totalTicks++
    //$$         SkyHanniTickEvent(totalTicks).post()
    //$$     })
    //$$
    //$$    ClientPlayConnectionEvents.JOIN.register(ClientPlayConnectionEvents.Join { handler, sender, server ->
    //$$          WorldChangeEvent.post()
    //$$    })
    //$$
    //$$
    //$$
    //$$
    //$$
    //$$
    //$$
    //$$ ClientReceiveMessageEvents.ALLOW_GAME.register {message, overlay ->
//$$    if (!overlay) {
//$$    val plainText = message.siblings.joinToString("") { sibling ->
//$$        sibling.string //todo this shit is fucked
//$$    }
//$$
//$$    println(plainText)
//$$    SkyHanniChatEvent(plainText, MutableText.of(message.content)).post()
//$$        println("Cool feature activated!")
//$$        true
//$$    } else {
//$$            ActionBarUpdateEvent(message.content.toString(), MutableText.of(message.content)).post()
//$$        true
//$$    }
//$$    }
    //$$
    //$$     }
    //#endif
}
