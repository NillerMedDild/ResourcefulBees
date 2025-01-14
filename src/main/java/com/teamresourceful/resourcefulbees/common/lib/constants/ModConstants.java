package com.teamresourceful.resourcefulbees.common.lib.constants;

import com.google.gson.Gson;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.ResourceLocation;

import java.text.DecimalFormat;

public class ModConstants {

    private ModConstants() {
        throw new IllegalStateException(UTILITY_CLASS);
    }

    public static final String UTILITY_CLASS = "Utility Class";
    public static final Gson GSON = new Gson();
    public static final ResourceLocation SHADES_OF_BEES = new ResourceLocation("resourcefulbees:fifty_shades_of_bees");
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##0.0");
    public static final int HONEY_PER_BOTTLE = 250;
    public static final EntityClassification BEE_MOB_CATEGORY = EntityClassification.create("RESOURCEFUL_BEES", "resourceful_bees", 20, true, false, 128);
}
