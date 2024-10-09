package net.mehvahdjukaar.moonlight.api.platform.network.neoforge;

import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.neoforge.MoonlightForge;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

import java.util.function.Consumer;

public class NetworkHelperImpl {

    public static void addNetworkRegistration(Consumer<NetworkHelper.RegisterMessagesEvent> eventListener, int version) {
        Consumer<RegisterPayloadHandlersEvent> eventConsumer = event -> {
            String versionStr = "" + version;
            NetworkHelper.RegisterMessagesEvent registerMessagesEvent = new NetworkHelper.RegisterMessagesEvent() {

                @Override
                public <M extends Message> void registerServerBound(CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, M> messageType) {
                    event.registrar(messageType.type().id().getPath())
                            .versioned(versionStr)
                            .executesOn(HandlerThread.MAIN)
                            .playToServer(messageType.type(), messageType.codec(),
                                    (m, c) -> m.handle(new ContextWrapper(c)));
                }

                @Override
                public <M extends Message> void registerClientBound(CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, M> messageType) {
                    event.registrar(messageType.type().id().getPath())
                            .versioned(versionStr)
                            .executesOn(HandlerThread.MAIN)
                            .playToClient(messageType.type(), messageType.codec(),
                                    (m, c) -> m.handle(new ContextWrapper(c)));
                }

                @Override
                public <M extends Message> void registerBidirectional(CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, M> messageType) {
                    event.registrar(messageType.type().id().getPath())
                            .versioned(versionStr)
                            .executesOn(HandlerThread.MAIN)
                            .playBidirectional(messageType.type(), messageType.codec(),
                                    (m, c) -> m.handle(new ContextWrapper(c)));
                }
            };
            eventListener.accept(registerMessagesEvent);
        };

        MoonlightForge.getCurrentBus().addListener(eventConsumer);
    }

    private record ContextWrapper(IPayloadContext c) implements Message.Context {

        @Override
        public Message.NetworkDir getDirection() {
            var flow = c.connection().getDirection();
            if (flow == PacketFlow.SERVERBOUND) return Message.NetworkDir.SERVER_BOUND;
            else return Message.NetworkDir.CLIENT_BOUND;
        }

        @Override
        public Player getPlayer() {
            return c.player();
        }

        @Override
        public void disconnect(Component reason) {
            c.disconnect(reason);
        }

        public void reply(CustomPacketPayload payload) {
            c.reply(payload);
        }

    }


    public static void sendToClientPlayer(ServerPlayer serverPlayer, CustomPacketPayload message) {
        PacketDistributor.sendToPlayer(serverPlayer, message);
    }

    public static void sendToAllClientPlayers(CustomPacketPayload message) {
        PacketDistributor.sendToAllPlayers(message);
    }

    public static void sendToAllClientPlayersInRange(ServerLevel level, BlockPos pos, double radius, CustomPacketPayload message) {
        PacketDistributor.sendToPlayersNear(level, null,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, radius, message);
    }

    public static void sendToAllClientPlayersTrackingEntity(Entity target, CustomPacketPayload message) {
        PacketDistributor.sendToPlayersTrackingEntity(target, message);
    }

    public static void sendToAllClientPlayersTrackingEntityAndSelf(Entity target, Message message) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(target, message);
    }

    public static void sendToServer(CustomPacketPayload message) {
        PacketDistributor.sendToServer(message);
    }


}
