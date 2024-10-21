package at.hannibal2.skyhanni.mixins.transformers;

import at.hannibal2.skyhanni.SkyHanniMod;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.SignatureState;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(PlayerSkinProvider.class)
public abstract class MixinPlayerSkinProvider {

    @Mutable
    @Shadow
    @Final
    private LoadingCache<PlayerSkinProvider.Key, CompletableFuture<SkinTextures>> cache;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    public abstract CompletableFuture<SkinTextures> fetchSkinTextures(GameProfile par1);

    @Shadow
    abstract CompletableFuture<SkinTextures> fetchSkinTextures(UUID par1, MinecraftProfileTextures par2);

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void initWarn(TextureManager textureManager, Path directory, MinecraftSessionService sessionService, Executor executor, CallbackInfo ci) {
        if (!SkyHanniMod.feature.dev.minecraftConsoles.consoleFilter.filterVerifyProperty) return;
        cache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofSeconds(15L)).build(new CacheLoader<PlayerSkinProvider.Key, CompletableFuture<SkinTextures>>() {
            public CompletableFuture<SkinTextures> load(PlayerSkinProvider.Key key) {
                return CompletableFuture.supplyAsync(() -> {
                    Property property = key.packedTextures();
                    if (property == null) {
                        return MinecraftProfileTextures.EMPTY;
                    } else {
                        MinecraftProfileTextures minecraftProfileTextures = sessionService.unpackTextures(property);
                        if (minecraftProfileTextures.signatureState() == SignatureState.INVALID) {
                            //all this just to remove this 1 line lmao
                            //LOGGER.warn("Profile contained invalid signature ferty (profile id: {})", key.profileId());
                        }

                        return minecraftProfileTextures;
                    }
                }, Util.getMainWorkerExecutor()).thenComposeAsync((MinecraftProfileTextures textures) -> {
                    return fetchSkinTextures2(key.profileId(), textures);
                }, executor);
            }
        });
    }

    private CompletableFuture<SkinTextures> fetchSkinTextures2(UUID uuid, MinecraftProfileTextures textures) {
        return this.fetchSkinTextures(uuid, textures);
    }
}
