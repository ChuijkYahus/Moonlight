package net.mehvahdjukaar.moonlight.core.map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.map.CustomMapData;
import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.markers.MapBlockMarker;
import net.mehvahdjukaar.moonlight.api.map.type.CustomDecorationType;
import net.mehvahdjukaar.moonlight.api.map.type.JsonDecorationType;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.moonlight.api.misc.TriFunction;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@ApiStatus.Internal
public class MapDataInternal {

    //pain
    public static final Codec<MapDecorationType<?, ?>> CODEC =
            Codec.either(CustomDecorationType.CODEC, JsonDecorationType.CODEC).xmap(
                    either -> either.map(s -> s, c -> c),
                    type -> {
                        if (type == null) {
                            Moonlight.LOGGER.error("map decoration type cant be null. how did this happen?");
                        }
                        if (type instanceof CustomDecorationType<?, ?> c) {
                            return Either.left(c);
                        }
                        return Either.right((JsonDecorationType) type);
                    });

    public static final Codec<MapDecorationType<?, ?>> NETWORK_CODEC =
            Codec.either(CustomDecorationType.CODEC, JsonDecorationType.NETWORK_CODEC).xmap(
                    either -> either.map(s -> s, c -> c),
                    type -> {
                        if (type == null) {
                            Moonlight.LOGGER.error("map decoration type cant be null. how did this happen?");
                        }
                        if (type instanceof CustomDecorationType<?, ?> c) {
                            return Either.left(c);
                        }
                        return Either.right((JsonDecorationType) type);
                    });

    //data holder
    @ApiStatus.Internal
    public static final Map<ResourceLocation, CustomMapData.Type<?>> CUSTOM_MAP_DATA_TYPES = new LinkedHashMap<>();

    /**
     * Registers a custom data type to be stored in map data. Type will provide its onw data implementation
     **/
    public static <T extends CustomMapData<?>> CustomMapData.Type<T> registerCustomMapSavedData(CustomMapData.Type<T> type) {
        if (CUSTOM_MAP_DATA_TYPES.containsKey(type.id())) {
            throw new IllegalArgumentException("Duplicate custom map data registration " + type.id());
        } else {
            CUSTOM_MAP_DATA_TYPES.put(type.id(), type);
        }
        return type;
    }

    //map markers

    public static final ResourceKey<Registry<MapDecorationType<?, ?>>> KEY = ResourceKey.createRegistryKey(Moonlight.res("map_markers"));
    public static final ResourceLocation GENERIC_STRUCTURE_ID = Moonlight.res("generic_structure");
    private static final BiMap<ResourceLocation, Supplier<CustomDecorationType<?, ?>>> CODE_TYPES_FACTORIES = HashBiMap.create();

    public static MapDecorationType<?, ?> getGenericStructure() {
        return get(GENERIC_STRUCTURE_ID);
    }

    /**
     * Call before mod setup. Register a code defined map marker type. You will still need to add a related json file
     */
    public static void registerCustomType(ResourceLocation id, Supplier<CustomDecorationType<?, ?>> decorationType) {
        CODE_TYPES_FACTORIES.put(id, decorationType);
    }

    //TODO: redo in 1.20.6
    //maybe rename the decoration and decortion type to MapDecorationInstance and MapDecorationType to MapDecoration and the factory to type
    public static CustomDecorationType<?, ?> createCustomType(ResourceLocation factoryID) {
        var factory = Objects.requireNonNull(CODE_TYPES_FACTORIES.get(factoryID),
                "No map decoration type with id: " + factoryID);
        var t = factory.get();
        //TODO: improve
        t.factoryId = factoryID;
        return t;
    }

    public static MapDecorationType<?, ?> getAssociatedType(Holder<Structure> structure) {
        for (var v : getValues()) {
            Optional<HolderSet<Structure>> associatedStructure = v.getAssociatedStructure();
            if (associatedStructure.isPresent() && associatedStructure.get().contains(structure)) {
                return v;
            }
        }
        return getGenericStructure();
    }

    @ApiStatus.Internal
    @ExpectPlatform
    public static void init() {
        throw new AssertionError();
    }

    @Deprecated
    public static Registry<MapDecorationType<?, ?>> hackyGetRegistry() {
        return Utils.hackyGetRegistryAccess().registryOrThrow(KEY);
    }

    public static Registry<MapDecorationType<?, ?>> getRegistry(RegistryAccess registryAccess) {
        return registryAccess.registryOrThrow(KEY);
    }

    public static Collection<MapDecorationType<?, ?>> getValues() {
        return hackyGetRegistry().stream().toList();
    }

    public static Set<Map.Entry<ResourceKey<MapDecorationType<?, ?>>, MapDecorationType<?, ?>>> getEntries() {
        return hackyGetRegistry().entrySet();
    }

    @Nullable
    public static MapDecorationType<? extends CustomMapDecoration, ?> get(String id) {
        return get(new ResourceLocation(id));
    }

    public static MapDecorationType<?, ?> get(ResourceLocation id) {
        var reg = hackyGetRegistry();
        var r = reg.get(id);
        if (r == null) return reg.get(GENERIC_STRUCTURE_ID);
        return r;
    }

    public static Optional<MapDecorationType<?, ?>> getOptional(ResourceLocation id) {
        return hackyGetRegistry().getOptional(id);
    }

    public static Set<MapBlockMarker<?>> getDynamicServer(Player player, int mapId, MapItemSavedData data) {
        Set<MapBlockMarker<?>> dynamic = new HashSet<>();
        for (var v : DYNAMIC_SERVER) {
            dynamic.addAll(v.apply(player, mapId, data));
        }
        return dynamic;
    }

    public static Set<MapBlockMarker<?>> getDynamicClient(int mapId, MapItemSavedData data) {
        Set<MapBlockMarker<?>> dynamic = new HashSet<>();
        for (var v : DYNAMIC_CLIENT) {
            dynamic.addAll(v.apply(mapId, data));
        }
        return dynamic;
    }


    @Nullable
    public static MapBlockMarker<?> readWorldMarker(CompoundTag compound) {
        for (var id : compound.getAllKeys()) {
            return get(new ResourceLocation(id)).loadMarkerFromNBT(compound.getCompound(id));
        }
        return null;
    }

    /**
     * returns a list of suitable world markers associated to a position. called by mixin code
     *
     * @param reader world
     * @param pos    world position
     * @return markers found, empty list if none found
     */
    public static List<MapBlockMarker<?>> getMarkersFromWorld(BlockGetter reader, BlockPos pos) {
        List<MapBlockMarker<?>> list = new ArrayList<>();
        for (MapDecorationType<?, ?> type : getValues()) {
            MapBlockMarker<?> c = type.getWorldMarkerFromWorld(reader, pos);
            if (c != null) list.add(c);
        }
        return list;
    }

    //dynamic markers

    private static final List<TriFunction<Player, Integer, MapItemSavedData, Set<MapBlockMarker<?>>>> DYNAMIC_SERVER = Collections.synchronizedList(new ArrayList<>());;
    private static final List<BiFunction<Integer, MapItemSavedData, Set<MapBlockMarker<?>>>> DYNAMIC_CLIENT = Collections.synchronizedList(new ArrayList<>());;


    public static void addDynamicClientMarkersEvent(BiFunction<Integer, MapItemSavedData, Set<MapBlockMarker<?>>> event) {
        DYNAMIC_CLIENT.add(event);
    }

    public static void addDynamicServerMarkersEvent(TriFunction<Player, Integer, MapItemSavedData, Set<MapBlockMarker<?>>> event) {
        DYNAMIC_SERVER.add(event);
    }

}
