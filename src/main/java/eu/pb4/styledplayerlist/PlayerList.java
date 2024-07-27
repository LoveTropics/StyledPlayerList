package eu.pb4.styledplayerlist;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.styledplayerlist.access.PlayerListViewerHolder;
import eu.pb4.styledplayerlist.command.Commands;
import eu.pb4.styledplayerlist.config.ConfigManager;
import eu.pb4.styledplayerlist.config.PlayerListStyle;
import eu.pb4.styledplayerlist.config.data.ConfigData;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;

public class PlayerList implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("Styled Player List");
	public static final String ID = "styledplayerlist";
	public static final Scoreboard SCOREBOARD = new Scoreboard();
	public static final String OBJECTIVE_NAME = "■SPL_OBJ";

	public static final Objective SCOREBOARD_OBJECTIVE = new Objective(
			SCOREBOARD, OBJECTIVE_NAME, ObjectiveCriteria.DUMMY,
			Component.empty(), ObjectiveCriteria.RenderType.INTEGER, false, null);


	@Override
	public void onInitialize() {
		GenericModInfo.build(FabricLoader.getInstance().getModContainer(ID).get());
		Commands.register();
		ServerLifecycleEvents.SERVER_STARTED.register(s -> {
			ConfigManager.loadConfig();

			CardboardWarning.checkAndAnnounce();
			//MicroScheduler.get(s).scheduleRepeating(50, () -> tick(s));
		});

		Placeholders.registerChangeEvent((a, b) -> ConfigManager.rebuildStyled());
	}

	private void tick(MinecraftServer server) {
		if (ConfigManager.isEnabled()) {
			ConfigData config = ConfigManager.getConfig().configData;
			for (var player : server.getPlayerList().getPlayers()) {
				var x = System.nanoTime();
				if (!SPLHelper.shouldSendPlayerList(player) || player.connection == null) {
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

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(ID, path);
	}

	public static final Event<PlayerList.PlayerListStyleLoad> PLAYER_LIST_STYLE_LOAD = EventFactory.createArrayBacked(PlayerList.PlayerListStyleLoad.class, (callbacks) -> (styleHelper) -> {
		for(PlayerListStyleLoad callback : callbacks ) {
			callback.onPlayerListUpdate(styleHelper);
		}

	});

	@FunctionalInterface
	public interface PlayerListStyleLoad {
		void onPlayerListUpdate(StyleHelper styleHelper);
	}


	public record StyleHelper(LinkedHashMap<String, PlayerListStyle> styles) {
		public void addStyle(PlayerListStyle style) {
			this.styles.put(style.id, style);
		}

		public void removeStyle(PlayerListStyle style) {
			this.styles.remove(style.id, style);
		}
	}


	public static String getPlayersStyle(ServerPlayer player) {
		return ((PlayerListViewerHolder) player.connection).styledPlayerList$getStyle();
	}

	public static void setPlayersStyle(ServerPlayer player, String key) {
		((PlayerListViewerHolder) player).styledPlayerList$setStyle(key);
	}

	public static void addUpdateSkipCheck(ModCompatibility check) {
		SPLHelper.COMPATIBILITY.add(check);
	}

	public interface ModCompatibility {
		boolean check(ServerPlayer player);
	}
}
