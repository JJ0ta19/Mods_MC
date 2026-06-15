package com.rasengan;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, RasenganMod.MODID);

    public static final RegistryObject<EntityType<RasenganEntity>> RASENGAN_PROJECTILE =
            ENTITIES.register("rasengan_projectile",
                    () -> EntityType.Builder.<RasenganEntity>of(RasenganEntity::new, MobCategory.MISC)
                            .sized(1.0f, 1.0f)
                            .build(new ResourceLocation(RasenganMod.MODID, "rasengan_projectile").toString())
            );

    public static final RegistryObject<EntityType<StickmanEntity>> STICKMAN =
            ENTITIES.register("stickman",
                    () -> EntityType.Builder.<StickmanEntity>of(StickmanEntity::new, MobCategory.MISC)
                            .sized(0.6f, 0.8f)
                            .build(new ResourceLocation(RasenganMod.MODID, "stickman").toString())
            );

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
