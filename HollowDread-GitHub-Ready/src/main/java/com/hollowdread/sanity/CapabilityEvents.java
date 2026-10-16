package com.hollowdread.sanity;

import com.hollowdread.HollowDread;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HollowDread.MODID)
public final class CapabilityEvents {
    private CapabilityEvents() {}

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof Player && !e.getObject().getCapability(Sanity.CAP).isPresent()) {
            e.addCapability(new ResourceLocation(HollowDread.MODID, "sanity"), new PlayerSanityProvider());
        }
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone e) {
        e.getOriginal().reviveCaps();
        e.getOriginal().getCapability(Sanity.CAP).ifPresent(old ->
                e.getEntity().getCapability(Sanity.CAP).ifPresent(n -> {
                    n.copyFrom(old);
                    if (e.isWasDeath()) n.afterDeath();
                }));
        e.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) Sanity.sync(sp);
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) Sanity.sync(sp);
    }

    @SubscribeEvent
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) Sanity.sync(sp);
    }
}
