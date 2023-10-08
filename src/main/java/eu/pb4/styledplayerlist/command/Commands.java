package eu.pb4.styledplayerlist.command;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.styledplayerlist.access.PlayerListViewerHolder;
import eu.pb4.styledplayerlist.config.ConfigManager;
import eu.pb4.styledplayerlist.config.PlayerListStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Locale;

import static net.minecraft.commands.Commands.literal;

public class Commands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                literal("styledplayerlist")
                        .requires(s -> s.hasPermission(net.minecraft.commands.Commands.LEVEL_ADMINS))
                        .then(literal("reload")
                                .executes(Commands::reloadConfig)
                        )
        );
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        if (ConfigManager.loadConfig()) {
            context.getSource().sendSuccess(() -> Component.literal("Reloaded config!"), false);
        } else {
            context.getSource().sendFailure(Component.literal("Error accrued while reloading config!").withStyle(ChatFormatting.RED));
        }
        for (var player : context.getSource().getServer().getPlayerList().getPlayers()) {
            ((PlayerListViewerHolder) player.connection).styledPlayerList$reloadStyle();
        }

        return 1;
    }

}
