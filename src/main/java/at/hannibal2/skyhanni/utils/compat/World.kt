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

//#if FORGE
fun IChatComponent.getFormattedTextCompat() =

    this.formattedText

/*//$$     run {
//$$         val sb = StringBuilder()
//$$         for (component in iterator()) {
//$$             sb.append(component.style.formattingCode)
//$$             sb.append(component.unformattedComponentText)
//$$             sb.append("§r")
//$$         }
//$$         sb.toString()
//$$     }
////////#endif */

object Effects {
    val invisibility =

        Potion.invisibility

}
//#endif


