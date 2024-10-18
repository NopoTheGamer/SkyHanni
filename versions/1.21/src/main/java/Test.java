import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;

public class Test implements ModInitializer {
    public void test() {
        for (AbstractClientPlayerEntity player : MinecraftClient.getInstance().world.getPlayers()) {
            player.headYaw = 0;
        }

    }

    @Override
    public void onInitialize() {

    }
}
