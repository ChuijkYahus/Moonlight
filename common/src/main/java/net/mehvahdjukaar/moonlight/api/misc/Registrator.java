package net.mehvahdjukaar.moonlight.api.misc;

import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface Registrator<T> {

    void register(ResourceLocation name, T instance);

    default void register(String name, T instance) {
        register(ResourceLocation.parse(name), instance);
    }

}
