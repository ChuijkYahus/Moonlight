package net.mehvahdjukaar.moonlight.core.set;

import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.leaves.LeavesType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.core.recipe.ShapelessRecipeTemplate;
import net.minecraft.resources.ResourceLocation;
//place for all known weird hardcoded wood types from mods that aren't getting detected
public class CompatTypes {

    public static void init() {

            // Aether Redux
        var cloudcap = WoodType.Finder.simple("aether_redux", "cloudcap", "cloudcap_planks", "cloudcap_stem");
        cloudcap.addChild("stripped_log", "stripped_cloudcap_stem");
        BlockSetAPI.addBlockTypeFinder(WoodType.class, cloudcap);

            // Domum Ornamentum
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(new ResourceLocation("domum_ornamentum:cactus"),
                new ResourceLocation("domum_ornamentum:green_cactus_extra"), new ResourceLocation("cactus")));
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(new ResourceLocation("domum_ornamentum:cactus_extra"),
                    new ResourceLocation("domum_ornamentum:cactus_extra"), new ResourceLocation("cactus")));
            // Ars Nouveau
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "ars_nouveau", "archwood", "archwood_planks", "blue_archwood_log"));

            // Blue Skies
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "blue_skies", "crystallized", "crystallized_planks", "crystallized_log"));

            // Darker Depths
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "darkerdepths", "petrified", "petrified_planks", "petrified_log"));

            // Pokecube Legends
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "pokecube_legends", "concrete", "concrete_planks", "concrete_log"));

            // Terraqueous
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "terraqueous", "storm_cloud", "storm_cloud", "storm_cloud_column"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "terraqueous", "light_cloud", "light_cloud", "light_cloud_column"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "terraqueous", "dense_cloud", "dense_cloud", "dense_cloud_column"));

            // Oh The Biomes You'll go
        var embur = WoodType.Finder.simple(
                "byg", "embur", "embur_planks", "embur_pedu");
        embur.addChild("stripped_log", "stripped_embur_pedu");
        embur.addChild("wood", "embur_pedu_top");
        embur.addChild("stripped_wood", "stripped_embur_pedu_top");
        BlockSetAPI.addBlockTypeFinder(WoodType.class, embur);


        //mcreator mod with typos...
            // Nether's Exoticism
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "nethers_exoticism", "jabuticaba", "jaboticaba_planks", "jabuticaba_log"));


            // Nourished End
        var verdant = WoodType.Finder.simple(
                "nourished_end", "verdant", "verdant_planks", "verdant_stalk");
        verdant.addChild("wood", "verdant_hyphae");
        verdant.addChild("stripped_wood", "stripped_verdant_hyphae");
        verdant.addChild("stripped_log", "stripped_verdant_stem");
        BlockSetAPI.addBlockTypeFinder(WoodType.class, verdant);

        var cerulean = WoodType.Finder.simple(
                "nourished_end", "cerulean", "cerulean_planks", "cerulean_stem_thick");
        cerulean.addChild("stripped_wood", "stripped_cerulean_hyphae");
        cerulean.addChild("stripped_log", "cerulean_stem_stripped");
        BlockSetAPI.addBlockTypeFinder(WoodType.class, cerulean);

            // Gardens Of The Dead
        var soulblight = WoodType.Finder.simple("gardens_of_the_dead",
                "soulblight", "soulblight_planks", "soulblight_stem");
        cerulean.addChild("stripped_wood", "stripped_soulblight_hyphae");
        cerulean.addChild("wood", "soulblight_hyphae");
        cerulean.addChild("stripped_log", "stripped_soulblight_stem");
        BlockSetAPI.addBlockTypeFinder(WoodType.class, soulblight);

        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple("gardens_of_the_dead",
                "whistlecane", "whistlecane_block", "whistlecane"));

            // Desolation
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple("desolation",
                "charred", "charredlog", "charred_planks"));

            // Quark
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple("quark", "bamboo",
                "bamboo_planks", "bamboo_block"));

        var quarkAzalea = WoodType.Finder.simple(
                "quark", "azalea", "azalea_planks", "azalea_log");
        quarkAzalea.addChild("leaves", new ResourceLocation("minecraft:azalea_leaves"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class, quarkAzalea);

            // Twigs
        var bamboo = WoodType.Finder.simple("twigs", "bamboo",
                "stripped_bamboo_planks", "bundled_bamboo");
        bamboo.addChild("stripped_log", "stripped_bundled_bamboo");
        BlockSetAPI.addBlockTypeFinder(WoodType.class, bamboo);

            // Habitat
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "habitat", "fairy_ring_mushroom", "fairy_ring_mushroom_planks", "enhanced_fairy_ring_mushroom_stem"));

            // Ecologics
        var floweringAzalea = WoodType.Finder.simple(
                "ecologics", "flowering_azalea", "flowering_azalea_planks", "flowering_azalea_log");
        floweringAzalea.addChild("stripped_log", "stripped_azalea_log");
        floweringAzalea.addChild("leaves", new ResourceLocation("minecraft:flowering_azalea_leaves"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class, floweringAzalea);

        var azalea = WoodType.Finder.simple(
                "ecologics", "azalea", "azalea_planks", "azalea_log");
        azalea.addChild("leaves", new ResourceLocation("minecraft:azalea_leaves"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class, azalea);


//leaves

            // Ars Nouveau
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "ars_nouveau", "blue_archwood", "blue_archwood_leaves", "ars_nouveau:archwood"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "ars_nouveau", "green_archwood", "green_archwood_leaves", "ars_nouveau:archwood"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "ars_nouveau", "purple_archwood", "purple_archwood_leaves", "ars_nouveau:archwood"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "ars_nouveau", "red_archwood", "red_archwood_leaves", "ars_nouveau:archwood"));

            // Biomes O' Plenty
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "biomesoplenty", "origin", "origin_leaves", "oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "biomesoplenty", "flowering_oak", "flowering_oak_leaves", "oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "biomesoplenty", "maple", "maple_leaves", "oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "biomesoplenty", "orange_autumn", "orange_autumn_leaves", "oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "biomesoplenty", "yellow_autumn", "yellow_autumn_leaves", "oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "biomesoplenty", "rainbow_birch", "rainbow_birch_leaves", "birch"));

            // Blue Skies
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "blue_skies", "crystallized", "crystallized_leaves", "blue_skies:crystallized"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "blue_skies", "crescent_fruit", "crescent_fruit_leaves", "blue_skies:dusk"));

            // Colorful Azaleas
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "blue_azalea", "blue_azalea_leaves", "colorfulazaleas:azule_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "blue_blooming_azalea", "blue_blooming_azalea_leaves", "colorfulazaleas:azule_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "blue_flowering_azalea", "blue_flowering_azalea_leaves", "colorfulazaleas:azule_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "orange_azalea", "orange_azalea_leaves", "colorfulazaleas:tecal_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "orange_blooming_azalea", "orange_blooming_azalea_leaves", "colorfulazaleas:tecal_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "orange_flowering_azalea", "orange_flowering_azalea_leaves", "colorfulazaleas:tecal_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "pink_azalea", "pink_azalea_leaves", "colorfulazaleas:bright_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "pink_blooming_azalea", "pink_blooming_azalea_leaves", "colorfulazaleas:bright_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "pink_flowering_azalea", "pink_flowering_azalea_leaves", "colorfulazaleas:bright_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "purple_azalea", "purple_azalea_leaves", "colorfulazaleas:walnut_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "purple_blooming_azalea", "purple_blooming_azalea_leaves", "colorfulazaleas:walnut_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "purple_flowering_azalea", "purple_flowering_azalea_leaves", "colorfulazaleas:walnut_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "red_azalea", "red_azalea_leaves", "colorfulazaleas:roze_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "red_blooming_azalea", "red_blooming_azalea_leaves", "colorfulazaleas:roze_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "red_flowering_azalea", "red_flowering_azalea_leaves", "colorfulazaleas:roze_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "white_azalea", "white_azalea_leaves", "colorfulazaleas:titanium_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "white_blooming_azalea", "white_blooming_azalea_leaves", "colorfulazaleas:titanium_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "white_flowering_azalea", "white_flowering_azalea_leaves", "colorfulazaleas:titanium_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "yellow_azalea", "yellow_azalea_leaves", "colorfulazaleas:fiss_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "yellow_blooming_azalea", "yellow_blooming_azalea_leaves", "colorfulazaleas:fiss_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "yellow_flowering_azalea", "yellow_flowering_azalea_leaves", "colorfulazaleas:fiss_azalea"));

            // Pokecube Legends
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "pokecube_legends", "dyna_pastel_pink", "dyna_leaves_pastel_pink", "pokecube_legends:aged"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "pokecube_legends", "dyna_pink", "dyna_leaves_pink", "pokecube_legends:aged"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "pokecube_legends", "dyna_red", "dyna_leaves_red", "pokecube_legends:aged"));

            // Regions Unexplored
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "regions_unexplored", "bamboo", "bamboo_leaves", "jungle"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "regions_unexplored", "golden_larch", "golden_larch_leaves", "regions_unexplored:larch"));

            // Terraqueous
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "terraqueous", "apple", "apple_leaves", "terraqueous:apple"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "terraqueous", "banana", "banana_leaves", "terraqueous:banana"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "terraqueous", "cherry", "cherry_leaves", "terraqueous:cherry"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "terraqueous", "coconut", "coconut_leaves", "terraqueous:coconut"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "terraqueous", "lemon", "lemon_leaves", "terraqueous:lemon"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "terraqueous", "mango", "mango_leaves", "terraqueous:mango"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "terraqueous", "mulberry", "mulberry_leaves", "terraqueous:mulberry"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "terraqueous", "orange", "orange_leaves", "terraqueous:orange"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "terraqueous", "peach", "peach_leaves", "terraqueous:peach"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "terraqueous", "pear", "pear_leaves", "terraqueous:pear"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "terraqueous", "plum", "plum_leaves", "terraqueous:plum"));

            // The Twilight Forest
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "twilightforest", "beanstalk", "beanstalk_leaves", "twilightforest:twilight_oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "twilightforest", "thorn", "thorn_leaves", "twilightforest:twilight_oak"));

            // Ulter Lands
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "ulterlands", "souldrained", "souldrained_leaves", "oak"));
    }
}
