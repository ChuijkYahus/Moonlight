package net.mehvahdjukaar.moonlight.core.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.misc.DynamicHolder;
import net.mehvahdjukaar.moonlight.api.misc.HolderReference;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.fluid.SoftFluidInternal;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

//after data load
public class ClientBoundFinalizeFluidsMessage implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundFinalizeFluidsMessage> TYPE =
            Message.makeType(Moonlight.res("s2c_finalize_fluids"), ClientBoundFinalizeFluidsMessage::new);

    public ClientBoundFinalizeFluidsMessage() {
    }

    public ClientBoundFinalizeFluidsMessage(RegistryFriendlyByteBuf pBuffer) {
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {

    }

    @Environment(EnvType.CLIENT)
    @Override
    public void handle(Context context) {
        SoftFluidInternal.postInitClient(Minecraft.getInstance().level.registryAccess());
        //just incase
        DynamicHolder.clearCache();
        HolderReference.clearCache();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE.type();
    }
}
