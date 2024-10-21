package at.hannibal2.skyhanni.mixins.transformers;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import at.hannibal2.skyhanni.events.KeyPressEvent;

@Mixin(Keyboard.class)
public class MixinKeyboard {

    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player == null) return;
        //System.out.println("Key: " + key + " Scancode: " + scancode + " Action: " + action + " Modifiers: " + modifiers);
        /*
            * action = 0: Key released
            * action = 1: Key pressed
            * action = 2: Key held
            * key = keycode
            * not sure what scancode means
            * modifiers = 0: No modifier
            * modifiers = 1: Shift
            * modifiers = 2: Control
            * modifiers = 4: Alt
         */
        if (action == 1) new KeyPressEvent(key).post();
    }
}
