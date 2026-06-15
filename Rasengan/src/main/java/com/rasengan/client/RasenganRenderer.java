package com.rasengan.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.rasengan.RasenganEntity;
import com.rasengan.RasenganMod;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class RasenganRenderer extends EntityRenderer<RasenganEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RasenganMod.MODID, "textures/entity/rasengan_sphere.png");
    private static final int LON = 32;
    private static final int LAT = 16;
    private static final int FULLBRIGHT = 15728880;

    public RasenganRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(RasenganEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(RasenganEntity entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        float t = entity.tickCount + partialTick;
        float scale = entity.getRenderScale();
        float pulse = 1.0f + 0.04f * (float) Math.sin(t * 0.12f);

        poseStack.pushPose();
        poseStack.translate(0, 0.5, 0);
        poseStack.scale(scale * pulse, scale * pulse, scale * pulse);

        float time = t * 0.04f;
        float R = 0.5f;
        PoseStack.Pose pose = poseStack.last();

        // ============ LAYER 1: Outer corona glow ============
        VertexConsumer glow = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        drawSphere(glow, pose, R * 1.7f, time, 60, 150, 255, 28, false);

        // ============ LAYER 2: Mid aura ============
        drawSphere(glow, pose, R * 1.3f, time, 100, 200, 255, 55, false);

        // ============ LAYER 3: Energy rings ============
        drawRing(glow, pose, R * 1.35f, 0.04f, time * 0.08f, 0, 0, 180, 255, 255, 160);
        drawRing(glow, pose, R * 1.5f, 0.03f, time * -0.06f + 1.5f, 0.4f, 0.25f, 130, 220, 255, 110);
        drawRing(glow, pose, R * 1.6f, 0.02f, time * 0.04f + 0.8f, 0.7f, -0.15f, 80, 200, 255, 70);
        drawRing(glow, pose, R * 1.2f, 0.02f, time * -0.09f + 2.0f, 0.15f, 0.5f, 200, 240, 255, 130);

        // ============ LAYER 4: Main sphere ============
        VertexConsumer main = buffer.getBuffer(RenderType.entityCutout(TEXTURE));
        drawSphere(main, pose, R, time, 255, 255, 255, 255, true);

        // ============ LAYER 5: Inner bright core ============
        drawSphere(glow, pose, R * 0.45f, time, 255, 255, 255, 200, false);

        // ============ LAYER 6: Orbiting energy dots ============
        drawOrbiters(glow, pose, R, time, t);

        // ============ LAYER 7: Spark dots ============
        drawSparks(glow, pose, R, t);

        poseStack.popPose();
    }

    private void drawSphere(VertexConsumer vc, PoseStack.Pose pose, float radius, float time,
                            int r, int g, int b, int a, boolean useNormals) {
        for (int lat = 0; lat < LAT; lat++) {
            float t1 = (float) (lat * Math.PI / LAT);
            float t2 = (float) ((lat + 1) * Math.PI / LAT);
            float sinT1 = (float) Math.sin(t1);
            float cosT1 = (float) Math.cos(t1);
            float sinT2 = (float) Math.sin(t2);
            float cosT2 = (float) Math.cos(t2);
            float v1 = (float) lat / LAT;
            float v2 = (float) (lat + 1) / LAT;

            for (int lon = 0; lon < LON; lon++) {
                float p1 = (float) (lon * 2 * Math.PI / LON) + time;
                float p2 = (float) ((lon + 1) * 2 * Math.PI / LON) + time;
                float sinP1 = (float) Math.sin(p1);
                float cosP1 = (float) Math.cos(p1);
                float sinP2 = (float) Math.sin(p2);
                float cosP2 = (float) Math.cos(p2);

                float x1 = radius * sinT1 * cosP1, y1 = radius * cosT1, z1 = radius * sinT1 * sinP1;
                float x2 = radius * sinT2 * cosP1, y2 = radius * cosT2, z2 = radius * sinT2 * sinP1;
                float x3 = radius * sinT2 * cosP2, y3 = radius * cosT2, z3 = radius * sinT2 * sinP2;
                float x4 = radius * sinT1 * cosP2, y4 = radius * cosT1, z4 = radius * sinT1 * sinP2;

                float u1 = (float) lon / LON;
                float u2 = (float) (lon + 1) / LON;

                if (useNormals) {
                    float nx1 = x1 / radius, ny1 = y1 / radius, nz1 = z1 / radius;
                    float nx4 = x4 / radius, ny4 = y4 / radius, nz4 = z4 / radius;
                    float nx3 = x3 / radius, ny3 = y3 / radius, nz3 = z3 / radius;
                    float nx2 = x2 / radius, ny2 = y2 / radius, nz2 = z2 / radius;
                    vc.vertex(pose.pose(), x1, y1, z1).color(r, g, b, a).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULLBRIGHT).normal(pose.normal(), nx1, ny1, nz1).endVertex();
                    vc.vertex(pose.pose(), x4, y4, z4).color(r, g, b, a).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULLBRIGHT).normal(pose.normal(), nx4, ny4, nz4).endVertex();
                    vc.vertex(pose.pose(), x3, y3, z3).color(r, g, b, a).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULLBRIGHT).normal(pose.normal(), nx3, ny3, nz3).endVertex();
                    vc.vertex(pose.pose(), x2, y2, z2).color(r, g, b, a).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULLBRIGHT).normal(pose.normal(), nx2, ny2, nz2).endVertex();
                } else {
                    vc.vertex(pose.pose(), x1, y1, z1).color(r, g, b, a).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULLBRIGHT).normal(0, 1, 0).endVertex();
                    vc.vertex(pose.pose(), x4, y4, z4).color(r, g, b, a).uv(u2, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULLBRIGHT).normal(0, 1, 0).endVertex();
                    vc.vertex(pose.pose(), x3, y3, z3).color(r, g, b, a).uv(u2, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULLBRIGHT).normal(0, 1, 0).endVertex();
                    vc.vertex(pose.pose(), x2, y2, z2).color(r, g, b, a).uv(u1, v2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULLBRIGHT).normal(0, 1, 0).endVertex();
                }
            }
        }
    }

    private void drawRing(VertexConsumer vc, PoseStack.Pose pose, float ringRadius, float thickness,
                          float ringTime, float tiltX, float tiltZ, int r, int g, int b, int a) {
        int segs = 28;
        float cosTX = (float) Math.cos(tiltX), sinTX = (float) Math.sin(tiltX);
        float cosTZ = (float) Math.cos(tiltZ), sinTZ = (float) Math.sin(tiltZ);
        float uScroll = ringTime * 0.3f;

        for (int i = 0; i < segs; i++) {
            float a1 = (i / (float) segs) * 2.0f * (float) Math.PI + ringTime;
            float a2 = ((i + 1) / (float) segs) * 2.0f * (float) Math.PI + ringTime;

            float cosA1 = (float) Math.cos(a1), sinA1 = (float) Math.sin(a1);
            float cosA2 = (float) Math.cos(a2), sinA2 = (float) Math.sin(a2);

            float inner1x = (ringRadius - thickness) * cosA1;
            float inner1z = (ringRadius - thickness) * sinA1;
            float inner2x = (ringRadius - thickness) * cosA2;
            float inner2z = (ringRadius - thickness) * sinA2;
            float outer1x = (ringRadius + thickness) * cosA1;
            float outer1z = (ringRadius + thickness) * sinA1;
            float outer2x = (ringRadius + thickness) * cosA2;
            float outer2z = (ringRadius + thickness) * sinA2;

            float iy1 = inner1z * sinTX + inner1x * sinTZ;
            float iy2 = inner2z * sinTX + inner2x * sinTZ;
            float oy1 = outer1z * sinTX + outer1x * sinTZ;
            float oy2 = outer2z * sinTX + outer2x * sinTZ;
            float ix1 = inner1x * cosTZ;
            float ix2 = inner2x * cosTZ;
            float ox1 = outer1x * cosTZ;
            float ox2 = outer2x * cosTZ;
            float iz1 = inner1z * cosTX;
            float iz2 = inner2z * cosTX;
            float oz1 = outer1z * cosTX;
            float oz2 = outer2z * cosTX;

            float u1 = (float) i / segs + uScroll;
            float u2 = (float) (i + 1) / segs + uScroll;

            float alpha = a * (0.7f + 0.3f * (float) Math.sin(i * 0.9f + ringTime * 2.0f));
            int aMod = (int) Math.max(0, Math.min(255, alpha));

            vc.vertex(pose.pose(), ix1, iy1, iz1).color(r, g, b, aMod).uv(u1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULLBRIGHT).normal(0, 1, 0).endVertex();
            vc.vertex(pose.pose(), ix2, iy2, iz2).color(r, g, b, aMod).uv(u2, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULLBRIGHT).normal(0, 1, 0).endVertex();
            vc.vertex(pose.pose(), ox2, oy2, oz2).color(r, g, b, aMod).uv(u2, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULLBRIGHT).normal(0, 1, 0).endVertex();
            vc.vertex(pose.pose(), ox1, oy1, oz1).color(r, g, b, aMod).uv(u1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULLBRIGHT).normal(0, 1, 0).endVertex();
        }
    }

    private void drawOrbiters(VertexConsumer vc, PoseStack.Pose pose, float radius, float time, float t) {
        int count = 18;
        for (int i = 0; i < count; i++) {
            float offset = (i / (float) count) * 2.0f * (float) Math.PI;
            float speed = 0.06f + 0.02f * (i % 3);
            float orbitTiltX = 0.3f + 0.15f * (i % 4);
            float orbitTiltZ = 0.1f * (i % 5);
            float orbitR = radius * (1.25f + 0.25f * ((i % 4) / 4.0f));

            float angle = time * speed + offset;
            float cosA = (float) Math.cos(angle);
            float sinA = (float) Math.sin(angle);

            float x = orbitR * cosA;
            float z = orbitR * sinA;
            float y = x * (float) Math.sin(orbitTiltZ) + z * (float) Math.sin(orbitTiltX);

            float sz = 0.025f + 0.015f * ((i % 3) / 3.0f);
            int alpha = 100 + (i % 5) * 25;
            int cr = 180 + (i % 2) * 50;
            int cg = 220;
            int cb = 255;

            vc.vertex(pose.pose(), x - sz, y - sz, z).color(cr, cg, cb, alpha).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULLBRIGHT).normal(0, 1, 0).endVertex();
            vc.vertex(pose.pose(), x + sz, y - sz, z).color(cr, cg, cb, alpha).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULLBRIGHT).normal(0, 1, 0).endVertex();
            vc.vertex(pose.pose(), x + sz, y + sz, z).color(cr, cg, cb, alpha).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULLBRIGHT).normal(0, 1, 0).endVertex();
            vc.vertex(pose.pose(), x - sz, y + sz, z).color(cr, cg, cb, alpha).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULLBRIGHT).normal(0, 1, 0).endVertex();
        }
    }

    private void drawSparks(VertexConsumer vc, PoseStack.Pose pose, float radius, float t) {
        for (int i = 0; i < 20; i++) {
            float angle = (float) ((i * 1.618f + t * 0.03f) % (2 * Math.PI));
            float latAngle = (float) ((i * 2.399f + t * 0.04f + i * 0.1f) % Math.PI);
            float pulseDist = 0.2f * (float) Math.sin(t * 0.15f + i * 1.3f);
            float dist = radius * (1.08f + 0.2f * ((i % 6) / 6.0f) + pulseDist * 0.1f);

            float x = dist * (float) Math.sin(latAngle) * (float) Math.cos(angle);
            float y = dist * (float) Math.cos(latAngle);
            float z = dist * (float) Math.sin(latAngle) * (float) Math.sin(angle);

            float pulseAlpha = 0.5f + 0.5f * (float) Math.sin(t * 0.2f + i * 0.7f);
            float sz = 0.01f + 0.025f * ((i % 4) / 4.0f) * (0.7f + 0.3f * pulseAlpha);
            int alpha = (int) (60 + 140 * pulseAlpha);
            int sparkR = 180 + (i % 3) * 25;
            int sparkG = 220 + (i % 2) * 20;
            int sparkB = 255;

            vc.vertex(pose.pose(), x - sz, y - sz, z).color(sparkR, sparkG, sparkB, alpha).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULLBRIGHT).normal(0, 1, 0).endVertex();
            vc.vertex(pose.pose(), x + sz, y - sz, z).color(sparkR, sparkG, sparkB, alpha).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULLBRIGHT).normal(0, 1, 0).endVertex();
            vc.vertex(pose.pose(), x + sz, y + sz, z).color(sparkR, sparkG, sparkB, alpha).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULLBRIGHT).normal(0, 1, 0).endVertex();
            vc.vertex(pose.pose(), x - sz, y + sz, z).color(sparkR, sparkG, sparkB, alpha).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULLBRIGHT).normal(0, 1, 0).endVertex();
        }
    }
}
