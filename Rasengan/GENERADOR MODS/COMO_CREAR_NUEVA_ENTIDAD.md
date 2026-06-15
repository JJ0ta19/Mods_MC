# Cómo crear una nueva entidad (basada en Stickman)

Guía paso a paso para crear un personaje similar al Stickman (Naruto) pero con
skins diferentes, comportamientos modificados, etc. Forge 1.20.1, Mojang mappings.

---

## Archivos que necesitas crear (6 archivos)

| Archivo | Propósito | Basado en |
|---------|-----------|-----------|
| `src/main/java/.../MiPersonajeEntity.java` | Lógica de la entidad | `StickmanEntity.java` |
| `src/main/java/.../client/MiPersonajeModel.java` | Modelo 3D | `StickmanModel.java` |
| `src/main/java/.../client/MiPersonajeRenderer.java` | Render + textura | `StickmanRenderer.java` |
| `src/main/resources/assets/<modid>/textures/entity/mi_personaje.png` | Skin PNG | `naruto.png` |
| Modificar `ModEntities.java` | Registrar la entidad | `ModEntities.java` |
| Modificar clase principal (`@Mod`) | Registrar render + commands | `RasenganMod.java` |

---

## Paso 1: Crea el modelo (MiPersonajeModel.java)

Copia el modelo del Stickman y cámbiale el LAYER_LOCATION:

```java
package com.rasengan.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.rasengan.MiPersonajeEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public class MiPersonajeModel extends EntityModel<MiPersonajeEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                new ResourceLocation("rasengan", "mi_personaje"), "main");

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public MiPersonajeModel(ModelPart root) {
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(16, 16)
                        .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(40, 16)
                        .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-5.0F, 2.0F, 0.0F));

        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(40, 16)
                        .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(5.0F, 2.0F, 0.0F));

        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-2.0F, 12.0F, 0.0F));

        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(2.0F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(MiPersonajeEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // Aquí controlas las animaciones según getAnimState()
        // (es el mismo sistema que Stickman)
        head.xRot = 0;
        body.xRot = 0;
        rightArm.xRot = 0;
        leftArm.xRot = 0;
        rightLeg.xRot = 0;
        leftLeg.xRot = 0;

        int animState = entity.getAnimState();

        if (animState == 1) {
            float armRamp = Math.min(ageInTicks / 20.0F, 1.0F);
            rightArm.xRot = -1.8F * armRamp;
        } else if (animState == 2) {
            rightArm.xRot = -1.8F;
            body.xRot = 0.2F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float r, float g, float b, float a) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        rightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        leftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        rightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        leftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
    }
}
```

---

## Paso 2: Crea la textura (Skin PNG)

- Formato: **PNG 64x64 píxeles**
- El layout de UV sigue el formato vanilla de player:
  - Head: (0, 0) a (8, 8) → 8x8
  - Body: (16, 16) a (24, 28) → 8x12
  - Arms: (40, 16) a (44, 28) → 4x12
  - Legs: (0, 16) a (4, 28) → 4x12

![UV Layout](https://static.wikia.nocookie.net/minecraft_gamepedia/images/6/6e/Player_Skin_Template_64x64.png)

Guarda en: `assets/<modid>/textures/entity/mi_personaje.png`

---

## Paso 3: Crea el Renderer (MiPersonajeRenderer.java)

```java
package com.rasengan.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.rasengan.MiPersonajeEntity;
import com.rasengan.RasenganMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class MiPersonajeRenderer extends EntityRenderer<MiPersonajeEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RasenganMod.MODID, "textures/entity/mi_personaje.png");
    private final MiPersonajeModel model;

    public MiPersonajeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new MiPersonajeModel(
                context.bakeLayer(MiPersonajeModel.LAYER_LOCATION));
    }

    @Override
    public ResourceLocation getTextureLocation(MiPersonajeEntity entity) {
        return TEXTURE;
    }

    @Override
    protected int getBlockLightLevel(MiPersonajeEntity entity, BlockPos pos) {
        return 15; // Siempre al maximo de luz (glow)
    }

    @Override
    public void render(MiPersonajeEntity entity, float yaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Player player = Minecraft.getInstance().player;
        float rotY = 0;
        if (player != null) {
            Vec3 toPlayer = player.getEyePosition().subtract(entity.position());
            rotY = (float) Math.toDegrees(Math.atan2(-toPlayer.x, toPlayer.z));
        }

        poseStack.pushPose();
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.scale(1.8F, 1.8F, 1.8F);
        poseStack.translate(0, -1.5F, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F + rotY));

        model.setupAnim(entity, 0, 0, entity.tickCount + partialTick, 0, 0);
        model.renderToBuffer(poseStack,
                buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);

        poseStack.popPose();
    }
}
```

**Para cambiar la skin dinámicamente** (ej: múltiples skins):

```java
private static final ResourceLocation[] SKINS = {
    new ResourceLocation(RasenganMod.MODID, "textures/entity/skin1.png"),
    new ResourceLocation(RasenganMod.MODID, "textures/entity/skin2.png"),
};

@Override
public ResourceLocation getTextureLocation(MiPersonajeEntity entity) {
    return SKINS[entity.getSkinIndex() % SKINS.length];
}
```

Y en la entidad agregas un `EntityDataAccessor<Integer> SKIN_INDEX`.

---

## Paso 4: Crea la entidad (MiPersonajeEntity.java)

Copia `StickmanEntity.java` y haz estos cambios:

1. **Cambia el nombre de la clase**
2. **Cambia los `EntityDataAccessor`** (son estáticos, necesitan tu nueva clase)
3. **Personaliza el comportamiento** (tick counts, velocidades, etc.)

```java
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

public class MiPersonajeEntity extends Entity {
    private static final EntityDataAccessor<Integer> ANIM_STATE =
            SynchedEntityData.defineId(MiPersonajeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RASENGAN_ID =
            SynchedEntityData.defineId(MiPersonajeEntity.class, EntityDataSerializers.INT);

    private static final int CHARGE_TICKS = 100;

    @Nullable private Player target;
    private int lifeTicks = 0;
    private boolean hasThrown = false;
    @Nullable private RasenganEntity heldRasengan;
    @Nullable private Player caster;

    public MiPersonajeEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    public void setTarget(Player player) { this.target = player; }
    public void setCaster(Player player) { this.caster = player; }

    @Override
    protected void defineSynchedData() {
        entityData.define(ANIM_STATE, 0);
        entityData.define(RASENGAN_ID, 0);
    }

    public int getAnimState() { return entityData.get(ANIM_STATE); }

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
                setPos(heldRasengan.position().subtract(getHandOffset()));
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
        return position().add(0, 2.95, 0)
                .add(forward.scale(1.145)).add(right.scale(0.5625));
    }

    private Vec3 getHandOffset() {
        float yawRad = (float) Math.toRadians(getYRot());
        Vec3 forward = new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad));
        Vec3 rightDir = new Vec3(-Math.cos(yawRad), 0, -Math.sin(yawRad));
        return new Vec3(0, 2.95, 0)
                .add(forward.scale(1.045)).add(rightDir.scale(0.5625));
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
```

---

## Paso 5: Registra la entidad en ModEntities.java

```java
public static final RegistryObject<EntityType<MiPersonajeEntity>> MI_PERSONAJE =
        ENTITIES.register("mi_personaje",
                () -> EntityType.Builder.<MiPersonajeEntity>of(MiPersonajeEntity::new, MobCategory.MISC)
                        .sized(0.6f, 0.8f)
                        .build(new ResourceLocation(RasenganMod.MODID, "mi_personaje").toString())
        );
```

---

## Paso 6: Registra el renderer en la clase principal

Dentro de `ClientModEvents`:

```java
@SubscribeEvent
public static void onRegisterRenderers(
        EntityRenderersEvent.RegisterRenderers event) {
    event.registerEntityRenderer(ModEntities.MI_PERSONAJE.get(),
            MiPersonajeRenderer::new);
}

@SubscribeEvent
public static void onRegisterLayerDefinitions(
        EntityRenderersEvent.RegisterLayerDefinitions event) {
    event.registerLayerDefinition(MiPersonajeModel.LAYER_LOCATION,
            MiPersonajeModel::createBodyLayer);
}
```

---

## Paso 7: Crea un comando para invocarlo

En tu método `onRegisterCommands()`:

```java
dispatcher.register(Commands.literal("mipersonaje")
        .then(Commands.argument("target", StringArgumentType.word())
                .executes(context -> {
                    String targetName = StringArgumentType.getString(context, "target");
                    ServerPlayer target = findPlayer(context.getSource(), targetName);
                    if (target == null) {
                        context.getSource().sendFailure(
                            Component.literal("Jugador no encontrado"));
                        return 0;
                    }
                    return spawnMiPersonaje(getSender(context.getSource()), target);
                }))
        .executes(context -> {
            ServerPlayer sender = getSender(context.getSource());
            if (sender == null) return 0;
            return spawnMiPersonaje(sender, sender);
        })
);
```

Y el método `spawnMiPersonaje()`:

```java
private int spawnMiPersonaje(@Nullable ServerPlayer player, ServerPlayer target) {
    UUID targetId = target.getUUID();
    EVENT_QUEUE.queueOrExecute(targetId, () -> {
        Vec3 spawnPos;
        if (player != null) {
            float yawRad = (float) Math.toRadians(player.getYRot());
            spawnPos = player.position()
                    .add(new Vec3(-Math.sin(yawRad) * 4.0, 0, Math.cos(yawRad) * 4.0));
        } else {
            spawnPos = target.position().add(-4, 0, 0);
        }

        MiPersonajeEntity entity = ModEntities.MI_PERSONAJE.get().create(target.serverLevel());
        if (entity != null) {
            entity.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            entity.setTarget(target);
            entity.setCaster(player);
            target.serverLevel().addFreshEntity(entity);
            trackEntity(entity, targetId);
        }
    });
    return 1;
}
```

---

## Resumen de cambios (checklist)

- [ ] `MiPersonajeEntity.java` creado (en tu package)
- [ ] `client/MiPersonajeModel.java` creado
- [ ] `client/MiPersonajeRenderer.java` creado
- [ ] Textura PNG 64x64 en `textures/entity/mi_personaje.png`
- [ ] `ModEntities.java` actualizado con el registro
- [ ] Clase principal actualizada con render + layer + comando
- [ ] Compilas con `.\gradlew.bat build`
