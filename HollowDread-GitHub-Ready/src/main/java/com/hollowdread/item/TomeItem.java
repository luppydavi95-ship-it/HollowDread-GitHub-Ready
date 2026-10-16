package com.hollowdread.item;

import com.hollowdread.sanity.Sanity;
import com.hollowdread.scare.DreadDirector;
import com.hollowdread.scare.DreadEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Tomo do Vazio: revela fragmentos de lore... a um preco. */
public class TomeItem extends Item {
    public static final int LINES = 8;

    public TomeItem(Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            int n = 1 + level.random.nextInt(LINES);
            sp.sendSystemMessage(Component.translatable("message.hollowdread.tome." + n)
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
            Sanity.add(sp, -6f);
            if (level.random.nextFloat() < 0.30f) DreadDirector.run(sp, DreadEvent.WATCHER);
            else if (level.random.nextFloat() < 0.35f) DreadDirector.run(sp, DreadEvent.GHOST);
            sp.getCooldowns().addCooldown(this, 200);
            Sanity.sync(sp);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack s, @Nullable Level l, List<Component> tip, TooltipFlag f) {
        tip.add(Component.translatable("tooltip.hollowdread.tome").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }
}
