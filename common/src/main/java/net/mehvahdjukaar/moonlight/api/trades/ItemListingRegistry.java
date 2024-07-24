package net.mehvahdjukaar.moonlight.api.trades;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.mehvahdjukaar.moonlight.api.misc.CodecMapRegistry;
import net.mehvahdjukaar.moonlight.api.misc.MapRegistry;
import net.mehvahdjukaar.moonlight.api.misc.RegistryAccessJsonReloadListener;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;

import java.util.*;

//TODO: rename to ItemListingManager
public class ItemListingRegistry extends RegistryAccessJsonReloadListener {

    protected static final CodecMapRegistry<ModItemListing> REGISTRY = MapRegistry.ofCodec();
    private static final Map<EntityType<?>, Int2ObjectArrayMap<List<ModItemListing>>> SPECIAL_CUSTOM_TRADES = new HashMap<>();
    private static final Map<VillagerProfession, Int2ObjectArrayMap<List<ModItemListing>>> CUSTOM_TRADES = new HashMap<>();

    private static final Map<EntityType<?>, Int2ObjectArrayMap<ModItemListing[]>> oldSpecialTrades = new HashMap<>();
    private static final Map<VillagerProfession, Int2ObjectArrayMap<ModItemListing[]>> oldTrades = new HashMap<>();

    private static int count = 0;

    static{
        REGISTRY.register(ResourceLocation.parse("simple"), SimpleItemListing.CODEC);
        REGISTRY.register(ResourceLocation.parse("remove_all_non_data"), RemoveNonDataListingListing.CODEC);
        REGISTRY.register(ResourceLocation.parse("no_op"), NoOpListing.CODEC);
    }


    public ItemListingRegistry() {
        super(new Gson(), "moonlight/villager_trades");
    }

    @Override
    public void parse(Map<ResourceLocation, JsonElement> jsons, RegistryAccess registryAccess) {

        mergeProfessionAndSpecial(false);

        count = 0;
        CUSTOM_TRADES.clear();
        SPECIAL_CUSTOM_TRADES.clear();

        DynamicOps<JsonElement> ops = ForgeHelper.addConditionOps(RegistryOps.create(JsonOps.INSTANCE, registryAccess));
        for (var e : jsons.entrySet()) {
            var json = e.getValue();
            var id = e.getKey();
            if (id.getPath().contains("/")) {
                parseAndAddTrade(json, id, ops);
            }
        }

        mergeProfessionAndSpecial(true);
        if (count != 0) {
            Moonlight.LOGGER.info("Applied {} data villager trades", count);
        }
    }

    private void parseAndAddTrade(JsonElement json, ResourceLocation id, DynamicOps<JsonElement> ops) {
        var targetId = id.withPath(p -> p.substring(0, p.lastIndexOf('/')));
        var profession = BuiltInRegistries.VILLAGER_PROFESSION.getOptional(targetId);
        if (profession.isPresent()) {
            ModItemListing trade = parseOrThrow(json, id, ops);
            if ((trade instanceof NoOpListing)) {
                // no op
            } else if (trade instanceof RemoveNonDataListingListing) {
                //TODO: add remove trades
            } else {
                CUSTOM_TRADES.computeIfAbsent(profession.get(), t ->
                                new Int2ObjectArrayMap<>()).computeIfAbsent(trade.getLevel(), a -> new ArrayList<>())
                        .add(trade);
            }
            return;
        }
        var entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(targetId);
        if (entityType.isPresent()) {
            ModItemListing trade = parseOrThrow(json, id, ops);
            if (!(trade instanceof NoOpListing)) {
                SPECIAL_CUSTOM_TRADES.computeIfAbsent(entityType.get(), t ->
                                new Int2ObjectArrayMap<>()).computeIfAbsent(trade.getLevel(), a -> new ArrayList<>())
                        .add(trade);
            }

        } else {
            Moonlight.LOGGER.warn("Unknown villager type: {}", targetId);
        }
    }

    private void mergeAll(Int2ObjectMap<VillagerTrades.ItemListing[]> originalValues,
                          Int2ObjectArrayMap<List<ModItemListing>> newValues, boolean add) {
        for (var e : newValues.int2ObjectEntrySet()) {
            int level = e.getIntKey();

            VillagerTrades.ItemListing[] elements = originalValues.get(level);
            var original = new ArrayList<>(elements == null ? List.of() : List.of(elements));
            List<ModItemListing> value = e.getValue();
            if (add) {
                original.addAll(value);
                count += value.size();
            } else original.removeAll(value);
            originalValues.put(level, original.toArray(VillagerTrades.ItemListing[]::new));
        }
    }

    private void mergeProfessionAndSpecial(boolean add) {
        for (var p : CUSTOM_TRADES.entrySet()) {
            VillagerProfession profession = p.getKey();
            Int2ObjectMap<VillagerTrades.ItemListing[]> map = VillagerTrades.TRADES.computeIfAbsent(profession, k ->
                    new Int2ObjectArrayMap<>());
            Int2ObjectArrayMap<List<ModItemListing>> value = p.getValue();
            mergeAll(map, value, add);
        }
        Int2ObjectArrayMap<List<ModItemListing>> wanderingStuff = SPECIAL_CUSTOM_TRADES.get(EntityType.WANDERING_TRADER);
        if (wanderingStuff != null) {
            mergeAll(VillagerTrades.WANDERING_TRADER_TRADES, wanderingStuff, add);
        }
    }

    private static ModItemListing parseOrThrow(JsonElement j, ResourceLocation id, DynamicOps<JsonElement> ops) {
        return ModItemListing.CODEC.decode(ops, j)
                .getOrThrow(false, errorMsg -> Moonlight.LOGGER.warn("Failed to parse custom trade with id {} - error: {}",
                        id, errorMsg)).getFirst();
    }

    public static List<? extends VillagerTrades.ItemListing> getVillagerListings(VillagerProfession profession, int level) {
        VillagerTrades.ItemListing[] array = VillagerTrades.TRADES.get(profession).get(level);
        if (array == null) return List.of();
        return Arrays.stream(array).toList();
    }

    public static List<? extends VillagerTrades.ItemListing> getSpecialListings(EntityType<?> entityType, int level) {
        if (entityType == EntityType.WANDERING_TRADER) {
            VillagerTrades.ItemListing[] array = VillagerTrades.WANDERING_TRADER_TRADES.get(level);
            if (array == null) return List.of();
            return Arrays.stream(array).toList();
        } else {
            var special = SPECIAL_CUSTOM_TRADES.get(entityType);
            if (special == null) return List.of();
            return special.getOrDefault(level, List.of());
        }
    }

    /**
     * Call on mod setup. Register a new serializer for your trade
     */
    public static void registerSerializer(ResourceLocation id, Codec<? extends ModItemListing> trade) {
        REGISTRY.register(id, trade);
    }

    /**
     * Registers a simple special trade
     */
    public static void registerSimple(ResourceLocation id, VillagerTrades.ItemListing instance, int level) {
        SpecialListing specialListing = new SpecialListing(instance, level);
        registerSerializer(id, specialListing.getCodec());
    }

    private static class SpecialListing implements ModItemListing {

        private final Codec<ModItemListing> codec = Codec.unit(this);
        private final VillagerTrades.ItemListing listing;
        private final int level;

        public SpecialListing(VillagerTrades.ItemListing listing, int level) {
            this.listing = listing;
            this.level = level;
        }

        @Override
        public Codec<? extends ModItemListing> getCodec() {
            return codec;
        }

        @Nullable
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            return listing.getOffer(trader, random);
        }

        @Override
        public int getLevel() {
            return level;
        }
    }

}
