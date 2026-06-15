package com.rosamod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class RitualEffects {

    public static Vec3 ringPosition(Vec3 center, double y, double radius, int count, int index, double angleOffset) {
        double ang = Math.PI * 2 * index / count + angleOffset;
        double x = center.x + Math.cos(ang) * radius;
        double z = center.z + Math.sin(ang) * radius;
        return new Vec3(x, y, z);
    }

    public static void clearBlocksAroundPlayer(ServerLevel level, Vec3 center, int radiusXZ, int down, int up) {
        int cx = (int) Math.floor(center.x);
        int cy = (int) Math.floor(center.y);
        int cz = (int) Math.floor(center.z);
        for (int x = cx - radiusXZ; x <= cx + radiusXZ; x++) {
            for (int z = cz - radiusXZ; z <= cz + radiusXZ; z++) {
                for (int yy = cy - down; yy <= cy + up; yy++) {
                    if (yy < level.getMinBuildHeight() || yy > level.getMaxBuildHeight() - 1) continue;
                    BlockPos pos = new BlockPos(x, yy, z);
                    if (level.getBlockState(pos).is(Blocks.BEDROCK)) continue;
                    if (!level.isEmptyBlock(pos)) {
                        level.removeBlock(pos, false);
                    }
                }
            }
        }
    }

    public static void clearCreeperSpawnSpace(ServerLevel level, Vec3 center, int radiusXZ, int down, int up) {
        clearBlocksAroundPlayer(level, center, radiusXZ, down, up);
    }

    public static void drawChineseFireRunes(ServerLevel level, Vec3 center, int tick, boolean finalPhase) {
        double outerRadius = 5.0;
        int outerRuneCount = 8;
        int density = finalPhase ? 4 : 2;

        for (int i = 0; i < outerRuneCount; i++) {
            double angle = Math.PI * 2 * i / outerRuneCount;
            double x = center.x + Math.cos(angle) * outerRadius;
            double z = center.z + Math.sin(angle) * outerRadius;
            double y = center.y + 0.02;

            for (int d = 0; d < density; d++) {
                drawBlackDust(level, x, y, z, 4);
            }
        }

        double innerRadius = 3.2;
        int innerRuneCount = 6;

        for (int i = 0; i < innerRuneCount; i++) {
            double angle = Math.PI * 2 * i / innerRuneCount + Math.PI / 6.0;
            double x = center.x + Math.cos(angle) * innerRadius;
            double z = center.z + Math.sin(angle) * innerRadius;
            double y = center.y + 0.02;

            drawBlackDust(level, x, y, z, 3);
        }

        drawBlackDust(level, center.x, center.y + 0.02, center.z, 8);
        drawBlackDust(level, center.x + 0.5, center.y + 0.02, center.z, 6);
        drawBlackDust(level, center.x - 0.5, center.y + 0.02, center.z, 6);
    }

    public static void drawFireRitualRing(ServerLevel level, Vec3 center, double radius, int tick, boolean finalPhase) {
        int outerPoints = finalPhase ? 28 : 20;
        int density = finalPhase ? 4 : 2;

        for (int i = 0; i < outerPoints; i++) {
            double angle = Math.PI * 2 * i / outerPoints;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;

            for (int d = 0; d < density; d++) {
                drawBlackDust(level, x, center.y, z, 3);
            }
        }
    }

    public static void drawCreeperRing(ServerLevel level, Vec3 center, double radius) {
        int points = 24;
        for (int i = 0; i < points; i++) {
            double angle = Math.PI * 2 * i / points;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            level.sendParticles(ParticleTypes.FLAME, x, center.y, z, 2, 0.0, 0.0, 0.0, 0.01);
        }
    }

    public static void drawSpiralEffect(ServerLevel level, Vec3 center, int tick) {
        double height = 3.5;
        int spirals = 3;
        int pointsPerSpiral = 30;

        for (int s = 0; s < spirals; s++) {
            double baseAngle = (Math.PI * 2 * s / spirals) + (tick * 0.12);
            for (int i = 0; i < pointsPerSpiral; i++) {
                double t = (double) i / pointsPerSpiral;
                double y = center.y + t * height;
                double radius = 2.0 - t * 1.5;
                double angle = baseAngle + t * Math.PI * 4;

                double x = center.x + Math.cos(angle) * radius;
                double z = center.z + Math.sin(angle) * radius;

                drawBlackDust(level, x, y, z, 2);
            }
        }
    }

    public static void drawFloatingParticles(ServerLevel level, Vec3 center, int tick) {
        int count = 16;
        double radius = 3.5;
        double height = 1.5 + Math.sin(tick * 0.1) * 0.4;

        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2 * i / count) + (tick * 0.05);
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;

            drawBlackDust(level, x, center.y + height, z, 2);
            drawBlackDust(level, x, center.y + height - 0.2, z, 2);
        }
    }

    public static void drawPulseBurst(ServerLevel level, Vec3 center, double radius) {
        int points = 24;
        for (int i = 0; i < points; i++) {
            double ang = Math.PI * 2 * i / points;
            double x = center.x + Math.cos(ang) * radius;
            double z = center.z + Math.sin(ang) * radius;
            level.sendParticles(ParticleTypes.FLAME, x, center.y, z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    public static void drawChainFromGround(ServerLevel level, Vec3 from, Vec3 to, double step) {
        Vec3 dir = to.subtract(from);
        double len = dir.length();
        if (len < 0.001) return;
        Vec3 norm = dir.normalize();
        int points = (int) Math.ceil(len / step);
        DustParticleOptions goldOpts = new DustParticleOptions(new Vector3f(1.0f, 215 / 255f, 0f), 1.0f);

        for (int i = 0; i <= points; i++) {
            Vec3 p = from.add(norm.scale(i * step));
            level.sendParticles(goldOpts, p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    public static Map<BlockPos, BlockState> placeRealGoldenChain(ServerLevel level, Vec3 from, Vec3 to) {
        Map<BlockPos, BlockState> originalBlocks = new HashMap<>();
        Vec3 dir = to.subtract(from);
        double len = dir.length();
        if (len < 0.001) return originalBlocks;
        Vec3 norm = dir.normalize();
        double step = 0.5;
        int points = (int) Math.ceil(len / step);

        for (int i = 0; i <= points; i++) {
            Vec3 p = from.add(norm.scale(i * step));
            BlockPos pos = new BlockPos((int) Math.floor(p.x), (int) Math.floor(p.y), (int) Math.floor(p.z));
            if (!originalBlocks.containsKey(pos)) {
                originalBlocks.put(pos, level.getBlockState(pos));
            }
            double absX = Math.abs(norm.x);
            double absY = Math.abs(norm.y);
            double absZ = Math.abs(norm.z);
            Direction.Axis axis;
            if (absY >= absX && absY >= absZ) {
                axis = Direction.Axis.Y;
            } else if (absX >= absZ) {
                axis = Direction.Axis.X;
            } else {
                axis = Direction.Axis.Z;
            }
            level.setBlock(pos, ModBlocks.GOLDEN_CHAIN_BLOCK.get().defaultBlockState().setValue(BlockStateProperties.AXIS, axis), 3);
        }
        return originalBlocks;
    }

    public static void drawEnergyBurst(ServerLevel level, Vec3 center, int phase) {
        double intensity = Math.min(1.0, phase / 10.0);

        int rays = 20;
        for (int r = 0; r < rays; r++) {
            double angle = Math.PI * 2 * r / rays;
            double height = 8.0 * intensity;

            for (int h = 0; h <= 12; h++) {
                double t = h / 12.0;
                double y = center.y + 1.0 + height * (1.0 - t);
                double spread = t * 2.0;
                double x = center.x + Math.cos(angle) * spread;
                double z = center.z + Math.sin(angle) * spread;

                level.sendParticles(ParticleTypes.FLAME, x, y, z, 2, 0.0, 0.0, 0.0, 0.02);
            }
        }

        double columnHeight = 8.0 * intensity;
        for (int i = 0; i <= 15; i++) {
            double t = i / 15.0;
            double y = center.y + 1.0 + columnHeight * (1.0 - t);
            double radius = 0.5 + t * 0.4;

            for (int p = 0; p < 6; p++) {
                double angle = Math.PI * 2 * p / 6.0;
                double x = center.x + Math.cos(angle) * radius;
                double z = center.z + Math.sin(angle) * radius;

                level.sendParticles(ParticleTypes.FLAME, x, y, z, 1, 0.0, 0.0, 0.0, 0.02);
            }
        }

        for (int ring = 0; ring < 4; ring++) {
            double ringRadius = (phase + ring * 2) * 0.4;
            int points = 24;
            for (int i = 0; i < points; i++) {
                double angle = Math.PI * 2 * i / points;
                double x = center.x + Math.cos(angle) * ringRadius;
                double z = center.z + Math.sin(angle) * ringRadius;

                level.sendParticles(ParticleTypes.FLAME, x, center.y + 0.5, z, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }

        level.sendParticles(ParticleTypes.FLAME, center.x, center.y + 1.0, center.z, 30, 0.8, 0.8, 0.8, 0.08);
    }

    public static void drawGreenLightBurst(ServerLevel level, Vec3 center) {
        int rays = 30;
        int pointsPerRay = 16;
        double maxRadius = 12.0;

        for (int r = 0; r < rays; r++) {
            double angle = Math.PI * 2 * r / rays;
            double x = center.x + Math.cos(angle) * maxRadius;
            double z = center.z + Math.sin(angle) * maxRadius;

            Vec3 dir = new Vec3(x - center.x, 0, z - center.z).normalize();
            for (int p = 0; p < pointsPerRay; p++) {
                double t = (double) p / pointsPerRay;
                Vec3 pos = center.add(dir.scale(t * maxRadius));

                if (r % 3 == 0) {
                    drawRedDust(level, pos.x, center.y + 1.5, pos.z, 2);
                } else if (r % 3 == 1) {
                    drawBlackDust(level, pos.x, center.y + 1.5, pos.z, 2);
                } else {
                    drawGreenDust(level, pos.x, center.y + 1.5, pos.z, 2);
                }
            }
        }

        drawBlackDust(level, center.x, center.y + 1.5, center.z, 60);
        drawRedDust(level, center.x, center.y + 2.0, center.z, 50);
        drawGreenDust(level, center.x, center.y + 2.5, center.z, 40);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y + 1.5, center.z, 5, 0.0, 0.0, 0.0, 0.0);
    }

    public static void drawGoldDust(ServerLevel level, double x, double y, double z, int count) {
        DustParticleOptions opts = new DustParticleOptions(new Vector3f(1.0f, 215 / 255f, 0f), 1.5f);
        level.sendParticles(opts, x, y, z, count, 0.15, 0.15, 0.15, 0.0);
    }

    public static void drawBlackDust(ServerLevel level, double x, double y, double z, int count) {
        DustParticleOptions opts = new DustParticleOptions(new Vector3f(20 / 255f, 20 / 255f, 20 / 255f), 1.0f);
        level.sendParticles(opts, x, y, z, count, 0.0, 0.0, 0.0, 0.0);
    }

    public static void drawRedDust(ServerLevel level, double x, double y, double z, int count) {
        DustParticleOptions opts = new DustParticleOptions(new Vector3f(1.0f, 0f, 0f), 1.0f);
        level.sendParticles(opts, x, y, z, count, 0.0, 0.0, 0.0, 0.0);
    }

    public static void drawGreenDust(ServerLevel level, double x, double y, double z, int count) {
        DustParticleOptions opts = new DustParticleOptions(new Vector3f(0f, 1.0f, 0f), 1.0f);
        level.sendParticles(opts, x, y, z, count, 0.0, 0.0, 0.0, 0.0);
    }

    public static void drawBlackRunes(ServerLevel level, Vec3 center, int tick, boolean finalPhase) {
        double outerRadius = 4.5;
        int outerRuneCount = 8;
        int density = finalPhase ? 2 : 1;

        for (int i = 0; i < outerRuneCount; i++) {
            double angle = Math.PI * 2 * i / outerRuneCount;
            double x = center.x + Math.cos(angle) * outerRadius;
            double z = center.z + Math.sin(angle) * outerRadius;
            double y = center.y + 0.02;

            for (int d = 0; d < density; d++) {
                if (i % 2 == 0) {
                    drawBlackDust(level, x, y, z, 2);
                } else {
                    drawBlackDust(level, x, y, z, 1);
                }
            }
        }

        double innerRadius = 2.5;
        int innerRuneCount = 6;

        for (int i = 0; i < innerRuneCount; i++) {
            double angle = Math.PI * 2 * i / innerRuneCount + Math.PI / 6.0;
            double x = center.x + Math.cos(angle) * innerRadius;
            double z = center.z + Math.sin(angle) * innerRadius;
            double y = center.y + 0.02;

            drawBlackDust(level, x, y, z, 1);
        }

        drawBlackDust(level, center.x, center.y + 0.02, center.z, 4);
        drawBlackDust(level, center.x, center.y + 0.05, center.z, 3);
    }

    public static void drawBlackRing(ServerLevel level, Vec3 center, double radius, int tick, boolean finalPhase) {
        int outerPoints = finalPhase ? 32 : 24;
        int density = finalPhase ? 2 : 1;

        for (int i = 0; i < outerPoints; i++) {
            double angle = Math.PI * 2 * i / outerPoints;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;

            for (int d = 0; d < density; d++) {
                if (i % 2 == 0) {
                    drawBlackDust(level, x, center.y, z, 1);
                } else {
                    drawBlackDust(level, x, center.y, z, 1);
                }
            }
        }
    }

    public static void drawBlackSpiral(ServerLevel level, Vec3 center, int tick, double height, int spirals, int pointsPerSpiral) {
        for (int s = 0; s < spirals; s++) {
            double baseAngle = (Math.PI * 2 * s / spirals) + (tick * 0.1);
            for (int i = 0; i < pointsPerSpiral; i++) {
                double t = (double) i / pointsPerSpiral;
                double y = center.y + t * height;
                double radius = 1.5 - t * 1.0;
                double angle = baseAngle + t * Math.PI * 3;

                double x = center.x + Math.cos(angle) * radius;
                double z = center.z + Math.sin(angle) * radius;

                if (i % 2 == 0) {
                    drawBlackDust(level, x, y, z, 1);
                } else {
                    drawBlackDust(level, x, y, z, 1);
                }
            }
        }
    }

    public static void drawBlackFloatingParticles(ServerLevel level, Vec3 center, int tick, double radius, double heightOffset) {
        int count = 8;
        double height = heightOffset + Math.sin(tick * 0.1) * 0.3;

        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2 * i / count) + (tick * 0.04);
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;

            if (i % 2 == 0) {
                drawBlackDust(level, x, center.y + height, z, 1);
                drawBlackDust(level, x, center.y + height - 0.2, z, 1);
            } else {
                drawBlackDust(level, x, center.y + height, z, 1);
            }
        }
    }

    public static void drawBlackPulseBurst(ServerLevel level, Vec3 center, double radius) {
        int points = 24;
        for (int i = 0; i < points; i++) {
            double ang = Math.PI * 2 * i / points;
            double x = center.x + Math.cos(ang) * radius;
            double z = center.z + Math.sin(ang) * radius;
            drawBlackDust(level, x, center.y, z, 1);
            drawBlackDust(level, x, center.y, z, 1);
        }
    }

    public static void drawBlackCore(ServerLevel level, Vec3 center, int tick, boolean finalPhase) {
        double innerRadius = 2.0;
        double midRadius = 3.5;
        double outerRadius = 5.0;

        int innerPoints = finalPhase ? 30 : 24;
        for (int i = 0; i < innerPoints; i++) {
            double angle = Math.PI * 2 * i / innerPoints + (tick * 0.08);
            double x = center.x + Math.cos(angle) * innerRadius;
            double z = center.z + Math.sin(angle) * innerRadius;
            double y = center.y + 0.5 + Math.sin(tick * 0.15 + i) * 0.3;

            drawBlackDust(level, x, y, z, 4);
            drawBlackDust(level, x, y, z, 2);
        }

        int midPoints = finalPhase ? 40 : 32;
        for (int i = 0; i < midPoints; i++) {
            double angle = Math.PI * 2 * i / midPoints - (tick * 0.06);
            double x = center.x + Math.cos(angle) * midRadius;
            double z = center.z + Math.sin(angle) * midRadius;
            double y = center.y + 1.0 + Math.sin(tick * 0.12 + i * 0.5) * 0.4;

            drawBlackDust(level, x, y, z, 3);
            drawBlackDust(level, x, y, z, 2);
        }

        int outerPoints = finalPhase ? 50 : 40;
        for (int i = 0; i < outerPoints; i++) {
            double angle = Math.PI * 2 * i / outerPoints + (tick * 0.04);
            double x = center.x + Math.cos(angle) * outerRadius;
            double z = center.z + Math.sin(angle) * outerRadius;
            double y = center.y + 1.5;

            drawBlackDust(level, x, y, z, 2);
        }

        drawBlackDust(level, center.x, center.y + 0.8, center.z, 8);
        drawBlackDust(level, center.x, center.y + 1.2, center.z, 5);
        drawBlackDust(level, center.x, center.y + 1.0, center.z, 6);
    }

    public static void drawBlackChains(ServerLevel level, Vec3 from, Vec3 to, double step) {
        Vec3 dir = to.subtract(from);
        double len = dir.length();
        if (len < 0.001) return;
        Vec3 norm = dir.normalize();
        int points = (int) Math.ceil(len / step);

        for (int i = 0; i <= points; i++) {
            Vec3 p = from.add(norm.scale(i * step));
            if (i % 2 == 0) {
                drawBlackDust(level, p.x, p.y, p.z, 1);
            } else {
                drawBlackDust(level, p.x, p.y, p.z, 1);
            }
        }
    }

    public static void drawCreeperRingColored(ServerLevel level, Vec3 center, double radius, int tick) {
        int points = 24;
        for (int i = 0; i < points; i++) {
            double angle = Math.PI * 2 * i / points;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;

            if (i % 3 == 0) {
                drawRedDust(level, x, center.y, z, 2);
            } else if (i % 3 == 1) {
                drawBlackDust(level, x, center.y, z, 2);
            } else {
                drawGreenDust(level, x, center.y, z, 2);
            }
        }
    }

    public static void drawEnergyBurstMajor(ServerLevel level, Vec3 center, int phase) {
        double intensity = Math.min(1.0, phase / 10.0);

        int rays = 30;
        for (int r = 0; r < rays; r++) {
            double angle = Math.PI * 2 * r / rays;
            double height = 12.0 * intensity;

            for (int h = 0; h <= 16; h++) {
                double t = h / 16.0;
                double y = center.y + 1.5 + height * (1.0 - t);
                double spread = t * 3.0;
                double x = center.x + Math.cos(angle) * spread;
                double z = center.z + Math.sin(angle) * spread;

                if (r % 3 == 0) {
                    drawRedDust(level, x, y, z, 2);
                } else if (r % 3 == 1) {
                    drawBlackDust(level, x, y, z, 2);
                } else {
                    drawGreenDust(level, x, y, z, 2);
                }
            }
        }

        double columnHeight = 12.0 * intensity;
        for (int i = 0; i <= 20; i++) {
            double t = i / 20.0;
            double y = center.y + 1.5 + columnHeight * (1.0 - t);
            double radius = 0.8 + t * 0.6;

            for (int p = 0; p < 8; p++) {
                double angle = Math.PI * 2 * p / 8.0;
                double x = center.x + Math.cos(angle) * radius;
                double z = center.z + Math.sin(angle) * radius;

                if (p % 2 == 0) {
                    drawBlackDust(level, x, y, z, 2);
                } else {
                    drawRedDust(level, x, y, z, 2);
                }
            }
        }

        for (int ring = 0; ring < 6; ring++) {
            double ringRadius = (phase + ring * 2.5) * 0.5;
            int points = 30;
            for (int i = 0; i < points; i++) {
                double angle = Math.PI * 2 * i / points;
                double x = center.x + Math.cos(angle) * ringRadius;
                double z = center.z + Math.sin(angle) * ringRadius;

                if (i % 3 == 0) {
                    drawRedDust(level, x, center.y + 0.5, z, 2);
                } else if (i % 3 == 1) {
                    drawBlackDust(level, x, center.y + 0.5, z, 2);
                } else {
                    drawGreenDust(level, x, center.y + 0.5, z, 2);
                }
            }
        }

        drawBlackDust(level, center.x, center.y + 1.5, center.z, 40);
        drawRedDust(level, center.x, center.y + 1.8, center.z, 30);
        drawGreenDust(level, center.x, center.y + 2.1, center.z, 25);
    }
}
