package com.hollowdread.scare;

import com.hollowdread.HollowDread;
import com.hollowdread.config.DreadConfig;
import com.hollowdread.entity.CrawlerEntity;
import com.hollowdread.entity.IDread;
import com.hollowdread.entity.StalkerEntity;
import com.hollowdread.entity.WatcherEntity;
import com.hollowdread.init.ModBlocks;
import com.hollowdread.init.ModEffects;
import com.hollowdread.init.ModEntities;
import com.hollowdread.init.ModItems;
import com.hollowdread.network.ModNetwork;
import com.hollowdread.sanity.PlayerSanity;
import com.hollowdread.sanity.Sanity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * O "Diretor do Medo": monitora a sanidade de cada jogador, dosa o ritmo dos
 * sustos (cooldown dinamico) e escolhe eventos ponderados por nivel de sanidade.
 */
@Mod.EventBusSubscriber(modid = HollowDread.MODID)
public final class DreadDirector {
    public static final int WHISPER_LINES = 8;

    private DreadDirector() {}

    // ------------------------------------------------------------------ tick

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer sp)) return;
        if (sp.isCreative() || sp.isSpectator()) return;
        PlayerSanity data = Sanity.data(sp);
        if (data == null) return;

        if (data.scareCooldown > 0) data.scareCooldown--;
        int t = sp.tickCount;
        if (t % 20 == 0) secondTick(sp, data);
        if (t % 20 == 7) maybeScare(sp, data);
        if (t % 10 == 3 && Math.abs(data.get() - data.lastSynced) >= 0.5f) Sanity.sync(sp);
    }

    private static void secondTick(ServerPlayer sp, PlayerSanity data) {
        ServerLevel level = sp.serverLevel();
        BlockPos pos = sp.blockPosition();
        float delta = 0f;

        int light = level.getMaxLocalRawBrightness(pos);
        boolean sky = level.canSeeSky(pos);
        if (light <= 3) delta -= 0.22f;
        else if (light <= 7) delta -= 0.06f;
        else if (light >= 12) delta += 0.12f;
        else delta += 0.03f;

        if (!sky && pos.getY() < 40) delta -= 0.05f;
        if (level.dimension() == Level.NETHER) delta -= 0.10f;
        else if (level.dimension() == Level.END) delta -= 0.20f;
        if (level.isThundering() && sky) delta -= 0.05f;
        if (sp.isSleeping()) delta += 0.8f;
        if (sp.hasEffect(ModEffects.DREAD.get())) delta -= 0.30f;

        // Presenca de entidades do terror por perto
        AABB box = sp.getBoundingBox().inflate(24);
        for (LivingEntity le : level.getEntitiesOfClass(LivingEntity.class, box, x -> x instanceof IDread)) {
            double d = sp.distanceTo(le);
            delta -= 0.35f * (1f - (float) (d / 24.0));
        }

        // Bonecas amaldicoadas no inventario
        delta -= 0.12f * Math.min(count(sp, ModItems.DOLL.get()), 3);

        // Lanterna da Razao
        InteractionHand lanternHand = lanternHand(sp);
        if (lanternHand != null) {
            if (delta < 0) delta *= 0.25f;
            delta += 0.15f;
            if (++data.lanternTicks >= 6) {
                data.lanternTicks = 0;
                final InteractionHand h = lanternHand;
                sp.getItemInHand(h).hurtAndBreak(1, sp, p -> p.broadcastBreakEvent(h));
            }
        }

        // Vela de protecao
        if (nearCandle(level, pos)) {
            if (delta < 0) delta *= 0.4f;
            delta += 0.35f;
        }

        if (delta < 0) delta *= DreadConfig.COMMON.drainMultiplier.get().floatValue();
        else delta *= DreadConfig.COMMON.regenMultiplier.get().floatValue();
        data.add(delta);

        if (sp.tickCount % 1200 == 0 && data.pillDose > 0) data.pillDose--;
        applyMadness(sp, data.get());
    }

    private static void applyMadness(ServerPlayer sp, float s) {
        RandomSource r = sp.getRandom();
        if (s < 20f && r.nextFloat() < 0.15f) {
            sp.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, false, false));
        }
        if (s < 10f) {
            sp.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0, false, false, false));
        }
        if (s <= 2f && sp.tickCount % 100 == 0 && sp.getHealth() > 4f) {
            sp.hurt(sp.damageSources().magic(), 1f);
        }
    }

    private static void maybeScare(ServerPlayer sp, PlayerSanity data) {
        if (data.scareCooldown > 0) return;
        if (sp.level().getDifficulty() == Difficulty.PEACEFUL && !DreadConfig.COMMON.scaresInPeaceful.get()) return;
        float s = data.get();
        if (s >= 85f) return;
        RandomSource r = sp.getRandom();
        float chance = Mth.clamp((85f - s) / 85f, 0f, 1f) * 0.30f;
        if (r.nextFloat() > chance) return;
        DreadEvent ev = DreadEvent.pick(s, r);
        if (ev == null) return;
        if (run(sp, ev)) {
            float min = DreadConfig.COMMON.minScareSeconds.get() * 20f;
            float max = DreadConfig.COMMON.maxScareSeconds.get() * 20f;
            float base = Mth.lerp(s / 100f, min, Math.max(min, max));
            data.scareCooldown = (int) (base * (0.7f + 0.6f * r.nextFloat()));
        }
    }

    // ---------------------------------------------------------------- eventos

    /** Executa um evento. Retorna false se nao foi possivel (ex.: sem espaco). */
    public static boolean run(ServerPlayer sp, DreadEvent ev) {
        ServerLevel level = sp.serverLevel();
        RandomSource r = sp.getRandom();
        Vec3 back = behind(sp, 9);

        switch (ev) {
            case CAVE_NOISE -> { scare(sp, ScareType.CAVE_NOISE, back, 0); return true; }
            case FOOTSTEPS -> { scare(sp, ScareType.FOOTSTEPS, behind(sp, 11), r.nextInt(3)); return true; }
            case WHISPER -> { scare(sp, ScareType.WHISPER, back, 1 + r.nextInt(WHISPER_LINES)); return true; }
            case BREATH -> { scare(sp, ScareType.BREATH, behind(sp, 1.5), 0); return true; }
            case KNOCKING -> { scare(sp, ScareType.KNOCKING, around(sp, 5, 9), 0); return true; }
            case HEARTBEAT -> { scare(sp, ScareType.HEARTBEAT, sp.position(), 0); return true; }
            case FAKE_CREEPER -> { scare(sp, ScareType.FAKE_CREEPER, back, 0); return true; }
            case DOOR_CREAK -> { scare(sp, ScareType.DOOR_CREAK, around(sp, 5, 10), 0); return true; }
            case FLASH -> { scare(sp, ScareType.FLASH, sp.position(), 0); return true; }
            case TORCH_SNUFF -> { return snuffTorches(sp); }
            case DOOR_SLAM -> { return slamDoors(sp); }
            case GHOST -> { return ghost(sp); }
            case WATCHER -> { return spawnWatcher(sp); }
            case CRAWLER_AMBUSH -> { return crawlerAmbush(sp); }
            case DARKNESS_PULSE -> {
                sp.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 140, 0, false, false));
                scare(sp, ScareType.HEARTBEAT, sp.position(), 0);
                scare(sp, ScareType.CAVE_NOISE, back, 0);
                return true;
            }
            case JUMPSCARE -> {
                if (!DreadConfig.COMMON.jumpscares.get()) return false;
                Sanity.add(sp, -5f);
                scare(sp, ScareType.JUMPSCARE, sp.position(), 0);
                return true;
            }
            case STALKER -> { return spawnStalker(sp); }
            default -> { return false; }
        }
    }

    private static boolean ghost(ServerPlayer sp) {
        ServerLevel level = sp.serverLevel();
        EntityType<?> type = sp.getRandom().nextBoolean() ? ModEntities.WATCHER.get() : ModEntities.STALKER.get();
        Vec3 pos = DreadSpawner.findPos(level, sp, type, 10, 22, false, true);
        if (pos == null) return false;
        Entity ent = type.create(level);
        if (ent == null) return false;
        float yaw = (float) (Mth.atan2(sp.getZ() - pos.z, sp.getX() - pos.x) * (180.0 / Math.PI)) - 90f;
        ent.moveTo(pos.x, pos.y, pos.z, yaw, 0f);
        if (ent instanceof LivingEntity le) {
            le.setYHeadRot(yaw);
            le.yBodyRot = yaw;
        }
        // Entidade que SO o jogador alvo ve: nunca entra no mundo do servidor.
        sp.connection.send(new ClientboundAddEntityPacket(ent, Ghosts.GHOST_MARKER));
        scare(sp, ScareType.HEARTBEAT, sp.position(), 0);
        return true;
    }

    private static boolean spawnWatcher(ServerPlayer sp) {
        if (!DreadConfig.COMMON.enableWatcher.get()) return false;
        ServerLevel level = sp.serverLevel();
        if (DreadSpawner.countNear(level, sp, WatcherEntity.class, 100) >= 1) return false;
        Vec3 pos = DreadSpawner.findPos(level, sp, ModEntities.WATCHER.get(), 28, 42, false, true);
        if (pos == null) return false;
        return DreadSpawner.spawn(level, ModEntities.WATCHER.get(), pos, sp) != null;
    }

    private static boolean spawnStalker(ServerPlayer sp) {
        if (!DreadConfig.COMMON.enableStalker.get()) return false;
        ServerLevel level = sp.serverLevel();
        if (DreadSpawner.countNear(level, sp, StalkerEntity.class, 96) >= 1) return false;
        Vec3 pos = DreadSpawner.findPos(level, sp, ModEntities.STALKER.get(), 22, 32, true, false);
        if (pos == null) return false;
        if (DreadSpawner.spawn(level, ModEntities.STALKER.get(), pos, sp) == null) return false;
        scare(sp, ScareType.HEARTBEAT, sp.position(), 0);
        scare(sp, ScareType.CAVE_NOISE, pos, 0);
        return true;
    }

    private static boolean crawlerAmbush(ServerPlayer sp) {
        if (!DreadConfig.COMMON.enableCrawlerAmbush.get()) return false;
        ServerLevel level = sp.serverLevel();
        if (DreadSpawner.countNear(level, sp, CrawlerEntity.class, 40) >= 3) return false;
        int spawned = 0;
        int want = 1 + sp.getRandom().nextInt(2);
        for (int i = 0; i < want; i++) {
            Vec3 pos = DreadSpawner.findPos(level, sp, ModEntities.CRAWLER.get(), 9, 15, true, false);
            if (pos == null) continue;
            if (level.getMaxLocalRawBrightness(BlockPos.containing(pos)) > 7) continue;
            if (DreadSpawner.spawn(level, ModEntities.CRAWLER.get(), pos, sp) != null) spawned++;
        }
        if (spawned > 0) scare(sp, ScareType.BREATH, behind(sp, 3), 0);
        return spawned > 0;
    }

    private static boolean snuffTorches(ServerPlayer sp) {
        if (!DreadConfig.COMMON.torchesGoOut.get()) return false;
        ServerLevel level = sp.serverLevel();
        BlockPos c = sp.blockPosition();
        int n = 0;
        for (BlockPos p : BlockPos.betweenClosed(c.offset(-10, -4, -10), c.offset(10, 4, 10))) {
            BlockState st = level.getBlockState(p);
            if (st.getBlock() instanceof TorchBlock && !(st.getBlock() instanceof RedstoneTorchBlock)
                    && level.random.nextFloat() < 0.5f) {
                level.destroyBlock(p.immutable(), true);
                if (++n >= 4) break;
            }
        }
        if (n > 0) {
            level.playSound(null, c, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1f, 0.6f);
            return true;
        }
        return false;
    }

    private static boolean slamDoors(ServerPlayer sp) {
        if (!DreadConfig.COMMON.doorSlams.get()) return false;
        ServerLevel level = sp.serverLevel();
        BlockPos c = sp.blockPosition();
        int n = 0;
        for (BlockPos p : BlockPos.betweenClosed(c.offset(-9, -3, -9), c.offset(9, 3, 9))) {
            BlockState st = level.getBlockState(p);
            if (st.getBlock() instanceof DoorBlock door && st.is(BlockTags.WOODEN_DOORS)
                    && st.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                door.setOpen(null, level, st, p.immutable(), !st.getValue(DoorBlock.OPEN));
                if (++n >= 2) break;
            }
        }
        return n > 0;
    }

    // --------------------------------------------------------------- helpers

    public static void scare(ServerPlayer sp, ScareType type, Vec3 pos, int extra) {
        ModNetwork.sendScare(sp, type, pos.x, pos.y, pos.z, extra);
    }

    private static Vec3 behind(ServerPlayer sp, double dist) {
        Vec3 look = sp.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0, look.z).normalize().scale(-dist);
        return sp.position().add(flat);
    }

    private static Vec3 around(ServerPlayer sp, double min, double max) {
        RandomSource r = sp.getRandom();
        double ang = r.nextDouble() * Math.PI * 2;
        double d = min + r.nextDouble() * (max - min);
        return sp.position().add(Math.cos(ang) * d, 0, Math.sin(ang) * d);
    }

    private static InteractionHand lanternHand(ServerPlayer sp) {
        if (sp.getMainHandItem().is(ModItems.LANTERN.get())) return InteractionHand.MAIN_HAND;
        if (sp.getOffhandItem().is(ModItems.LANTERN.get())) return InteractionHand.OFF_HAND;
        return null;
    }

    private static int count(ServerPlayer sp, Item item) {
        int n = 0;
        for (ItemStack s : sp.getInventory().items) if (s.is(item)) n++;
        if (sp.getOffhandItem().is(item)) n++;
        return n;
    }

    public static boolean nearCandle(ServerLevel level, BlockPos c) {
        for (BlockPos p : BlockPos.betweenClosed(c.offset(-5, -2, -5), c.offset(5, 2, 5))) {
            if (level.getBlockState(p).is(ModBlocks.WARDING_CANDLE.get())) return true;
        }
        return false;
    }
}
