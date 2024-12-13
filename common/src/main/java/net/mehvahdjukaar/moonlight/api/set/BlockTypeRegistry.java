package net.mehvahdjukaar.moonlight.api.set;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.set.BlockSetInternal;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class BlockTypeRegistry<T extends BlockType> {

    public static Codec<BlockTypeRegistry<?>> getRegistryCodec() {
        return BlockSetInternal.REGISTRIES_BY_NAME;
    }

    protected boolean frozen = false;
    private final String name;
    private final List<BlockType.SetFinder<T>> finders = new ArrayList<>();
    private final List<ResourceLocation> notInclude = new ArrayList<>();
    protected final List<T> builder = new ArrayList<>();
    private final Class<T> typeClass;
    private Map<ResourceLocation, T> types = new LinkedHashMap<>();
    private final Object2ObjectOpenHashMap<Object, T> childrenToType = new Object2ObjectOpenHashMap<>();

    protected BlockTypeRegistry(Class<T> typeClass, String name) {
        this.typeClass = typeClass;
        this.name = name;
    }

    public Class<T> getType() {
        return typeClass;
    }

    /**
     * Gets corresponding block type or oak if the provided one is not installed or missing
     *
     * @param name string resource location name of the type
     * @return wood type
     */
    public T getFromNBT(String name) {
        return this.types.getOrDefault(new ResourceLocation(name), this.getDefaultType());
    }

    @Nullable
    public T get(ResourceLocation res) {
        return this.types.get(res);
    }

    public abstract T getDefaultType();

    public Collection<T> getValues() {
        return Collections.unmodifiableCollection(types.values());
    }

    public String typeName() {
        return name;
    }

    /**
     * Returns an optional block Type based on the given block. Pretty much defines the logic of how a block set is constructed
     */
    public abstract Optional<T> detectTypeFromBlock(Block block, ResourceLocation blockId);

    public void registerBlockType(T newType) {
        if (frozen) {
            throw new UnsupportedOperationException("Tried to register a wood types after registry events");
        }
        builder.add(newType);
    }

    public Collection<BlockType.SetFinder<T>> getFinders() {
        return finders;
    }

    public void addFinder(BlockType.SetFinder<T> finder) {
        if (frozen) {
            throw new UnsupportedOperationException("Tried to register a block type finder after registry events");
        }
        finders.add(finder);
    }

    public void addRemover(ResourceLocation id) {
        if (frozen) {
            throw new UnsupportedOperationException("Tried remove a block type after registry events");
        }
        notInclude.add(id);
    }

    protected void finalizeAndFreeze() {
        if (frozen) {
            throw new UnsupportedOperationException("Block types are already finalized");
        }
        LinkedHashMap<ResourceLocation, T> linkedHashMap = new LinkedHashMap<>();
        List<String> modOrder = new ArrayList<>();
        modOrder.add("minecraft");
        builder.forEach(e -> {
            String modId = e.getNamespace();
            if (!modOrder.contains(modId)) modOrder.add(modId);
        });
        //orders them by mod id
        for (String modId : modOrder) {
            builder.forEach(e -> {
                if (Objects.equals(e.getNamespace(), modId)) {
                    if (!linkedHashMap.containsKey(e.getId())) {
                        linkedHashMap.put(e.getId(), e);
                    }
                }
            });
        }
        this.types = ImmutableMap.copyOf(linkedHashMap);
        builder.clear();
        this.frozen = true;
    }

    @ApiStatus.Internal
    public void onBlockInit() {
        this.types.values().forEach(BlockType::initializeChildrenBlocks);
    }

    @ApiStatus.Internal
    public void onItemInit() {
        this.types.values().forEach(BlockType::initializeChildrenItems);
    }

    @ApiStatus.Internal
    public void buildAll() {
        if (!frozen) {
            //adds default
            this.registerBlockType(this.getDefaultType());
            //adds finders
            finders.stream().map(BlockType.SetFinder::get).forEach(f -> f.ifPresent(this::registerBlockType));
            for (Block b : BuiltInRegistries.BLOCK) {
                this.detectTypeFromBlock(b, Utils.getID(b)).ifPresent(t -> {
                    if (!notInclude.contains(t.getId())) this.registerBlockType(t);
                });
            }
            finders.clear();
            notInclude.clear();
            this.finalizeAndFreeze();
        }
    }

    /**
     * Called at the right time on language reload. Use to add translations of your block type names.
     * Useful to merge more complex translation strings using RPAwareDynamicTextureProvider::addDynamicLanguage
     */
    public void addTypeTranslations(AfterLanguageLoadEvent language) {

    }

    @Nullable
    public T getBlockTypeOf(ItemLike itemLike) {
        var fist = childrenToType.getOrDefault(itemLike, null);
        if (fist != null) return fist;
        if (itemLike instanceof BlockItem bi) {
            return childrenToType.getOrDefault(bi.getBlock(), null);
        }
        return null;
    }

    protected void mapObjectToType(Object itemLike, BlockType type) {
        this.childrenToType.put(itemLike, (T) type);
        if (itemLike instanceof BlockItem bi) {
            if (!this.childrenToType.containsKey(bi.asItem())) {
                this.childrenToType.put(bi.asItem(), (T) type);
            }
        }
    }

    // load priority. higher is loaded first. 100 is default
    public int priority() {
        return 100;
    }
}
