package at.hannibal2.skyhanni.mixins.transformers;

import at.hannibal2.skyhanni.features.misc.visualwords.ModifyVisualWords;
import at.hannibal2.skyhanni.utils.OrderedTextUtils;
import net.minecraft.client.font.TextHandler;
import net.minecraft.text.StringVisitable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TextHandler.class)
public class MixinTextHandler {
    @ModifyVariable(
        method = "wrapLines(Lnet/minecraft/text/StringVisitable;ILnet/minecraft/text/Style;Ljava/util/function/BiConsumer;)V",
        index = 1,
        at = @At("HEAD"),
        argsOnly = true
    )
    public StringVisitable wrapLines(StringVisitable value) {
        String replaced = ModifyVisualWords.INSTANCE.modifyText(
            OrderedTextUtils.stringVisitableToLegacyString(value)
        );
        if (replaced == null) return value;
        return OrderedTextUtils.legacyStringToStringVisitable(replaced);
    }
}
