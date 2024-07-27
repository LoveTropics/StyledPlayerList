package eu.pb4.styledplayerlist.config;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import eu.pb4.styledplayerlist.PlayerList;
import eu.pb4.styledplayerlist.config.data.ConfigData;
import eu.pb4.styledplayerlist.config.data.StyleData;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@EventBusSubscriber(modid = PlayerList.ID)
public class ConfigManager {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().setLenient()
            .registerTypeAdapter(StyleData.ElementList.class, new StyleData.ElementList.Serializer())
            .create();

    private static final Logger LOGGER = LogUtils.getLogger();

    private static Config CONFIG;
    private static boolean ENABLED = false;
    private static final LinkedHashMap<ResourceLocation, PlayerListStyle> STYLES = new LinkedHashMap<>();
    private static final LinkedHashMap<ResourceLocation, StyleData> STYLES_DATA = new LinkedHashMap<>();

    public static Config getConfig() {
        return CONFIG;
    }

    public static boolean isEnabled() {
        return ENABLED;
    }

    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SimplePreparableReloadListener<LoadResult>() {
            @Override
            protected LoadResult prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
                return load(resourceManager);
            }

            @Override
            protected void apply(LoadResult result, ResourceManager resourceManager, ProfilerFiller profiler) {
                ConfigManager.apply(result);
            }
        });
    }

    private static void apply(LoadResult result) {
        STYLES.clear();
        STYLES_DATA.clear();
        ENABLED = false;

        CONFIG = null;
        try {
            result.styles.forEach((id, styleData) -> {
                var style = new PlayerListStyle(id, styleData);
                STYLES.put(id, style);
                STYLES_DATA.put(id, styleData);
            });
        } catch (Throwable t) {
            ENABLED = false;
            LOGGER.error("Something went wrong while reading config!", t);
        }

        CONFIG = new Config(result.config);
        ENABLED = true;
    }

    private static LoadResult load(ResourceManager resourceManager) {
        Optional<Resource> configResource = resourceManager.getResource(PlayerList.location("styled_player_list.json"));
        ConfigData config = new ConfigData();
        if (configResource.isPresent()) {
            try (BufferedReader reader = configResource.get().openAsReader()) {
                config = GSON.fromJson(reader, ConfigData.class);
            } catch (IOException | JsonParseException e) {
                LOGGER.error("Failed to load player list config", e);
            }
        }

        FileToIdConverter styleLister = FileToIdConverter.json("player_list_style");
        ImmutableMap.Builder<ResourceLocation, StyleData> styles = ImmutableMap.builder();
        styleLister.listMatchingResources(resourceManager).forEach((location, resource) -> {
            ResourceLocation id = styleLister.fileToId(location);
            try (BufferedReader reader = resource.openAsReader()) {
                styles.put(id, GSON.fromJson(reader, StyleData.class));
            } catch (IOException | JsonParseException e) {
                LOGGER.error("Failed to load player list style at {}", location, e);
            }
        });

        return new LoadResult(config, styles.build());
    }

    public static boolean reloadConfig(MinecraftServer server) {
        apply(load(server.getResourceManager()));
        return ENABLED;
    }

    public static PlayerListStyle getStyle() {
        return STYLES.getOrDefault(PlayerList.location("default"), DefaultValues.EMPTY_STYLE);
    }

    public static void rebuildStyled() {
        if (CONFIG != null) {
            CONFIG = new Config(CONFIG.configData);
        }
        for (var e : STYLES_DATA.entrySet()) {
            STYLES.put(e.getKey(), new PlayerListStyle(e.getKey(), e.getValue()));
        }
    }

    private record LoadResult(ConfigData config, Map<ResourceLocation, StyleData> styles) {
    }
}
