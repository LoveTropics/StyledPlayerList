package eu.pb4.styledplayerlist;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.styledplayerlist.access.PlayerListViewerHolder;
import eu.pb4.styledplayerlist.command.Commands;
import eu.pb4.styledplayerlist.config.ConfigManager;
import eu.pb4.styledplayerlist.config.data.ConfigData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(PlayerList.ID)
public class PlayerList {
	public static final String ID = "styledplayerlist";

	public PlayerList() {
		NeoForge.EVENT_BUS.addListener(Commands::register);
		NeoForge.EVENT_BUS.addListener(this::tick);

		Placeholders.registerChangeEvent((a, b) -> ConfigManager.rebuildStyled());
	}

	private void tick(ServerTickEvent.Pre event) {
		MinecraftServer server = event.getServer();
		if (ConfigManager.isEnabled()) {
			ConfigData config = ConfigManager.getConfig().configData;
			for (var player : server.getPlayerList().getPlayers()) {
				var x = System.nanoTime();
				if (player.connection == null) {
					continue;
				}
				var tick = server.getTickCount();
				var holder = (PlayerListViewerHolder) player.connection;

				var style = holder.styledPlayerList$getStyleObject();

				if (tick % style.updateRate == 0) {
					var context = PlaceholderContext.of(player, SPLHelper.PLAYER_LIST_VIEW);
					var animationTick = holder.styledPlayerList$getAndIncreaseAnimationTick();
					player.connection.send(new ClientboundTabListPacket(style.getHeader(context, animationTick), style.getFooter(context, animationTick)));
				}

				if (config.playerName.playerNameUpdateRate > 0 && tick % config.playerName.playerNameUpdateRate == 0) {
					holder.styledPlayerList$updateName();
				}
				player.displayClientMessage(Component.literal(tick + " | " + ((System.nanoTime() - x) / 1000000f)), true);
			}
		}
	}

	public static ResourceLocation location(String path) {
		return ResourceLocation.fromNamespaceAndPath(ID, path);
	}

}
