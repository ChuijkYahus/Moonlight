package net.mehvahdjukaar.moonlight.api.events.forge;

import net.mehvahdjukaar.moonlight.api.events.IDropItemOnDeathEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class DropItemOnDeathEvent extends Event implements IDropItemOnDeathEvent {
    private final ItemStack itemStack;
    private final Player player;
    private final boolean beforeDrop;
    private ItemStack returnStack;

    public DropItemOnDeathEvent(ItemStack itemStack, Player player, boolean beforeDrop) {
        this.itemStack = itemStack;
        this.player = player;
        this.returnStack = itemStack;
        this.beforeDrop = beforeDrop;
    }

    public boolean isBeforeDrop() {
        return beforeDrop;
    }

    public static IDropItemOnDeathEvent create(ItemStack itemStack, Player player, boolean beforeDrop) {
        return new DropItemOnDeathEvent(itemStack, player, beforeDrop);
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public ItemStack getItemStack() {
        return this.itemStack;
    }

    @Override
    public void setReturnItemStack(ItemStack stack) {
        this.returnStack = stack;
    }

    @Override
    public ItemStack getReturnItemStack() {
        return returnStack;
    }

}
