package at.hannibal2.skyhanni.utils.compat

import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.potion.Potion
import net.minecraft.util.IChatComponent

fun WorldClient.getLoadedPlayers(): List<EntityPlayer> =
//#if MC < 1.14
    this.playerEntities
//#else
//$$ this.players()
//#endif


fun Entity.getNameAsString(): String =
    this.name
//#if MC >= 1.14
//$$ .getString()
//#endif

fun EntityArmorStand.getArmorOrFullInventory() =
//#if MC < 1.12
    this.inventory
//#else
//$$ this.getArmorInventoryList()
//#endif

fun Minecraft.isOnMainThread() =
//#if MC < 1.14
    this.isCallingFromMinecraftThread
//#else
//$$ this.isSameThread
//#endif

fun IChatComponent.getFormattedTextCompat() =
//#if MC < 1.16
    this.formattedText
//#else
//$$     run {
//$$         val sb = StringBuilder() //todo this only works on some chat message
//$$         for (component in siblings) {
//$$             sb.append(component.string)
//$$             sb.append("§r")
//$$         }
//$$         sb.toString()
//$$     }
//#endif

//#if MC <1.12
object Effects {
    val invisibility =

        Potion.invisibility

}
//#endif


