package com.hollowdread.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class DreadConfig {
    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static {
        Pair<Common, ForgeConfigSpec> c = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = c.getLeft();
        COMMON_SPEC = c.getRight();
        Pair<Client, ForgeConfigSpec> k = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = k.getLeft();
        CLIENT_SPEC = k.getRight();
    }

    private DreadConfig() {}

    public static class Common {
        public final ForgeConfigSpec.DoubleValue drainMultiplier;
        public final ForgeConfigSpec.DoubleValue regenMultiplier;
        public final ForgeConfigSpec.IntValue minScareSeconds;
        public final ForgeConfigSpec.IntValue maxScareSeconds;
        public final ForgeConfigSpec.BooleanValue enableStalker;
        public final ForgeConfigSpec.BooleanValue enableWatcher;
        public final ForgeConfigSpec.BooleanValue enableCrawlerAmbush;
        public final ForgeConfigSpec.BooleanValue torchesGoOut;
        public final ForgeConfigSpec.BooleanValue doorSlams;
        public final ForgeConfigSpec.BooleanValue jumpscares;
        public final ForgeConfigSpec.BooleanValue scaresInPeaceful;

        Common(ForgeConfigSpec.Builder b) {
            b.push("sanity");
            drainMultiplier = b.comment("Multiplicador da perda de sanidade (0 desliga).")
                    .defineInRange("drainMultiplier", 1.0, 0.0, 10.0);
            regenMultiplier = b.comment("Multiplicador da recuperacao de sanidade.")
                    .defineInRange("regenMultiplier", 1.0, 0.0, 10.0);
            b.pop();
            b.push("director");
            minScareSeconds = b.comment("Tempo minimo (em sanidade 0) entre sustos.")
                    .defineInRange("minScareSeconds", 12, 1, 3600);
            maxScareSeconds = b.comment("Tempo maximo (em sanidade 100) entre sustos.")
                    .defineInRange("maxScareSeconds", 110, 5, 7200);
            enableStalker = b.define("enableStalker", true);
            enableWatcher = b.define("enableWatcher", true);
            enableCrawlerAmbush = b.define("enableCrawlerAmbush", true);
            torchesGoOut = b.comment("Tochas proximas podem ser apagadas (quebradas) pelo Diretor.")
                    .define("torchesGoOut", true);
            doorSlams = b.define("doorSlams", true);
            jumpscares = b.define("jumpscares", true);
            scaresInPeaceful = b.define("scaresInPeaceful", false);
            b.pop();
        }
    }

    public static class Client {
        public final ForgeConfigSpec.BooleanValue showSanityHud;
        public final ForgeConfigSpec.BooleanValue vignette;
        public final ForgeConfigSpec.BooleanValue cameraShake;
        public final ForgeConfigSpec.BooleanValue jumpscares;
        public final ForgeConfigSpec.DoubleValue shakeIntensity;
        public final ForgeConfigSpec.BooleanValue sanityFog;

        Client(ForgeConfigSpec.Builder b) {
            showSanityHud = b.define("showSanityHud", true);
            vignette = b.define("vignette", true);
            cameraShake = b.define("cameraShake", true);
            jumpscares = b.comment("Desligue se tiver fotossensibilidade.").define("jumpscares", true);
            shakeIntensity = b.defineInRange("shakeIntensity", 1.0, 0.0, 3.0);
            sanityFog = b.define("sanityFog", true);
        }
    }
}
