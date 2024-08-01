package net.mehvahdjukaar.moonlight.api.entity;

import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class ImprovedFallingBlockEntity extends FallingBlockEntity {

    protected boolean saveTileDataToItem;

    public ImprovedFallingBlockEntity(EntityType<? extends FallingBlockEntity> type, Level level) {
        super(type, level);
        saveTileDataToItem = false;
    }

    public ImprovedFallingBlockEntity(EntityType<? extends FallingBlockEntity> type, Level level, BlockPos pos,
                                      BlockState blockState, boolean saveDataToItem) {
        super(type, level);
        this.blocksBuilding = true;
        this.xo = pos.getX() + 0.5D;
        this.yo = pos.getY();
        this.zo = pos.getZ() + 0.5D;
        this.setPos(xo, yo + ((1.0F - this.getBbHeight()) / 2.0F), zo);
        this.setDeltaMovement(Vec3.ZERO);
        this.setStartPos(this.blockPosition());
        this.setBlockState(blockState);
        this.saveTileDataToItem = saveDataToItem;
    }

    public static ImprovedFallingBlockEntity fall(EntityType<? extends FallingBlockEntity> type, Level level,
                                                  BlockPos pos, BlockState state, boolean saveDataToItem) {
        ImprovedFallingBlockEntity entity = new ImprovedFallingBlockEntity(type, level, pos, state,
                saveDataToItem);
        level.setBlock(pos, state.getFluidState().createLegacyBlock(), 3);
        level.addFreshEntity(entity);
        return entity;
    }

    public void setSaveTileDataToItem(boolean b) {
        this.saveTileDataToItem = b;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("saveToItem", this.saveTileDataToItem);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.saveTileDataToItem = tag.getBoolean("saveToItem");
    }

    //workaround
    public void setBlockState(BlockState state) {
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        CompoundTag tag = new CompoundTag();
        tag.put("BlockState", NbtUtils.writeBlockState(state));
        tag.putInt("Time", this.time);
        this.readAdditionalSaveData(tag);
    }

    @Override
    public ItemEntity spawnAtLocation(ItemLike itemIn, int offset) {
        ItemStack stack = new ItemStack(itemIn);
        if (itemIn instanceof Block && this.saveTileDataToItem && this.blockData != null) {
            BlockEntity be = BlockEntity.loadStatic(BlockPos.ZERO, getBlockState(), blockData, level().registryAccess());
            if (be != null) stack.applyComponents(be.collectComponents());
            else Moonlight.LOGGER.warn("Failed to load block entity for falling block. Block Entity data: {}", blockData);
        }
        return this.spawnAtLocation(stack, offset);
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return super.causeFallDamage(pFallDistance, pMultiplier, pSource);
    }

    public void setCancelDrop(boolean cancelDrop) {
        this.cancelDrop = cancelDrop;
    }
}
