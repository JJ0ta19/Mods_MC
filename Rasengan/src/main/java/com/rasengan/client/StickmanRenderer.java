package com.rasengan.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.rasengan.RasenganMod;
import com.rasengan.StickmanEntity;
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

public class StickmanRenderer extends EntityRenderer<StickmanEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RasenganMod.MODID, "textures/entity/naruto.png");
    private final StickmanModel model;

    public StickmanRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new StickmanModel(context.bakeLayer(StickmanModel.LAYER_LOCATION));
    }

    @Override
    public ResourceLocation getTextureLocation(StickmanEntity entity) {
        return TEXTURE;
    }

    @Override
    protected int getBlockLightLevel(StickmanEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void render(StickmanEntity entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
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
