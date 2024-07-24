package net.mehvahdjukaar.moonlight.api.set.leaves;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class LeavesType extends BlockType {

    public static final Codec<LeavesType> CODEC = ResourceLocation.CODEC.flatXmap(r -> {
                LeavesType w = LeavesTypeRegistry.getValue(r);
                if (w == null) return DataResult.error(() -> "No such leaves type: " + r);
                return DataResult.success(w);
            },
            t -> DataResult.success(t.id));

    public final Block leaves;

    private final Supplier<WoodType> woodType;

    protected LeavesType(ResourceLocation id, Block leaves) {
        this(id, leaves, Suppliers.memoize(() -> Objects.requireNonNullElse(WoodTypeRegistry.getValue(id), WoodTypeRegistry.OAK_TYPE)));
    }

    protected LeavesType(ResourceLocation id, Block leaves, Supplier<WoodType> woodType) {
        super(id);
        this.leaves = leaves;
        this.woodType = woodType;
    }

    public WoodType getWoodType() {
        return woodType.get();
    }

    @Override
    public ItemLike mainChild() {
        return leaves;
    }

    @Override
    public String getTranslationKey() {
        return "leaves_type." + this.getNamespace() + "." + this.getTypeName();
    }

    @Override
    public void initializeChildrenBlocks() {
        this.addChild("leaves", leaves);
        this.woodType.get();
    }

    @Override
    public void initializeChildrenItems() {
        this.addChild("sapling", this.findRelatedEntry("sapling", BuiltInRegistries.ITEM));
    }

    public static class Finder implements SetFinder<LeavesType> {

        private final Map<String, ResourceLocation> childNames = new HashMap<>();
        private final Supplier<Block> leavesFinder;
        private final Supplier<WoodType> woodFinder;
        private final ResourceLocation id;

        public Finder(ResourceLocation id, Supplier<Block> leaves, @Nullable Supplier<WoodType> wood) {
            this.id = id;
            this.leavesFinder = leaves;
            this.woodFinder = wood;
        }

        public static Finder simple(String modId, String leavesTypeName, String leavesName) {
            return new Finder(ResourceLocation.fromNamespaceAndPath(modId, leavesTypeName),
                    () -> BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(modId, leavesName)), null);
        }

        public static Finder simple(String modId, String leavesTypeName, String leavesName, String woodTypeName) {
            return new Finder(ResourceLocation.fromNamespaceAndPath(modId, leavesTypeName),
                    () -> BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(modId, leavesName)),
                    () -> WoodTypeRegistry.INSTANCE.get(ResourceLocation.parse(woodTypeName)));
        }

        public void addChild(String childType, String childName) {
            addChild(childType, id.withPath(childName));
        }

        public void addChild(String childType, ResourceLocation childName) {
            this.childNames.put(childType, childName);
        }

        @Override
        public Optional<LeavesType> get() {
            if (PlatHelper.isModLoaded(id.getNamespace())) {
                try {
                    Block leaves = leavesFinder.get();
                    var d = BuiltInRegistries.BLOCK.get(BuiltInRegistries.BLOCK.getDefaultKey());
                    if (leaves != d && leaves != null) {
                        if (woodFinder == null) {
                            var l = new LeavesType(id, leaves);
                            childNames.forEach((key, value) -> l.addChild(key, BuiltInRegistries.BLOCK.get(value)));
                            return Optional.of(l);
                        } else {
                            var l = new LeavesType(id, leaves, woodFinder);
                            childNames.forEach((key, value) -> l.addChild(key, BuiltInRegistries.BLOCK.get(value)));
                            return Optional.of(l);
                        }
                    }
                } catch (Exception ignored) {
                }
                Moonlight.LOGGER.warn("Failed to find custom wood type {}", id);
            }
            return Optional.empty();
        }

    }
}
