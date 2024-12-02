package net.mehvahdjukaar.moonlight.api.resources.recipe;

import com.mojang.serialization.*;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.set.BlockTypeRegistry;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public abstract class BlockTypeSwapIngredient<T extends BlockType> {


    @ExpectPlatform
    public static <T extends BlockType> Ingredient create(Ingredient original, T from, T to) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends BlockType> BlockTypeSwapIngredient<T> create(Ingredient original, T from, T to, BlockTypeRegistry<T> reg) {
        throw new AssertionError();
    }

    protected final Ingredient inner;
    protected final T fromType;
    protected final T toType;
    protected final BlockTypeRegistry<T> registry;

    private List<ItemStack> items;

    protected BlockTypeSwapIngredient(Ingredient inner, T fromType, T toType, BlockTypeRegistry<T> reg) {
        super();
        this.inner = inner;
        this.fromType = fromType;
        this.toType = toType;
        this.registry = reg;
    }

    public boolean test(ItemStack stack) {
        if (stack != null) {
            for (ItemStack itemStack : this.getMatchingStacks()) {
                if (itemStack.is(stack.getItem())) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<ItemStack> getMatchingStacks() {
        if (this.items == null) {
            var original = this.inner.getItems();
            List<ItemStack> newItems = new ArrayList<>();
            boolean success = false;
            for (ItemStack it : this.items) {
                var type = this.registry.getBlockTypeOf(it.getItem());
                if (type != this.fromType) {
                    break;
                } else {
                    var newItem = BlockType.changeItemType(it.getItem(), this.fromType, this.toType);
                    if (newItem != null) {
                        newItems.add(new ItemStack(newItem));
                        success = true;
                    }
                }
            }
            if (!success) {
                newItems.addAll(Arrays.stream(original).toList());
            }
            this.items = newItems;
        }
        return this.items;
    }


    public static final ResourceLocation ID = Moonlight.res("block_set_swap");

    public static final MapCodec<BlockTypeSwapIngredient<?>> CODEC = makeCodec(false);
    public static final MapCodec<BlockTypeSwapIngredient<?>> CODEC_NONEMPTY = makeCodec(true);
    public static final StreamCodec<RegistryFriendlyByteBuf, BlockTypeSwapIngredient<?>> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public BlockTypeSwapIngredient<?> decode(RegistryFriendlyByteBuf object) {
                    Ingredient inner = Ingredient.CONTENTS_STREAM_CODEC.decode(object);
                    BlockTypeRegistry<?> reg = BlockTypeRegistry.getRegistryStreamCodec().decode(object);
                    BlockType from = reg.getStreamCodec().decode(object);
                    BlockType to = reg.getStreamCodec().decode(object);
                    return create(inner, from, to, (BlockTypeRegistry<? super BlockType>) reg);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, BlockTypeSwapIngredient<?> ing) {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ing.inner);
                    BlockTypeRegistry.getRegistryStreamCodec().encode(buf, ing.registry);
                    StreamCodec streamCodec = ing.registry.getStreamCodec();
                    streamCodec.encode(buf, ing.fromType);
                    streamCodec.encode(buf, ing.toType);
                }
            };


    private static @NotNull MapCodec<BlockTypeSwapIngredient<?>> makeCodec(boolean nonEmpty) {
        return new MapCodec<>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.of("block_type", "from", "to", "ingredient").map(ops::createString);
            }

            @Override
            public <T> DataResult<BlockTypeSwapIngredient<?>> decode(DynamicOps<T> ops, MapLike<T> input) {
                var ingCodec = nonEmpty ? Ingredient.CODEC_NONEMPTY : Ingredient.CODEC;
                Ingredient inner = ingCodec.parse(ops, input.get(ops.createString("ingredient"))).result().orElseThrow();
                BlockTypeRegistry<?> reg = BlockTypeRegistry.getRegistryCodec().parse(ops, input.get(ops.createString("block_type"))).result().orElseThrow();
                BlockType from = reg.getCodec().parse(ops, input.get(ops.createString("from"))).result().orElseThrow();
                BlockType to = reg.getCodec().parse(ops, input.get(ops.createString("to"))).result().orElseThrow();
                return DataResult.success(create(inner, from, to, (BlockTypeRegistry<? super BlockType>) reg));
            }

            @Override
            public <T> RecordBuilder<T> encode(BlockTypeSwapIngredient<?> ingr, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                var ingCodec = nonEmpty ? Ingredient.CODEC_NONEMPTY : Ingredient.CODEC;
                prefix.add(ops.createString("ingredient"), ingCodec.encodeStart(ops, ingr.inner));
                prefix.add(ops.createString("block_type"), BlockTypeRegistry.getRegistryCodec().encodeStart(ops, ingr.registry));
                Codec codec = ingr.registry.getCodec();
                prefix.add(ops.createString("from"), codec.encodeStart(ops, ingr.fromType));
                prefix.add(ops.createString("to"), codec.encodeStart(ops, ingr.toType));
                return prefix;
            }
        };
    }


}
