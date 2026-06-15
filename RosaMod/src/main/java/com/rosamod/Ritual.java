package com.rosamod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Ritual {
    private final ServerPlayer target;
    private final UUID uuid;
    private final Vec3 lockPos;
    private final ServerLevel level;
    private final float originalWalkSpeed;
    private int count = 0;
    private boolean finished = false;
    private final Map<BlockPos, BlockState> chainOriginalBlocks = new HashMap<>();

    public Ritual(ServerPlayer target) {
        this.target = target;
        this.uuid = target.getUUID();
        this.lockPos = target.position();
        this.level = (ServerLevel) target.level();
        this.originalWalkSpeed = (float) target.getAttributeBaseValue(Attributes.MOVEMENT_SPEED);
    }

    public void start() {
        RitualEffects.clearBlocksAroundPlayer(level, lockPos, 5, 3, 3);

        Vec3 neckPoint = lockPos.add(0, 1.6, 0);
        double staticChainRadius = 5.3;
        for (int i = 0; i < 8; ++i) {
            double angle = Math.PI * 2 * i / 8.0;
            double groundX = lockPos.x + Math.cos(angle) * staticChainRadius;
            double groundZ = lockPos.z + Math.sin(angle) * staticChainRadius;
            Vec3 groundPoint = new Vec3(groundX, lockPos.y + 0.1, groundZ);
            Map<BlockPos, BlockState> originals = RitualEffects.placeRealGoldenChain(level, groundPoint, neckPoint);
            chainOriginalBlocks.putAll(originals);
        }
    }

    public void tick() {
        if (!target.isAlive() || target.isRemoved()) {
            cleanup(false);
            return;
        }
        if (count >= 100) {
            cleanup(true);
            return;
        }

        runRitualTick();
        count++;
    }

    public boolean isFinished() {
        return finished;
    }

    private void runRitualTick() {
        Vec3 center = target.position();
        boolean finalPhase = count >= 100;
        double pulse = Math.sin(count * 0.15);
        double currentRadius = 4.2 + pulse * 0.35;

        if (finalPhase) {
            double progress = (count - 100) / 20.0;
            currentRadius = Math.max(2.6, currentRadius - progress * 1.2);
        }

        if (count == 0) {
            Vec3 c = center.add(0, 1, 0);
            level.sendParticles(ParticleTypes.EXPLOSION, c.x, c.y, c.z, 1, 0, 0, 0, 0);
            level.playSound(null, BlockPos.containing(c), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.75f, 1.55f);
            level.playSound(null, BlockPos.containing(c), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.9f, 0.7f);
        } else if (count % 20 == 0 && !finalPhase) {
            level.playSound(null, BlockPos.containing(center), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.45f, 1.8f);
        }

        RitualEffects.drawChineseFireRunes(level, center, count, finalPhase);
        RitualEffects.drawFireRitualRing(level, center.add(0, 0.08, 0), 4.6, count, finalPhase);
        RitualEffects.drawCreeperRing(level, center.add(0, 0.05, 0), currentRadius + 1.1);
        RitualEffects.drawSpiralEffect(level, center, count);
        RitualEffects.drawFloatingParticles(level, center, count);
        RitualEffects.drawBlackCore(level, center, count, finalPhase);

        // Las cadenas doradas reales ya están colocadas, sin partículas adicionales

        float yaw = target.getYRot();
        float pitch = target.getXRot();
        target.teleportTo(lockPos.x, lockPos.y, lockPos.z);
        target.setYRot(yaw);
        target.setXRot(pitch);
        target.setYHeadRot(yaw);

        target.setDeltaMovement(Vec3.ZERO);
        target.hurtMarked = true;
        target.setSprinting(false);
        target.setShiftKeyDown(false);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 12, 255, true, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.JUMP, 12, 128, true, false, false));
        target.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0);

        target.hurt(target.damageSources().generic(), 0.3f);
        target.invulnerableTime = 0;
    }

    private void cleanup(boolean explode) {
        finished = true;
        target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        target.removeEffect(MobEffects.JUMP);
        target.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(originalWalkSpeed);
        target.setDeltaMovement(Vec3.ZERO);
        target.hurtMarked = true;

        for (java.util.Map.Entry<BlockPos, BlockState> entry : chainOriginalBlocks.entrySet()) {
            level.setBlock(entry.getKey(), entry.getValue(), 3);
        }
        chainOriginalBlocks.clear();
    }
}
