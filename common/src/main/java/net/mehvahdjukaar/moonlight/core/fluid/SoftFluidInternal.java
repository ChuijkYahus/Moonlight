package net.mehvahdjukaar.moonlight.core.fluid;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidColors;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundFinalizeFluidsMessage;
import net.mehvahdjukaar.moonlight.core.network.ModMessages;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.WeakHashMap;

import static net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry.getHolders;

@ApiStatus.Internal
public class SoftFluidInternal {

    private static final WeakHashMap<RegistryAccess, Map<Fluid, Holder<SoftFluid>>> FLUID_MAP = new WeakHashMap<>();
    private static final WeakHashMap<RegistryAccess, Map<Item, Holder<SoftFluid>>> ITEM_MAP = new WeakHashMap<>();


    public static Holder<SoftFluid> fromVanillaFluid(Fluid fluid, RegistryAccess registryAccess) {
        if (!FLUID_MAP.containsKey(registryAccess)) {
            populateSlaveMaps(registryAccess);
        }
        return FLUID_MAP.get(registryAccess).get(fluid);
    }

    public static Holder<SoftFluid> fromVanillaItem(Item item, RegistryAccess registryAccess) {
        if (!ITEM_MAP.containsKey(registryAccess)) {
            populateSlaveMaps(registryAccess);
        }
        return ITEM_MAP.get(registryAccess).get(item);
    }

    //needs to be called on both sides
    private static void populateSlaveMaps(RegistryAccess registryAccess) {
        var fluidMap = FLUID_MAP.computeIfAbsent(registryAccess, k -> new IdentityHashMap<>());
        var itemMap = ITEM_MAP.computeIfAbsent(registryAccess, k -> new IdentityHashMap<>());
        fluidMap.clear();
        itemMap.clear();
        for (var h : getHolders()) {
            var s = h.value();
            if (s.isEnabled()) {
                s.getEquivalentFluids().forEach(f -> fluidMap.put(f, h));
                s.getContainerList().getPossibleFilled().forEach(i -> {
                    //don't associate water to potion bottle
                    if (i != Items.POTION || !BuiltInSoftFluids.WATER.is(h)) {
                        itemMap.put(i, h);
                    }
                });
            }
        }
    }


    @ExpectPlatform
    public static void init() {
    }

    //wtf is going on here

    //called by data sync to player
    public static void postInitClient() {
        FLUID_MAP.clear();
        ITEM_MAP.clear();
        //ok so here the extra registered fluids should have already been sent to the client
        SoftFluidColors.refreshParticleColors();
    }

    public static void onDataSyncToPlayer(ServerPlayer player, boolean isJoined) {
        //just sends on login
        if (isJoined) {
            ModMessages.CHANNEL.sendToClientPlayer(player, new ClientBoundFinalizeFluidsMessage());
        }
    }

    //on data load
    public static void doPostInitServer(RegistryAccess reg) {
        populateSlaveMaps(reg);
        //registers existing fluids. also update the salve maps
        //we need to call this on bont server and client as this happens too late and these wont be sent
        registerExistingVanillaFluids(FLUID_MAP.get(reg), ITEM_MAP.get(reg));
    }

    @ExpectPlatform
    private static void registerExistingVanillaFluids(Map<Fluid, Holder<SoftFluid>> fluidMap, Map<Item, Holder<SoftFluid>> itemMap) {
        throw new AssertionError();
    }


}

