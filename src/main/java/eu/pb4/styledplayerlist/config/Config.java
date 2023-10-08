package eu.pb4.styledplayerlist.config;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.PatternPlaceholderParser;
import eu.pb4.placeholders.api.parsers.StaticPreParser;
import eu.pb4.placeholders.api.parsers.TextParserV1;
import eu.pb4.styledplayerlist.SPLHelper;
import eu.pb4.styledplayerlist.config.data.ConfigData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class Config {
    public static final NodeParser PARSER = NodeParser.merge(
            TextParserV1.DEFAULT, Placeholders.DEFAULT_PLACEHOLDER_PARSER,
            new PatternPlaceholderParser(PatternPlaceholderParser.PREDEFINED_PLACEHOLDER_PATTERN, DynamicNode::of),
            StaticPreParser.INSTANCE
    );

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

    public static TextNode parseText(String string) {
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
