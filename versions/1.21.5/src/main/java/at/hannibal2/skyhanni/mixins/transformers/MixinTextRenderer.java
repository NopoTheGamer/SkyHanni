package at.hannibal2.skyhanni.mixins.transformers;

import at.hannibal2.skyhanni.features.misc.visualwords.ModifyVisualWords;
import at.hannibal2.skyhanni.utils.OrderedTextUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TextRenderer.class)
public class MixinTextRenderer {
    @ModifyVariable(
        method = "drawInternal(Lnet/minecraft/text/OrderedText;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;IIZ)I",
        index = 1,
        at = @At("HEAD"),
        argsOnly = true
    )
    private OrderedText modifyOrderedText(OrderedText value) {
        String replaced = ModifyVisualWords.INSTANCE.modifyText(
            OrderedTextUtils.orderedTextToLegacyString(value)
        );
        if (replaced == null) return value;
        return OrderedTextUtils.legacyTextToOrderedText(
            replaced
        );
    }
    @ModifyVariable(
        method = "getWidth(Lnet/minecraft/text/OrderedText;)I",
        index = 1,
        at = @At("HEAD"),
        argsOnly = true
    )
    private OrderedText modifyWidth(OrderedText value) {
        String replaced = ModifyVisualWords.INSTANCE.modifyText(
            OrderedTextUtils.orderedTextToLegacyString(value)
        );
        if (replaced == null) return value;
        return OrderedTextUtils.legacyTextToOrderedText(
            replaced
        );
    }
}
