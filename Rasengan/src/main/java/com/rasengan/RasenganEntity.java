package com.rasengan;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RasenganEntity extends Entity {
    private static final EntityDataAccessor<Float> SCALE =
            SynchedEntityData.defineId(RasenganEntity.class, EntityDataSerializers.FLOAT);
    private static final int GROW_TICKS = 80;
    private static final int GRAPPLE_TICKS = 300;
    private static final int DESPAWN_DELAY = 15;
    private static final float MAX_SCALE = 2.5f;
    private static final float DAMAGE = 15.0f;
    private static final double DAMAGE_RADIUS = 4.0;
    private static final double CHASE_SPEED = 0.5;
    private static final double IMPACT_DIST = 1.8;

    private boolean grappling = false;
    private boolean exploded = false;
    private int grappleTimer = 0;
    private int despawnTimer = 0;
    private int chaseTimer = 0;

    @Nullable
    private Player owner;
    @Nullable
    private Player targetPlayer;
    private int lifeTicks = 0;
    private Vec3 startPos;

    public RasenganEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    public void setOwner(Player player) {
        this.owner = player;
    }

    public void setTargetPlayer(Player player) {
        this.targetPlayer = player;
    }

    public void setStartPos(Vec3 pos) {
        this.startPos = pos;
    }

    public float getRenderScale() {
        return entityData.get(SCALE);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(SCALE, 0.2f);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTicks++;

        if (level().isClientSide) {
            float scale = lifeTicks <= GROW_TICKS
                    ? 0.2f + (lifeTicks / (float) GROW_TICKS) * (MAX_SCALE - 0.2f)
                    : entityData.get(SCALE);
            entityData.set(SCALE, scale);

            if (lifeTicks <= GROW_TICKS) {
                for (int i = 0; i < 2; i++) {
                    double theta = Math.acos(2 * random.nextDouble() - 1);
                    double phi = random.nextDouble() * 2 * Math.PI;
                    double r = scale * 0.7;
                    double px = getX() + r * Math.sin(theta) * Math.cos(phi);
                    double py = getY() + 0.5 + r * Math.cos(theta);
                    double pz = getZ() + r * Math.sin(theta) * Math.sin(phi);
                    level().addParticle(ParticleTypes.END_ROD, px, py, pz, 0, 0.02, 0);
                }
            } else {
                level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        getX(), getY() + 0.5, getZ(),
                        0.2 * (random.nextDouble() - 0.5),
                        0.2 * (random.nextDouble() - 0.5),
                        0.2 * (random.nextDouble() - 0.5));
            }
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level();

        if (exploded) {
            if (--despawnTimer <= 0) discard();
            return;
        }

        // ====== GROW PHASE ======
        if (lifeTicks <= GROW_TICKS) {
            float scale = 0.2f + (lifeTicks / (float) GROW_TICKS) * (MAX_SCALE - 0.2f);
            entityData.set(SCALE, scale);
            setPos(startPos.x, startPos.y, startPos.z);

            for (int i = 0; i < 5; i++) {
                double theta = Math.acos(2 * Math.random() - 1);
                double phi = Math.random() * 2 * Math.PI;
                double r = scale * 1.1;
                double px = startPos.x + r * Math.sin(theta) * Math.cos(phi);
                double py = startPos.y + r * Math.sin(theta) * Math.sin(phi);
                double pz = startPos.z + r * Math.cos(theta);
                serverLevel.sendParticles(ParticleTypes.END_ROD, px, py, pz, 1, 0, 0, 0, 0.01);
            }

            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    startPos.x, startPos.y + 0.5, startPos.z, 2,
                    0.8, 0.8, 0.8, 0.0);

            if (lifeTicks == 1) {
                level().playSound(null, startPos.x, startPos.y, startPos.z,
                        SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 3.0f, 0.5f);
            }
            if (lifeTicks == 70) {
                level().playSound(null, startPos.x, startPos.y, startPos.z,
                        SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 4.0f, 1.0f);
            }
            return;
        }

        // find target
        Player target = targetPlayer;
        if (target == null || !target.isAlive()) {
            target = null;
            double nearest = Double.MAX_VALUE;
            for (Player p : serverLevel.getServer().getPlayerList().getPlayers()) {
                if (p == owner || !p.isAlive()) continue;
                double d = distanceToSqr(p);
                if (d < nearest) { nearest = d; target = p; }
            }
        }
        if (target == null || !target.isAlive()) { discard(); return; }

        Vec3 toTarget = target.position().add(0, target.getEyeHeight() * 0.5, 0).subtract(position());
        double dist = toTarget.length();

        // ====== GRAPPLE PHASE ======
        if (grappling) {
            Vec3 grapplePos = target.position().add(
                    toTarget.normalize().scale(-0.5)
            ).add(0, 0.2, 0);
            setPos(grapplePos.x, grapplePos.y, grapplePos.z);
            entityData.set(SCALE, MAX_SCALE);

            // push target backward horizontally (no upward launch)
            Vec3 pushDir = new Vec3(
                    target.getX() - getX(), 0,
                    target.getZ() - getZ()
            ).normalize();
            target.setDeltaMovement(pushDir.x * 1.0, target.getDeltaMovement().y * 0.5, pushDir.z * 1.0);
            target.hurtMarked = true;

            // destroy blocks in radius around rasengan
            int radius = 2;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (dx * dx + dy * dy + dz * dz > radius * radius) continue;
                        BlockPos bp = BlockPos.containing(getX() + dx, getY() + dy + 0.5, getZ() + dz);
                        BlockState bs = serverLevel.getBlockState(bp);
                        if (!bs.isAir() && !bs.is(Blocks.BEDROCK) && !bs.is(Blocks.OBSIDIAN)) {
                            serverLevel.destroyBlock(bp, false);
                        }
                    }
                }
            }

            // grapple particles
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    getX(), getY() + 0.5, getZ(), 3,
                    0.6, 0.6, 0.6, 0.1);
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    getX(), getY() + 0.5, getZ(), 2,
                    0.4, 0.4, 0.4, 0.03);

            grappleTimer++;
            if (grappleTimer >= GRAPPLE_TICKS) {
                explode();
            }
            return;
        }

        // ====== CHASE PHASE ======
        if (dist <= IMPACT_DIST) {
            // impact: start grappling
            grappling = true;
            grappleTimer = 0;
            if (target instanceof ServerPlayer sp) RasenganMod.onGrappleStart(sp);

            // initial hit sound
            level().playSound(null, getX(), getY(), getZ(),
                    SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 3.0f, 0.5f);
            level().playSound(null, getX(), getY(), getZ(),
                    ModSounds.RASENGAN_GRAPPLE.get(), SoundSource.PLAYERS, 3.0f, 1.0f);
            return;
        }

        chaseTimer++;
        if (chaseTimer > 100) {
            discard();
            return;
        }

        Vec3 dir = toTarget.normalize();
        Vec3 vel = dir.scale(CHASE_SPEED);
        setDeltaMovement(vel);
        setPos(getX() + vel.x, getY() + vel.y, getZ() + vel.z);

        for (int i = 0; i < 3; i++) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    getX(), getY() + 0.5, getZ(), 1, 0.3, 0.3, 0.3, 0.01);
        }
        serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                getX(), getY() + 0.5, getZ(), 2, 0.4, 0.4, 0.4, 0.05);

        Vec3 trailPos = position().subtract(vel.scale(0.5));
        serverLevel.sendParticles(ParticleTypes.END_ROD,
                trailPos.x, trailPos.y + 0.5, trailPos.z, 1, 0.1, 0.1, 0.1, 0.005);
    }

    private void explode() {
        if (!(level() instanceof ServerLevel serverLevel)) return;

        exploded = true;
        Vec3 pos = position();

        // ====== BIG FLASH ======
        serverLevel.sendParticles(ParticleTypes.FLASH, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);

        // ====== EXPLOSION CLOUD ======
        serverLevel.sendParticles(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 15, 3.0, 2.0, 3.0, 0.3);
        serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);

        // ====== ENERGY SPHERE BURST (3D expanding sphere of particles) ======
        for (int i = 0; i < 60; i++) {
            double theta = Math.acos(2 * random.nextDouble() - 1);
            double phi = random.nextDouble() * 2 * Math.PI;
            double r = 0.5 + random.nextDouble() * 3.5;
            double px = pos.x + r * Math.sin(theta) * Math.cos(phi);
            double py = pos.y + 0.5 + r * Math.cos(theta);
            double pz = pos.z + r * Math.sin(theta) * Math.sin(phi);
            serverLevel.sendParticles(ParticleTypes.END_ROD, px, py, pz, 1, 0, 0, 0, 0.06);
        }

        // ====== EXPANDING SHOCKWAVE RINGS ======
        for (int ring = 0; ring < 6; ring++) {
            float ringR = 0.8f + ring * 1.0f;
            int count = 16 + ring * 6;
            for (int i = 0; i < count; i++) {
                double angle = (i / (double) count) * 2 * Math.PI;
                double px = pos.x + ringR * Math.cos(angle);
                double pz = pos.z + ringR * Math.sin(angle);
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, px, pos.y + 0.3, pz, 1, 0, 0, 0, 0.15);
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px, pos.y + 0.8 + ring * 0.3, pz, 1, 0, 0, 0, 0.04);
            }
        }

        // ====== GROUND DUST RING ======
        for (int i = 0; i < 40; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double r = 0.5 + random.nextDouble() * 4.0;
            double px = pos.x + r * Math.cos(angle);
            double pz = pos.z + r * Math.sin(angle);
            serverLevel.sendParticles(ParticleTypes.POOF, px, pos.y, pz, 1, 0, 0.3, 0, 0.04);
        }

        // ====== RISING ENERGY PILLARS ======
        for (int i = 0; i < 16; i++) {
            double angle = (i / 16.0) * 2 * Math.PI;
            double px = pos.x + 0.6 * Math.cos(angle);
            double pz = pos.z + 0.6 * Math.sin(angle);
            for (int h = 0; h < 6; h++) {
                serverLevel.sendParticles(ParticleTypes.END_ROD, px, pos.y + 0.2 + h * 0.5, pz, 1, 0, 0, 0, 0.008);
            }
        }

        // ====== RANDOM SPARKS ======
        for (int i = 0; i < 40; i++) {
            double px = pos.x + (random.nextDouble() - 0.5) * 6.0;
            double py = pos.y + random.nextDouble() * 3.5;
            double pz = pos.z + (random.nextDouble() - 0.5) * 6.0;
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 1, 0, 0, 0, 0.03);
        }

        // ====== SOUNDS ======
        level().playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 4.0f, 0.5f);
        level().playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 3.0f, 0.3f);

        // ====== DAMAGE & KNOCKBACK ======
        AABB area = new AABB(
                pos.x - DAMAGE_RADIUS, pos.y - DAMAGE_RADIUS, pos.z - DAMAGE_RADIUS,
                pos.x + DAMAGE_RADIUS, pos.y + DAMAGE_RADIUS, pos.z + DAMAGE_RADIUS
        );
        List<Entity> entities = level().getEntities(this, area);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity living && entity != owner) {
                double dist = entity.distanceToSqr(pos);
                if (dist <= DAMAGE_RADIUS * DAMAGE_RADIUS) {
                    living.hurt(level().damageSources().indirectMagic(owner != null ? owner : this, this), DAMAGE);

                    // Rasengan-style knockback: launch victim backward aggressively
                    Vec3 kbDir = new Vec3(
                            living.getX() - pos.x, 0,
                            living.getZ() - pos.z
                    ).normalize();
                    double kbStrength = 3.0 * (1.0 - Math.sqrt(dist) / DAMAGE_RADIUS) + 1.5;
                    living.setDeltaMovement(kbDir.x * kbStrength, 0.6, kbDir.z * kbStrength);
                    living.hurtMarked = true;
                }
            }
        }
        despawnTimer = 15;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        lifeTicks = tag.getInt("LifeTicks");
        entityData.set(SCALE, tag.getFloat("Scale"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("LifeTicks", lifeTicks);
        tag.putFloat("Scale", entityData.get(SCALE));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
