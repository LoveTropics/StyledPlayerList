package eu.pb4.styledplayerlist.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import eu.pb4.styledplayerlist.PlayerList;
import eu.pb4.styledplayerlist.config.data.ConfigData;
import eu.pb4.styledplayerlist.config.data.StyleData;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;


public class ConfigManager {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().setLenient()
            .registerTypeAdapter(StyleData.ElementList.class, new StyleData.ElementList.Serializer())
            .create();

    private static Config CONFIG;
    private static boolean ENABLED = false;
    private static final LinkedHashMap<String, PlayerListStyle> STYLES = new LinkedHashMap<>();
    private static final LinkedHashMap<String, StyleData> STYLES_DATA = new LinkedHashMap<>();

    public static Config getConfig() {
        return CONFIG;
    }

    public static boolean isEnabled() {
        return ENABLED;
    }

    public static boolean loadConfig() {
        ENABLED = false;

        CONFIG = null;
        try {
            var configDir = FMLPaths.GAMEDIR.get().resolve("config").resolve("styledplayerlist");
            var configStyle = configDir.resolve("styles");
            if (!Files.exists(configStyle)) {
                Files.createDirectories(configStyle);
                Files.writeString(configStyle.resolve("default.json"), GSON.toJson(DefaultValues.exampleStyleData()), StandardCharsets.UTF_8);
                Files.writeString(configStyle.resolve("animated.json"), GSON.toJson(DefaultValues.exampleAnimatedStyleData()), StandardCharsets.UTF_8);
            }

            ConfigData config;

            var configFile = configDir.resolve("config.json");
            if (Files.exists(configFile)) {
                var data = JsonParser.parseString(Files.readString(configFile));
                config = GSON.fromJson(data, ConfigData.class);
            } else {
                config = new ConfigData();
            }

            Files.writeString(configFile, GSON.toJson(config));

            STYLES.clear();

            Files.list(configStyle).filter((name) -> !name.endsWith(".json")).forEach((path) -> {
                String data;
                try {
                    data = Files.readString(path);;
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                StyleData styleData = GSON.fromJson(data, StyleData.class);

                var name = path.getFileName().toString();
                name = name.substring(0, name.length() - 5);
                var style = new PlayerListStyle(name, styleData);
                STYLES.put(name, style);
                STYLES_DATA.put(name, styleData);
            });

            CONFIG = new Config(config);
            ENABLED = true;
        } catch(Throwable exception) {
            ENABLED = false;
            PlayerList.LOGGER.error("Something went wrong while reading config!");
            exception.printStackTrace();
        }

        return ENABLED;
    }

    public static PlayerListStyle getStyle() {
        return STYLES.getOrDefault("default", DefaultValues.EMPTY_STYLE);
    }

    public static void rebuildStyled() {
        if (CONFIG != null) {
            CONFIG = new Config(CONFIG.configData);
        }
        for (var e : STYLES_DATA.entrySet()) {
            STYLES.put(e.getKey(), new PlayerListStyle(e.getKey(), e.getValue()));
        }
    }
}
