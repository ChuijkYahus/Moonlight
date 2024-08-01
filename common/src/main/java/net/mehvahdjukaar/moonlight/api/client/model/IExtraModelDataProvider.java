package net.mehvahdjukaar.moonlight.api.client.model;

import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

/**
 * Implement in your tile entity
 */
public interface IExtraModelDataProvider {

    @ApiStatus.Internal
    default ExtraModelData getExtraModelData() {
        var builder = ExtraModelData.builder();
        addExtraModelData(builder);
        return builder.build();
    }

    default void addExtraModelData(ExtraModelData.Builder builder) {
    }

    default void requestModelReload() {
    }

    default void afterDataPacket(ExtraModelData oldData) {
        if (!Objects.equals(oldData, this.getExtraModelData())) {
            //this request render data refresh
            this.requestModelReload();
        }
    }
}
