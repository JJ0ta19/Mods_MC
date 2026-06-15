package com.rosamod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("rosamod")
public class RosaMod {
    public static final String MOD_ID = "rosamod";
    private static final Logger LOGGER = LogManager.getLogger();

    public RosaMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("RosaMod se ha habilitado correctamente");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        LOGGER.info("Registrando comando /rosamod");
        RitualCommand.register(event.getDispatcher());
        LOGGER.info("Comando /rosamod registrado correctamente");
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            RitualManager.tick();
        }
    }
}
