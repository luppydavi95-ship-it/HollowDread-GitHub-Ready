package com.hollowdread.scare;

import net.minecraft.util.RandomSource;

/**
 * Eventos que o Diretor do Medo pode disparar. Cada um so entra no sorteio
 * quando a sanidade do jogador esta ABAIXO do limite (threshold).
 */
public enum DreadEvent {
    CAVE_NOISE(85f, 10),
    FOOTSTEPS(80f, 10),
    WHISPER(80f, 9),
    BREATH(70f, 6),
    KNOCKING(70f, 6),
    HEARTBEAT(62f, 5),
    FAKE_CREEPER(60f, 5),
    DOOR_CREAK(60f, 6),
    TORCH_SNUFF(55f, 5),
    FLASH(50f, 3),
    WATCHER(50f, 6),
    GHOST(45f, 6),
    DOOR_SLAM(40f, 5),
    CRAWLER_AMBUSH(38f, 4),
    DARKNESS_PULSE(32f, 4),
    JUMPSCARE(30f, 3),
    STALKER(25f, 8);

    public final float threshold;
    public final int weight;

    DreadEvent(float threshold, int weight) {
        this.threshold = threshold;
        this.weight = weight;
    }

    public static DreadEvent pick(float sanity, RandomSource r) {
        int total = 0;
        for (DreadEvent e : values()) if (sanity <= e.threshold) total += e.weight;
        if (total <= 0) return null;
        int roll = r.nextInt(total);
        for (DreadEvent e : values()) {
            if (sanity > e.threshold) continue;
            roll -= e.weight;
            if (roll < 0) return e;
        }
        return null;
    }
}
