package com.hollowdread.item;

import com.hollowdread.network.ModNetwork;
import com.hollowdread.scare.DreadDirector;
import com.hollowdread.scare.ScareType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Boneca amaldicoada: drena sanidade no inventario e sussurra de vez em quando. */
public class DollItem extends Item {
    public DollItem(Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !(entity instanceof ServerPlayer sp)) return;
        if (sp.tickCount % 200 == 0 && level.random.nextFloat() < 0.15f) {
            ModNetwork.sendScare(sp, ScareType.WHISPER, sp.getX(), sp.getY(), sp.getZ(),
                    1 + level.random.nextInt(DreadDirector.WHISPER_LINES));
        }
    }

    @Override
    public void appendHoverText(ItemStack s, @Nullable Level l, List<Component> tip, TooltipFlag f) {
        tip.add(Component.translatable("tooltip.hollowdread.doll").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }
}
