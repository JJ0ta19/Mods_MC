package com.rosamod;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, RosaMod.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RosaMod.MOD_ID);

    public static final RegistryObject<Block> GOLDEN_CHAIN_BLOCK = BLOCKS.register("golden_chain",
            () -> new ChainBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GOLD)
                    .strength(0.5f)
                    .sound(SoundType.CHAIN)
                    .noOcclusion()));

    public static final RegistryObject<Item> GOLDEN_CHAIN_ITEM = ITEMS.register("golden_chain",
            () -> new BlockItem(GOLDEN_CHAIN_BLOCK.get(), new Item.Properties()));
}
