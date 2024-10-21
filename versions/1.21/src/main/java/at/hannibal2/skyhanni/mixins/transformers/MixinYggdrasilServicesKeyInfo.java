package at.hannibal2.skyhanni.mixins.transformers;

import at.hannibal2.skyhanni.SkyHanniMod;
import at.hannibal2.skyhanni.config.Features;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.yggdrasil.YggdrasilServicesKeyInfo;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.security.Signature;

@Mixin(value = YggdrasilServicesKeyInfo.class, remap = false)
public class MixinYggdrasilServicesKeyInfo {

    @WrapOperation(method = "validateProperty", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
    private void validateProperty(Logger instance, String s, Object property, Object error, Operation<Void> original) {
        Features feature = SkyHanniMod.feature;
        if (feature.dev.minecraftConsoles.consoleFilter.filterVerifyProperty) {
            return;
        } else {
            original.call(instance, s, property, error);
        }
    }

    @Inject(method = "signature", at = @At(value = "INVOKE", target = "Ljava/lang/AssertionError;<init>(Ljava/lang/String;Ljava/lang/Throwable;)V", shift = At.Shift.BEFORE), cancellable = true)
    public void signature(CallbackInfoReturnable<Signature> cir) {
        Features feature = SkyHanniMod.feature;
        if (feature.dev.minecraftConsoles.consoleFilter.filterVerifyProperty) {
            throw new AssertionError();
        }
    }
}
