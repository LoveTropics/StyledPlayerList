package eu.pb4.styledplayerlist;

import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.styledplayerlist.command.Commands;
import eu.pb4.styledplayerlist.config.ConfigManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(PlayerList.ID)
public class PlayerList {
	public static final Logger LOGGER = LogManager.getLogger("Styled Player List");
	public static final String ID = "styledplayerlist";

	public PlayerList() {
		MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> Commands.register(event.getDispatcher()));
		MinecraftForge.EVENT_BUS.addListener((ServerStartedEvent event) -> ConfigManager.loadConfig());

		Placeholders.registerChangeEvent((a, b) -> ConfigManager.rebuildStyled());
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(ID, path);
	}
}
