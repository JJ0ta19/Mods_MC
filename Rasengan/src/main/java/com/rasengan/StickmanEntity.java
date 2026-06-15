package com.rasengan;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class StickmanEntity extends Entity {
    private static final EntityDataAccessor<Integer> ANIM_STATE =
            SynchedEntityData.defineId(StickmanEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RASENGAN_ID =
            SynchedEntityData.defineId(StickmanEntity.class, EntityDataSerializers.INT);

    private static final int CHARGE_TICKS = 100;

    @Nullable
    private Player target;
    private int lifeTicks = 0;
    private boolean hasThrown = false;
    @Nullable
    private RasenganEntity heldRasengan;
    @Nullable
    private Player caster;

    public StickmanEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    public void setTarget(Player player) {
        this.target = player;
    }

    public void setCaster(Player player) {
        this.caster = player;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(ANIM_STATE, 0);
        entityData.define(RASENGAN_ID, 0);
    }

    public int getAnimState() {
        return entityData.get(ANIM_STATE);
    }

    public int getRasenganId() {
        return entityData.get(RASENGAN_ID);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTicks++;

        if (level().isClientSide) return;

        ServerLevel serverLevel = (ServerLevel) level();

        if (target == null || !target.isAlive()) { discard(); return; }

        Vec3 targetPos = target.position().add(0, target.getEyeHeight() * 0.5, 0);

        if (lifeTicks <= CHARGE_TICKS) {
            entityData.set(ANIM_STATE, 1);
            Vec3 handPos = getHandPosition();

            if (lifeTicks == 20) {
                heldRasengan = ModEntities.RASENGAN_PROJECTILE.get().create(serverLevel);
                if (heldRasengan != null) {
                    heldRasengan.setPos(handPos.x, handPos.y, handPos.z);
                    heldRasengan.setStartPos(handPos);
                    heldRasengan.setOwner(null);
                    heldRasengan.setTargetPlayer(target);
                    serverLevel.addFreshEntity(heldRasengan);
                    entityData.set(RASENGAN_ID, heldRasengan.getId());
                }
            }

            if (heldRasengan != null && heldRasengan.isAlive()) {
                heldRasengan.setStartPos(handPos);
                heldRasengan.setPos(handPos.x, handPos.y, handPos.z);
            }

            for (int i = 0; i < 3; i++) {
                double theta = Math.acos(2 * random.nextDouble() - 1);
                double phi = random.nextDouble() * 2 * Math.PI;
                double r = 0.2 + (lifeTicks / (float) CHARGE_TICKS) * 0.3;
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        handPos.x + r * Math.sin(theta) * Math.cos(phi),
                        handPos.y + r * Math.sin(theta) * Math.sin(phi),
                        handPos.z + r * Math.cos(theta),
                        1, 0, 0, 0, 0.02);
            }

            if (lifeTicks == 10) {
                level().playSound(null, getX(), getY(), getZ(),
                        SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 2.0f, 0.8f);
            }
        }
        else if (!hasThrown) {
            hasThrown = true;
            entityData.set(ANIM_STATE, 2);

            if (heldRasengan != null && heldRasengan.isAlive()) {
                heldRasengan.setTargetPlayer(target);
                Vec3 throwDir = targetPos.subtract(getHandPosition()).normalize();
                heldRasengan.setDeltaMovement(throwDir.scale(0.5));
            }

            level().playSound(null, getX(), getY(), getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 2.0f, 1.0f);
        }
        else {
            entityData.set(ANIM_STATE, 2);

            if (heldRasengan != null && heldRasengan.isAlive()) {
                Vec3 rasenganPos = heldRasengan.position();

                float yawRad = (float) Math.toRadians(getYRot());
                Vec3 forward = new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad));
                Vec3 rightDir = new Vec3(-Math.cos(yawRad), 0, -Math.sin(yawRad));
                Vec3 handOffset = new Vec3(0, 2.95, 0).add(forward.scale(1.045)).add(rightDir.scale(0.5625));
                Vec3 targetStickPos = rasenganPos.subtract(handOffset);

                setPos(targetStickPos);
            }

            if (heldRasengan == null || !heldRasengan.isAlive()) {
                discard();
            }
        }
    }

    private Vec3 getHandPosition() {
        float yawRad = (float) Math.toRadians(getYRot());
        Vec3 forward = new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad));
        Vec3 right = new Vec3(-Math.cos(yawRad), 0, -Math.sin(yawRad));
        return position().add(0, 2.95, 0).add(forward.scale(1.145)).add(right.scale(0.5625));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        lifeTicks = tag.getInt("LifeTicks");
        hasThrown = tag.getBoolean("HasThrown");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("LifeTicks", lifeTicks);
        tag.putBoolean("HasThrown", hasThrown);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
