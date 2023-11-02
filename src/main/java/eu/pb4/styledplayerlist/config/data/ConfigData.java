package eu.pb4.styledplayerlist.config.data;

import com.google.gson.annotations.SerializedName;

public class ConfigData {
    @SerializedName("config_version")
    public int version = 2;
    @SerializedName("__comment")
    public String _comment = "Before changing anything, see https://github.com/Patbox/StyledPlayerList#configuration";
    @SerializedName("player")
    public PlayerName playerName = new PlayerName();

    @SerializedName("client_show_in_singleplayer")
    public boolean displayOnSingleplayer = true;

    public static class PlayerName {
        @SerializedName("modify_name")
        public boolean changePlayerName = false;
        @SerializedName("passthrough")
        public boolean ignoreFormatting = false;
        @SerializedName("hidden")
        public boolean hidePlayer = false;
        @SerializedName("format")
        public String playerNameFormat = "%player:displayname%";
    }
}
