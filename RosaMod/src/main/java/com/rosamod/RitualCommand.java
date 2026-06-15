package com.rosamod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class RitualCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("rosamod")
                .executes(ctx -> {
                    if (!ctx.getSource().hasPermission(2)) {
                        ctx.getSource().sendFailure(Component.literal("No tienes permiso para usar este comando."));
                        return 0;
                    }
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    return execute(ctx, player);
                })
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(ctx -> {
                        if (!ctx.getSource().hasPermission(2)) {
                            ctx.getSource().sendFailure(Component.literal("No tienes permiso para usar este comando."));
                            return 0;
                        }
                        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                        return execute(ctx, target);
                    })
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        RitualManager.startRitual(target);
        ctx.getSource().sendSuccess(() -> Component.literal("Ritual iniciado para " + target.getName().getString()), true);
        return 1;
    }
}
