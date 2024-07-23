package net.mehvahdjukaar.moonlight.api.fluids;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.misc.StrOpt;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import java.util.Map;
import java.util.function.Consumer;

public class FoodProvider {

    public static final Codec<FoodProvider> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(f -> f.food),
            StrOpt.of(SoftFluid.Capacity.INT_CODEC, "divider", 1).forGetter(f -> f.divider)
    ).apply(instance, FoodProvider::create));

    public static final FoodProvider EMPTY = new FoodProvider(Items.AIR, 1);

    protected final Item food;
    protected final int divider;

    private FoodProvider(Item food, int divider) {
        this.food = food;
        this.divider = divider;
    }

    public Item getFood() {
        return food;
    }

    public int getDivider() {
        return divider;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    /**
     * Consumes some fluid and gives the player the appropriate effect
     *
     * @return if one unit has been consumed
     */
    public boolean consume(Player player, Level world, @Nullable Consumer<ItemStack> nbtApplier) {

        ItemStack stack = this.food.getDefaultInstance();
        if (nbtApplier != null) nbtApplier.accept(stack);

        //food

        FoodProperties foodProperties = PlatHelper.getFoodProperties(this.food, stack, player);
        //single items are handled by items themselves
        if (this.divider == 1) {
            this.food.finishUsingItem(stack.copy(), world, player);
            if (foodProperties == null || stack.getItem().isEdible()) {
                player.playSound(this.food.getDrinkingSound(), 1, 1);
            }
            //player already plays sound
            return true;
        }
        if (foodProperties != null && player.canEat(false)) {

            player.getFoodData().eat(foodProperties.getNutrition() / this.divider, foodProperties.getSaturationModifier() / (float) this.divider);
            player.playSound(this.food.getDrinkingSound(), 1, 1);
            return true;
        }
        return false;
    }

    public static FoodProvider create(Item item, int divider) {
        return CUSTOM_PROVIDERS.getOrDefault(item, new FoodProvider(item, divider));
    }

    private static final FoodProvider XP = new FoodProvider(Items.EXPERIENCE_BOTTLE, 1) {

        @Override
        public boolean consume(Player player, Level world, @Nullable Consumer<ItemStack> nbtApplier) {
            player.giveExperiencePoints(Utils.getXPinaBottle(1, world.random));
            player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1, 1);
            return true;
        }
    };

    private static final FoodProvider MILK = new FoodProvider(Items.MILK_BUCKET, 3) {

        @Override
        public boolean consume(Player player, Level world, @Nullable Consumer<ItemStack> nbtApplier) {
            ItemStack stack = this.food.getDefaultInstance();
            if (nbtApplier != null) nbtApplier.accept(stack);
            for (MobEffectInstance effect : player.getActiveEffectsMap().values()) {
                if (ForgeHelper.isCurativeItem(stack, effect)) {
                    player.removeEffect(effect.getEffect());
                    break;
                }
            }
            player.playSound(this.food.getDrinkingSound(), 1, 1);
            return true;
        }
    };

    private static final FoodProvider SUS_STEW = new FoodProvider(Items.SUSPICIOUS_STEW, 2) {

        @Override
        public boolean consume(Player player, Level world, @Nullable Consumer<ItemStack> nbtApplier) {

            ItemStack stack = this.food.getDefaultInstance();
            if (nbtApplier != null) nbtApplier.accept(stack);
            FoodProperties foodProperties = this.food.getFoodProperties();
            if (foodProperties != null && player.canEat(false)) {
                List<SuspiciousEffectHolder.EffectEntry> list = new ArrayList<>();
                CompoundTag compoundTag = stack.getTag();
                if (compoundTag != null && compoundTag.contains("effects", 9)) {
                    SuspiciousEffectHolder.EffectEntry.LIST_CODEC.parse(NbtOps.INSTANCE, compoundTag.getList("effects", 10)).result()
                            .ifPresent(list::addAll);
                }
                for (var effect : list) {
                    int j = effect.duration() / this.divider;
                    player.addEffect(new MobEffectInstance(effect.effect(), j));
                }
                player.getFoodData().eat(foodProperties.getNutrition() / this.divider, foodProperties.getSaturationModifier() / (float) this.divider);
                player.playSound(this.food.getDrinkingSound(), 1, 1);
                return true;
            }
            return false;
        }
    };

    public static final Map<Item, FoodProvider> CUSTOM_PROVIDERS = Map.of(
            Items.AIR, EMPTY,
            Items.SUSPICIOUS_STEW, SUS_STEW,
            Items.MILK_BUCKET, MILK,
            Items.EXPERIENCE_BOTTLE, XP
    );
}
