package com.rasengan;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod(RasenganMod.MODID)
public class RasenganMod {
    public static final String MODID = "rasengan";
    public static final EventQueueManager EVENT_QUEUE = new EventQueueManager();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static java.lang.reflect.Field INVULN_FIELD;
    private static final Map<UUID, Integer> GRAPPLE_TIMERS = new HashMap<>();
    private static final Set<UUID> LOCKED_PLAYERS = new HashSet<>();

    // entityId -> targetUUID (for queue management)
    private final Map<Integer, UUID> entityToTarget = new HashMap<>();

    public static void onGrappleStart(ServerPlayer target) {
        GRAPPLE_TIMERS.put(target.getUUID(), 300);
        LOCKED_PLAYERS.remove(target.getUUID());

        target.connection.send(new ClientboundSetTitleTextPacket(
                Component.literal("RASENGAN").withStyle(
                        Style.EMPTY.withColor(TextColor.parseColor("#00FCFF"))
                                .withBold(true)
                )
        ));
        target.connection.send(new ClientboundSetSubtitleTextPacket(
                Component.literal("§7¡Golpe directo!").withStyle(
                        Style.EMPTY.withColor(TextColor.parseColor("#FFFFFF"))
                )
        ));
    }

    public RasenganMod() {
        try {
            INVULN_FIELD = LivingEntity.class.getDeclaredField("invulnerableTime");
            INVULN_FIELD.setAccessible(true);
        } catch (Exception e) {
            try {
                INVULN_FIELD = LivingEntity.class.getDeclaredField("noDamageTicks");
                INVULN_FIELD.setAccessible(true);
            } catch (Exception e2) {
                try {
                    INVULN_FIELD = LivingEntity.class.getDeclaredField("field_147247_i");
                    INVULN_FIELD.setAccessible(true);
                } catch (Exception ignored) {}
            }
        }
        if (INVULN_FIELD != null) {
            INVULN_FIELD.setAccessible(true);
        }

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModEntities.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onEntityLeave);
        MinecraftForge.EVENT_BUS.addListener(this::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(this::onLivingDamage);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStopping);
    }

    private void onServerStopping(ServerStoppingEvent event) {
        EVENT_QUEUE.clearAll();
        entityToTarget.clear();
        GRAPPLE_TIMERS.clear();
        LOCKED_PLAYERS.clear();
    }

    private void onEntityLeave(EntityLeaveLevelEvent event) {
        UUID targetId = entityToTarget.remove(event.getEntity().getId());
        if (targetId != null) {
            LOCKED_PLAYERS.remove(targetId);
            EVENT_QUEUE.onEventCompleted(targetId);
        }
    }

    private void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        for (UUID uuid : new ArrayList<>(LOCKED_PLAYERS)) {
            ServerPlayer target = (ServerPlayer) server.getPlayerList().getPlayer(uuid);
            if (target == null || !target.isAlive()) {
                LOCKED_PLAYERS.remove(uuid);
                continue;
            }
            target.connection.teleport(target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
        }

        for (UUID uuid : new ArrayList<>(GRAPPLE_TIMERS.keySet())) {
            int ticks = GRAPPLE_TIMERS.get(uuid);
            if (--ticks <= 0) { GRAPPLE_TIMERS.remove(uuid); continue; }
            GRAPPLE_TIMERS.put(uuid, ticks);

            ServerPlayer target = (ServerPlayer) server.getPlayerList().getPlayer(uuid);
            if (target == null || !target.isAlive() || target.isCreative()) {
                GRAPPLE_TIMERS.remove(uuid);
                continue;
            }

            target.hurt(server.overworld().damageSources().outOfBorder(), 12.0f);
        }
    }

    private void onLivingDamage(LivingDamageEvent event) {
        if (INVULN_FIELD != null && event.getSource().is(DamageTypeTags.BYPASSES_COOLDOWN)) {
            try { INVULN_FIELD.setInt(event.getEntity(), 0); } catch (Exception ignored) {}
        }
    }

    private void trackEntity(Entity entity, UUID targetId) {
        entityToTarget.put(entity.getId(), targetId);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Rasengan mod cargado!");
    }

    private ServerPlayer findPlayer(CommandSourceStack source, String name) {
        for (ServerPlayer p : source.getServer().getPlayerList().getPlayers()) {
            if (p.getName().getString().equalsIgnoreCase(name)) return p;
        }
        return null;
    }

    private @Nullable ServerPlayer getSender(CommandSourceStack source) {
        Entity e = source.getEntity();
        return e instanceof ServerPlayer ? (ServerPlayer) e : null;
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("rasengan")
                .then(Commands.argument("target", StringArgumentType.word())
                        .executes(context -> {
                            String targetName = StringArgumentType.getString(context, "target");
                            ServerPlayer target = findPlayer(context.getSource(), targetName);
                            if (target == null) {
                                context.getSource().sendFailure(
                                        net.minecraft.network.chat.Component.literal("Jugador no encontrado: " + targetName));
                                return 0;
                            }
                            return spawnRasengan(getSender(context.getSource()), target);
                        }))
                .executes(context -> {
                    ServerPlayer sender = getSender(context.getSource());
                    if (sender == null) {
                        context.getSource().sendFailure(net.minecraft.network.chat.Component.literal("Uso: /rasengan <jugador>"));
                        return 0;
                    }
                    return spawnRasengan(sender, null);
                })
        );
        LOGGER.info("Comando /rasengan registrado");

        dispatcher.register(Commands.literal("stickman")
                .then(Commands.argument("target", StringArgumentType.word())
                        .executes(context -> {
                            String targetName = StringArgumentType.getString(context, "target");
                            ServerPlayer target = findPlayer(context.getSource(), targetName);
                            if (target == null) {
                                context.getSource().sendFailure(
                                        net.minecraft.network.chat.Component.literal("Jugador no encontrado: " + targetName));
                                return 0;
                            }
                            return spawnStickman(getSender(context.getSource()), target);
                        }))
                .executes(context -> {
                    ServerPlayer sender = getSender(context.getSource());
                    if (sender == null) {
                        context.getSource().sendFailure(net.minecraft.network.chat.Component.literal("Uso: /stickman <jugador>"));
                        return 0;
                    }
                    return spawnStickman(sender, sender);
                })
        );
        LOGGER.info("Comando /stickman registrado");

        dispatcher.register(Commands.literal("stickmancancel")
                .executes(context -> {
                    EVENT_QUEUE.clearAll();
                    entityToTarget.clear();
                    GRAPPLE_TIMERS.clear();
                    LOCKED_PLAYERS.clear();
                    context.getSource().sendSuccess(
                            () -> net.minecraft.network.chat.Component.literal("§cEventos cancelados."), false);
                    return 1;
                })
        );
        LOGGER.info("Comando /stickmancancel registrado");
    }

    private int spawnStickman(@Nullable ServerPlayer player, ServerPlayer target) {
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

            StickmanEntity entity = ModEntities.STICKMAN.get().create(target.serverLevel());
            if (entity != null) {
                entity.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
                entity.setTarget(target);
                entity.setCaster(player);

                if (player != null) {
                    Vec3 faceDir = player.getEyePosition().subtract(spawnPos);
                    entity.setYRot((float) Math.toDegrees(Math.atan2(-faceDir.x, faceDir.z)));
                } else {
                    entity.setYRot(0);
                }

                target.serverLevel().addFreshEntity(entity);
                trackEntity(entity, targetId);
                LOCKED_PLAYERS.add(targetId);

                // clear blocks around spawn so Naruto is fully visible
                int radius = 16;
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dy = -radius; dy <= radius; dy++) {
                        for (int dz = -radius; dz <= radius; dz++) {
                            if (dx * dx + dy * dy + dz * dz > radius * radius) continue;
                            BlockPos bp = BlockPos.containing(spawnPos.x + dx, spawnPos.y + dy, spawnPos.z + dz);
                            if (player != null && bp.distSqr(player.blockPosition()) < 9.0) continue;
                            BlockState bs = target.serverLevel().getBlockState(bp);
                            if (!bs.isAir() && !bs.is(Blocks.BEDROCK)) {
                                target.serverLevel().setBlock(bp, Blocks.AIR.defaultBlockState(), 3);
                            }
                        }
                    }
                }

                LOGGER.info("Stickman spawned at {} targeting {}", spawnPos, target.getName().getString());
            }
        });
        return 1;
    }

    private int spawnRasengan(@Nullable ServerPlayer player, ServerPlayer target) {
        Player eventTarget = (target != null) ? target : (player != null ? player : target);
        UUID targetId = eventTarget.getUUID();

        EVENT_QUEUE.queueOrExecute(targetId, () -> {
            Vec3 startPos;
            if (player != null) {
                Vec3 look = player.getLookAngle();
                startPos = player.position().add(0, player.getEyeHeight() * 0.8, 0)
                        .add(look.scale(3.5));
            } else {
                startPos = target.position().add(0, 1, 0);
            }

            RasenganEntity entity = ModEntities.RASENGAN_PROJECTILE.get().create(target.serverLevel());
            if (entity != null) {
                entity.setPos(startPos.x, startPos.y, startPos.z);
                entity.setStartPos(startPos);
                entity.setOwner(player);
                entity.setTargetPlayer(target);
                target.serverLevel().addFreshEntity(entity);
                trackEntity(entity, targetId);
            }
        });
        return 1;
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onRegisterRenderers(
                net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.RASENGAN_PROJECTILE.get(),
                    com.rasengan.client.RasenganRenderer::new);
            event.registerEntityRenderer(ModEntities.STICKMAN.get(),
                    com.rasengan.client.StickmanRenderer::new);
        }

        @SubscribeEvent
        public static void onRegisterLayerDefinitions(
                net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(com.rasengan.client.StickmanModel.LAYER_LOCATION,
                    com.rasengan.client.StickmanModel::createBodyLayer);
        }
    }
}
