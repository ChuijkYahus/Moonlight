package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    //for some reason this takes ahuge amount of time when
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;adjustSpawnLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;"))
    private BlockPos ml$preventUselessCalculations(ServerPlayer instance, ServerLevel serverLevel, BlockPos blockPos, Operation<BlockPos> original) {
        if (PlatHelper.isFakePlayer(instance)) {
            return BlockPos.ZERO;
        }
        return original.call(instance, serverLevel, blockPos);
    }
}
