package net.mehvahdjukaar.moonlight.api.map.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.markers.SimpleMapBlockMarker;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

//Base type for simple data-driven type. Basically a simple version of CustomDecorationType that can be serialized
public final class JsonDecorationType implements MapDecorationType<CustomMapDecoration, SimpleMapBlockMarker> {


    public static final Codec<JsonDecorationType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RuleTest.CODEC.optionalFieldOf("target_block").forGetter(JsonDecorationType::getTarget),
            Codec.STRING.optionalFieldOf("name").forGetter(JsonDecorationType::getName),
            Codec.INT.optionalFieldOf("rotation", 0).forGetter(JsonDecorationType::getRotation),
            ColorUtils.CODEC.optionalFieldOf("map_color", 0).forGetter(JsonDecorationType::getDefaultMapColor),
            RegistryCodecs.homogeneousList(Registries.STRUCTURE).optionalFieldOf("target_structures").forGetter(
                    JsonDecorationType::getAssociatedStructure), Codec.STRING.xmap(PlatHelper::isModLoaded, b -> "minecraft")
                    .optionalFieldOf("from_mod", true)
                    .forGetter(t -> t.enabled)
    ).apply(instance, JsonDecorationType::new));

    //we cant reference other data pack registries in network codec...
    public static final Codec<JsonDecorationType> NETWORK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RuleTest.CODEC.optionalFieldOf("target_block").forGetter(JsonDecorationType::getTarget),
            Codec.STRING.optionalFieldOf("name").forGetter(JsonDecorationType::getName),
            Codec.INT.optionalFieldOf("rotation", 0).forGetter(JsonDecorationType::getRotation),
            ColorUtils.CODEC.optionalFieldOf("map_color", 0).forGetter(JsonDecorationType::getDefaultMapColor),
            Codec.BOOL.fieldOf("enabled").forGetter(t -> t.enabled)
    ).apply(instance, JsonDecorationType::new));

    //using this and not block predicate since it requires a worldLevelGen...
    @Nullable
    private final RuleTest target;

    @Nullable
    private final String name;
    @Nullable
    private final HolderSet<Structure> structures;
    private final int mapColor;
    private final int rotation;
    private final boolean enabled;

    public JsonDecorationType(Optional<RuleTest> target) {
        this(target, Optional.empty(), 0, 0, true);
    }

    public JsonDecorationType(Optional<RuleTest> target, Optional<String> name, int rotation,
                              int mapColor, boolean enabled) {
        this(target, name, rotation, mapColor, Optional.empty(), enabled);
    }

    public JsonDecorationType(Optional<RuleTest> target, Optional<String> name, int rotation,
                              int mapColor, Optional<HolderSet<Structure>> structure, Boolean enabled) {
        this.target = target.orElse(null);
        this.name = name.orElse(null);
        this.rotation = rotation;
        this.structures = structure.orElse(null);
        this.mapColor = mapColor;
        this.enabled = enabled;
    }


    public Optional<RuleTest> getTarget() {
        return Optional.ofNullable(target);
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public int getRotation() {
        return rotation;
    }

    public Optional<HolderSet<Structure>> getAssociatedStructure() {
        return Optional.ofNullable(structures);
    }

    public int getDefaultMapColor() {
        return mapColor;
    }

    @Override
    public boolean isFromWorld() {
        return target != null;
    }

    public ResourceLocation getId() {
        return Utils.getID(this);
    }

    @Nullable
    @Override
    public CustomMapDecoration loadDecorationFromBuffer(FriendlyByteBuf buffer) {
        try {
            return new CustomMapDecoration(this, buffer);
        } catch (Exception e) {
            Moonlight.LOGGER.warn("Failed to load custom map decoration for decoration type" + this.getId() + ": " + e);
        }
        return null;
    }

    @Nullable
    @Override
    public SimpleMapBlockMarker load(CompoundTag compound, HolderLookup.Provider registries) {
        SimpleMapBlockMarker marker = new SimpleMapBlockMarker(this);
        try {
            marker.load(compound, registries);
            return marker;
        } catch (Exception e) {
            Moonlight.LOGGER.warn("Failed to load world map marker for decoration type" + this.getId() + ": " + e);
        }
        return null;
    }

    @Nullable
    @Override
    public SimpleMapBlockMarker getWorldMarkerFromWorld(BlockGetter reader, BlockPos pos) {
        if (this.target != null && enabled) {
            if (target.test(reader.getBlockState(pos), RandomSource.create())) {
                SimpleMapBlockMarker m = createEmptyMarker();
                m.setPos(pos);
                return m;
            }
        }
        return null;
    }


    @Override
    public SimpleMapBlockMarker createEmptyMarker() {
        var m = new SimpleMapBlockMarker(this);
        m.setRotation(rotation);
        m.setName(name == null ? null : Component.translatable(name));
        return m;
    }

}
