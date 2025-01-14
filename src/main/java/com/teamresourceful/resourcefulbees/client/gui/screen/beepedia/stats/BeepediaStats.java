package com.teamresourceful.resourcefulbees.client.gui.screen.beepedia.stats;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

@OnlyIn(Dist.CLIENT)
public abstract class BeepediaStats {
    abstract void initSearchTerms();
    abstract boolean isValidSearch(String search);

    public boolean getSearch(List<String> list, String search) {
        AtomicReference<Boolean> result = new AtomicReference<>(false);
        list.forEach(i -> {
            if (i.toLowerCase(Locale.ENGLISH).contains(search.toLowerCase(Locale.ENGLISH))) {
                result.set(true);
            }
        });
        return result.get();
    }

//    public TabImageButton getTabButton(ItemStack stack, Button.IPressable pressable, ITextComponent tooltip) {
//        TabImageButton button = new TabImageButton(this.xPos + 40 + tabCounter * 21, this.yPos + 27,
//                20, 20, 0, 0, 20, BeepediaImages.BUTTON_IMAGE, stack, 2, 2, pressable, beepedia.getTooltipProvider(tooltip));
//        beepedia.addButton(button);
//        button.visible = false;
//        tabCounter++;
//        return button;
//    }
}
