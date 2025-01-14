package com.teamresourceful.resourcefulbees.common.multiblocks.centrifuge.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.teamresourceful.resourcefulbees.common.lib.constants.ModConstants;

import java.util.Collection;

public class CentrifugeCommandHolder {

    private CentrifugeCommandHolder() {
        throw new IllegalAccessError(ModConstants.UTILITY_CLASS);
    }

    public static CommandDispatcher<CentrifugeCommandSource> dispatcher = generateCommands();

    public static CommandDispatcher<CentrifugeCommandSource> generateCommands() {
        final CommandDispatcher<CentrifugeCommandSource> newDispatcher = new CommandDispatcher<>();
        newDispatcher.register(literal("help").executes(ctx -> {
            Collection<String> commands = newDispatcher.getSmartUsage(newDispatcher.getRoot(), ctx.getSource()).values();
            ctx.getSource().sendMessage("Centrifuge Commands: ");
            for (String command : commands) ctx.getSource().sendMessage(" - " + command);
            return 1;
        }));
        newDispatcher.register(literal("say").then(argument("message", StringArgumentType.greedyString()).executes(ctx -> {
            ctx.getSource().sendMessage(StringArgumentType.getString(ctx, "message"));
            return 1;
        })));
        newDispatcher.register(literal("ping").executes(ctx -> {
            ctx.getSource().sendMessage("pong");
            return 1;
        }));
        newDispatcher.register(literal("reload").executes(ctx -> {
            dispatcher = generateCommands();
            return 1;
        }));
        return newDispatcher;
    }

    public static LiteralArgumentBuilder<CentrifugeCommandSource> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public static <T> RequiredArgumentBuilder<CentrifugeCommandSource, T> argument(String pName, ArgumentType<T> pType) {
        return RequiredArgumentBuilder.argument(pName, pType);
    }

    public static void callDispatcher(String data, CentrifugeCommandSource source) {
        try {
            dispatcher.execute(data, source);
        } catch (CommandSyntaxException e) {
            source.sendError(e.getRawMessage().getString());
        }
    }
}
