package com.hollowdread.sanity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraftforge.common.util.INBTSerializable;

public class PlayerSanity implements INBTSerializable<CompoundTag> {
    private float sanity = 100f;
    /** Ticks ate o proximo susto permitido (ritmo do Diretor). */
    public int scareCooldown = 1200;
    public float lastSynced = -1f;
    /** Overdose de Lucid Pills (decai com o tempo). */
    public int pillDose = 0;
    public int lanternTicks = 0;

    public float get() { return sanity; }
    public void set(float v) { sanity = Mth.clamp(v, 0f, 100f); }
    public void add(float v) { set(sanity + v); }

    public void copyFrom(PlayerSanity o) { this.sanity = o.sanity; this.pillDose = o.pillDose; }

    public void afterDeath() {
        this.sanity = Math.max(this.sanity, 60f);
        this.scareCooldown = 1800;
        this.pillDose = 0;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag t = new CompoundTag();
        t.putFloat("Sanity", sanity);
        t.putInt("PillDose", pillDose);
        return t;
    }

    @Override
    public void deserializeNBT(CompoundTag t) {
        sanity = t.contains("Sanity") ? t.getFloat("Sanity") : 100f;
        pillDose = t.getInt("PillDose");
    }
}
