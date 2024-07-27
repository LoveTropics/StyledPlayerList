package eu.pb4.styledplayerlist.config;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.styledplayerlist.SPLHelper;
import eu.pb4.styledplayerlist.config.data.ConfigData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class Config {
    public static final NodeParser PARSER = NodeParser.builder()
            .simplifiedTextFormat()
            .quickText()
            .globalPlaceholders()
            .staticPreParsing()
            .build();

    public final ConfigData configData;
    public final TextNode playerNameFormat;
    public final boolean isHidden;
    private final boolean passthroughDefault;


    public Config(ConfigData data) {
        this.configData = data;
        this.playerNameFormat = parseText(data.playerName.playerNameFormat);
        this.isHidden = data.playerName.hidePlayer;
        this.passthroughDefault = data.playerName.ignoreFormatting;
    }

    @Nullable
    public static TextNode parseText(@Nullable String string) {
        if (string == null) {
            return null;
        }
        return PARSER.parseNode(string);
    }

    @Nullable
    public Component formatPlayerUsername(ServerPlayer player) {
        return this.passthroughDefault ? null : this.playerNameFormat.toText(PlaceholderContext.of(player, SPLHelper.PLAYER_NAME_VIEW));
    }

    public boolean isPlayerHidden(ServerPlayer player) {
        return this.isHidden;
    }
}
