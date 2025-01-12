package net.mehvahdjukaar.moonlight.core.set;

import com.google.common.base.Stopwatch;
import com.mojang.serialization.Codec;
import com.mojang.serialization.*;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.misc.MapRegistry;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.set.BlockTypeRegistry;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

@ApiStatus.Internal
public class BlockSetInternal {

    //Frick mod loading is multithreaded, so we need to beware of concurrent access
    private static final Queue<Runnable> FINDER_ADDER = new ArrayDeque<>();
    private static final Queue<Runnable> REMOVER_ADDER = new ArrayDeque<>();

    private static final Map<Class<? extends BlockType>, BlockTypeRegistry<?>> REGISTRIES_BY_CLASS = new LinkedHashMap<>();
    private static final MapRegistry<BlockTypeRegistry<?>> REGISTRIES_BY_NAME = new MapRegistry<>("block_set_registry");
    private static final List<BlockTypeRegistry<?>> ORDERED_REGISTRIES = new ArrayList<>();

    public static void initializeBlockSets() {
        Stopwatch sw = Stopwatch.createStarted();
        if (hasFilledBlockSets()) throw new UnsupportedOperationException("block sets have already bee initialized");
        FINDER_ADDER.forEach(Runnable::run);
        FINDER_ADDER.clear();

        var regs = getRegistries();
        regs.forEach(BlockTypeRegistry::buildAll);
        regs.forEach(BlockTypeRegistry::onBlockInit);

        //remove not wanted ones
        REMOVER_ADDER.forEach(Runnable::run);
        REMOVER_ADDER.clear();

        Moonlight.LOGGER.info("Initialized block sets in {}ms", sw.elapsed().toMillis());
    }

    @ExpectPlatform
    protected static boolean hasFilledBlockSets() {
        throw new AssertionError();
    }

    public synchronized static <T extends BlockType> void registerBlockSetDefinition(BlockTypeRegistry<T> typeRegistry) {
        if (hasFilledBlockSets()) {
            throw new UnsupportedOperationException(
                    String.format("Tried to register block set definition %s after registry events", typeRegistry));
        }
        REGISTRIES_BY_CLASS.put(typeRegistry.getType(), typeRegistry);
        REGISTRIES_BY_NAME.register(typeRegistry.typeName(), typeRegistry);
    }

    public synchronized static <T extends BlockType> void addBlockTypeFinder(Class<T> type, BlockType.SetFinder<T> blockFinder) {
        if (hasFilledBlockSets()) {
            throw new UnsupportedOperationException(
                    String.format("Tried to register block %s finder %s after registry events", type, blockFinder));
        }
        FINDER_ADDER.add(() -> {
            BlockTypeRegistry<T> container = getBlockSet(type);
            container.addFinder(blockFinder);
        });
    }

    public synchronized static <T extends BlockType> void addBlockTypeRemover(Class<T> type, ResourceLocation id) {
        if (hasFilledBlockSets()) {
            throw new UnsupportedOperationException(
                    String.format("Tried to remove block type %s for type %s after registry events", id, type));
        }
        REMOVER_ADDER.add(() -> {
            BlockTypeRegistry<T> container = getBlockSet(type);
            container.addRemover(id);
        });
    }

    @SuppressWarnings("unchecked")
    public static <T extends BlockType> BlockTypeRegistry<T> getBlockSet(Class<T> type) {
        return (BlockTypeRegistry<T>) REGISTRIES_BY_CLASS.get(type);
    }

    @EventCalled
    public static void addTranslations(AfterLanguageLoadEvent event) {
        BlockSetAPI.getRegistries().forEach(r -> r.addTypeTranslations(event));
    }

    @ExpectPlatform
    public static <T extends BlockType, E> void addDynamicRegistration(
            BlockSetAPI.BlockTypeRegistryCallback<E, T> registrationFunction, Class<T> blockType,
            Registry<E> registry) {
        throw new AssertionError();
    }


    //gets registries in order
    public static Collection<BlockTypeRegistry<?>> getRegistries() {
        if (ORDERED_REGISTRIES.isEmpty()) {
            synchronized (ORDERED_REGISTRIES) {
                Comparator<BlockTypeRegistry<?>> comparator = Comparator.comparingInt(BlockTypeRegistry::priority);
                // Sort the registries based on their priority. Higher priority comes first.
                ORDERED_REGISTRIES.addAll(REGISTRIES_BY_NAME.getValues()
                        .stream()
                        .sorted(comparator.reversed())
                        .toList());
            }
        }
        return ORDERED_REGISTRIES;
    }

    @Nullable
    public static <T extends BlockType> BlockTypeRegistry<T> getRegistry(Class<T> typeClass) {
        return (BlockTypeRegistry<T>) REGISTRIES_BY_CLASS.get(typeClass);
    }

    public static BlockTypeRegistry<?> getRegistry(String name) {
        return REGISTRIES_BY_NAME.getValue(name);
    }

    @Nullable
    public static <T extends BlockType> T getBlockTypeOf(ItemLike itemLike, Class<T> typeClass) {
        BlockTypeRegistry<T> r = getRegistry(typeClass);
        if (r != null) {
            return r.getBlockTypeOf(itemLike);
        }
        return null;
    }

    public static Codec<BlockTypeRegistry<?>> getRegistriesCodec() {
        return REGISTRIES_BY_NAME;
    }


    //very dumb
    // experimental. Returns first block type in a map codec
    public static MapCodec<BlockType> createGenericCodec() {
        return new MapCodec<BlockType>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return REGISTRIES_BY_NAME.keySet().stream().map(ResourceLocation::toString).map(ops::createString);
            }

            @Override
            public <T> DataResult<BlockType> decode(DynamicOps<T> ops, MapLike<T> input) {
                for (var e : input.entries().toList()) {
                    T keyStr = e.getFirst();
                    T valueStr = e.getSecond();
                    var registry = REGISTRIES_BY_NAME.decode(ops, keyStr).getOrThrow().getFirst();
                    var value = registry.getCodec().decode(ops, valueStr).getOrThrow();
                    return DataResult.success(value.getFirst());
                }
                return DataResult.error(() -> "No block type found");
            }

            @Override
            public <T> RecordBuilder<T> encode(BlockType input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                BlockTypeRegistry<?> registry = input.getRegistry();
                ResourceLocation key = REGISTRIES_BY_NAME.getKey(registry);
                if (key != null) {
                    Codec<BlockType> codec = (Codec<BlockType>) registry.getCodec();
                    return prefix.add(ops.createString(key.toString()),
                            codec.encodeStart(ops, input));
                }
                return prefix;
            }
        };
    }

}
