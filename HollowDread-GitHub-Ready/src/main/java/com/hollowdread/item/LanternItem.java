package com.hollowdread.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Lanterna da Razao: segurada na mao, estabiliza a mente e repele Rastejantes. Reparar com po de glowstone. */
public class LanternItem extends Item {
    public LanternItem(Properties props) {
        super(props.durability(900));
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
        return repair.is(Items.GLOWSTONE_DUST);
    }

    @Override
    public void appendHoverText(ItemStack s, @Nullable Level l, List<Component> tip, TooltipFlag f) {
        tip.add(Component.translatable("tooltip.hollowdread.lantern").withStyle(ChatFormatting.GOLD));
    }
}
