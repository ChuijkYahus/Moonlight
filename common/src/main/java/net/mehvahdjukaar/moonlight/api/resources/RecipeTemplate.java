package net.mehvahdjukaar.moonlight.api.resources;

import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

public class RecipeTemplate {

    private static final Map<Class<? extends Recipe<?>>, BiFunction<Recipe<?>, UnaryOperator<ItemStack>, Recipe<?>>> REMAPPERS = new HashMap<>();

    public static <R extends Recipe<?>> void registerSimple(Class<R> type, RecipeFactory<R> factory) {
        register(type, (r, t) -> createSimple(r, factory, t));
    }

    public static <R extends Recipe<?>> void register(Class<R> type, BiFunction<R, UnaryOperator<ItemStack>, R> factory) {
        REMAPPERS.put(type, (r, t) -> factory.apply((R) r, t));
    }

    public interface RecipeFactory<R extends Recipe<?>> {
        R create(String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients);
    }


    private static <R extends Recipe<?>> @NotNull List<Ingredient> convertIngredients(NonNullList<Ingredient> or, UnaryOperator<ItemStack> typeChanger) {
        List<Ingredient> newList = new ArrayList<>(or);
        for (var ingredient : or) {
            if (ingredient.getItems().length > 0) {
                ItemStack i = typeChanger.apply(ingredient.getItems()[0]);
                if (i != null) newList.add(Ingredient.of(i));
            }
        }
        return newList;
    }


    public static <T extends BlockType, R extends Recipe<?>> RecipeHolder<?> makeSimilarRecipe(R original, T originalMat,
                                                                                               T destinationMat, String baseID) {
        var clazz = original.getClass();
        var remapper = REMAPPERS.get(clazz);
        if (remapper == null) {
            throw new UnsupportedOperationException("Recipe class " + clazz + " not supported. You must register it using RecipeTemplate.register()");
        }
        ResourceLocation newId = ResourceLocation.parse(baseID + "/" + destinationMat.getAppendableId());

        var remapped = remapper.apply(original, (stack) -> convertItemStack(stack, originalMat, destinationMat));

        return new RecipeHolder<>(newId, remapped);
    }

    @Nullable
    public static <T extends BlockType> ItemStack convertItemStack(ItemStack original, T originalMat, T destinationMat) {
        var changed = BlockType.changeItemType(original.getItem(), originalMat, destinationMat);
        if (changed == null) return null;
        return original.transmuteCopy(changed);

    }

    static {
        register(ShapedRecipe.class, RecipeTemplate::createShaped);
        registerSimple(ShapelessRecipe.class, ShapelessRecipe::new);
    }


    private static <R extends Recipe<?>> R createSimple(R or, RecipeFactory<R> factory, UnaryOperator<ItemStack> typeChanger) {
        List<Ingredient> newList = convertIngredients(or.getIngredients(), typeChanger);
        ItemStack originalResult = or.getResultItem(RegistryAccess.EMPTY);
        ItemStack newResult = typeChanger.apply(originalResult);
        if (newResult == null) throw new UnsupportedOperationException("Failed to convert recipe result");
        NonNullList<Ingredient> ingredients = NonNullList.of(Ingredient.EMPTY, newList.toArray(Ingredient[]::new));

        CraftingBookCategory cat = CraftingBookCategory.MISC;
        if (or instanceof CraftingRecipe cr) {
            cat = cr.category();
        }
        return factory.create(or.getGroup(), cat, newResult, ingredients);
    }

    private static ShapedRecipe createShaped(ShapedRecipe or, UnaryOperator<ItemStack> typeChanger) {
        List<Ingredient> newList = convertIngredients(or.getIngredients(), typeChanger);
        ItemStack originalResult = or.getResultItem(RegistryAccess.EMPTY);
        ItemStack newResult = typeChanger.apply(originalResult);
        if (newResult == null) throw new UnsupportedOperationException("Failed to convert recipe result");
        NonNullList<Ingredient> ingredients = NonNullList.of(Ingredient.EMPTY, newList.toArray(Ingredient[]::new));

        ShapedRecipePattern pattern = new ShapedRecipePattern(or.getWidth(), or.getHeight(), ingredients,
                Optional.of(packRecipePattern(or.getWidth(), or.getHeight(), ingredients)));

        return new ShapedRecipe(or.getGroup(), or.category(), pattern, newResult);
    }

    private static ShapedRecipePattern.Data packRecipePattern(int width, int height, NonNullList<Ingredient> ingredients) {
        // Create a new map to hold the unique character keys and corresponding ingredients.
        Map<Character, Ingredient> key = new HashMap<>();
        List<String> pattern = new ArrayList<>();

        int counter = 0;  // To generate unique character symbols for each ingredient.
        char nextSymbol = 'A';  // Start with 'A' as the symbol for mapping ingredients.

        // Iterate over each row in the grid based on the width and height of the pattern.
        for (int row = 0; row < height; row++) {
            StringBuilder rowPattern = new StringBuilder();
            for (int col = 0; col < width; col++) {
                Ingredient ingredient = ingredients.get(row * width + col);

                // Check if ingredient is empty, then use space.
                if (ingredient.isEmpty()) {
                    rowPattern.append(' ');
                } else {
                    // Check if ingredient already has an assigned symbol in the map.
                    Character symbol = null;
                    for (Map.Entry<Character, Ingredient> entry : key.entrySet()) {
                        if (entry.getValue().equals(ingredient)) {
                            symbol = entry.getKey();
                            break;
                        }
                    }

                    // If no symbol is found, assign a new one.
                    if (symbol == null) {
                        symbol = nextSymbol++;
                        key.put(symbol, ingredient);
                    }

                    rowPattern.append(symbol);
                }
            }
            // Add the constructed row to the pattern.
            pattern.add(rowPattern.toString());
        }

        // Return a new Data object with the constructed key and pattern.
        return  new ShapedRecipePattern.Data(key, pattern);
    }


}
