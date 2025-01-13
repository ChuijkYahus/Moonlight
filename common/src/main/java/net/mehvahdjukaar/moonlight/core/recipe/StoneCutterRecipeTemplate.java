package net.mehvahdjukaar.moonlight.core.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.api.resources.recipe.IRecipeTemplate;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class StoneCutterRecipeTemplate implements IRecipeTemplate<SingleItemRecipeBuilder.Result> {

    private final List<Object> conditions = new ArrayList<>();

    public final Item result;
    public final int count;
    public final String group;
    public final Ingredient ingredient;
    public final CraftingBookCategory category;

    public StoneCutterRecipeTemplate(JsonObject json) {
        JsonElement result = json.get("result");
        ResourceLocation item = new ResourceLocation(result.getAsString());
        int count = 1;
        var c = json.get("count");
        if (c != null) count = c.getAsInt();

        this.count = count;
        this.result = BuiltInRegistries.ITEM.get(item);
        this.category = CraftingBookCategory.CODEC.byName(GsonHelper.getAsString(json, "category", null), CraftingBookCategory.MISC);

        var g = json.get("group");
        this.group = g == null ? "" : g.getAsString();

        this.ingredient = Ingredient.fromJson(json.get("ingredient"));
    }

    @Override
    public <T extends BlockType> SingleItemRecipeBuilder.Result createSimilar(
            T originalMat, T destinationMat, Item unlockItem, @Nullable String id) {
        ItemLike newRes = BlockType.changeItemType(this.result, originalMat, destinationMat);
        if (newRes == null) {
            throw new UnsupportedOperationException(String.format("Could not convert output item %s from type %s to %s",
                    this.result, originalMat, destinationMat));
        }

        Ingredient ing = IRecipeTemplate.convertIngredients(originalMat, destinationMat, ingredient);

        //if recipe fails
        if (ing == null) return null;

        SingleItemRecipeBuilder builder = SingleItemRecipeBuilder.stonecutting(
                ing, determineBookCategory(this.category), newRes);
        builder.group(group);

        builder.unlockedBy("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(unlockItem));

        AtomicReference<SingleItemRecipeBuilder.Result> newRecipe = new AtomicReference<>();

        if (id == null) {
            builder.save(r -> newRecipe.set((SingleItemRecipeBuilder.Result) r));
        } else {
            builder.save(r -> newRecipe.set((SingleItemRecipeBuilder.Result) r), id);
        }

        return newRecipe.get();
    }


    @Override
    public List<Object> getConditions() {
        return conditions;
    }

    @Override
    public void addCondition(Object condition) {
        this.conditions.add(condition);
    }
}