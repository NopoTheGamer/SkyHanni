package at.hannibal2.skyhanni.utils.json

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.utils.SimpleTimeMark
//#if FORGE
import at.hannibal2.skyhanni.utils.KotlinTypeAdapterFactory
import at.hannibal2.skyhanni.features.fishing.trophy.TrophyRarity
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.tracker.SkyHanniTracker
import net.minecraft.item.ItemStack
//#endif
import com.google.gson.GsonBuilder
import io.github.notenoughupdates.moulconfig.observer.PropertyTypeAdapterFactory
import java.time.LocalDate
import java.util.UUID

object BaseGsonBuilder {
    fun gson(): GsonBuilder = GsonBuilder().setPrettyPrinting()
        .excludeFieldsWithoutExposeAnnotation()
        .serializeSpecialFloatingPointValues()
        .registerTypeAdapterFactory(PropertyTypeAdapterFactory())
        //#if FORGE
        .registerTypeAdapterFactory(KotlinTypeAdapterFactory())
        .registerTypeAdapter(UUID::class.java, SkyHanniTypeAdapters.UUID.nullSafe())
        .registerTypeAdapter(LorenzVec::class.java, SkyHanniTypeAdapters.VEC_STRING.nullSafe())
        .registerTypeAdapter(TrophyRarity::class.java, SkyHanniTypeAdapters.TROPHY_RARITY.nullSafe())
        .registerTypeAdapter(ItemStack::class.java, SkyHanniTypeAdapters.NEU_ITEMSTACK.nullSafe())
        .registerTypeAdapter(NEUInternalName::class.java, SkyHanniTypeAdapters.INTERNAL_NAME.nullSafe())
        .registerTypeAdapter(LorenzRarity::class.java, SkyHanniTypeAdapters.RARITY.nullSafe())
        .registerTypeAdapter(
            SkyHanniTracker.DefaultDisplayMode::class.java,
            SkyHanniTypeAdapters.TRACKER_DISPLAY_MODE.nullSafe(),
        )
        .registerTypeAdapter(LocalDate::class.java, SkyHanniTypeAdapters.LOCALE_DATE.nullSafe())
        //#endif
        .enableComplexMapKeySerialization()

    fun lenientGson(): GsonBuilder = gson()
        .registerTypeAdapterFactory(SkippingTypeAdapterFactory)
        .registerTypeAdapterFactory(ListEnumSkippingTypeAdapterFactory)
}
