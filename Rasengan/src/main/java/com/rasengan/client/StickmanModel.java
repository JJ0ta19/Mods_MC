package com.rasengan.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.rasengan.StickmanEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public class StickmanModel extends EntityModel<StickmanEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation("rasengan", "stickman"), "main");

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public StickmanModel(ModelPart root) {
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
    public void setupAnim(StickmanEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        head.xRot = 0;
        head.yRot = 0;
        body.xRot = 0;
        rightArm.xRot = 0;
        rightArm.yRot = 0;
        rightArm.zRot = 0;
        leftArm.xRot = 0;
        leftArm.zRot = 0;
        rightLeg.xRot = 0;
        leftLeg.xRot = 0;

        int animState = entity.getAnimState();

        if (animState == 1) {
            float armRamp = Math.min(ageInTicks / 20.0F, 1.0F);
            rightArm.xRot = -1.8F * armRamp;
            leftArm.xRot = 0.5F * armRamp;
        } else if (animState == 2) {
            rightArm.xRot = -1.8F;
            leftArm.xRot = 0.5F;
            body.xRot = 0.2F;
            rightLeg.xRot = 0.3F;
            leftLeg.xRot = 0.3F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay, float r, float g, float b, float a) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        rightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        leftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        rightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        leftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
    }
}
