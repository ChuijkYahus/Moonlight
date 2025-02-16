package net.mehvahdjukaar.moonlight.api.set.wood;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class WoodType extends BlockType {

    public static Codec<WoodType> CODEC;
    public static StreamCodec<FriendlyByteBuf, WoodType> STREAM_CODEC;
    static{
        WoodTypeRegistry.touch();
    }

    public final Block planks;
    public final Block log;

    // like this so it can be called early. not too early tho as children might not be initialized
    private final Supplier<net.minecraft.world.level.block.state.properties.WoodType> vanillaType = Suppliers.memoize(this::detectVanillaWood);

    @Nullable
    private net.minecraft.world.level.block.state.properties.WoodType detectVanillaWood() {
        if (getChild("hanging_sign") instanceof CeilingHangingSignBlock c) {
            return c.type();
        }
        if (getChild("sign") instanceof SignBlock f) {
            return f.type();
        }
        String i = id.getNamespace().equals("minecraft") ? id.getPath() : id.toString();
        var values = net.minecraft.world.level.block.state.properties.WoodType.values();
        var o = values.filter(v -> v.name().equals(i)).findAny();
        return o.orElse(null);
    }

    public WoodType(ResourceLocation id, Block baseBlock, Block logBlock) {
        super(id);
        this.planks = baseBlock;
        this.log = logBlock;
    }

    @Nullable
    protected Block findStrippedLog(String... possibleNames) {
        for (var v : possibleNames) {
            var b = this.getBlockOfThis(v);
            if (v != null) {
                Block stripped = AxeItem.STRIPPABLES.get(b);
                if (stripped != null && stripped != b) {
                    return stripped;
                }
            }
        }
        return findLogRelatedBlock("stripped", possibleNames);
    }

    @Nullable
    protected Block findLogRelatedBlock(String prefix, String... possibleNames) {
        for (var n : possibleNames) {
            var b = findWithPrefix(prefix, n);
            if (b != null) return b;
        }
        return null;
    }

    @Nullable
    protected Block findWithPrefix(String prefix, String postfix) {
        String prefix_ = prefix.isEmpty() ? "" : prefix + "_";
        var id = this.getId();
        String logN = Utils.getID(this.log).getPath();

        // SUPPORT: TFC & AFC
        String path = id.getPath();
        if (this.id.getNamespace().equals("tfc") || this.id.getNamespace().equals("afc")) {
            var o = BuiltInRegistries.BLOCK.getOptional(
                    id.withPath("wood/" + prefix_ + postfix + "/" + path));
            if (o.isPresent()) return o.get();
        }

        Set<ResourceLocation> targets = new HashSet<>();
        Collections.addAll(targets,
                id.withPath(path + "_" + prefix_ + postfix),
                id.withPath(prefix_ + path + "_" + postfix),
                id.withPath(logN + "_" + prefix_ + postfix),
                id.withPath(prefix_ + logN + "_" + postfix)
        );
        Block found = null;
        for (var r : targets) {
            if (BuiltInRegistries.BLOCK.containsKey(r)) {
                found = BuiltInRegistries.BLOCK.get(r);
                break;
            }
        }
        return found;
    }

    @Override
    public ItemLike mainChild() {
        return planks;
    }

    @Nullable
    public net.minecraft.world.level.block.state.properties.WoodType toVanilla() {
        return this.vanillaType.get();
    }

    @NotNull
    public net.minecraft.world.level.block.state.properties.WoodType toVanillaOrOak() {
        var v = toVanilla();
        if (v != null) return v;
        return net.minecraft.world.level.block.state.properties.WoodType.OAK;
    }

    /**
     * Use this to get the texture path of a wood type
     *
     * @return something like minecraft/oak
     */
    public String getTexturePath() {
        String namespace = this.getNamespace();
        if (namespace.equals("minecraft")) return this.getTypeName();
        return this.getNamespace() + "/" + this.getTypeName();
    }

    public boolean canBurn() {
        return this.planks.defaultBlockState().ignitedByLava();
    }

    public MapColor getColor() {
        return this.planks.defaultMapColor();
    }

    @Override
    public String getTranslationKey() {
        return "wood_type." + this.getNamespace() + "." + this.getTypeName();
    }

    @Override
    public void initializeChildrenBlocks() {
        this.addChild("planks", this.planks);
        this.addChild("log", this.log);
        this.addChild("leaves", this.findRelatedEntry("leaves", BuiltInRegistries.BLOCK));
        this.addChild("wood", this.findLogRelatedBlock("", "wood", "hyphae"));
        this.addChild("stripped_log", this.findStrippedLog("log", "stem", "stalk"));
        this.addChild("stripped_wood", this.findStrippedLog("wood", "hyphae"));
        this.addChild("slab", this.findRelatedEntry("slab", BuiltInRegistries.BLOCK));
        this.addChild("stairs", this.findRelatedEntry("stairs", BuiltInRegistries.BLOCK));
        this.addChild("fence", this.findRelatedEntry("fence", BuiltInRegistries.BLOCK));
        this.addChild("fence_gate", this.findRelatedEntry("fence_gate", BuiltInRegistries.BLOCK));
        this.addChild("door", this.findRelatedEntry("door", BuiltInRegistries.BLOCK));
        this.addChild("trapdoor", this.findRelatedEntry("trapdoor", BuiltInRegistries.BLOCK));
        this.addChild("button", this.findRelatedEntry("button", BuiltInRegistries.BLOCK));
        this.addChild("pressure_plate", this.findRelatedEntry("pressure_plate", BuiltInRegistries.BLOCK));
        this.addChild("hanging_sign", this.findRelatedEntry("hanging_sign", BuiltInRegistries.BLOCK));
        this.addChild("wall_hanging_sign", this.findRelatedEntry("wall_hanging_sign", BuiltInRegistries.BLOCK));
        this.addChild("sign", this.findRelatedEntry("sign", BuiltInRegistries.BLOCK));
        this.addChild("wall_sign", this.findRelatedEntry("wall_sign", BuiltInRegistries.BLOCK));
    }

    @Override
    public void initializeChildrenItems() {
        this.addChild("boat", this.findRelatedEntry("boat", BuiltInRegistries.ITEM));
        this.addChild("chest_boat", this.findRelatedEntry("chest_boat", BuiltInRegistries.ITEM));
        this.addChild("sapling", this.findRelatedEntry("sapling", BuiltInRegistries.ITEM));
        this.addChild("stick", this.findRelatedEntry("twig", BuiltInRegistries.BLOCK)); // TFC & AFC only
    }

    @Nullable
    protected <V> V findRelatedEntry(String before, String after, Registry<V> reg) {
        if (!after.isEmpty()) after = "_" + after;
        ResourceLocation[] targets = {
                id.withPath(id.getPath() + "_" + before + after),
                id.withPath(before + "_" + id.getPath() + after),
                //weird conventions here
                id.withPath(id.getPath() + "_planks_" + before + after),
                // TFC & AFC: Include children of wood_type: stairs, slab...
                id.withPath("wood/planks/" + id.getPath() + "_" + before),
                // TFC & AFC: Include twig (sticks), leaves, planks
                id.withPath("wood/" + before + after + "/" + id.getPath())
        };
        V found = null;
        for (var r : targets) {
            if (reg.containsKey(r)) {
                found = reg.get(r);
                break;
            }
        }
        return found;
    }

    public static class Finder implements SetFinder<WoodType> {

        private final Map<String, ResourceLocation> childNames = new HashMap<>();
        private final Supplier<Block> planksFinder;
        private final Supplier<Block> logFinder;
        private final ResourceLocation id;

        public Finder(ResourceLocation id, Supplier<Block> planks, Supplier<Block> log) {
            this.id = id;
            this.planksFinder = planks;
            this.logFinder = log;
        }

        public static Finder simple(String modId, String woodTypeName, String planksName, String logName) {
            return simple(ResourceLocation.fromNamespaceAndPath(modId, woodTypeName), ResourceLocation.fromNamespaceAndPath(modId, planksName),
                    ResourceLocation.fromNamespaceAndPath(modId, logName));
        }

        public static Finder simple(ResourceLocation woodTypeName, ResourceLocation planksName, ResourceLocation logName) {
            return new Finder(woodTypeName,
                    () -> BuiltInRegistries.BLOCK.get(planksName),
                    () -> BuiltInRegistries.BLOCK.get(logName));
        }

        public void addChild(String childType, String childName) {
            addChild(childType, id.withPath(childName));
        }

        public void addChild(String childType, ResourceLocation childName) {
            this.childNames.put(childType, childName);
        }

        @ApiStatus.Internal
        @Override
        public Optional<WoodType> get() {
            if (PlatHelper.isModLoaded(id.getNamespace())) {
                try {
                    Block plank = planksFinder.get();
                    Block log = logFinder.get();
                    var d = BuiltInRegistries.BLOCK.get(BuiltInRegistries.BLOCK.getDefaultKey());
                    if (plank != d && log != d && plank != null && log != null) {
                        var w = new WoodType(id, plank, log);
                        childNames.forEach((key, value) -> {
                            Block obj = BuiltInRegistries.BLOCK.get(value);
                            if (obj != null && obj != d) w.addChild(key, obj);
                        });
                        return Optional.of(w);
                    }
                } catch (Exception ignored) {
                }
                Moonlight.LOGGER.warn("Failed to find custom wood type {}", id);
            }
            return Optional.empty();
        }
    }

    //just copies base properties without calling copy
    public BlockBehaviour.Properties copyProperties() {
        var p = BlockBehaviour.Properties.of();
        p.mapColor(this.getColor());
        if (this.canBurn()) p.ignitedByLava();
        p.sound(this.getSound());
        return p;
    }

}
