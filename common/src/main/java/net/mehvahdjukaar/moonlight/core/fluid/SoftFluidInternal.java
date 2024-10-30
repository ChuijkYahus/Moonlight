package net.mehvahdjukaar.moonlight.core.fluid;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidColors;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundFinalizeFluidsMessage;
import net.mehvahdjukaar.moonlight.core.network.ModNetworking;
import net.minecraft.core.Holder;
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

    //TODO: improve, this isnt very robust. Same with DynamicHolder stuff
    //needs thread local as each level has its own holderand registry
    public static final WeakHashMap <RegistryAccess, Map<Fluid, Holder<SoftFluid>>> FLUID_MAP = new WeakHashMap<>();
    public static final WeakHashMap<RegistryAccess, Map<Item, Holder<SoftFluid>>> ITEM_MAP = new WeakHashMap<>();

    //needs to be called on both sides
    private static void populateSlaveMaps(RegistryAccess registryAccess) {
        var fludiMap = SoftFluidInternal.FLUID_MAP.computeIfAbsent(registryAccess, k -> new IdentityHashMap<>());
        var itemMap = SoftFluidInternal.ITEM_MAP.computeIfAbsent(registryAccess, k -> new IdentityHashMap<>());
        fludiMap.clear();
        itemMap.clear();
        for (var h : getHolders()) {
            var s = h.value();
            if (s.isEnabled()) {
                s.getEquivalentFluids().forEach(f -> fludiMap.put(f.value(), h));
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
        var reg = Utils.hackyGetRegistryAccess();
        populateSlaveMaps(reg);
        //ok so here the extra registered fluids should have already been sent to the client
        SoftFluidColors.refreshParticleColors();
    }

    public static void onDataSyncToPlayer(ServerPlayer player, boolean isJoined) {
        //just sends on login
        if(isJoined) {
            NetworkHelper.sendToClientPlayer(player, new ClientBoundFinalizeFluidsMessage());
        }
    }

    //on data load
    public static void doPostInitServer() {
        var reg = Utils.hackyGetRegistryAccess();
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

