package net.mehvahdjukaar.moonlight.fabric;

import net.fabricmc.api.ClientModInitializer;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MoonlightFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        //dont remove
        MoonlightFabric.commonSetup();

        PRE_CLIENT_SETUP_WORK.forEach(Runnable::run);
        CLIENT_SETUP_WORK.forEach(Runnable::run);
        PRE_CLIENT_SETUP_WORK.clear();
        CLIENT_SETUP_WORK.clear();
    }

    public static Queue<Runnable> CLIENT_SETUP_WORK = new ConcurrentLinkedQueue<>();
    public static Queue<Runnable> PRE_CLIENT_SETUP_WORK = new ConcurrentLinkedQueue<>();

}
