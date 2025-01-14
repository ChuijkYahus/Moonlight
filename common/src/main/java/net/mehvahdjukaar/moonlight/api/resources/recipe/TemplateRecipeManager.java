package net.mehvahdjukaar.moonlight.api.resources.recipe;

import com.google.gson.JsonObject;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.recipe.ShapedRecipeTemplate;
import net.mehvahdjukaar.moonlight.core.recipe.ShapelessRecipeTemplate;
import net.mehvahdjukaar.moonlight.core.recipe.SmeltingRecipeTemplate;
import net.mehvahdjukaar.moonlight.core.recipe.StoneCutterRecipeTemplate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class TemplateRecipeManager {

    private static final Map<ResourceLocation, Function<JsonObject, ? extends IRecipeTemplate<?>>> DESERIALIZERS = new HashMap<>();

    /**
     * Registers a recipe template deserializer. Will be used to parse existing recipes and be able to merge new ones
     *
     * @param deserializer usually IRecipeTemplate::new
     * @param serializer   recipe serializer type
     */
    public static <T extends IRecipeTemplate<?>> void registerTemplate(
            RecipeSerializer<?> serializer, Function<JsonObject, T> deserializer) {
        registerTemplate(Utils.getID(serializer), deserializer);
    }

    public static <T extends IRecipeTemplate<?>> void registerTemplate(
            ResourceLocation serializerId, Function<JsonObject, T> deserializer) {
        DESERIALIZERS.put(serializerId, deserializer);
    }

    public static IRecipeTemplate<?> read(JsonObject recipe) throws UnsupportedOperationException {
        String type = GsonHelper.getAsString(recipe, "type");

        var templateFactory = DESERIALIZERS.get(new ResourceLocation(type));

        if (templateFactory != null) {
            var template = templateFactory.apply(recipe);
            //special case for shaped with a single item...
            if (template instanceof ShapedRecipeTemplate st && st.shouldBeShapeless()) {
                template = st.toShapeless();
            }
            addRecipeConditions(recipe, template);
            return template;
        } else {
            throw new UnsupportedOperationException(String.format("Invalid recipe serializer: %s. Supported deserializers: %s",
                    type, DESERIALIZERS.keySet()));
        }
    }

    @ExpectPlatform
    private static void addRecipeConditions(JsonObject recipe, IRecipeTemplate<?> template) {
        throw new AssertionError();
    }

    static {
        registerTemplate(RecipeSerializer.SHAPED_RECIPE, ShapedRecipeTemplate::new);
        registerTemplate(RecipeSerializer.SHAPELESS_RECIPE, ShapelessRecipeTemplate::new);
        registerTemplate(RecipeSerializer.STONECUTTER, StoneCutterRecipeTemplate::new);
        registerTemplate(RecipeSerializer.SMELTING_RECIPE, j -> new SmeltingRecipeTemplate(j, (SimpleCookingSerializer<?>) RecipeSerializer.SMELTING_RECIPE));
        registerTemplate(RecipeSerializer.BLASTING_RECIPE, j -> new SmeltingRecipeTemplate(j, (SimpleCookingSerializer<?>) RecipeSerializer.BLASTING_RECIPE));
        registerTemplate(RecipeSerializer.SMOKING_RECIPE, j -> new SmeltingRecipeTemplate(j, (SimpleCookingSerializer<?>) RecipeSerializer.SMOKING_RECIPE));
        registerTemplate(RecipeSerializer.CAMPFIRE_COOKING_RECIPE, j -> new SmeltingRecipeTemplate(j, (SimpleCookingSerializer<?>) RecipeSerializer.CAMPFIRE_COOKING_RECIPE));
        registerTemplate(new ResourceLocation("forge:conditional"), TemplateRecipeManager::forgeConditional);
    }

    private static IRecipeTemplate<?> forgeConditional(JsonObject recipe) {
        JsonObject object = GsonHelper.getAsJsonArray(recipe, "recipes").get(0).getAsJsonObject();
        var template = read(object.getAsJsonObject("recipe"));
        addRecipeConditions(object, template);
        return template;
    }

}
