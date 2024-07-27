package eu.pb4.styledplayerlist.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import eu.pb4.styledplayerlist.access.PlayerListViewerHolder;
import eu.pb4.styledplayerlist.config.ConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import static net.minecraft.commands.Commands.literal;

public class Commands {
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
                literal("styledplayerlist")
                        .requires(source -> source.hasPermission(net.minecraft.commands.Commands.LEVEL_ADMINS))

                        .then(literal("reload")
                                .executes(Commands::reloadConfig)
                        )
        );
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        if (ConfigManager.reloadConfig(context.getSource().getServer())) {
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
