package net.mehvahdjukaar.moonlight.api.platform.fabric;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ForgeHelperImpl {

    public static boolean fireOnProjectileImpact(Projectile improvedProjectileEntity, HitResult blockHitResult) {
        return false;
    }

    public static <T extends RecipeInput> Recipe<T> copyRecipeConditions(Recipe<T> originalRecipe, Recipe<?> otherRecipe) {
        return originalRecipe;
    }

    public static boolean isCurativeItem(ItemStack stack, MobEffectInstance effect) {
        return stack.getItem() == Items.MILK_BUCKET || stack.getItem() == Items.HONEY_BOTTLE;
    }

    public static boolean canHarvestBlock(BlockState state, ServerLevel level, BlockPos pos, ServerPlayer player) {
        return !state.requiresCorrectToolForDrops() || player.hasCorrectToolForDrops(state);
    }

    public static float getFriction(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return state.getBlock().getFriction();
    }


    public static boolean canEntityDestroy(Level level, BlockPos pos, Animal animal) {
        if (!level.isLoaded(pos)) {
            return false;
        } else {
            return PlatHelper.isMobGriefingOn(level, animal);
        }
    }

    public static boolean fireOnExplosionStart(Level level, Explosion explosion) {
        return false; //true if event cancelled
    }

    public static void fireOnExplosionDetonate(Level level, Explosion explosion, List<Entity> entities, double diameter) {
    }

    public static void fireOnLivingConvert(LivingEntity skellyHorseMixin, LivingEntity newHorse) {
    }

    public static boolean canLivingConvert(LivingEntity entity, EntityType<? extends LivingEntity> outcome, Consumer<Integer> timer) {
        return true;
    }

    public static float getExplosionResistance(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        return state.getBlock().getExplosionResistance();
    }

    public static void fireOnBlockExploded(BlockState blockstate, Level level, BlockPos blockpos, Explosion explosion) {
        level.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 3);
        blockstate.getBlock().wasExploded(level, blockpos, explosion);
    }

    public static boolean areStacksEqual(ItemStack stack, ItemStack other, boolean sameNbt) {
        return stack.equals(other);
    }


    public static boolean isFireSource(BlockState blockState, Level level, BlockPos pos, Direction up) {
        return blockState.is(level.dimensionType().infiniburn());
    }

    public static boolean canDropFromExplosion(BlockState blockstate, Level level, BlockPos blockpos, Explosion explosion) {
        return blockstate.getBlock().dropFromExplosion(explosion);
    }

    public static boolean isDye(ItemStack itemstack) {
        return itemstack.getItem() instanceof DyeItem;
    }

    public static DyeColor getColor(ItemStack stack) {
        if (stack.getItem() instanceof DyeItem dyeItem) {
            return dyeItem.getDyeColor();
        }
        return null;
    }

    public static BlockState rotateBlock(BlockState state, Level world, BlockPos targetPos, Rotation rot) {
        return state.rotate(rot);
    }

    public static boolean isMultipartEntity(Entity e) {
        return e instanceof EnderDragon;
    }

    public static void setPoolName(LootPool.Builder pool, String name) {
    }

    public static RailShape getRailDirection(BaseRailBlock railBlock, BlockState blockstate, Level level, BlockPos blockpos, AbstractMinecart o) {
        return blockstate.getValue(railBlock.getShapeProperty());
    }

    public static Optional<ItemStack> getCraftingRemainingItem(ItemStack itemstack) {
        return Optional.ofNullable(itemstack.getItem().getCraftingRemainingItem()).map(Item::getDefaultInstance);
    }

    public static void reviveEntity(Entity entity) {
    }

    public static boolean fireOnCropsGrowPre(ServerLevel level, BlockPos pos, BlockState state, boolean b) {
        return b;
    }

    public static void fireOnCropsGrowPost(ServerLevel level, BlockPos pos, BlockState state) {
    }

    @org.jetbrains.annotations.Nullable
    public static InteractionResult fireOnRightClickBlock(Player player, InteractionHand hand, BlockPos below, BlockHitResult rayTraceResult) {
        return null;
    }

    public static boolean canEquipItem(LivingEntity entity, ItemStack stack, EquipmentSlot slot) {
        return entity.getEquipmentSlotForItem(stack) == slot;
    }

    public static void fireOnEquipmentChange(LivingEntity entity, EquipmentSlot slot, ItemStack from, ItemStack to) {
    }

    public static int getLightEmission(BlockState state, Level level, BlockPos pos) {
        return state.getLightEmission();
    }

    public static Map<Block, Item> getBlockItemMap() {
        return Item.BY_BLOCK;
    }

    public static void registerDefaultContainerCap(BlockEntityType<? extends Container> container) {
    }

    public static boolean isInFluidThatCanExtinguish(Entity entity) {
        return false;
    }
}