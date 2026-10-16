package com.hollowdread.client;

import com.hollowdread.config.DreadConfig;
import com.hollowdread.scare.ScareType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Executa os sustos que so existem na tela/ouvidos do jogador. */
@OnlyIn(Dist.CLIENT)
public final class ClientScareManager {
    public static int jumpscareTicks;
    public static int flashTicks;
    public static float shake;
    private static int heartTimer;
    private static final List<Pending> QUEUE = new ArrayList<>();

    private static final class Pending {
        int delay;
        final Runnable run;
        Pending(int delay, Runnable run) { this.delay = delay; this.run = run; }
    }

    private ClientScareManager() {}

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        ClientState.smoothSanity += (ClientState.sanity - ClientState.smoothSanity) * 0.08f;
        if (mc.player == null || mc.level == null) {
            QUEUE.clear();
            return;
        }
        if (mc.isPaused()) return;

        if (jumpscareTicks > 0) jumpscareTicks--;
        if (flashTicks > 0) flashTicks--;
        shake *= 0.9f;

        List<Runnable> due = new ArrayList<>();
        Iterator<Pending> it = QUEUE.iterator();
        while (it.hasNext()) {
            Pending p = it.next();
            if (--p.delay <= 0) {
                due.add(p.run);
                it.remove();
            }
        }
        due.forEach(Runnable::run);

        // Coracao acelerado quando a sanidade esta baixa
        float s = ClientState.smoothSanity;
        if (s < 35f) {
            if (--heartTimer <= 0) {
                mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                        SoundEvents.WARDEN_HEARTBEAT, SoundSource.AMBIENT, 0.9f, 1.0f, false);
                heartTimer = (int) Mth.lerp(s / 35f, 26f, 60f);
            }
        }
    }

    public static void handle(ScareType type, double x, double y, double z, int extra) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        ClientLevel lv = mc.level;
        if (p == null || lv == null) return;

        switch (type) {
            case FLASH -> flashTicks = 4;
            case JUMPSCARE -> {
                if (DreadConfig.CLIENT.jumpscares.get()) {
                    jumpscareTicks = 14;
                    shake = 1.6f;
                }
                play(lv, SoundEvents.ENDERMAN_SCREAM, p.getX(), p.getY(), p.getZ(), 1.0f, 0.5f);
                play(lv, SoundEvents.WARDEN_ROAR, p.getX(), p.getY(), p.getZ(), 0.8f, 1.0f);
            }
            case CAVE_NOISE -> play(lv, SoundEvents.WARDEN_AMBIENT, x, y, z, 0.5f, 0.55f + lv.random.nextFloat() * 0.2f);
            case FOOTSTEPS -> {
                SoundEvent step = switch (extra % 3) {
                    case 0 -> SoundEvents.STONE_STEP;
                    case 1 -> SoundEvents.GRAVEL_STEP;
                    default -> SoundEvents.WOOD_STEP;
                };
                int steps = 7;
                for (int i = 0; i < steps; i++) {
                    final double f = (i / (double) steps) * 0.85;
                    final double sx = x + (p.getX() - x) * f;
                    final double sz = z + (p.getZ() - z) * f;
                    schedule(i * 9 + 1, () -> play(lv, step, sx, p.getY(), sz, 0.9f, 0.85f + lv.random.nextFloat() * 0.2f));
                }
            }
            case WHISPER -> {
                mc.gui.setOverlayMessage(Component.translatable("whisper.hollowdread." + Mth.clamp(extra, 1, 8))
                        .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
                play(lv, SoundEvents.WARDEN_AMBIENT, p.getX(), p.getY(), p.getZ(), 0.25f, 1.5f);
            }
            case HEARTBEAT -> {
                for (int i = 0; i < 6; i++) {
                    schedule(i * 18 + 1, () -> play(lv, SoundEvents.WARDEN_HEARTBEAT, p.getX(), p.getY(), p.getZ(), 1.0f, 1.0f));
                }
            }
            case FAKE_CREEPER -> play(lv, SoundEvents.CREEPER_PRIMED, x, y, z, 1.0f, 1.0f);
            case DOOR_CREAK -> {
                play(lv, SoundEvents.WOODEN_DOOR_OPEN, x, y, z, 1.0f, 0.7f);
                schedule(45, () -> play(lv, SoundEvents.WOODEN_DOOR_CLOSE, x, y, z, 1.0f, 0.7f));
            }
            case BREATH -> {
                for (int i = 0; i < 3; i++) {
                    schedule(i * 30 + 1, () -> play(lv, SoundEvents.PLAYER_BREATH, x, y, z, 1.0f, 0.6f));
                }
            }
            case KNOCKING -> {
                for (int i = 0; i < 3; i++) {
                    schedule(i * 8 + 1, () -> play(lv, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, x, y, z, 1.0f, 0.8f));
                }
            }
        }
    }

    private static void play(ClientLevel lv, SoundEvent ev, double x, double y, double z, float vol, float pitch) {
        lv.playLocalSound(x, y, z, ev, SoundSource.AMBIENT, vol, pitch, false);
    }

    private static void schedule(int delay, Runnable r) {
        QUEUE.add(new Pending(delay, r));
    }
}
