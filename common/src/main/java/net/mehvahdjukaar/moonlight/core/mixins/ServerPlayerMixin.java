package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    //for some reason this takes ahuge amount of time when
    @WrapWithCondition(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;fudgeSpawnLocation(Lnet/minecraft/server/level/ServerLevel;)V"))
    private boolean ml$preventUselessCalculations(ServerPlayer instance, ServerLevel level) {
        if (PlatHelper.isFakePlayer(instance)) {
            return false;
        }
        return true;
    }
}
