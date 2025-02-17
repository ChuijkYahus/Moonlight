package net.mehvahdjukaar.moonlight.api.client.model;

import dev.architectury.injectables.annotations.ExpectPlatform;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Just wraps forge and fabric ones
 */
public interface ExtraModelData {

    ExtraModelData EMPTY = ClassLoadingBs.INSTANCE;

    @ExpectPlatform
    static Builder builder() {
        throw new AssertionError();
    }

    @Nullable <T> T get(ModelDataKey<T> key);

    Map<ModelDataKey<?>, Object> values();

    interface Builder {
        <A> Builder with(ModelDataKey<A> key, A data);

        ExtraModelData build();
    }

    default boolean isEmpty() {
        return this == EMPTY;
    }

    class ClassLoadingBs {
        static final ExtraModelData INSTANCE = ExtraModelData.builder().build();
    }
}


