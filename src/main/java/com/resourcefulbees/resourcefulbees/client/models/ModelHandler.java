package com.resourcefulbees.resourcefulbees.client.models;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.resourcefulbees.resourcefulbees.ResourcefulBees;
import com.resourcefulbees.resourcefulbees.registry.BeeRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;

import java.util.Collection;
import java.util.Map;

public class ModelHandler {

    private static final Multimap<ResourceLocation, ResourceLocation> MODEL_MAP = LinkedHashMultimap.create();

    public static void registerModels(ModelRegistryEvent event) {
        IResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

        BeeRegistry.getRegistry().getBees().forEach((string, customBee) -> {
            Block honeycombBlock = customBee.getCombBlockRegistryObject().get();
            Item honeycombBlockItem = customBee.getCombBlockItemRegistryObject().get();
            Item honeycomb = customBee.getCombRegistryObject().get();
            Item spawnEgg = customBee.getSpawnEggItemRegistryObject().get();

            if(honeycombBlock.getRegistryName() != null && !resourceManager.hasResource(new ResourceLocation(ResourcefulBees.MOD_ID, "blockstates/"+honeycombBlock.getRegistryName().getPath()+".json"))) {
                honeycombBlock.getStateContainer().getValidStates().forEach((state)->{
                    String propertyMapString = BlockModelShapes.getPropertyMapString(state.getValues());
                    ModelResourceLocation defaultModelLocation = new ModelResourceLocation(
                            ResourcefulBees.MOD_ID +":honeycomb_block", propertyMapString);
                    ModelLoader.addSpecialModel(defaultModelLocation);
                    MODEL_MAP.put(defaultModelLocation, new ModelResourceLocation(honeycombBlock.getRegistryName(), propertyMapString));
                });
            }
            if(honeycombBlockItem.getRegistryName() != null && !resourceManager.hasResource(new ResourceLocation(ResourcefulBees.MOD_ID, "item/models/"+honeycombBlockItem.getRegistryName().getPath()+".json"))) {
                ModelResourceLocation defaultModelLocation = new ModelResourceLocation(
                        ResourcefulBees.MOD_ID +":honeycomb_block", "inventory");
                ModelLoader.addSpecialModel(defaultModelLocation);
                MODEL_MAP.put(defaultModelLocation, new ModelResourceLocation(honeycombBlockItem.getRegistryName(), "inventory"));
            }
            if(honeycomb.getRegistryName() != null && !resourceManager.hasResource(new ResourceLocation(ResourcefulBees.MOD_ID, "item/models/"+honeycomb.getRegistryName().getPath()+".json"))) {
                ModelResourceLocation defaultModelLocation = new ModelResourceLocation(
                        ResourcefulBees.MOD_ID +":honeycomb", "inventory");
                ModelLoader.addSpecialModel(defaultModelLocation);
                MODEL_MAP.put(defaultModelLocation, new ModelResourceLocation(honeycomb.getRegistryName(), "inventory"));
            }
            if(spawnEgg.getRegistryName() != null && !resourceManager.hasResource(new ResourceLocation(ResourcefulBees.MOD_ID, "item/models/"+spawnEgg.getRegistryName().getPath()+".json"))) {
                ModelResourceLocation defaultModelLocation = new ModelResourceLocation(
                         "minecraft:template_spawn_egg", "inventory");
                ModelLoader.addSpecialModel(defaultModelLocation);
                MODEL_MAP.put(defaultModelLocation, new ModelResourceLocation(spawnEgg.getRegistryName(), "inventory"));
            }
        });
    }

    public static void onModelBake(ModelBakeEvent event) {
        Map<ResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
        IBakedModel missingModel = modelRegistry.get(ModelLoader.MODEL_MISSING);
        for(Map.Entry<ResourceLocation, Collection<ResourceLocation>> entry : MODEL_MAP.asMap().entrySet()) {
            IBakedModel defaultModel = modelRegistry.getOrDefault(entry.getKey(), missingModel);
            for(ResourceLocation modelLocation : entry.getValue()) {
                modelRegistry.put(modelLocation, defaultModel);
            }
        }
    }
}