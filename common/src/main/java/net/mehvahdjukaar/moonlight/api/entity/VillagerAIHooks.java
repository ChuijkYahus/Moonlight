package net.mehvahdjukaar.moonlight.api.entity;

import com.google.common.collect.ImmutableList;
import net.mehvahdjukaar.moonlight.api.events.IVillagerBrainEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;

import java.util.function.Consumer;

//TODO: rename
public class VillagerAIHooks {

    /**
     * Register an event listener for the villager brain event.
     * On forge Use the subscribe event annotation instead
     */
    public static void addBrainModification(Consumer<IVillagerBrainEvent> eventConsumer){
        MoonlightEventsHelper.addListener(eventConsumer, IVillagerBrainEvent.class);
    }

    /**
     * Adds a memory module to the villager brain when it's created.
     * Add here and not in the event if that memory needs to be saved,
     * otherwise it will not be loaded since the event is called after the brain is deserialized from tag
     */
    public static void registerMemory(MemoryModuleType<?> memoryModuleType) {

        try {
            ImmutableList.Builder<MemoryModuleType<?>> builder = ImmutableList.builder();
            builder.addAll( Villager.MEMORY_TYPES);
            builder.add(memoryModuleType);
            Villager.MEMORY_TYPES = (builder.build());

        } catch (Exception e) {
            Moonlight.LOGGER.warn("failed to register memory module type for villagers: {}", String.valueOf(e));
        }
    }

}
